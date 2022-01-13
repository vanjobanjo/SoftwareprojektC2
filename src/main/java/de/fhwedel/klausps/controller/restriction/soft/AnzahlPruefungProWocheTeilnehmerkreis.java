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
  public static int LIMIT_DEFAULT = 5;
  private static final int DAYS_WEEK_DEFAULT = 7;
  private final int limit;

  private LocalDate startPeriode;
  // the set contains all pruefungen of the week, also the sibblings in the block
  private Map<Integer, Set<Pruefung>> weekPruefungMap;

  public AnzahlPruefungProWocheTeilnehmerkreis() {
    this(ServiceProvider.getDataAccessService(), LIMIT_DEFAULT);
  }

  //Mock Konstruktor
  AnzahlPruefungProWocheTeilnehmerkreis(
      DataAccessService dataAccessService,
      final int LIMIT_TEST) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    try {
      // TODO das kann nicht im Konstruktor gesetzt werden weil die Klasse instanziiert wird bevor
      //  es eine Pruefungsperiode geben kann.
      //  Generell ist von Instanzvariablen in Restriktionen abzusehen.
      startPeriode = dataAccessService.getStartOfPeriode();
      weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(),
          startPeriode);
    } catch (NoPruefungsPeriodeDefinedException e) {
      e.printStackTrace();
    }
    limit = LIMIT_TEST;
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

  private Optional<WeichesKriteriumAnalyse> evaluateForTkConcat(Pruefung pruefung,
      Teilnehmerkreis tk,
      Optional<WeichesKriteriumAnalyse> analyse) {
    assert pruefung.getTeilnehmerkreise().contains(tk);

    int week = getWeek(startPeriode, pruefung);
    Set<Pruefung> pruefungenSameWeek = weekPruefungMap.get(week);
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

  private Optional<WeichesKriteriumAnalyse> concatAnalyse(
      WeichesKriteriumAnalyse newAnalyse, Optional<WeichesKriteriumAnalyse> oldAnalyse) {
    assert newAnalyse != null;
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

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(), startPeriode);
    Optional<WeichesKriteriumAnalyse> analyseForTks = Optional.empty();
    for (Teilnehmerkreis tk : pruefung.getTeilnehmerkreise()) {
      analyseForTks = evaluateForTkConcat(pruefung, tk, analyseForTks);
    }
    return analyseForTks;
  }

  boolean isAboveLimit(Pruefung pruefung, Teilnehmerkreis tk) {
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
