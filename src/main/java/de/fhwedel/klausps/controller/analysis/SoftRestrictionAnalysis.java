package de.fhwedel.klausps.controller.analysis;


import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Set;

/**
 * This class give the opportunity for collecting from all SoftRestrictions the Analysis
 * And then transform  a SoftRestrictionAnalysis into a KriteriumsAnalyse for the view.
 *
 * The SoftRestrictionAnalysis an internally used container for ana
 * //TODO hier nochmal bitte lesen
 */
public class SoftRestrictionAnalysis {


  Set<Pruefung> causingPruefungen;
  WeichesKriterium kriterium;
  Set<Teilnehmerkreis> affectedTeilnehmerKreise;
  Integer amountAffectedStudents;
  //Verursachte Scoring Änderung durch dieses verletzte Kriterium.
  //Um das Scoring der Klausur zu bestimmen, muss die Summe der deltaScoring aus der Liste ergeben
  //das Scoring der Pruefung

  Integer deltaScoring;

  public SoftRestrictionAnalysis(Set<Pruefung> causingPruefungen, WeichesKriterium kriterium,
      Set<Teilnehmerkreis> affectedTeilnehmerKreise, int affectedStudents, int scoring) {
    this.causingPruefungen = causingPruefungen;
    this.kriterium = kriterium;
    this.affectedTeilnehmerKreise = affectedTeilnehmerKreise;
    this.amountAffectedStudents = affectedStudents;
    this.deltaScoring = scoring;
  }

  public Set<Pruefung> getAffectedPruefungen() {
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

  public Integer getDeltaScoring() {
    return deltaScoring;
  }
}
