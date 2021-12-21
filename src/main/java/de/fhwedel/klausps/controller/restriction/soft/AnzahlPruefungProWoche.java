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
    super(dataAccessService, ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    startPeriode = dataAccessService.getStartOfPeriode();
    weekPruefungMap = weekMapOfPruefung(dataAccessService.getGeplanteModelPruefung(), startPeriode);
    limit = LIMIT_TEST;
  }

  public AnzahlPruefungProWoche() {
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
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    boolean violationRestriction = test(pruefung);

    if (!violationRestriction) {
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

    Set<Teilnehmerkreis> conflictedTeilnehmerkreis = conflictedPruefungen.stream()
        .flatMap(prue -> prue.getTeilnehmerkreise().stream()).collect(
            Collectors.toSet());

    int affected = numberAffectedStudents(conflictedPruefungen);

    return Optional.of(new WeichesKriteriumAnalyse(conflictedPruefungen,
        ANZAHL_PRUEFUNGEN_PRO_WOCHE, conflictedTeilnehmerkreis, affected));
  }

  /**
   * Calculates the number of students of the passed Set of pruefungen. It considera als the
   * duplicates Teilnehmerkreise.
   *
   * @param pruefungen Set of Pruefung.
   * @return number of affected students.
   */
  private int numberAffectedStudents(Set<Pruefung> pruefungen) {
    return pruefungen.stream()
        .flatMap(pruefung -> pruefung.getSchaetzungen().entrySet().stream())
        .filter(distinctByKey(Entry::getKey)).mapToInt(Entry::getValue).sum();
  }

  /**
   * Can't use normal distinct, when there are different Teilnhmerkreis with different Schätzung.
   * https://stackoverflow.com/questions/23699371/java-8-distinct-by-property
   *
   * @param keyExtractor filter
   * @param <T>          Type.
   * @return Predicate.
   */
  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
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
