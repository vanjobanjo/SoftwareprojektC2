package de.fhwedel.klausps.controller.restriction.hard;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class TwoKlausurenSameTime extends HarteRestriktion {

  private final Duration bufferBetweenPlanungseinheiten;

  private int countStudents = 0;

  public TwoKlausurenSameTime() {
    this(ServiceProvider.getDataAccessService());
  }

  protected TwoKlausurenSameTime(DataAccessService dataAccessService) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
    this.bufferBetweenPlanungseinheiten = DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN;
  }

  protected TwoKlausurenSameTime(DataAccessService dataAccessService,
      Duration bufferBetweenPlanungseinheiten) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
    this.bufferBetweenPlanungseinheiten = bufferBetweenPlanungseinheiten;
  }

  @Override
  public Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung) {

    boolean hartKriterium = false;
    if (pruefung.isGeplant()) {
      LocalDateTime start = pruefung.getStartzeitpunkt().minus(bufferBetweenPlanungseinheiten);
      HashSet<Pruefung> inConflictROPruefung = new HashSet<>();
      HashSet<Teilnehmerkreis> inConflictTeilnehmerkreis = new HashSet<>();
      countStudents = 0;
      LocalDateTime end = getEndTime(pruefung);
      List<Planungseinheit> testList = null;
      try {
        testList = dataAccessService.getAllPlanungseinheitenBetween(start, end);
      } catch (IllegalTimeSpanException e) {
        //start kann nicht vor ende liegen, da ich das berechne
        e.printStackTrace();
      }
      //Damit die Pruefung nicht mit sich selbst in Konflict steht

      if (testList != null) {
        testList.remove(pruefung);

        Set<Pruefung> pruefungenFromBlock;
        for (Planungseinheit planungseinheit : testList) {
          if (planungseinheit.isBlock()) {
            Block block = planungseinheit.asBlock();
            pruefungenFromBlock = block.getPruefungen();
            if (!pruefungenFromBlock.contains(pruefung)) {
              if (uebereinStimmendeTeilnehmerkreise(block, pruefung)) {
                if (block.getTyp() == Blocktyp.SEQUENTIAL) {
                  for (Pruefung pruefungBlock : pruefungenFromBlock) {
                    hartKriterium =
                        getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock,
                            inConflictROPruefung,
                            inConflictTeilnehmerkreis) || hartKriterium;
                  }
                } else {
                  for (Pruefung pruefungBlock : pruefungenFromBlock) {
                    if ((uebereinStimmendeTeilnehmerkreise(pruefungBlock, pruefung))
                        && !outOfRange(start, end, pruefungBlock)) {
                      hartKriterium =
                          getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock,
                              inConflictROPruefung,
                              inConflictTeilnehmerkreis) || hartKriterium;
                    }
                  }
                }
              }
            }
          } else {
            hartKriterium = getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung(),
                inConflictROPruefung, inConflictTeilnehmerkreis) || hartKriterium;
          }
        }
        if (hartKriterium) {
          inConflictROPruefung.add(pruefung);
          HartesKriteriumAnalyse hKA = new HartesKriteriumAnalyse(inConflictROPruefung,
              inConflictTeilnehmerkreis, countStudents, this.hardRestriction);
          return Optional.of(hKA);
        }
      }
    }
    return Optional.empty();
  }

  private boolean outOfRange(LocalDateTime start, LocalDateTime end, Pruefung pruefungBlock) {
    return pruefungBlock.endzeitpunkt().isBefore(start) || pruefungBlock.getStartzeitpunkt()
        .isAfter(end);
  }

  private boolean uebereinStimmendeTeilnehmerkreise(Planungseinheit block, Pruefung pruefung) {
    boolean sameTeilnehmerkreis = false;
    for (Teilnehmerkreis teilnehmerkreis : pruefung.getTeilnehmerkreise()) {
      sameTeilnehmerkreis =
          block.getTeilnehmerkreise().contains(teilnehmerkreis) || sameTeilnehmerkreis;
    }
    return sameTeilnehmerkreis;
  }

  @Override
  public Set<Pruefung> getAllPotentialConflictingPruefungenWith(
      Planungseinheit planungseinheitToCheckFor) {

    Set<Pruefung> geplantePruefungen = new HashSet<>(dataAccessService.getGeplanteModelPruefung());

    geplantePruefungen.removeIf(x -> notSameTeilnehmerkreis(x, planungseinheitToCheckFor));

    return geplantePruefungen;
  }

  @Override
  public boolean wouldBeHardConflictAt(LocalDateTime time, Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(time, planungseinheit);
    boolean isInConflict = false;
    Set<Planungseinheit> planungseinheiten = dataAccessService.getPlanungseinheitenAt(time);
    Iterator<Planungseinheit> planungseinheitIterator = planungseinheiten.iterator();
    while (planungseinheitIterator.hasNext() && !isInConflict) {
      Planungseinheit other = planungseinheitIterator.next();
      isInConflict = areInConflict(planungseinheit, other);
    }
    return isInConflict;
  }

  private boolean areInConflict(Planungseinheit planungseinheit, Planungseinheit other) {
    if (!areSame(planungseinheit, other)) {
      return haveCommonTeilnehmerkreis(planungseinheit, other);
    }
    return false;
  }

  private boolean areSame(@NotNull Planungseinheit pe1, @NotNull Planungseinheit pe2) {
    if (pe1.isBlock() && pe2.isBlock()) {
      return pe1.asBlock().getId() == pe2.asBlock().getId();
    }
    if (!pe1.isBlock() && !pe2.isBlock()) {
      return pe1.asPruefung().getReferenzVerwaltungsystem()
          .equals(pe2.asPruefung().getReferenzVerwaltungsystem());
    }
    return false;
  }

  private boolean haveCommonTeilnehmerkreis(@NotNull Planungseinheit pe1,
      @NotNull Planungseinheit pe2) {
    return !intersect(pe1.getTeilnehmerkreise(), pe2.getTeilnehmerkreise()).isEmpty();
  }

  @NotNull
  private Set<Teilnehmerkreis> intersect(@NotNull Set<Teilnehmerkreis> setA,
      @NotNull Set<Teilnehmerkreis> setB) {
    Set<Teilnehmerkreis> intersection = new HashSet<>(setA);
    intersection.retainAll(setB);
    return intersection;
  }

  private boolean notSameTeilnehmerkreis(Pruefung x, Planungseinheit planungseinheitToCheckFor) {
    for (Teilnehmerkreis teilnehmerkreis : x.getTeilnehmerkreise()) {
      if (planungseinheitToCheckFor.getTeilnehmerkreise().contains(teilnehmerkreis)) {
        return false;
      }
    }
    return true;
  }

  @NotNull
  private LocalDateTime getEndTime(Pruefung pruefung) {
    Optional<Block> maybeBlock = dataAccessService.getBlockTo(pruefung);
    LocalDateTime date = pruefung.getStartzeitpunkt().plus(bufferBetweenPlanungseinheiten);

    return maybeBlock.isPresent() && maybeBlock.get().getTyp() == Blocktyp.SEQUENTIAL ? date.plus(
        maybeBlock.get().getDauer()) : date.plus(pruefung.getDauer());
  }

  private boolean getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck,
      HashSet<Pruefung> inConflictROPruefung, HashSet<Teilnehmerkreis> inConflictTeilnehmerkreis) {
    boolean retBool = false;
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        if (!inConflictTeilnehmerkreis.contains(teilnehmerkreis)) {
          //hier sollte ein Teilnehmerkreis nur einmal dazu addiert werden.
          countStudents += toCheck.getSchaetzungen().get(teilnehmerkreis);
        }
        //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
        inConflictTeilnehmerkreis.add(teilnehmerkreis);
        inConflictROPruefung.add(toCheck);
        retBool = true;
      }
    }
    return retBool;
  }

}
