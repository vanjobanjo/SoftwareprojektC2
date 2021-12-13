package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Set;

public class HartesKriteriumAnalyse {

  Set<ReadOnlyPruefung> causingPruefungen;

  Set<Teilnehmerkreis> affectedTeilnehmerkreise;

  Integer amountAffectedStudents;

  public HartesKriteriumAnalyse(
      Set<ReadOnlyPruefung> causingPruefungen,
      Set<Teilnehmerkreis> affectedTeilnehmerkreise, Integer amountAffectedStudents) {
    this.causingPruefungen = causingPruefungen;
    this.affectedTeilnehmerkreise = affectedTeilnehmerkreise;
    this.amountAffectedStudents = amountAffectedStudents;
  }

  public Set<ReadOnlyPruefung> getCausingPruefungen() {
    return causingPruefungen;
  }

  public Set<Teilnehmerkreis> getAffectedTeilnehmerkreise() {
    return affectedTeilnehmerkreise;
  }

  public Integer getAmountAffectedStudents() {
    return amountAffectedStudents;
  }
}
