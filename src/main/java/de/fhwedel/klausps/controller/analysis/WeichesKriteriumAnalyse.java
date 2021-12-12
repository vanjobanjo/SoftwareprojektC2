package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Set;

public class WeichesKriteriumAnalyse {

  Set<ReadOnlyPruefung> causingPruefungen;
  WeichesKriterium kriterium;
  Set<Teilnehmerkreis> affectedTeilnehmerKreise;
  Integer amountAffectedStudents;

  public Set<ReadOnlyPruefung> getCausingPruefungen() {
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
