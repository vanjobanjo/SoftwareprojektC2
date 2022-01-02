package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.PlanungseinheitUtil.getAllPruefungen;
import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
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
    Duration bufferPerSide = puffer.dividedBy(2);
    if (pruefung.isGeplant()) {
      LocalDateTime startOfPruefung = pruefung.getStartzeitpunkt().minus(bufferPerSide);
      LocalDateTime endOfPruefung = pruefung.endzeitpunkt().plus(bufferPerSide);

      List<Planungseinheit> planungseinheitenOverlappingTheOneToCheck = tryToGetAllPlanungseinheitenBetween(
          startOfPruefung, endOfPruefung);

      return getAnalyseIfRestrictionViolated(planungseinheitenOverlappingTheOneToCheck);
    }
    return Optional.empty();
  }

  @NotNull
  private List<Planungseinheit> tryToGetAllPlanungseinheitenBetween(@NotNull LocalDateTime from,
      @NotNull LocalDateTime to) {
    try {
      return dataAccessService.getAllPruefungenBetween(from, to);
    } catch (IllegalTimeSpanException e) {
      // can never happen, as the duration of a pruefung is checked to be > 0
      throw new IllegalStateException("A Pruefung with a negative duration can not exist.", e);
    }
  }

  @NotNull
  private Optional<WeichesKriteriumAnalyse> getAnalyseIfRestrictionViolated(
      List<Planungseinheit> planungseinheitenOverlappingTheOneToCheck) {
    if (planungseinheitenOverlappingTheOneToCheck.size() > maxPruefungenAtATime) {
      // find overlapping pruefungen
      Set<Planungseinheit> conflictingPlanungseinheiten = findTooManyOverlappingPlanungseinheiten(
          planungseinheitenOverlappingTheOneToCheck);
      if (!conflictingPlanungseinheiten.isEmpty()) {
        return createAnalyse(conflictingPlanungseinheiten);
      }
    }
    return Optional.empty();
  }

  @NotNull
  private Set<Planungseinheit> findTooManyOverlappingPlanungseinheiten(
      @NotNull List<Planungseinheit> planungseinheiten) {
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
  private Optional<WeichesKriteriumAnalyse> createAnalyse(
      @NotNull Set<Planungseinheit> violatingPlanungseinheiten) {
    if (violatingPlanungseinheiten.size() > maxPruefungenAtATime) {
      return Optional.of(
          new WeichesKriteriumAnalyse(getAllPruefungen(violatingPlanungseinheiten), this.kriterium,
              getAllTeilnehmerkreiseFrom(violatingPlanungseinheiten),
              getAmountAffectedStudents(violatingPlanungseinheiten),
              calcScoring(violatingPlanungseinheiten)));
    }
    return Optional.empty();
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
      for (Map.Entry<Teilnehmerkreis, Integer> entry : planungseinheit.getSchaetzungen()
          .entrySet()) {
        if (!maxTeilnehmerPerTeilnehmerkreis.containsKey(entry.getKey())
            || maxTeilnehmerPerTeilnehmerkreis.get(entry.getKey()) <= entry.getValue()) {
          maxTeilnehmerPerTeilnehmerkreis.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return getSumm(maxTeilnehmerPerTeilnehmerkreis.values());
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
