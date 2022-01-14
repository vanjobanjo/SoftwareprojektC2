package de.fhwedel.klausps.controller.restriction.hard;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class TwoKlausurenSameTime extends HarteRestriktion {

  private final Duration bufferBetweenPlanungseinheiten;

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
  public Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (pruefung.isGeplant()) {
      //Setzen von den start und end Termin, wo das Kriterium verletzt werden könnte
      LocalDateTime start = pruefung.getStartzeitpunkt().minus(bufferBetweenPlanungseinheiten);
      LocalDateTime end = getEndTime(pruefung);
      // Anm.: Liste muss kopiert werden, da Model nur unmodifiable Lists zurückgibt
      List<Planungseinheit> testList = new ArrayList<>(tryToGetAllPlanungseinheitenBetween(start, end));
      Optional<HartesKriteriumAnalyse> hKA = checkforPlanungseinheitenHartesKriterium(
          pruefung, start, end, testList);
      if (hKA.isPresent()) {
        return hKA;
      }
    }
    return Optional.empty();
  }

  private List<Planungseinheit> tryToGetAllPlanungseinheitenBetween(LocalDateTime start,
      LocalDateTime end) throws NoPruefungsPeriodeDefinedException {
    try {
      return dataAccessService.getAllPlanungseinheitenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
      return Collections.emptyList();
    }
  }


  /**
   * Methode die durch alle Planungseinheiten in der übergebenen Liste geht und die für die
   * Planungseinheit zuständige Methode aufruft
   *
   * @param pruefung die Pruefung die neu Hinzugefügt wurde und für die das Krterium getestet wird
   * @param start    der Starttermin ab wo die Überprüfung durchgeführt werden soll (Nur für Blöcke
   *                 relevant)
   * @param end      der Endtermin bis wohin die Überprüfung durchgeführt werden sol (Nur für Blöcke
   *                 relevant)
   * @param testList die Liste mit Planungseinheiten, die zu dem Startzeitpunkt stattfinden
   * @return Entweder eine HartesKriterumAnalyse, wo die Pruefungen , das Kriterium und die
   * Teilnehmer mit ihrer Anzahl drin steht oder ein leeres Optional
   */
  private Optional<HartesKriteriumAnalyse> checkforPlanungseinheitenHartesKriterium(
      Pruefung pruefung, LocalDateTime start, LocalDateTime end, List<Planungseinheit> testList) {

    //Zum Sammeln der Teilnehnehmerkreise und die Pruefungen, die einen harten Konflikt verursachen
    Map<Teilnehmerkreis, Integer> teilnehmercount = new HashMap<>();
    HashSet<Pruefung> inConflictROPruefung = new HashSet<>();
    Optional<HartesKriteriumAnalyse> hKA = Optional.empty();
    if (testList != null) {
      testList.remove(pruefung);

      //Durchgehen der Liste von Planungseinheiten und unterscheiden von unterschiedlichem Typ
      for (Planungseinheit planungseinheit : testList) {
        if (planungseinheit.isBlock()) {
          testForBlockHard(pruefung, planungseinheit.asBlock(), start, end, inConflictROPruefung,
              teilnehmercount);
        } else {
          getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung(),
              inConflictROPruefung, teilnehmercount);
        }
      }
      hKA = testAndCreateNewHartesKriterumAnalyse(teilnehmercount, inConflictROPruefung);
    }
    return hKA;
  }


  /**
   * Hier werden die Parameter zu einer HartenKriterumsAnalyse zusammen gebaut, falls es einen
   * Konflikt gibt
   *
   * @param teilnehmercount      eine Map,  wo die Inkonflikt stehende Teilnehmerkreise und ihre
   *                             Anzahl gespeichert sind
   * @param inConflictROPruefung ein Set, in welchem die In Konflikt stehende Pruefungen gespeichert
   *                             sind
   * @return Entweder die HarteKriterumsAnalyse mit den in Konfliktstehnenden Pruefungen und
   * Teilnehmerkreisen oder ein leeres Optinal
   */
  private Optional<HartesKriteriumAnalyse> testAndCreateNewHartesKriterumAnalyse(
      Map<Teilnehmerkreis, Integer> teilnehmercount, HashSet<Pruefung> inConflictROPruefung) {
    if (!inConflictROPruefung.isEmpty()) {
      HartesKriteriumAnalyse hKA = new HartesKriteriumAnalyse(inConflictROPruefung,
          this.hardRestriction, teilnehmercount);
      return Optional.of(hKA);
    }
    return Optional.empty();
  }

  /**
   * In dieser Methode wird der übergebene Block unterschieden in den unterschiedlichen Blocktypen
   * und jeweils ihre Methode dann aufgerufen
   *
   * @param pruefung             die zu Überpruefende Pruefung
   * @param block                der zu Überpruefende Block
   * @param start                der Startzeitpunkt ab dem es zu einem Konflikt kommen kann
   * @param end                  der Endzeitpunkt bis zu dem es zu einem Konflikt kommen kann
   * @param inConflictROPruefung das Set mit in Konflikt stehenden Pruefungen
   * @param teilnehmerCount      die Teilnehmer und ihre Anzahl, die aktuell im Konflikt stehen
   */
  private void testForBlockHard(Pruefung pruefung, Block block, LocalDateTime start,
      LocalDateTime end, HashSet<Pruefung> inConflictROPruefung,
      Map<Teilnehmerkreis, Integer> teilnehmerCount) {
    Set<Pruefung> pruefungenFromBlock;
    pruefungenFromBlock = block.getPruefungen();
    if (!pruefungenFromBlock.contains(pruefung)
        && (uebereinStimmendeTeilnehmerkreise(block, pruefung))) {
      if (block.getTyp() == Blocktyp.SEQUENTIAL) {
        testForSequentialBlock(pruefung, pruefungenFromBlock, inConflictROPruefung,
            teilnehmerCount);
      } else {
        testForParallelBlock(pruefung, pruefungenFromBlock, start, end,
            teilnehmerCount, inConflictROPruefung);
      }
    }
  }

  /**
   * In dieser Methode wird für ein Block von Typ Parallel das Kriterum getestet
   *
   * @param pruefung             die zu Ueberpruefende Pruefung
   * @param start                der Startermin, ab wo es zu einem Konflikt kommen könnte
   * @param end                  der Endtermin, ab wo es zu einen Konflikt kommen könnte
   * @param pruefungenFromBlock  die Pruefungen, die sich in dem Block befinden
   * @param teilnehmerCount      die in Konflikt stehenden Teilnehmerkreise mit ihrer Anzahl
   * @param inConflictROPruefung die in  Konflikt stehenden Pruefungen
   */
  private void testForParallelBlock(Pruefung pruefung, Set<Pruefung> pruefungenFromBlock,
      LocalDateTime start, LocalDateTime end,
      Map<Teilnehmerkreis, Integer> teilnehmerCount, HashSet<Pruefung> inConflictROPruefung) {
    for (Pruefung pruefungBlock : pruefungenFromBlock) {
      if ((uebereinStimmendeTeilnehmerkreise(pruefungBlock, pruefung))
          && !outOfRange(start, end, pruefungBlock)) {
        getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock,
            inConflictROPruefung, teilnehmerCount);
      }
    }
  }

  /**
   * Diese Methode händelt den Sequential Block typ ab
   *
   * @param pruefung             die zu Überpruefende Pruefung
   * @param pruefungenFromBlock  ein Set von Pruefungen, welches sich in einen Sequenziellen Block
   *                             gefinden
   * @param inConflictROPruefung die aktuell in Konflikt stehnde Pruefungen
   * @param teilnehmerCount      die aktuellen in Konflikt stehenden Teilnehmerkreise und ihre
   *                             Anzahl
   */
  private void testForSequentialBlock(Pruefung pruefung, Set<Pruefung> pruefungenFromBlock,
      HashSet<Pruefung> inConflictROPruefung, Map<Teilnehmerkreis, Integer> teilnehmerCount) {
    for (Pruefung pruefungBlock : pruefungenFromBlock) {
      getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock,
          inConflictROPruefung, teilnehmerCount);
    }
  }

  /**
   * Methode um zu überpruefen ob sich ein Block mit der Übergebenen Zeitspanne nicht überschneidet
   *
   * @param start         der Zeitspanne
   * @param end           der Zeitspanne
   * @param pruefungBlock die zu testende Pruefung
   * @return true wenn die sich nicht überschneiden und false, wenn sie sich überschneiden
   */
  private boolean outOfRange(LocalDateTime start, LocalDateTime end, Pruefung pruefungBlock) {
    return pruefungBlock.endzeitpunkt().isBefore(start) || pruefungBlock.getStartzeitpunkt()
        .isAfter(end);
  }

  /**
   * Diese Methode testet, ob eine Planungseinheit und eine Pruefung einen gemeinsamen
   * Teilnehmerkreis besitzt
   *
   * @param planungseinheit die Planungseinheit mit der zu testen ist
   * @param pruefung        die Pruefung mit der zu testen ist
   * @return true, wenn es einen gemeinsamen Teilnehmerkreis gibt und false, wenn es kein
   * gemeinsamen Teilnehmerkreis gibt
   */
  private boolean uebereinStimmendeTeilnehmerkreise(Planungseinheit planungseinheit,
      Pruefung pruefung) {
    for (Teilnehmerkreis teilnehmerkreis : pruefung.getTeilnehmerkreise()) {
      if (planungseinheit.getTeilnehmerkreise().contains(teilnehmerkreis)) {
        return true;
      }
    }
    return false;
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

  //TODO JAVADOC
  private boolean areInConflict(Planungseinheit planungseinheit, Planungseinheit other) {
    if (!areSame(planungseinheit, other)) {
      return haveCommonTeilnehmerkreis(planungseinheit, other);
    }
    return false;
  }

  //TODO JAVADOC
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

  //TODO JAVADOC
  private boolean haveCommonTeilnehmerkreis(@NotNull Planungseinheit pe1,
      @NotNull Planungseinheit pe2) {
    return !intersect(pe1.getTeilnehmerkreise(), pe2.getTeilnehmerkreise()).isEmpty();
  }

  //TODO JAVADOC
  @NotNull
  private Set<Teilnehmerkreis> intersect(@NotNull Set<Teilnehmerkreis> setA,
      @NotNull Set<Teilnehmerkreis> setB) {
    Set<Teilnehmerkreis> intersection = new HashSet<>(setA);
    intersection.retainAll(setB);
    return intersection;
  }

  //TODO JAVADOC
  private boolean notSameTeilnehmerkreis(Pruefung x, Planungseinheit planungseinheitToCheckFor) {
    for (Teilnehmerkreis teilnehmerkreis : x.getTeilnehmerkreise()) {
      if (planungseinheitToCheckFor.getTeilnehmerkreise().contains(teilnehmerkreis)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Berechnet den Endzeitpunkt, von der Übergebenden Pruefung. Der Endzeitpunkt unterscheidet sich,
   * falls die Pruefung in einem Block existiert
   *
   * @param pruefung die Pruefung, mit der der Endzeitpunkt festgelegt werden soll
   * @return der EndZeitpunkt von einer Pruefunng, welcher sich unterscheidet, ob es ein Block oder
   * ob es sich um eine Pruefung handelt
   */
  @NotNull
  private LocalDateTime getEndTime(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    Optional<Block> maybeBlock = dataAccessService.getBlockTo(pruefung);
    LocalDateTime date = pruefung.getStartzeitpunkt().plus(bufferBetweenPlanungseinheiten);

    return maybeBlock.isPresent() && maybeBlock.get().getTyp() == Blocktyp.SEQUENTIAL ? date.plus(
        maybeBlock.get().getDauer()) : date.plus(pruefung.getDauer());
  }

  /**
   * In dieser Methode werden zwei Pruefungen mit einander verglichen und die Übereinstimmenden
   * Teilnehmerkreise mit ihrer Anzahl in eine Map gespeichert. zusätzlich werden die in Konflikt
   * stehenden Pruefungen in ein Set gespeichert
   *
   * @param pruefung             die Pruefung für die das HarteKriterium gecheckt werden soll
   * @param toCheck              die Pruefung mit der verglichen werden soll
   * @param inConflictROPruefung das Set, von Pruefungen, welche in Konfklikt stehen
   * @param teilnehmercount      die Map mit Teilnehmerkreisen und deren Anzahl
   */
  private void getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck,
      HashSet<Pruefung> inConflictROPruefung,
      Map<Teilnehmerkreis, Integer> teilnehmercount) {

    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        //Vergleich der Teilnehmerkreise auf ihrer Anzahl
        Integer teilnehmerKreisSchaetzung = teilnehmercount.get(teilnehmerkreis);
        Integer teilnehmerkreistoCheck = toCheck.getSchaetzungen().get(teilnehmerkreis);
        if (teilnehmerKreisSchaetzung != null) {
          if (teilnehmerKreisSchaetzung < teilnehmerkreistoCheck) {
            teilnehmercount.replace(teilnehmerkreis, teilnehmerkreistoCheck);
          }
        } else {
          teilnehmercount.put(teilnehmerkreis, teilnehmerkreistoCheck);
        }

        //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
        inConflictROPruefung.add(pruefung);
        inConflictROPruefung.add(toCheck);
      }
    }
    if (!inConflictROPruefung.isEmpty()) {
      TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(teilnehmercount,
          pruefung.getSchaetzungen());
    }
  }

}
