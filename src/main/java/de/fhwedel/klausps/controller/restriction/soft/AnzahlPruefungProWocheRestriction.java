package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This restriction checks if a Teilnehmerkreis don't have so many Pruefungen in a week
 */
public class AnzahlPruefungProWocheRestriction extends SoftRestriction {

  // for testing
  public static final int LIMIT_DEFAULT = 5;
  private static final int DAYS_WEEK_DEFAULT = 7;
  private final int limit;

  //Mock Konstruktor
  AnzahlPruefungProWocheRestriction(
      DataAccessService dataAccessService,
      final int LIMIT_TEST) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    limit = LIMIT_TEST;
  }

  /**
   * Public constructor
   */
  public AnzahlPruefungProWocheRestriction() {
    super(ServiceProvider.getDataAccessService(), ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    limit = LIMIT_DEFAULT;
  }

  /**
   * Creates a Map, Pruefungen grouped by scheduled week. Week 0 is the first week of the
   * startPeriode.
   *
   * @param scheduledPruefungen schedule Pruefungen
   * @param startPeriode        start of the periode
   * @return Map with Pruefungen grouped by week of Pruefung.
   */
  Map<Integer, Set<Pruefung>> weekMapOfPruefung(Set<Pruefung> scheduledPruefungen,
      LocalDate startPeriode) {
    return scheduledPruefungen.stream().collect(
        Collectors.groupingBy(pruefung -> getWeek(startPeriode, pruefung), Collectors.toSet()));
  }

  /**
   * @return scheduled week of the passed pruefung.
   */
  private int getWeek(LocalDate startPeriode, Pruefung pruefung) {
    return (pruefung.getStartzeitpunkt().getDayOfYear() - startPeriode.getDayOfYear())
        / DAYS_WEEK_DEFAULT;
  }

  /**
   * @return the amount of the passed Teilnehmerkreis, in the given set of Pruefung.
   */
  private int countOfTeilnehmerkreis(Teilnehmerkreis tk, Set<Pruefung> pruefungen) {
    return (int) pruefungen.stream().filter(pruefung -> pruefung.getTeilnehmerkreise().contains(tk))
        .count();
  }

  @Override
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {

    Map<Integer, Set<Pruefung>> weekPruefungMap;
    LocalDate start;

    start = dataAccessService.getStartOfPeriode();
    weekPruefungMap = weekMapOfPruefung(dataAccessService.getPlannedPruefungen(), start);

    Optional<SoftRestrictionAnalysis> analyseForTks = Optional.empty();
    // every tk of the pruefung must be checked with all other pruefung of the same week
    // and every violation must be counted
    for (Teilnehmerkreis tk : pruefung.getTeilnehmerkreise()) {
      int week = getWeek(start, pruefung);
      Set<Pruefung> pruefungenScheduleSameWeek = weekPruefungMap.get(week);
      analyseForTks = evaluateForTkConcat(pruefung, tk, analyseForTks,
          pruefungenScheduleSameWeek);
    }
    return analyseForTks;
  }

  /**
   * Will concat two analyse
   *
   * @param newAnalyse is always present
   * @param oldAnalyse could be empty
   * @return WeichesKriteriumsAnalyse
   */
  private Optional<SoftRestrictionAnalysis> concatAnalyse(
      SoftRestrictionAnalysis newAnalyse, Optional<SoftRestrictionAnalysis> oldAnalyse) {
    assert newAnalyse != null;

    if (oldAnalyse.isEmpty()) {
      return Optional.of(newAnalyse);
    }
    Set<Pruefung> combinedPruefung = new HashSet<>();
    combinedPruefung.addAll(newAnalyse.getAffectedPruefungen());
    combinedPruefung.addAll(oldAnalyse.get().getAffectedPruefungen());

    Set<Teilnehmerkreis> combinedTk = new HashSet<>();
    combinedTk.addAll(newAnalyse.getAffectedTeilnehmerKreise());
    combinedTk.addAll(oldAnalyse.get().getAffectedTeilnehmerKreise());
    int sum = newAnalyse.getAmountAffectedStudents() + oldAnalyse.get().getAmountAffectedStudents();
    int deltaScoring = oldAnalyse.get().getDeltaScoring() + newAnalyse.getDeltaScoring();
    return Optional.of(
        new SoftRestrictionAnalysis(new HashSet<>(combinedPruefung), ANZAHL_PRUEFUNGEN_PRO_WOCHE,
            new HashSet<>(combinedTk), sum, deltaScoring));

  }

  /**
   * Evaluation for the Anzahl Prufung Pro Woche Teilnehmerkreis. Will concat the analysen when
   * there was another violation.
   *
   * @param pruefung           Pruefung to check the violation
   * @param tk                 a Teilnehmerkreis of the passed Pruefung to check
   * @param analyse            an other violation of the same pruefung
   * @param pruefungenSameWeek Pruefungen which are scheduled in the same week
   * @return a optional WeichesKriteriumsAnalyse when there is a violation.
   */
  private Optional<SoftRestrictionAnalysis> evaluateForTkConcat(Pruefung pruefung,
      Teilnehmerkreis tk,
      Optional<SoftRestrictionAnalysis> analyse,
      Set<Pruefung> pruefungenSameWeek) throws NoPruefungsPeriodeDefinedException {
    assert pruefung.getTeilnehmerkreise().contains(tk);

    Optional<Block> blockOpt = dataAccessService.getBlockTo(pruefung);
    // filter the pruefungen that are in the same block of the parameter
    if (blockOpt.isPresent()) {
      pruefungenSameWeek = filterSiblingsOfPruefung(pruefung, pruefungenSameWeek);
    }

    if (countOfTeilnehmerkreis(tk, pruefungenSameWeek) < limit) {
      return analyse;
    }

    // put the violated pruefungen in a set
    Set<Pruefung> pruefungenSameTk = pruefungenSameWeek.stream()
        .filter(pr -> pr.getTeilnehmerkreise().contains(tk)).collect(
            Collectors.toSet());

    return concatAnalyse((new SoftRestrictionAnalysis(pruefungenSameTk,
            ANZAHL_PRUEFUNGEN_PRO_WOCHE, Set.of(tk), pruefung.getSchaetzungen().get(tk),
            ANZAHL_PRUEFUNGEN_PRO_WOCHE.getWert())),
        analyse);
  }

  /**
   * Filters the block siblings of the passed pruefung. When the passed pruefung doesn't belong to
   * any block, the same set will be returned.
   *
   * @param pruefung   Pruefung to filter its siblings.
   * @param pruefungen Set with Pruefung to filter.
   * @return filtered Set without the sibling of pruefung, but it contains still the passed
   * pruefung.
   * @pre pruefungen must contains pruefung
   */
  private Set<Pruefung> filterSiblingsOfPruefung(Pruefung pruefung,
      Set<Pruefung> pruefungen) throws NoPruefungsPeriodeDefinedException {
    assert pruefungen.contains(pruefung);
    Optional<Block> blockOpt = dataAccessService.getBlockTo(pruefung);

    if (blockOpt.isEmpty()) {
      return pruefungen;
    }

    Set<Planungseinheit> accPlanungseinheiten = getAccumulatedPlanungseinheiten(
        pruefungen);
    accPlanungseinheiten.remove(blockOpt.get());
    Set<Pruefung> filtered = getExtractedPruefungen(
        accPlanungseinheiten);
    filtered.add(pruefung);
    return filtered;
  }

  /**
   * If there is block, it will give us the siblings exam inside the block and not the block
   * itself.
   *
   * @param planungseinheiten blocks or exams
   * @return All Pruefungen
   */
  private Set<Pruefung> getExtractedPruefungen(
      Set<Planungseinheit> planungseinheiten) {
    Stream<Pruefung> fromBlock = planungseinheiten.stream().filter(Planungseinheit::isBlock)
        .flatMap(block -> block.asBlock().getPruefungen().stream());

    Stream<Pruefung> withoutBlock = planungseinheiten.stream()
        .filter(planungseinheit -> !planungseinheit.isBlock())
        .map(Planungseinheit::asPruefung);

    return Stream.concat(fromBlock, withoutBlock).collect(Collectors.toSet());
  }

  /**
   * Accumulates the exams, the set only contains distinct Planungseinheiten. When a pruefung is in
   * a block the returned Set contains the block of the pruefung and not the exam itself.
   *
   * @param pruefungen Pruefungen.
   * @return Accumulated Planungseinheiten.
   */
  private Set<Planungseinheit> getAccumulatedPlanungseinheiten(Set<Pruefung> pruefungen)
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> planungseinheiten = new HashSet<>();
    for (Pruefung p : pruefungen) {
      Optional<Block> blockOpt = dataAccessService.getBlockTo(p);
      if (blockOpt.isPresent()) {
        planungseinheiten.add(blockOpt.get());
      } else {
        planungseinheiten.add(p);
      }
    }
    return planungseinheiten;
  }

}
