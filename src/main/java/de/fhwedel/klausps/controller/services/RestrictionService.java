package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.restriction.hard.HarteRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.WeicheRestriktion;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class RestrictionService {

  private final Set<HarteRestriktion> hardRestrictions;

  private final Set<WeicheRestriktion> softRestrictions;

  RestrictionService() {
    softRestrictions = new HashSet<>();
    hardRestrictions = new HashSet<>();
    RestrictionFactory factory = new RestrictionFactory();
    factory.createRestrictions(this);
  }

  void registerSoftCriteria(Set<WeicheRestriktion> restrictions) {
    softRestrictions.addAll(restrictions);
  }

  void registerHardCriteria(Set<HarteRestriktion> restrictions) {
    hardRestrictions.addAll(restrictions);
  }

  public Set<Pruefung> getAffectedPruefungen(Block block) {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      result.addAll(getAffectedPruefungen(pruefung));
    }
    return result;
  }

  public Set<Pruefung> getAffectedPruefungen(Pruefung pruefung) {
    Set<Pruefung> result = new HashSet<>();
    for (WeichesKriteriumAnalyse w : checkWeicheKriterien(pruefung)) {
      result.addAll(w.getCausingPruefungen());
    }
    return result;
  }

  /**
   * Checks all restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return WeichesKriteriumAnalysen
   */
  public List<WeichesKriteriumAnalyse> checkWeicheKriterien(Pruefung pruefung) {
    List<WeichesKriteriumAnalyse> result = new LinkedList<>();
    softRestrictions.forEach(soft -> soft.evaluate(pruefung).ifPresent(result::add));
    return result;
  }

  /**
   * @param pruefung
   * @return
   */
  public Map<Pruefung, List<WeichesKriteriumAnalyse>> checkWeicheKriterienAll(
      Set<Pruefung> pruefung) {
    return pruefung.stream().collect(Collectors.groupingBy(prue -> prue,
        Collectors.flatMapping(prue -> checkWeicheKriterien(prue).stream(), Collectors.toList())));
  }

  /**
   * @param pruefung
   * @return
   */
  public Map<Pruefung, List<HartesKriteriumAnalyse>> checkHarteKriterienAll(
      Set<Pruefung> pruefung) {
    return pruefung.stream().collect(Collectors.groupingBy(prue -> prue,
        Collectors.flatMapping(prue -> checkHarteKriterien(prue).stream(), Collectors.toList())));
  }

  /**
   * Checks all hard restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return HartesKriteriumAnalysen
   */
  public List<HartesKriteriumAnalyse> checkHarteKriterien(Pruefung pruefung) {
    List<HartesKriteriumAnalyse> result = new LinkedList<>();
    hardRestrictions.forEach(hard -> hard.evaluate(pruefung).ifPresent(result::add));
    return result;
  }

  public int getScoringOfPruefung(Pruefung pruefung) {
    if (!pruefung.isGeplant()) {
      return 0;
    }
    return checkWeicheKriterien(pruefung).stream()
        .reduce(0, (scoring, analyse) -> scoring + analyse.getDeltaScoring(), Integer::sum);
  }

  @NotNull
  public Set<ReadOnlyPruefung> getPruefungenInHardConflictWith(
      ReadOnlyPlanungseinheit planungseinheitToCheckFor) {
    return null;
  }
}
