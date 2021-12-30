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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
  public Optional<WeichesKriteriumAnalyse> evaluate(@NotNull Pruefung pruefung) {
    if (pruefung.isGeplant()) {
      LocalDateTime startOfPruefung = pruefung.getStartzeitpunkt().minus(puffer);
      LocalDateTime endOfPruefung = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
          .plus(puffer);

      List<Planungseinheit> pruefungenDuringCheck = tryToGetAllPlanungseinheitenBetween(
          startOfPruefung, endOfPruefung);

      if (pruefungenDuringCheck.size() > maxPruefungenAtATime) {
        // find overlapping pruefungen
        return findTooManyOverlappingPlanungseinheiten(startOfPruefung, endOfPruefung,
            pruefungenDuringCheck);
      }
    }
    return Optional.empty();
  }

  private List<Planungseinheit> tryToGetAllPlanungseinheitenBetween(@NotNull LocalDateTime from,
      @NotNull LocalDateTime to) {
    try {
      return dataAccessService.getAllPruefungenBetween(from, to);
    } catch (IllegalTimeSpanException e) {
      // can never happen, as the duration of a pruefung is checked to be > 0
      throw new IllegalStateException("A Pruefung with a negative duration can not exist.", e);
    }
  }

  private Optional<WeichesKriteriumAnalyse> findTooManyOverlappingPlanungseinheiten(
      LocalDateTime startOfPruefung, LocalDateTime endOfPruefung,
      List<Planungseinheit> planungseinheiten) {
    Set<Planungseinheit> conflictingPlanungseinheiten = new HashSet<>();
    LocalDateTime timeToCheck = startOfPruefung;

    while (timeToCheck.isBefore(endOfPruefung)) {
      // get all pruefungen at a point in time
      conflictingPlanungseinheiten.addAll(
          getPlanungseinheitenIfToManyAt(timeToCheck, planungseinheiten));
      timeToCheck = timeToCheck.plus(puffer);
    }

    boolean endedOnEndOfPlanungseinheit = timeToCheck.isEqual(endOfPruefung);
    if (!endedOnEndOfPlanungseinheit) {
      timeToCheck = endOfPruefung;
      conflictingPlanungseinheiten.addAll(
          getPlanungseinheitenIfToManyAt(timeToCheck, planungseinheiten));
    }
    return createAnalyse(conflictingPlanungseinheiten);
  }

  private Collection<Planungseinheit> getPlanungseinheitenIfToManyAt(LocalDateTime time,
      List<Planungseinheit> pruefungen) {
    Collection<Planungseinheit> tmp = selectPruefungenAt(time, pruefungen);
    if (tmp.size() > maxPruefungenAtATime) {
      return tmp;
    }
    return Collections.emptySet();
  }

  private Optional<WeichesKriteriumAnalyse> createAnalyse(Set<Planungseinheit> planungseinheiten) {
    if (planungseinheiten.size() > maxPruefungenAtATime) {
      // make result
      return Optional.of(
          new WeichesKriteriumAnalyse(getAllPruefungen(planungseinheiten), this.kriterium,
              getAllTeilnehmerkreiseFrom(planungseinheiten),
              getAmountAffectedStudents(planungseinheiten), calcScoring(planungseinheiten)));
    }
    return Optional.empty();
  }

  private Collection<Planungseinheit> selectPruefungenAt(@NotNull LocalDateTime time,
      @NotNull Iterable<Planungseinheit> planungseinheiten) {
    // gets all pruefungen at a specific point in time
    Collection<Planungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (!planungseinheit.getStartzeitpunkt().minus(puffer).isAfter(time)
          && planungseinheit.endzeitpunkt().isAfter(time)) {
        result.add(planungseinheit);
      }
    }
    return result;
  }

  private Set<Teilnehmerkreis> getAllTeilnehmerkreiseFrom(
      @NotNull Iterable<Planungseinheit> planungseinheiten) {
    Set<Teilnehmerkreis> teilnehmerkreise = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      teilnehmerkreise.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return teilnehmerkreise;
  }

  private int getAmountAffectedStudents(Iterable<Planungseinheit> planungseinheiten) {
    // TODO calculate affected students
    return 0;
  }

  private int calcScoring(Set<Planungseinheit> planungseinheiten) {
    // TODO calculate adequate scoring
    return 0;
  }

}
