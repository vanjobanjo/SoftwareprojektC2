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
 * Restriction describing that it should not be allowed for two or more {@link Planungseinheit}en
 * with identical {@link Teilnehmerkreis}en to be planned on overlapping times.
 */
public class ZweiPruefungenGleichzeitigRestriction extends HardRestriction {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ZweiPruefungenGleichzeitigRestriction.class);

  /**
   * A time buffer between planungseinheiten that should be minded.
   */
  private final Duration bufferBetweenPlanungseinheiten;

  /**
   * Create a ZweiPruefungenGleichzeitigRestriction using a service for data access from the {@link
   * ServiceProvider}.
   */
  public ZweiPruefungenGleichzeitigRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  /**
   * Create a ZweiPruefungenGleichzeitigRestriction.
   *
   * @param dataAccessService The service to use for accessing the underlying data.
   */
  protected ZweiPruefungenGleichzeitigRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
    this.bufferBetweenPlanungseinheiten = DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN;
  }

  @Override
  public Optional<HardRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (pruefung.isGeplant()) {
      // determine the start and end time between which the restriction could be violated
      LocalDateTime start = pruefung.getStartzeitpunkt().minus(bufferBetweenPlanungseinheiten);
      LocalDateTime end = getEndTime(pruefung);
      // List must be a copy as the result retrieved from the data model is unmodifiable
      List<Planungseinheit> listWithPlanungseinheitenInTimeSpan = new ArrayList<>(
          tryToGetAllPlanungseinheitenBetween(start, end));
      Optional<HardRestrictionAnalysis> hKA = checkForPlanungseinheitenHartesKriterium(
          pruefung, start, end, listWithPlanungseinheitenInTimeSpan);
      if (hKA.isPresent()) {
        return hKA;
      }
    }
    return Optional.empty();
  }

  @Override
  public Set<Pruefung> getAllPotentialConflictingPruefungenWith(
      Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {

    Set<Pruefung> geplantePruefungen = new HashSet<>(dataAccessService.getPlannedPruefungen());
    geplantePruefungen.removeIf(
        (Pruefung pruefung) -> !haveCommonTeilnehmerkreis(pruefung, planungseinheit));
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
    Set<Planungseinheit> planungseinheiten = getAllDuringPlanungseinheitAt(startTime,
        planungseinheit);
    Iterator<Planungseinheit> planungseinheitIterator = planungseinheiten.iterator();
    while (planungseinheitIterator.hasNext() && !isInConflict) {
      Planungseinheit other = planungseinheitIterator.next();
      isInConflict = areInConflict(planungseinheit, other);
    }
    LOGGER.debug("{} was {}found to cause a conflict starting at {}.", planungseinheit,
        (isInConflict ? "" : "not "), startTime);
    return isInConflict;
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

    Set<Pruefung> pruefungenFromBlock = block.getPruefungen();

    if (!pruefungenFromBlock.contains(pruefung)
        && (uebereinstimmendeTeilnehmerkreise(block, pruefung))) {
      //the different Blocktypes will be handled different
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
      //check if the Pruefung from the Block and the pruefungtoCheck have the same Teilnehmerkreis
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

  /**
   * Get all {@link Planungseinheit}en that happen to be scheduled during the time a specific
   * planungseinheit would occupy if scheduled at a specific start time.
   *
   * @param startTime       The hypothetical start time for the planungseinheit during which to
   *                        search.
   * @param planungseinheit The planungseinheit during which to search.
   * @return All planungseinheiten that happen to be scheduled during the time a specific
   * planungseinheit would occupy if scheduled at a specific start time.
   * @throws NoPruefungsPeriodeDefinedException In case no Pruefungsperiode is defined.
   */
  @NotNull
  private Set<Planungseinheit> getAllDuringPlanungseinheitAt(@NotNull LocalDateTime startTime,
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

  /**
   * Check whether two {@link Planungseinheit}en are in conflict.
   *
   * @param planungseinheit One planungseinheit for the check for conflict.
   * @param other           The other planungseinheit for the check for conflict.
   * @return True in case two distinct planungseinheiten that are in conflict with each other are
   * passed, otherwise False.
   */
  private boolean areInConflict(Planungseinheit planungseinheit, Planungseinheit other) {
    if (!planungseinheit.equals(other)) {
      return haveCommonTeilnehmerkreis(planungseinheit, other);
    }
    return false;
  }

  /**
   * Check whether two {@link Planungseinheit}en have one or more common {@link Teilnehmerkreis}e.
   *
   * @param pe1 One planungseinheit for the check for common teilnehmerkreis.
   * @param pe2 The other planungseinheit for the check for common teilnehmerkreis.
   * @return True in case at least one teilnehmerkreis is in both planungseinheiten, otherwise
   * False.
   */
  private boolean haveCommonTeilnehmerkreis(@NotNull Planungseinheit pe1,
      @NotNull Planungseinheit pe2) {
    return !intersect(pe1.getTeilnehmerkreise(), pe2.getTeilnehmerkreise()).isEmpty();
  }

  /**
   * Get the intersection of two sets.
   *
   * @param setA The first set for intersection.
   * @param setB The second set for intersection.
   * @return The intersection between setA and setB.
   */
  @NotNull
  private <T> Set<T> intersect(@NotNull Set<T> setA,
      @NotNull Set<T> setB) {
    Set<T> intersection = new HashSet<>(setA);
    intersection.retainAll(setB);
    return intersection;
  }

  /**
   * Calculate the estimated end time for a {@link Pruefung} based on its belonging to a block.
   *
   * @param pruefung The preufung for which to calculate the end time.
   * @return The estimated end time for the pruefung.
   */
  @NotNull
  private LocalDateTime getEndTime(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    Optional<Block> maybeBlock = dataAccessService.getBlockTo(pruefung);
    LocalDateTime date = pruefung.getStartzeitpunkt().plus(bufferBetweenPlanungseinheiten);

    return maybeBlock.isPresent() && maybeBlock.get().getTyp() == Blocktyp.SEQUENTIAL ? date.plus(
        maybeBlock.get().getDauer()) : date.plus(pruefung.getDauer());
  }

  /**
   * Get all Planungseinheiten witch are scheduled at any time overlapping the timespan between a
   * given start and end time.
   *
   * @param start The start of the timespan to check in.
   * @param end   The end of the timespan to check in.
   * @return All Planungseinheiten witch are scheduled at any time overlapping the timespan between
   * a given start and end time.
   * @throws NoPruefungsPeriodeDefinedException In case no Pruefungsperiode is Defined.
   */
  private Set<Planungseinheit> tryToGetAllPlanungseinheitenBetween(LocalDateTime start,
      LocalDateTime end) throws NoPruefungsPeriodeDefinedException {
    try {
      return dataAccessService.getAllPlanungseinheitenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      // start can never after end as previously checked
      e.printStackTrace();
      return Collections.emptySet();
    }
  }

  /**
   * Check all given {@link Planungseinheit}en for being in conflict with a certain {@link Pruefung}
   * concerning this restriction.
   *
   * @param pruefung       The pruefung for which to check violations of this restriction.
   * @param start          The start time of the timespan to check violations in, based on the
   *                       pruefungs start time, buffer and other influences.
   * @param end            The end time of the timespan to check violations in, based on the
   *                       pruefungs end time, buffer and other influences.
   * @param listInTimeSpan All planungseinheiten covering the start time.
   * @return In case that the restriction is violated an optional containing an analysis describing
   * this violation is returned, otherwise the result is an empty optional.
   */
  private Optional<HardRestrictionAnalysis> checkForPlanungseinheitenHartesKriterium(
      Pruefung pruefung, LocalDateTime start, LocalDateTime end,
      List<Planungseinheit> listInTimeSpan) {

    // The participants causing a violation paired with the amount of students in each of these teilnehmerkreise
    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    HashSet<Pruefung> inConflictROPruefung = new HashSet<>();
    Optional<HardRestrictionAnalysis> hKA = Optional.empty();
    if (listInTimeSpan != null) {
      // The checked pruefung should not be in conflict with itself
      listInTimeSpan.remove(pruefung);

      // Loop through all planungseinheiten in the timespan,
      // deferring the actual restriction check dependent on the type of planungseinheit
      for (Planungseinheit planungseinheit : listInTimeSpan) {
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
   * Compare two {@link Pruefung}en and save their common {@link Teilnehmerkreis}e into a map. Also,
   * the pruefungen that happen to be in conflict are saved onto a set.
   *
   * @param pruefung             The pruefung for which the restriction is being checked.
   * @param toCheck              The pruefung with wich to compare the pruefung under test.
   * @param inConflictROPruefung The set into which to save the conflicting pruefungen.
   * @param teilnehmerCount      The map into which to save the teilnehmerkreise with their
   *                             respective student amounts.
   */
  private void getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck,
      HashSet<Pruefung> inConflictROPruefung,
      Map<Teilnehmerkreis, Integer> teilnehmerCount) {

    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        // comparison of amount of participants of the teilnehmerkreise
        Integer teilnehmerKreisSchaetzung = teilnehmerCount.get(teilnehmerkreis);
        Integer teilnehmerkreisToCheck = toCheck.getSchaetzungen().get(teilnehmerkreis);
        if (teilnehmerKreisSchaetzung != null) {
          if (teilnehmerKreisSchaetzung < teilnehmerkreisToCheck) {
            teilnehmerCount.replace(teilnehmerkreis, teilnehmerkreisToCheck);
          }
        } else {
          teilnehmerCount.put(teilnehmerkreis, teilnehmerkreisToCheck);
        }

        // does not matter here ase a set is used and duplicate entries are ignored
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
