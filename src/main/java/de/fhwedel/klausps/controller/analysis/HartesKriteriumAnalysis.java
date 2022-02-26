package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Map;
import java.util.Set;

public class HartesKriteriumAnalysis {

  Set<Pruefung> causingPruefungen;


  HartesKriterium kriterium;

  Map<Teilnehmerkreis, Integer>  teilnehmerCount ;
  public HartesKriteriumAnalysis(
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
