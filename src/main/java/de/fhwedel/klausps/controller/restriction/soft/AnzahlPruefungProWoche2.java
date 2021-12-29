package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnzahlPruefungProWoche2 extends WeicheRestriktion {

  // for testing
  public static int LIMIT_DEFAULT = 5;
  private static final int DAYS_WEEK_DEFAULT = 7;
  private final int limit;

  private final LocalDate startPeriode;
  // the set contains all pruefungen of the week, also the sibblings in the block
  private final Map<Integer, Set<Pruefung>> weekPruefungMap;

  //Mock Konstruktor
  AnzahlPruefungProWoche2(
      DataAccessService dataAccessService,
      final int LIMIT_TEST) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    startPeriode = dataAccessService.getStartOfPeriode();
    weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(), startPeriode);
    limit = LIMIT_TEST;
  }

  public AnzahlPruefungProWoche2() {
    super(ServiceProvider.getDataAccessService(), ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    startPeriode = dataAccessService.getStartOfPeriode();
    weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(), startPeriode);
    limit = LIMIT_DEFAULT;
  }

  Map<Integer, Set<Pruefung>> weekMapOfPruefung(Set<Pruefung> geplantePruefung,
      LocalDate startPeriode) {
    return geplantePruefung.stream().collect(
        Collectors.groupingBy(pruefung -> getWeek(startPeriode, pruefung), Collectors.toSet()));
  }

  private int getWeek(LocalDate startPeriode, Pruefung pruefung) {
    return (pruefung.getStartzeitpunkt().getDayOfYear() - startPeriode.getDayOfYear())
        / DAYS_WEEK_DEFAULT;
  }

  private int countOfTeilnehmerkreis(Teilnehmerkreis tk, Set<Pruefung> pruefungen) {
    return (int) pruefungen.stream().filter(pruefung -> pruefung.getTeilnehmerkreise().contains(tk))
        .count();
  }

  private Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung, Teilnehmerkreis tk,
      WeichesKriteriumAnalyse analyse) {
    assert pruefung.getTeilnehmerkreise().contains(tk);

    if (!isAboveLimit(pruefung, tk)) {
      return Optional.of(analyse);
    }

    return concatAnalyse((new WeichesKriteriumAnalyse(Set.of(pruefung),
            ANZAHL_PRUEFUNGEN_PRO_WOCHE, Set.of(tk), pruefung.getSchaetzungen().get(tk))),
        analyse);
  }

  private Optional<WeichesKriteriumAnalyse> concatAnalyse(
      WeichesKriteriumAnalyse newAnalyse, WeichesKriteriumAnalyse oldAnalyse) {
    assert newAnalyse != null;
    if (oldAnalyse == null) {
      return Optional.of(newAnalyse);
    }
    Set<Pruefung> combinedPruefung = new HashSet<>();
    combinedPruefung.addAll(newAnalyse.getCausingPruefungen());
    combinedPruefung.addAll(oldAnalyse.getCausingPruefungen());

    Set<Teilnehmerkreis> combinedTk = new HashSet<>();
    combinedTk.addAll(newAnalyse.getAffectedTeilnehmerKreise());
    combinedTk.addAll(oldAnalyse.getAffectedTeilnehmerKreise());
    int sum = newAnalyse.getAmountAffectedStudents() + oldAnalyse.getAmountAffectedStudents();
    return Optional.of(
        new WeichesKriteriumAnalyse(new HashSet<>(combinedPruefung), ANZAHL_PRUEFUNGEN_PRO_WOCHE,
            new HashSet<>(combinedTk), sum));

  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    Optional<WeichesKriteriumAnalyse> temp = Optional.empty();
    for (Teilnehmerkreis tk : pruefung.getTeilnehmerkreise()) {
      temp = evaluate(pruefung, tk, temp.get());
    }
    return temp;
  }

  private boolean isAboveLimit(Pruefung pruefung, Teilnehmerkreis tk) {
    int week = getWeek(startPeriode, pruefung);
    Set<Pruefung> pruefungen = weekPruefungMap.get(week);
    Optional<Block> blockOpt = dataAccessService.getBlockTo(pruefung);
    if (blockOpt.isPresent()) {
      pruefungen = filterSiblingsOfPruefung(pruefung, pruefungen);
    }
    return countOfTeilnehmerkreis(tk, pruefungen) >= limit;
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
      Set<Pruefung> pruefungen) {
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
  private Set<Planungseinheit> getAccumulatedPlanungseinheiten(Set<Pruefung> pruefungen) {
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
