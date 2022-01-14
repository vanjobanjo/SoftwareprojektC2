package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
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

public class AnzahlPruefungProWocheTeilnehmerkreis extends WeicheRestriktion {

  // for testing
  public static final int LIMIT_DEFAULT = 5;
  private static final int DAYS_WEEK_DEFAULT = 7;
  private final int limit;

  //Mock Konstruktor
  AnzahlPruefungProWocheTeilnehmerkreis(
      DataAccessService dataAccessService,
      final int LIMIT_TEST) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    limit = LIMIT_TEST;
  }

  /**
   * Public constructor
   */
  public AnzahlPruefungProWocheTeilnehmerkreis() {
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
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {

    Map<Integer, Set<Pruefung>> weekPruefungMap;
    LocalDate start;

    try {
      start = dataAccessService.getStartOfPeriode();
      weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(), start);
    } catch (NoPruefungsPeriodeDefinedException e) {
      return Optional.empty();
    }

    Optional<WeichesKriteriumAnalyse> analyseForTks = Optional.empty();
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
  private Optional<WeichesKriteriumAnalyse> concatAnalyse(
      WeichesKriteriumAnalyse newAnalyse, Optional<WeichesKriteriumAnalyse> oldAnalyse) {
    assert newAnalyse != null;
    // todo zweiter Parameter muss gar nicht erst übergeben werden, wenn vor Aufruf der
    //  Methode schon auf isEmpty() überprüft wird
    if (oldAnalyse.isEmpty()) {
      return Optional.of(newAnalyse);
    }
    Set<Pruefung> combinedPruefung = new HashSet<>();
    combinedPruefung.addAll(newAnalyse.getCausingPruefungen());
    combinedPruefung.addAll(oldAnalyse.get().getCausingPruefungen());

    Set<Teilnehmerkreis> combinedTk = new HashSet<>();
    combinedTk.addAll(newAnalyse.getAffectedTeilnehmerKreise());
    combinedTk.addAll(oldAnalyse.get().getAffectedTeilnehmerKreise());
    int sum = newAnalyse.getAmountAffectedStudents() + oldAnalyse.get().getAmountAffectedStudents();
    int deltaScoring = oldAnalyse.get().getDeltaScoring() + newAnalyse.getDeltaScoring();
    return Optional.of(
        new WeichesKriteriumAnalyse(new HashSet<>(combinedPruefung), ANZAHL_PRUEFUNGEN_PRO_WOCHE,
            new HashSet<>(combinedTk), sum, deltaScoring));

  }

  /**
   * Evaluation for the Anzahl Prufung Pro Woche Teilnehmerkreis. Will concate the analysen when
   * there was another violation.
   *
   * @param pruefung           Pruefung to check the violation
   * @param tk                 a Teilnehmerkreis of the passed Pruefung to check
   * @param analyse            an other violation of the same pruefung
   * @param pruefungenSameWeek Pruefungen which are scheduled in the same week
   * @return a optional WeichesKriteriumsAnalyse when there is a violation.
   */
  private Optional<WeichesKriteriumAnalyse> evaluateForTkConcat(Pruefung pruefung,
      Teilnehmerkreis tk,
      Optional<WeichesKriteriumAnalyse> analyse,
      Set<Pruefung> pruefungenSameWeek) throws NoPruefungsPeriodeDefinedException {
    assert pruefung.getTeilnehmerkreise().contains(tk);

    Optional<Block> blockOpt = dataAccessService.getBlockTo(pruefung);
    if (blockOpt.isPresent()) {
      pruefungenSameWeek = filterSiblingsOfPruefung(pruefung, pruefungenSameWeek);
    }

    if (countOfTeilnehmerkreis(tk, pruefungenSameWeek) < limit) {
      return analyse;
    }

    Set<Pruefung> pruefungenSameTk = pruefungenSameWeek.stream()
        .filter(pr -> pr.getTeilnehmerkreise().contains(tk)).collect(
            Collectors.toSet());

    return concatAnalyse((new WeichesKriteriumAnalyse(pruefungenSameTk,
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
   * @return Accumlated Planungseinheiten.
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
