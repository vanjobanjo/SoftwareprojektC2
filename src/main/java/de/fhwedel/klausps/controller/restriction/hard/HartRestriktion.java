package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HartRestriktion {


  protected Pruefung pruefung;
  Set<Pruefung> inConflictROPruefung;
  Set<Teilnehmerkreis> inConfilictTeilnehmerkreis;
  int countStudents;
  HartesKriterium hardRestriction;
  DataAccessService dataAccessService;

  HartRestriktion(DataAccessService dataAccessService, HartesKriterium kriterium) {
    this.hardRestriction = kriterium;
    this.dataAccessService = dataAccessService;
    countStudents = 0;
    inConfilictTeilnehmerkreis = new HashSet<>();
    inConflictROPruefung = new HashSet<>();
  }


  public List<KriteriumsAnalyse> evaluate() throws HartesKriteriumException {
    throw new UnsupportedOperationException("Not implemented yet!");

  }


}
