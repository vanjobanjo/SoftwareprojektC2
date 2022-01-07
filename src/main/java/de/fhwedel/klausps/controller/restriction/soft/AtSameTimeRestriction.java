package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.PlanungseinheitUtil.getAllPruefungen;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class AtSameTimeRestriction extends WeicheRestriktion {

  // TODO refactor methods to overwrite the methods from WeicheRestriktion

  // TODO use global default
  protected static final Duration DEFAULT_BUFFER = Duration.ofMinutes(30);

  protected final Duration puffer;

  protected AtSameTimeRestriction(DataAccessService dataAccessService, WeichesKriterium kriterium,
      Duration puffer) {
    super(dataAccessService, kriterium);
    this.puffer = puffer;
  }

  @Override
  @NotNull
  public Optional<WeichesKriteriumAnalyse> evaluate(@NotNull Pruefung pruefung) {
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    Duration bufferPerSide = puffer.dividedBy(2);
    LocalDateTime startOfPruefung = pruefung.getStartzeitpunkt().minus(bufferPerSide);
    LocalDateTime endOfPruefung = pruefung.endzeitpunkt().plus(bufferPerSide);

    List<Planungseinheit> planungseinheitenOverlappingTheOneToCheck = tryToGetAllPlanungseinheitenBetween(
        startOfPruefung, endOfPruefung);
    ignorePruefungenOf(planungseinheitenOverlappingTheOneToCheck, pruefung);

    return getAnalyseIfRestrictionViolated(planungseinheitenOverlappingTheOneToCheck);
  }

  @NotNull
  private List<Planungseinheit> tryToGetAllPlanungseinheitenBetween(@NotNull LocalDateTime from,
      @NotNull LocalDateTime to) {
    try {
      return dataAccessService.getAllPlanungseinheitenBetween(from, to);
    } catch (IllegalTimeSpanException e) {
      // can never happen, as the duration of a pruefung is checked to be > 0
      throw new IllegalStateException("A Pruefung with a negative duration can not exist.", e);
    }
  }

  protected abstract void ignorePruefungenOf(@NotNull List<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor);

  @NotNull
  private Optional<WeichesKriteriumAnalyse> getAnalyseIfRestrictionViolated(
      @NotNull List<Planungseinheit> planungseinheitenOverlappingTheOneToCheck) {
    if (!violatesRestriction(planungseinheitenOverlappingTheOneToCheck)) {
      return Optional.empty();
    }
    // find overlapping pruefungen
    Set<Planungseinheit> conflictingPlanungseinheiten = findConflictingPlanungseinheiten(
        planungseinheitenOverlappingTheOneToCheck);
    if (!conflictingPlanungseinheiten.isEmpty()) {
      return Optional.of(createAnalyse(conflictingPlanungseinheiten));
    }
    return Optional.empty();
  }

  protected abstract boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten);

  @NotNull
  private Set<Planungseinheit> findConflictingPlanungseinheiten(
      @NotNull List<Planungseinheit> planungseinheiten) {
    // might be possible to implement more efficient for the suspected most common use case of many
    // pruefungen starting at the same time and only few variations in the length of pruefung
    // with usage of interval-tree. Although such a structure would perform way worse in the worst
    // case and costs a lot of time to implement.
    Set<Planungseinheit> conflictingPlanungseinheiten = new HashSet<>();
    // O(n^2) with n = amount of Planungseinheiten overlapping the given time interval
    for (Planungseinheit planungseinheit : planungseinheiten) {
      LocalDateTime startOfPlanungseinheit = planungseinheit.getStartzeitpunkt()
          .minus(puffer.dividedBy(2));
      Collection<Planungseinheit> planungseinheitenAtSameTime = selectAllPlanungseinheitenContaining(
          startOfPlanungseinheit, planungseinheiten);
      if (!planungseinheitenAtSameTime.isEmpty()) {
        planungseinheitenAtSameTime.add(planungseinheit);
      }
      if (violatesRestriction(planungseinheitenAtSameTime)) {
        conflictingPlanungseinheiten.addAll(planungseinheitenAtSameTime);
      }
    }
    return conflictingPlanungseinheiten;
  }

  @NotNull
  private WeichesKriteriumAnalyse createAnalyse(
      @NotNull Set<Planungseinheit> violatingPlanungseinheiten) {
    return new WeichesKriteriumAnalyse(getAllPruefungen(violatingPlanungseinheiten), this.kriterium,
        getAffectedTeilnehmerkreiseFrom(violatingPlanungseinheiten),
        getAffectedStudentsFrom(violatingPlanungseinheiten),
        calcScoringFor(violatingPlanungseinheiten));
  }

  @NotNull
  private Collection<Planungseinheit> selectAllPlanungseinheitenContaining(
      @NotNull LocalDateTime time, @NotNull Iterable<Planungseinheit> planungseinheiten) {
    // O(n) with n = amount of Planungseinheiten to check
    Set<Planungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (!time.isBefore(planungseinheit.getStartzeitpunkt().minus(puffer.dividedBy(2)))
          && time.isBefore(planungseinheit.endzeitpunkt().plus(puffer.dividedBy(2)))) {
        result.add(planungseinheit);
      }
    }
    return result;
  }

  protected abstract Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten);

  protected abstract int getAffectedStudentsFrom(Set<Planungseinheit> violatingPlanungseinheiten);

  protected abstract int calcScoringFor(Set<Planungseinheit> violatingPlanungseinheiten);

}
