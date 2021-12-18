package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    Set<Pruefung> pruefungen  = weekPruefungMap.get(week);
    return getPlanungseinheitenToPruefungen(pruefungen).size() >= limit;
  }

  @Override
  public Optional<KriteriumsAnalyse> evaluate(Pruefung pruefung) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public KriteriumsAnalyse evaluate(Pruefung toEvaluate) {
    if (test(toEvaluate)) {
      Set<ReadOnlyPruefung> betroffen = weekPruefungMap.get(getWeek(startPeriode, toEvaluate)).stream()
          .map(x -> new PruefungDTOBuilder(x).build()).collect(
              Collectors.toSet());
      betroffen.add(new PruefungDTOBuilder(toEvaluate).build());
      return new KriteriumsAnalyse(betroffen,
          WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE, new HashSet<>(toEvaluate.getTeilnehmerkreise()),
          toEvaluate.schaetzung());
    } else {
      return null;
    }
  }
}
