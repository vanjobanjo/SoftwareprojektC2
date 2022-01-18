package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Map;
import java.util.Set;

public class HartesKriteriumAnalyse {

  Set<Pruefung> causingPruefungen;


  HartesKriterium kriterium;

  Map<Teilnehmerkreis, Integer>  teilnehmercount ;
  public HartesKriteriumAnalyse(
      Set<Pruefung> causingPruefungen,
      HartesKriterium kriterium, Map<Teilnehmerkreis, Integer> teilnehmercount) {
    this.causingPruefungen = causingPruefungen;
    this.kriterium = kriterium;
    this.teilnehmercount = teilnehmercount;
  }

  public Set<Pruefung> getCausingPruefungen() {
    return this.causingPruefungen;
  }

  public HartesKriterium getKriterium() {
    return this.kriterium;
  }
  public Map<Teilnehmerkreis, Integer> getTeilnehmercount(){
    return teilnehmercount;
  }
}
