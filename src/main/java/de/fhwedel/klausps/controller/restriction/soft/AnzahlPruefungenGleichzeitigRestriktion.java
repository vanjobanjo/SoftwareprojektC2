package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.PlanungseinheitUtil.getAllPruefungen;
import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AnzahlPruefungenGleichzeitigRestriktion extends WeicheRestriktion {

  private static final int DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME = 6;

  // TODO use global default
  private static final Duration DEFAULT_BUFFER = Duration.ofMinutes(30);

  private final int maxPruefungenAtATime;

  private final Duration puffer;

  protected AnzahlPruefungenGleichzeitigRestriktion(@NotNull DataAccessService dataAccessService) {
    this(dataAccessService, DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME, DEFAULT_BUFFER);
  }

  protected AnzahlPruefungenGleichzeitigRestriktion(@NotNull DataAccessService dataAccessService,
      int maxPruefungenAtATime, @NotNull Duration puffer) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH);
    this.maxPruefungenAtATime = maxPruefungenAtATime;
    this.puffer = puffer;
  }

  protected AnzahlPruefungenGleichzeitigRestriktion(@NotNull DataAccessService dataAccessService,
      int maxPruefungenAtATime) {
    this(dataAccessService, maxPruefungenAtATime, DEFAULT_BUFFER);
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
    ignorePruefungenInSameBlockOf(planungseinheitenOverlappingTheOneToCheck, pruefung);

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

  private void ignorePruefungenInSameBlockOf(@NotNull List<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor) {
    Pruefung pruefung = toFilterFor.asPruefung();
    Optional<Block> block = dataAccessService.getBlockTo(pruefung);
    if (block.isPresent()) {
      planungseinheiten.removeAll(block.get().getPruefungen());
      planungseinheiten.add(toFilterFor);
    }
  }

  @NotNull
  private Optional<WeichesKriteriumAnalyse> getAnalyseIfRestrictionViolated(
      @NotNull List<Planungseinheit> planungseinheitenOverlappingTheOneToCheck) {
    if (planungseinheitenOverlappingTheOneToCheck.size() <= maxPruefungenAtATime) {
      return Optional.empty();
    }
    // find overlapping pruefungen
    Set<Planungseinheit> conflictingPlanungseinheiten = findTooManyOverlappingPlanungseinheiten(
        planungseinheitenOverlappingTheOneToCheck);
    if (!conflictingPlanungseinheiten.isEmpty()) {
      return Optional.of(createAnalyse(conflictingPlanungseinheiten));
    }
    return Optional.empty();
  }

  @NotNull
  private Set<Planungseinheit> findTooManyOverlappingPlanungseinheiten(
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
      if (planungseinheitenAtSameTime.size() > maxPruefungenAtATime) {
        conflictingPlanungseinheiten.addAll(planungseinheitenAtSameTime);
      }
    }
    return conflictingPlanungseinheiten;
  }

  @NotNull
  private WeichesKriteriumAnalyse createAnalyse(
      @NotNull Set<Planungseinheit> violatingPlanungseinheiten) {
    return new WeichesKriteriumAnalyse(getAllPruefungen(violatingPlanungseinheiten), this.kriterium,
        getAllTeilnehmerkreiseFrom(violatingPlanungseinheiten),
        getAmountAffectedStudents(violatingPlanungseinheiten),
        calcScoring(violatingPlanungseinheiten));
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

  @NotNull
  private Set<Teilnehmerkreis> getAllTeilnehmerkreiseFrom(
      @NotNull Iterable<Planungseinheit> planungseinheiten) {
    Set<Teilnehmerkreis> teilnehmerkreise = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      teilnehmerkreise.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return teilnehmerkreise;
  }

  private int getAmountAffectedStudents(@NotNull Iterable<Planungseinheit> planungseinheiten) {
    HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis = new HashMap<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      collectMaxMountOfStudentsInFor(maxTeilnehmerPerTeilnehmerkreis, planungseinheit);
    }
    return getSumm(maxTeilnehmerPerTeilnehmerkreis.values());
  }

  private void collectMaxMountOfStudentsInFor(HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis,
      Planungseinheit planungseinheit) {
    for (Map.Entry<Teilnehmerkreis, Integer> entry : planungseinheit.getSchaetzungen()
        .entrySet()) {
      if (isNotContainedWithHigherValueIn(maxTeilnehmerPerTeilnehmerkreis, entry)) {
        maxTeilnehmerPerTeilnehmerkreis.put(entry.getKey(), entry.getValue());
      }
    }
  }

  private boolean isNotContainedWithHigherValueIn(HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis,
      Entry<Teilnehmerkreis, Integer> entry) {
    return !maxTeilnehmerPerTeilnehmerkreis.containsKey(entry.getKey())
        || maxTeilnehmerPerTeilnehmerkreis.get(entry.getKey()) <= entry.getValue();
  }

  private int calcScoring(@NotNull Set<Planungseinheit> planungseinheiten) {
    int scoring = 0;
    if (planungseinheiten.size() > maxPruefungenAtATime) {
      scoring = planungseinheiten.size() - maxPruefungenAtATime;
      scoring *= this.kriterium.getWert();
    }
    return scoring;
  }

  private int getSumm(@NotNull Iterable<Integer> values) {
    int result = 0;
    for (Integer value : values) {
      result += value;
    }
    return result;
  }
}
