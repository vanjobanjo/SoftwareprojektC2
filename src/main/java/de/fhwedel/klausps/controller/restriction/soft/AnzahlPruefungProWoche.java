package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnzahlPruefungProWoche extends WeicheRestriktion implements Predicate<Pruefung> {

  // for testing
  public static int LIMIT_DEFAULT = 5;
  private static final int  DAYS_WEEK_DEFAULT = 7;
  private final int limit;

  private final LocalDate startPeriode;
  private final Map<Integer, Set<Pruefung>> weekPruefungMap;

  //Mock Konstruktor
  AnzahlPruefungProWoche(
      DataAccessService dataAccessService,
      final int LIMIT_TEST) {
    super(dataAccessService, WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    startPeriode = dataAccessService.getStartOfPeriode();
    weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(), startPeriode);
    limit = LIMIT_TEST;
  }

  public AnzahlPruefungProWoche() {
    super(ServiceProvider.getDataAccessService(), WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
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
  @Override
  public boolean test(Pruefung pruefung) {
    int week = getWeek(startPeriode, pruefung);
    Set<Pruefung> pruefungen = weekPruefungMap.get(week);
    Optional<Block> blockOpt = dataAccessService.getBlockTo(pruefung);

    return blockOpt.isEmpty() ? pruefungen.size() >= limit
        : (pruefungen.size() - blockOpt.get().getPruefungen().size()) + 1 >= limit;
  }

  @Override
  public KriteriumsAnalyse evaluate(Pruefung toEvaluate) {
    if (!test(toEvaluate)) {
      return null;
    }

    Optional<Block> blockOpt = dataAccessService.getBlockTo(toEvaluate);
    Set<Pruefung> conflictedPruefungen = weekPruefungMap.get(getWeek(startPeriode, toEvaluate));

    if (blockOpt.isPresent()) {
      Set<Planungseinheit> conflictedPlanungseinheiten = getPlanungseinheitenToPruefungen(
          conflictedPruefungen);
      conflictedPlanungseinheiten.remove(blockOpt.get());
      conflictedPruefungen = getPruefungenFromPlanungseinheiten(
          conflictedPlanungseinheiten);
      conflictedPruefungen.add(toEvaluate);
    }

    Set<ReadOnlyPruefung> conflictedRoPruefungen = conflictedPruefungen
        .stream()
        .map(x -> new PruefungDTOBuilder(x).build()).collect(
            Collectors.toSet());

    Set<Teilnehmerkreis> conflictedTeilnehmerkreis = conflictedPruefungen.stream()
        .flatMap(pruefung -> pruefung.getTeilnehmerkreise().stream()).collect(
            Collectors.toSet());

    int affected =  conflictedPruefungen.stream().mapToInt(Planungseinheit::schaetzung).sum();

    return new KriteriumsAnalyse(conflictedRoPruefungen,
        WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE, conflictedTeilnehmerkreis, affected);
  }


  private Set<Pruefung> getPruefungenFromPlanungseinheiten(
      Set<Planungseinheit> planungseinheiten) {
    Stream<Pruefung> fromBlock = planungseinheiten.stream().filter(Planungseinheit::isBlock)
        .flatMap(block -> block.asBlock().getPruefungen().stream());

    try (Stream<Pruefung> withoutBlock = planungseinheiten.stream()
        .filter(planungseinheit -> !planungseinheit.isBlock())
        .map(Planungseinheit::asPruefung)) {

      return Stream.concat(fromBlock, withoutBlock).collect(Collectors.toSet());
    }
  }

  private Set<Planungseinheit> getPlanungseinheitenToPruefungen(Set<Pruefung> pruefungen) {
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
