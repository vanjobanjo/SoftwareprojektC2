package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Map;
import java.util.Set;

/**
 * This class is for the temp Save of an HardRestrictrion.
 * That we can test first all HardRestrictions and then collect the data from all HardRestrictionAnalysis
 * and then throw one HardRestrictionException with the data.
 * TODO nochmal queer lesen, ob das hier so stehen kann
 */
public class HardRestrictionAnalysis {

  Set<Pruefung> causingPruefungen;


  HartesKriterium kriterium;

  Map<Teilnehmerkreis, Integer>  teilnehmerCount ;
  public HardRestrictionAnalysis(
      Set<Pruefung> causingPruefungen,
      HartesKriterium kriterium, Map<Teilnehmerkreis, Integer> teilnehmerCount) {
    this.causingPruefungen = causingPruefungen;
    this.kriterium = kriterium;
    this.teilnehmerCount = teilnehmerCount;
  }

  public Set<Pruefung> getCausingPruefungen() {
    return this.causingPruefungen;
  }

  public HartesKriterium getKriterium() {
    return this.kriterium;
  }
  public Map<Teilnehmerkreis, Integer> getTeilnehmerCount(){
    return teilnehmerCount;
  }
}
