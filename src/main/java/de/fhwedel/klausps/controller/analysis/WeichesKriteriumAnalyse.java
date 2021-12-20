package de.fhwedel.klausps.controller.analysis;


import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Set;

public class WeichesKriteriumAnalyse {


  Set<Pruefung> causingPruefungen;
  WeichesKriterium kriterium;
  Set<Teilnehmerkreis> affectedTeilnehmerKreise;
  Integer amountAffectedStudents;


  public WeichesKriteriumAnalyse(Set<Pruefung> causingPruefungen, WeichesKriterium kriterium,
      Set<Teilnehmerkreis> affectedTeilnehmerKreise, int affectedStudents) {
    this.causingPruefungen = causingPruefungen;
    this.kriterium = kriterium;
    this.affectedTeilnehmerKreise = affectedTeilnehmerKreise;
    this.amountAffectedStudents = affectedStudents;
  }

  public Set<Pruefung> getCausingPruefungen() {
    return causingPruefungen;
  }

  public WeichesKriterium getKriterium() {
    return kriterium;
  }

  public Set<Teilnehmerkreis> getAffectedTeilnehmerKreise() {
    return affectedTeilnehmerKreise;
  }

  public Integer getAmountAffectedStudents() {
    return amountAffectedStudents;
  }
}
