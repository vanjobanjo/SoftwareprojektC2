package de.fhwedel.klausps.controller.restriction.hard;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Klasse, die für das Testen da ist, wenn zwei Klausuren gleichzeitig stattfinden, mit dem gleichen
 * Teilnehmerkreis
 */
public class ZweiPruefungenGleichzeitigRestriction extends HardRestriction {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ZweiPruefungenGleichzeitigRestriction.class);

  /**
   * Speichern von der Zeit, die zwischen den Pruefungen mit dem gleichen Teilnehmerkreis liegen
   * darf
   */
  private final Duration bufferBetweenPlanungseinheiten;

  public ZweiPruefungenGleichzeitigRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  protected ZweiPruefungenGleichzeitigRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
    this.bufferBetweenPlanungseinheiten = DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN;
  }

  protected ZweiPruefungenGleichzeitigRestriction(DataAccessService dataAccessService,
      Duration bufferBetweenPlanungseinheiten) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
    this.bufferBetweenPlanungseinheiten = bufferBetweenPlanungseinheiten;
  }

  @Override
  public Optional<HardRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (pruefung.isGeplant()) {
      //Setzen von den start und end Termin, wo das Kriterium verletzt werden könnte
      LocalDateTime start = pruefung.getStartzeitpunkt().minus(bufferBetweenPlanungseinheiten);
      LocalDateTime end = getEndTime(pruefung);
      // Anm.: Liste muss kopiert werden, da Model nur unmodifiable Lists zurückgibt
      List<Planungseinheit> testList = new ArrayList<>(
          tryToGetAllPlanungseinheitenBetween(start, end));
      Optional<HardRestrictionAnalysis> hKA = checkForPlanungseinheitenHartesKriterium(
          pruefung, start, end, testList);
      if (hKA.isPresent()) {
        return hKA;
      }
    }
    return Optional.empty();
  }

  private Set<Planungseinheit> tryToGetAllPlanungseinheitenBetween(LocalDateTime start,
      LocalDateTime end) throws NoPruefungsPeriodeDefinedException {
    try {
      return dataAccessService.getAllPlanungseinheitenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor Ende liegen, da ich das berechne
      e.printStackTrace();
      return Collections.emptySet();
    }
  }

  /**
   * Methode die durch alle Planungseinheiten in der übergebenen Liste geht und die für die
   * Planungseinheit zuständige Methode aufruft
   *
   * @param pruefung die Pruefung die neu Hinzugefügt wurde und für die das Kriterium getestet wird
   * @param start    der Starttermin ab wo die Überprüfung durchgeführt werden soll (Nur für Blöcke
   *                 relevant)
   * @param end      der Endtermin bis wohin die Überprüfung durchgeführt werden sol (Nur für Blöcke
   *                 relevant)
   * @param testList die Liste mit Planungseinheiten, die zu dem Startzeitpunkt stattfinden
   * @return Entweder eine HardRestrictionAnalysis, wo die Pruefungen, das Kriterium und die
   * Teilnehmer mit ihrer Anzahl drin steht oder ein leeres Optional
   */
  private Optional<HardRestrictionAnalysis> checkForPlanungseinheitenHartesKriterium(
      Pruefung pruefung, LocalDateTime start, LocalDateTime end, List<Planungseinheit> testList) {

    //Zum Sammeln der Teilnehmerkreise und die Pruefungen, die einen harten Konflikt verursachen
    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    HashSet<Pruefung> inConflictROPruefung = new HashSet<>();
    Optional<HardRestrictionAnalysis> hKA = Optional.empty();
    if (testList != null) {
      testList.remove(pruefung);

      //Durchgehen der Liste von Planungseinheiten und unterscheiden von unterschiedlichem Typ
      for (Planungseinheit planungseinheit : testList) {
        if (planungseinheit.isBlock()) {
          testForBlockHard(pruefung, planungseinheit.asBlock(), start, end, inConflictROPruefung,
              teilnehmerCount);
        } else {
          getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung(),
              inConflictROPruefung, teilnehmerCount);
        }
      }
      hKA = testAndCreateNewHartesKriteriumAnalyse(teilnehmerCount, inConflictROPruefung);
    }
    return hKA;
  }

  /**
   * Hier werden die Parameter zu einer HartenKriteriumsAnalyse zusammen gebaut, falls es einen
   * Konflikt gibt
   *
   * @param teilnehmerCount      eine Map, wo die in Konflikt stehende Teilnehmerkreise und ihre
   *                             Anzahl gespeichert sind
   * @param inConflictROPruefung ein Set, in welchem die in Konflikt stehende Pruefungen gespeichert
   *                             sind
   * @return Entweder die HarteKriteriumsAnalyse mit den in Konflikt stehenden Pruefungen und
   * Teilnehmerkreisen oder ein leeres Optional
   */
  private Optional<HardRestrictionAnalysis> testAndCreateNewHartesKriteriumAnalyse(
      Map<Teilnehmerkreis, Integer> teilnehmerCount, HashSet<Pruefung> inConflictROPruefung) {
    if (!inConflictROPruefung.isEmpty()) {
      HardRestrictionAnalysis hKA = new HardRestrictionAnalysis(inConflictROPruefung,
          this.kriterium, teilnehmerCount);
      return Optional.of(hKA);
    }
    return Optional.empty();
  }

  /**
   * In dieser Methode wird der übergebene Block unterschieden in den unterschiedlichen Blocktypen
   * und jeweils ihre Methode dann aufgerufen
   *
   * @param pruefung             die zu überprüfende Pruefung
   * @param block                der zu überprüfende Block
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
        && (uebereinstimmendeTeilnehmerkreise(block, pruefung))) {
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
   * In dieser Methode wird für einen Block von Typ parallel das Kriterium getestet
   *
   * @param pruefung             die zu überprüfende Pruefung
   * @param start                der Starttermin, ab wo es zu einem Konflikt kommen könnte
   * @param end                  der Endtermin, ab wo es zu einem Konflikt kommen könnte
   * @param pruefungenFromBlock  die Pruefungen, die sich in dem Block befinden
   * @param teilnehmerCount      die in Konflikt stehenden Teilnehmerkreise mit ihrer Anzahl
   * @param inConflictROPruefung die in Konflikt stehenden Pruefungen
   */
  private void testForParallelBlock(Pruefung pruefung, Set<Pruefung> pruefungenFromBlock,
      LocalDateTime start, LocalDateTime end,
      Map<Teilnehmerkreis, Integer> teilnehmerCount, HashSet<Pruefung> inConflictROPruefung) {
    for (Pruefung pruefungBlock : pruefungenFromBlock) {
      if ((uebereinstimmendeTeilnehmerkreise(pruefungBlock, pruefung))
          && !outOfRange(start, end, pruefungBlock)) {
        getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock,
            inConflictROPruefung, teilnehmerCount);
      }
    }
  }

  /**
   * Diese Methode händelt den Sequential Block typ ab
   *
   * @param pruefung             die zu überprüfende Pruefung
   * @param pruefungenFromBlock  ein Set von Pruefungen, welches sich in einen sequentiellen Block
   *                             gefunden
   * @param inConflictROPruefung die aktuell in Konflikt stehende Pruefungen
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
   * Methode um zu überprüfen, ob sich ein Block mit der übergebenen Zeitspanne nicht überschneidet
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
   * @return true, wenn es einen gemeinsamen Teilnehmerkreis gibt und false, wenn es keinen
   * gemeinsamen Teilnehmerkreis gibt
   */
  private boolean uebereinstimmendeTeilnehmerkreise(Planungseinheit planungseinheit,
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
      Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {

    Set<Pruefung> geplantePruefungen = new HashSet<>(dataAccessService.getPlannedPruefungen());
    geplantePruefungen.removeIf(
        (Pruefung pruefung) -> notSameTeilnehmerkreis(pruefung, planungseinheit));
    if (!planungseinheit.isBlock()) {
      geplantePruefungen.remove(planungseinheit.asPruefung());
      Optional<Block> potentialBlock = dataAccessService.getBlockTo(planungseinheit.asPruefung());
      potentialBlock.ifPresent(block -> geplantePruefungen.removeIf(
          pruefung -> block.getPruefungen().contains(pruefung)));
    }
    LOGGER.trace("Found {} conflicting with {} because of common Teilnehmerkreise.",
        geplantePruefungen, planungseinheit);
    return geplantePruefungen;
  }

  @Override
  public boolean wouldBeHardConflictAt(LocalDateTime startTime, Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(startTime, planungseinheit);
    boolean isInConflict = false;
    Set<Planungseinheit> planungseinheiten = getPlanungseinheitenDuring(startTime, planungseinheit);
    Iterator<Planungseinheit> planungseinheitIterator = planungseinheiten.iterator();
    while (planungseinheitIterator.hasNext() && !isInConflict) {
      Planungseinheit other = planungseinheitIterator.next();
      isInConflict = areInConflict(planungseinheit, other);
    }
    LOGGER.debug("{} was {}found to cause a conflict starting at {}.", planungseinheit,
        (isInConflict ? "" : "not "), startTime);
    return isInConflict;
  }

  @NotNull
  private Set<Planungseinheit> getPlanungseinheitenDuring(@NotNull LocalDateTime startTime,
      @NotNull Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> result = new HashSet<>();
    LocalDateTime endTime = startTime.plus(planungseinheit.getDauer())
        .plus(bufferBetweenPlanungseinheiten);
    startTime = startTime.minus(bufferBetweenPlanungseinheiten);
    try {
      result.addAll(dataAccessService.getAllPlanungseinheitenBetween(startTime, endTime));
    } catch (IllegalTimeSpanException e) {
      e.printStackTrace();
      // can never happen as a planungseinheit by definition has to have a positive duration
    }
    return result;
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
   * Berechnet den Endzeitpunkt, von der übergebenen Pruefung. Der Endzeitpunkt unterscheidet sich,
   * falls die Pruefung in einem Block existiert
   *
   * @param pruefung die Pruefung, mit der der Endzeitpunkt festgelegt werden soll
   * @return der EndZeitpunkt von einer Prüfung, welcher sich unterscheidet, ob es ein Block oder ob
   * es sich um eine Pruefung handelt
   */
  @NotNull
  private LocalDateTime getEndTime(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    Optional<Block> maybeBlock = dataAccessService.getBlockTo(pruefung);
    LocalDateTime date = pruefung.getStartzeitpunkt().plus(bufferBetweenPlanungseinheiten);

    return maybeBlock.isPresent() && maybeBlock.get().getTyp() == Blocktyp.SEQUENTIAL ? date.plus(
        maybeBlock.get().getDauer()) : date.plus(pruefung.getDauer());
  }

  /**
   * In dieser Methode werden zwei Pruefungen miteinander verglichen und die übereinstimmenden
   * Teilnehmerkreise mit ihrer Anzahl in eine Map gespeichert. zusätzlich werden die in Konflikt
   * stehenden Pruefungen in ein Set gespeichert
   *
   * @param pruefung             die Pruefung für die das HarteKriterium gecheckt werden soll
   * @param toCheck              die Pruefung mit der verglichen werden soll
   * @param inConflictROPruefung das Set, von Pruefungen, welche in Konflikt stehen
   * @param teilnehmerCount      die Map mit Teilnehmerkreisen und deren Anzahl
   */
  private void getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck,
      HashSet<Pruefung> inConflictROPruefung,
      Map<Teilnehmerkreis, Integer> teilnehmerCount) {

    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        //Vergleich der Teilnehmerkreise auf ihrer Anzahl
        Integer teilnehmerKreisSchaetzung = teilnehmerCount.get(teilnehmerkreis);
        Integer teilnehmerkreisToCheck = toCheck.getSchaetzungen().get(teilnehmerkreis);
        if (teilnehmerKreisSchaetzung != null) {
          if (teilnehmerKreisSchaetzung < teilnehmerkreisToCheck) {
            teilnehmerCount.replace(teilnehmerkreis, teilnehmerkreisToCheck);
          }
        } else {
          teilnehmerCount.put(teilnehmerkreis, teilnehmerkreisToCheck);
        }

        //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
        inConflictROPruefung.add(pruefung);
        inConflictROPruefung.add(toCheck);
      }
    }
    if (!inConflictROPruefung.isEmpty()) {
      TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(teilnehmerCount,
          pruefung.getSchaetzungen());
    }
  }

}
