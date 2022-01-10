package de.fhwedel.klausps.controller.restriction.hard;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class TwoKlausurenSameTime extends HarteRestriktion {

  static final long MINUTES_BETWEEN_PRUEFUNGEN = 30;

  private int countStudents = 0;

  public TwoKlausurenSameTime() {
    this(ServiceProvider.getDataAccessService());
  }

  protected TwoKlausurenSameTime(DataAccessService dataAccessService) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
  }

  @Override
  public Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung) {

    boolean hartKriterium = false;
    if (pruefung.isGeplant()) {
      LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
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
                        && checkTime(start, end, pruefungBlock)) {
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

  private boolean checkTime(LocalDateTime start, LocalDateTime end, Pruefung pruefungBlock) {
    return
        isBeforeBlock(start, end, pruefungBlock)
            ||
            isSameBlock(start, end, pruefungBlock)
            ||
            isAfterBlock(start, end, pruefungBlock);

  }

  private boolean isAfterBlock(LocalDateTime start, LocalDateTime end, Pruefung pruefungBlock) {
    return start.isBefore(pruefungBlock.endzeitpunkt())
        && (end.isEqual(pruefungBlock.endzeitpunkt())
        || end.isAfter(pruefungBlock.endzeitpunkt()));
  }

  private boolean isSameBlock(LocalDateTime start, LocalDateTime end, Pruefung pruefungBlock) {
    return (start.isEqual(pruefungBlock.getStartzeitpunkt())
        || start.isAfter(pruefungBlock.getStartzeitpunkt()))
        && (end.isBefore(pruefungBlock.endzeitpunkt())
        || end.isEqual(pruefungBlock.endzeitpunkt()));
  }

  private boolean isBeforeBlock(LocalDateTime start, LocalDateTime end, Pruefung pruefungBlock) {
    return start.isBefore(pruefungBlock.getStartzeitpunkt())
        && (end.isAfter(pruefungBlock.getStartzeitpunkt())
        || end.isEqual(pruefungBlock.getStartzeitpunkt()));
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
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @NotNull
  private LocalDateTime getEndTime(Pruefung pruefung) {
    Optional<Block> maybeBlock = dataAccessService.getBlockTo(pruefung);
    LocalDateTime date = pruefung.getStartzeitpunkt().plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);

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
