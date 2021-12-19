package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnzahlPruefungProWoche extends WeicheRestriktion implements Predicate<Pruefung> {

  // for testing
  public static int LIMIT_DEFAULT = 5;
  private static final int DAYS_WEEK_DEFAULT = 7;
  private final int limit;

  private final LocalDate startPeriode;
  // the set contains all pruefungen of the week, also the sibblings in the block
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

  /**
   * Test whether the passed pruefung violates the crterium, by checking the Set size of the the
   * specific Map entry. The Map key is the week of the periode starting with 0.
   *
   * @param pruefung Pruefung to check the restriction for.
   * @return violates the restriction?
   */
  @Override
  public boolean test(Pruefung pruefung) {
    int week = getWeek(startPeriode, pruefung);
    Set<Pruefung> pruefungen = weekPruefungMap.get(week);
    Optional<Block> blockOpt = dataAccessService.getBlockTo(pruefung);

    return blockOpt.isEmpty() ? pruefungen.size() >= limit
        : (pruefungen.size() - blockOpt.get().getPruefungen().size()) + 1 >= limit;
    //ignore the exams in the block of pruefung.
  }

  @Override
  public Optional<KriteriumsAnalyse> evaluate(Pruefung pruefung) {
    if (!test(pruefung)) {
      return Optional.empty();
    }

    Optional<Block>
        blockOpt = dataAccessService.getBlockTo(pruefung);
    Set<Pruefung> conflictedPruefungen = weekPruefungMap.get(getWeek(startPeriode, pruefung));

    //when pruefung is in block, don't add the sibblings into the conflicted exams
    if (blockOpt.isPresent()) {
      conflictedPruefungen = filterSiblingsOfPruefung(pruefung,
          conflictedPruefungen);
    }

    Set<ReadOnlyPruefung> conflictedRoPruefungen = conflictedPruefungen
        .stream()
        .map(x -> new PruefungDTOBuilder(x).build()).collect(
            Collectors.toSet());

    Set<Teilnehmerkreis> conflictedTeilnehmerkreis = conflictedPruefungen.stream()
        .flatMap(p -> p.getTeilnehmerkreise().stream()).collect(
            Collectors.toSet());

    int affected = numberAffectedStudents(conflictedPruefungen);

    return Optional.of(new KriteriumsAnalyse(conflictedRoPruefungen,
        WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE, conflictedTeilnehmerkreis, affected));
  }

}
