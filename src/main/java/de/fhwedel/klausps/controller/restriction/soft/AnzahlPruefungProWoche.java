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
  private static int DAYS_WEEK_DEFAULT = 7;
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
    Set<Planungseinheit> planungseinheiten = getPlanungseinheitenToPruefungen(pruefungen);
    return getPlanungseinheitenToPruefungen(pruefungen).size() >= limit; // Blöcke werden kumuliert.
  }

  @Override
  public KriteriumsAnalyse evaluate(Pruefung toEvaluate) {
    if (test(toEvaluate)) {
      Optional<Block> blockOpt = dataAccessService.getBlockTo(toEvaluate);
      Set<Pruefung> conflictedPruefungen = weekPruefungMap.get(getWeek(startPeriode, toEvaluate));
      if (blockOpt.isPresent()) {
        Set<Planungseinheit> conflictedPlanungseinheiten = getPlanungseinheitenToPruefungen(
            conflictedPruefungen);
        assert conflictedPlanungseinheiten.contains(blockOpt.get());
        conflictedPlanungseinheiten.remove(blockOpt.get());
        Set<ReadOnlyPruefung> betroffen = extractROFromPlanungseinheiten(
            conflictedPlanungseinheiten);
        return new KriteriumsAnalyse(betroffen,
            WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE,
            new HashSet<>(toEvaluate.getTeilnehmerkreise()),
            toEvaluate.schaetzung());
      }
      Set<Pruefung> conflicted = weekPruefungMap.get(getWeek(startPeriode, toEvaluate));
      assert conflicted.remove(toEvaluate);

      Set<ReadOnlyPruefung> betroffen = conflicted
          .stream()
          .map(x -> new PruefungDTOBuilder(x).build()).collect(
              Collectors.toSet());

      return new KriteriumsAnalyse(betroffen,
          WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE,
          new HashSet<>(toEvaluate.getTeilnehmerkreise()),
          toEvaluate.schaetzung());
    } else {
      return null;
    }
  }

  private Set<ReadOnlyPruefung> extractROFromPlanungseinheiten(
      Set<Planungseinheit> planungseinheiten) {
    Stream<ReadOnlyPruefung> fromBlock = planungseinheiten.stream().filter(Planungseinheit::isBlock)
        .flatMap(block -> block.asBlock().getPruefungen().stream()
            .map(pruefung -> new PruefungDTOBuilder(pruefung).build()));
    Stream<ReadOnlyPruefung> withoutBlock = planungseinheiten.stream()
        .filter(planungseinheit -> !planungseinheit.isBlock())
        .map(pruefung -> new PruefungDTOBuilder(pruefung.asPruefung()).build());

    return Stream.concat(fromBlock, withoutBlock).collect(Collectors.toSet());
  }
}
