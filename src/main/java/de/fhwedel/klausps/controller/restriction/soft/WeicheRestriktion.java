package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class WeicheRestriktion {

  protected final DataAccessService dataAccessService;
  protected final WeichesKriterium kriterium;

  protected WeicheRestriktion(
      DataAccessService dataAccessService,
      WeichesKriterium kriterium) {
    this.dataAccessService = dataAccessService;
    this.kriterium = kriterium;
  }

  /**
   * Evaluates for a {@link Pruefung} in which way it violates a restriction.
   *
   * @param pruefung The pruefung for which to check for violations of a restriction.
   * @return Either an {@link Optional} containing a {@link KriteriumsAnalyse} for the violated
   * restriction, or an empty Optional in case the Restriction was not violated.
   */
  public abstract Optional<KriteriumsAnalyse> evaluate(Pruefung pruefung);

  /**
   * Calculates the number of students he passed Set of pruefung. It filters the duplicates
   * Teilnehmerkreise.
   *
   * @param pruefungen Set of Pruefung.
   * @return number of affected students.
   */
  protected int numberAffectedStudents(Set<Pruefung> pruefungen) {
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
   * @return filtered Set without the sibbling of pruefung, but it contains still the passed
   * pruefung.
   * @assert pruefungen must contains pruefung
   */
  protected Set<Pruefung> filterSiblingsOfPruefung(Pruefung pruefung,
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
