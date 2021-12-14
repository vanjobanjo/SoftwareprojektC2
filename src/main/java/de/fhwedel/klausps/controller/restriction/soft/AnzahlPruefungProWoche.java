package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDate;
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
    if (!isInPeriod(pruefung)) {
      throw new IllegalArgumentException("Pruefung: " + pruefung + " liegt nicht in der Periode");
    }
    int week = getWeek(startPeriode, pruefung);

    Optional<Set<Pruefung>> pruefungen = Optional.ofNullable(weekPruefungMap.get(week));

    return pruefungen.isPresent() && pruefungen.get().size() >= limit; //TODO how to check?
  }

  private boolean isInPeriod(Pruefung pruefung) {
    return dataAccessService.terminIsInPeriod(pruefung.getStartzeitpunkt());
  }
}
