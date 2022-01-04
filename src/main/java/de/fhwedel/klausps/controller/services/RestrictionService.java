package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.restriction.hard.HartRestriktion;
import de.fhwedel.klausps.controller.restriction.hard.TwoKlausurenSameTime;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungProWoche;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungProWocheTeilnehmerkreis;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungenGleichzeitigRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlTeilnehmerGleichzeitigZuHochRestriction;
import de.fhwedel.klausps.controller.restriction.soft.FreierTagZwischenPruefungen;
import de.fhwedel.klausps.controller.restriction.soft.KeineKlausurAmSonntag;
import de.fhwedel.klausps.controller.restriction.soft.MehrePruefungenAmTag;
import de.fhwedel.klausps.controller.restriction.soft.UniformeZeitslots;
import de.fhwedel.klausps.controller.restriction.soft.WeicheRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.WocheVierFuerMaster;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RestrictionService {

  private final Set<HartRestriktion> hardRestrictions;

  private final Set<WeicheRestriktion> softRestrictions;


  RestrictionService() {
    softRestrictions = new HashSet<>();
    hardRestrictions = new HashSet<>();
    registerSoftCriteria();
    registerHardCriteria();

  }


  private void registerSoftCriteria() {
    softRestrictions.addAll(Set.of(
        //new AnzahlPruefungProWoche(),
        new AnzahlPruefungProWocheTeilnehmerkreis(),
        new AnzahlPruefungenGleichzeitigRestriktion(),
        new AnzahlTeilnehmerGleichzeitigZuHochRestriction(),
        new FreierTagZwischenPruefungen(),
        new KeineKlausurAmSonntag(),
        new UniformeZeitslots(),
        new MehrePruefungenAmTag(),
        new WocheVierFuerMaster()
        // todo register KlausurenMitVielenAmAnfang
    ));
  }


  private void registerHardCriteria() {
    hardRestrictions.add(new TwoKlausurenSameTime());
  }


  /**
   * Checks all restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return WeichesKriteriumAnalysen
   */
  public List<WeichesKriteriumAnalyse> checkWeicheKriterien(
      Pruefung pruefung) {
    List<WeichesKriteriumAnalyse> result = new LinkedList<>();
    softRestrictions.forEach(soft -> soft.evaluate(pruefung).ifPresent(result::add));
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
   * Checks all hard restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return HartesKriteriumAnalysen
   */
  public List<HartesKriteriumAnalyse> checkHarteKriterien(
      Pruefung pruefung) {
    List<HartesKriteriumAnalyse> result = new LinkedList<>();
    hardRestrictions.forEach(hard -> hard.evaluate(pruefung).ifPresent(result::add));
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

  public int getScoringOfPruefung(Pruefung pruefung) {
    if (!pruefung.isGeplant()) {
      return 0;
    }
    return checkWeicheKriterien(pruefung).stream()
        .reduce(0, (scoring, analyse) -> scoring + analyse.getDeltaScoring(), Integer::sum);
  }
}
