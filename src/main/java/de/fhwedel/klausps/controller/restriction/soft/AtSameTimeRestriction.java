package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.PlanungseinheitUtil.getAllPruefungen;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * A generic Restriction on a multitude of {@link Pruefung} and {@link Block} that are planned at
 * overlapping times.
 */
public abstract class AtSameTimeRestriction extends SoftRestriction {

  /**
   * The time buffer before and after a Pruefung which should be evaluated as part of the Pruefung
   * to allow for preparation and follow-up tasks.
   */
  protected final Duration buffer;

  /**
   * Create a AtSameTimeRestriction.
   *
   * @param dataAccessService The service to use for data access.
   * @param kriterium         The type of {@link WeichesKriterium} resembled by this class.
   * @param buffer            The time buffer before and after a Pruefung which should be evaluated
   *                          as part of the Pruefung to allow for preparation and follow-up tasks.
   */
  protected AtSameTimeRestriction(DataAccessService dataAccessService, WeichesKriterium kriterium,
      Duration buffer) {
    super(dataAccessService, kriterium);
    this.buffer = buffer;
  }

  @Override
  @NotNull
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(@NotNull Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    Duration bufferPerSide = buffer.dividedBy(2);
    LocalDateTime startOfPruefung = getSequentialBlockOrSelf(pruefung).getStartzeitpunkt()
        .minus(bufferPerSide);
    LocalDateTime endOfPruefung = getSequentialBlockOrSelf(pruefung).endzeitpunkt()
        .plus(bufferPerSide);

    Set<Planungseinheit> planungseinheitenOverlappingTheOneToCheck = tryToGetAllPlanungseinheitenBetween(
        startOfPruefung, endOfPruefung);
    ignorePruefungenOf(planungseinheitenOverlappingTheOneToCheck, pruefung);

    return getAnalyseIfRestrictionViolated(planungseinheitenOverlappingTheOneToCheck);
  }

  /**
   * Get the block a Pruefung is in if it is of type SEQUENTIAL, or the Pruefung itself otherwise.
   *
   * @param pruefung The Pruefung to get the block for.
   * @return Either the block associated with the Pruefung ot the Pruefung itself.
   * @throws NoPruefungsPeriodeDefinedException In case that no Pruefungsperiode is set.
   */
  protected final Planungseinheit getSequentialBlockOrSelf(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Optional<Block> block = dataAccessService.getBlockTo(pruefung);
    if (block.isPresent() && block.get().getTyp().equals(SEQUENTIAL)) {
      return block.get();
    }
    return pruefung;
  }

  /**
   * Get all {@link Planungseinheit Planungseinheiten} in a defined timespan.
   *
   * @param from The start of the timespan - inclusive.
   * @param to   The end of the timespan - exclusive.
   * @return All Planungseinheiten that happen to overlap the described timespan.
   * @throws NoPruefungsPeriodeDefinedException In case that no Pruefungsperiode is set.
   */
  @NotNull
  private Set<Planungseinheit> tryToGetAllPlanungseinheitenBetween(@NotNull LocalDateTime from,
      @NotNull LocalDateTime to) throws NoPruefungsPeriodeDefinedException {
    try {
      return dataAccessService.getAllPlanungseinheitenBetween(from, to);
    } catch (IllegalTimeSpanException e) {
      // can never happen, as the duration of a pruefung is checked to be > 0
      throw new IllegalStateException("A Pruefung with a negative duration can not exist.", e);
    }
  }

  /**
   * Remove {@link Planungseinheit Planungseinheit} that should be ignored while checking the
   * restriction.
   *
   * @param planungseinheiten The planungseinheiten to filter from.
   * @param toFilterFor       The Planungseinheit for which the restriction is beeing checked.
   * @throws NoPruefungsPeriodeDefinedException In case that no Pruefungsperiode is set.
   */
  protected abstract void ignorePruefungenOf(@NotNull Set<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor) throws NoPruefungsPeriodeDefinedException;

  /**
   * Get an {@link SoftRestrictionAnalysis} corresponding to this restriction in case it is
   * violated.
   *
   * @param planungseinheitenOverlappingTheOneToCheck The Planungseinheiten that overlap the
   *                                                  timespan in which the Pruefung the check is
   *                                                  for is planned.
   * @return Either an empty {@link Optional} in case the restriction is not violated or one
   * containing an analysis describing the violation.
   */
  @NotNull
  private Optional<SoftRestrictionAnalysis> getAnalyseIfRestrictionViolated(
      @NotNull Set<Planungseinheit> planungseinheitenOverlappingTheOneToCheck) {
    if (!violatesRestriction(planungseinheitenOverlappingTheOneToCheck)) {
      return Optional.empty();
    }
    // find overlapping pruefungen
    Set<Planungseinheit> conflictingPlanungseinheiten = findConflictingPlanungseinheiten(
        planungseinheitenOverlappingTheOneToCheck);
    if (!conflictingPlanungseinheiten.isEmpty()) {
      return Optional.of(buildAnalysis(conflictingPlanungseinheiten));
    }
    return Optional.empty();
  }

