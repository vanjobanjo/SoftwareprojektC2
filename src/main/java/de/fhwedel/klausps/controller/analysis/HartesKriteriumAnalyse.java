package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashSet;
import java.util.Set;

public class HartesKriteriumAnalyse {

  Set<Pruefung> causingPruefungen;

  Set<Teilnehmerkreis> affectedTeilnehmerkreise;

  Integer amountAffectedStudents;
  HartesKriterium kriterium;

  public HartesKriteriumAnalyse(
      Set<Pruefung> causingPruefungen,
      Set<Teilnehmerkreis> affectedTeilnehmerkreise, Integer amountAffectedStudents, HartesKriterium kriterium) {
    this.causingPruefungen = causingPruefungen;
    this.affectedTeilnehmerkreise = affectedTeilnehmerkreise;
    this.amountAffectedStudents = amountAffectedStudents;
    this.kriterium = kriterium;
  }

  public Set<Pruefung> getCausingPruefungen() {
    return this.causingPruefungen;
  }

  public Set<Teilnehmerkreis> getAffectedTeilnehmerkreise() {
    return affectedTeilnehmerkreise;
  }

  public Integer getAmountAffectedStudents() {
    return amountAffectedStudents;
  }

  public HartesKriterium getKriterium() {
    return this.kriterium;
  }
}
