package de.fhwedel.klausps.controller.analysis;


import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Set;

/**
 * Used as data transfer object representing an analysis of a {@link
 * de.fhwedel.klausps.controller.restriction.soft.SoftRestriction soft Restriction}.
 */
public class SoftRestrictionAnalysis {

  /**
   * The Pruefungen affected by the soft Restriction
   */
  Set<Pruefung> affectedPruefungen;
  /**
   * The criteria of the evaluated Restriction
   */
  WeichesKriterium criteria;
  /**
   * The teilnehmerkreise affected by the evaluated Restriction
   */
  Set<Teilnehmerkreis> affectedTeilnehmerKreise;

  /**
   * The amount of affected students by the evaluated Restriction
   */
  Integer amountAffectedStudents;

  /**
   * the scoring caused by the evaluated Restriction
   */
  Integer scoring;

  public SoftRestrictionAnalysis(Set<Pruefung> affectedPruefungen, WeichesKriterium criteria,
      Set<Teilnehmerkreis> affectedTeilnehmerKreise, int affectedStudents, int scoring) {
    this.affectedPruefungen = affectedPruefungen;
    this.criteria = criteria;
    this.affectedTeilnehmerKreise = affectedTeilnehmerKreise;
    this.amountAffectedStudents = affectedStudents;
    this.scoring = scoring;
  }

  /**
   * gets the affected Pruefungen
   *
   * @return the affected Pruefungen
   */
  public Set<Pruefung> getAffectedPruefungen() {
    return affectedPruefungen;
  }

  /**
   * gets the criteria of the evaluated Restriction
   *
   * @return the criteria
   */
  public WeichesKriterium getCriteria() {
    return criteria;
  }

  /**
   * gets the affected Teilnehmerkreise
   *
   * @return the affected Teilnehmerkreise
   */
  public Set<Teilnehmerkreis> getAffectedTeilnehmerKreise() {
    return affectedTeilnehmerKreise;
  }

  /**
   * gets the amount of students affected by the Restriction
   *
   * @return the amount of students
   */
  public Integer getAmountAffectedStudents() {
    return amountAffectedStudents;
  }

  /**
   * gets the scoring caused by the evaluated Restriction
   *
   * @return the scoring
   */
  public Integer getScoring() {
    return scoring;
  }
}