  /**
   * Find which {@link Planungseinheit Planungseinheit} from a set are in conflict with each other in terms of
   * this restriction.
   *
   * @param planungseinheiten The planungseinheiten to check for conflicts in.
   * @return All Planungseinheiten in conflict with each other in terms of this restriction.
   */
  @NotNull
  private Set<Planungseinheit> findConflictingPlanungseinheiten(
      @NotNull Set<Planungseinheit> planungseinheiten) {
    // might be possible to implement more efficient for the suspected most common use case of many
    // pruefungen starting at the same time and only few variations in the length of pruefung
    // with usage of interval-tree. Although such a structure would perform way worse in the worst
    // case and costs a lot of time to implement.
    Set<Planungseinheit> conflictingPlanungseinheiten = new HashSet<>();
    // O(n^2) with n = amount of Planungseinheiten overlapping the given time interval
    for (Planungseinheit planungseinheit : planungseinheiten) {
      conflictingPlanungseinheiten.addAll(getConflicting(planungseinheit, planungseinheiten));
    }
    return conflictingPlanungseinheiten;
  }

  /**
   * Get the {@link Planungseinheit Planungseinheit} in conflict.
   *
   * @param planungseinheit   The planungseinheit to check for.
   * @param planungseinheiten The planungseinheiten to check against.
   * @return Planungseinheiten in conflict with the checked planungseinheit.
   */
  @NotNull
  private Set<Planungseinheit> getConflicting(@NotNull Planungseinheit planungseinheit,
      @NotNull Set<Planungseinheit> planungseinheiten) {
    LocalDateTime startOfPlanungseinheit = planungseinheit.getStartzeitpunkt()
        .minus(buffer.dividedBy(2));
    Collection<Planungseinheit> planungseinheitenAtSameTime = getAllPlanungseinheitenCovering(
        startOfPlanungseinheit, planungseinheiten);
    if (!planungseinheitenAtSameTime.isEmpty()) {
      planungseinheitenAtSameTime.add(planungseinheit);
    }
    if (violatesRestriction(planungseinheitenAtSameTime)) {
      return Set.copyOf(planungseinheitenAtSameTime);
    }
    return emptySet();
  }

  /**
   * Select all {@link Planungseinheit Planungseinheit} that cover a certain moment.
   *
   * @param time              The time covered be the desired Planungseinheiten.
   * @param planungseinheiten The planungseinheiten to search through.
   * @return All Planungseinheiten that cover a certain moment.
   */
  @NotNull
  private Collection<Planungseinheit> getAllPlanungseinheitenCovering(
      @NotNull LocalDateTime time, @NotNull Iterable<Planungseinheit> planungseinheiten) {
    // O(n) with n = amount of Planungseinheiten to check
    Set<Planungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (!time.isBefore(planungseinheit.getStartzeitpunkt().minus(buffer.dividedBy(2)))
          && time.isBefore(planungseinheit.endzeitpunkt().plus(buffer.dividedBy(2)))) {
        result.add(planungseinheit);
      }
    }
    return result;
  }

  /**
   * Check whether a set of {@link Planungseinheit Planungseinheit} violates the restriction.
   *
   * @param planungseinheiten The planungseinheiten to check.
   * @return True in case the Planungseinheiten violate the restriction, otherwise False.
   */
  protected abstract boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten);

  /**
   * Build a new Analysis of the restriction violation considering certain {@link Planungseinheit
   * Planungseinheit}.
   *
   * @param violatingPlanungseinheiten The planungseinheiten causing a violation of this
   *                                   restriction.
   * @return A new Analysis of the restriction violation.
   */
  @NotNull
  private SoftRestrictionAnalysis buildAnalysis(
      @NotNull Set<Planungseinheit> violatingPlanungseinheiten) {
    return new SoftRestrictionAnalysis(getAllPruefungen(violatingPlanungseinheiten), this.kriterium,
        getAffectedTeilnehmerkreiseFrom(violatingPlanungseinheiten),
        getAmountOfAttendingStudents(violatingPlanungseinheiten),
        calcScoringFor(violatingPlanungseinheiten));
  }

  /**
   * Get all {@link Teilnehmerkreis Teilnehmerkreise} involved in a set of {@link Planungseinheit
   * Planungseinheit}.
   *
   * @param violatingPlanungseinheiten The Planungseinheiten to search through.
   * @return All Teilnehmerkreise involved.
   */
  @NotNull
  protected abstract Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten);

  /**
   * Get the amount of students attending certain {@link Planungseinheit Planungseinheit}.
   *
   * @param planungseinheiten The planungseinheiten tu count the attending students for.
   * @return The amount of students attending the Planungseinheiten.
   */
  protected abstract int getAmountOfAttendingStudents(
      Collection<Planungseinheit> planungseinheiten);

  /**
   * Calculate the scoring for a violation of this restriction based on involved {@link
   * Planungseinheit Planungseinheit}.
   *
   * @param violatingPlanungseinheiten The planungseinheiten causing the violation.
   * @return The scoring based on the causing Planungseinheiten.
   */
  protected abstract int calcScoringFor(Collection<Planungseinheit> violatingPlanungseinheiten);

}
