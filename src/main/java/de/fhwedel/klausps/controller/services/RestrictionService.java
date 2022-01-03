package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RestrictionService {

  /**
   * Checks all restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return WeichesKriteriumAnalysen
   */
  public List<WeichesKriteriumAnalyse> checkWeicheKriterien(
      Pruefung pruefung) {
    throw new IllegalStateException("Not implemented yet!");
  }

  public Set<Pruefung> getAffectedPruefungen(Pruefung pruefung) {
    Set<Pruefung> result = new HashSet<>();
    for (WeichesKriteriumAnalyse w : checkWeicheKriterien(pruefung)) {
      result.addAll(w.getCausingPruefungen());
    }
    return result;
  }

  /**
   * Checks all hard restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return HartesKriteriumAnalysen
   */
  public List<HartesKriteriumAnalyse> checkHarteKriterien(
      Pruefung pruefung) {
    throw new IllegalStateException("Not implemented yet!");
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

  public int getScoringOfPruefung(Pruefung pruefung) {
    if (!pruefung.isGeplant()) {
      return 0;
    }
    return checkWeicheKriterien(pruefung).stream()
        .reduce(0, (scoring, analyse) -> scoring + analyse.getDeltaScoring(), Integer::sum);
  }
}
