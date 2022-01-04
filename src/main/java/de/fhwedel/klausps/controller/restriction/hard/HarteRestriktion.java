package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class HarteRestriktion {


  protected Pruefung pruefung;
  protected Set<Pruefung> inConflictROPruefung;
  protected Set<Teilnehmerkreis> inConfilictTeilnehmerkreis;
  protected int countStudents;
  private HartesKriterium hardRestriction;
  protected DataAccessService dataAccessService;

  HarteRestriktion(DataAccessService dataAccessService, HartesKriterium kriterium) {
    this.hardRestriction = kriterium;
    this.dataAccessService = dataAccessService;
    countStudents = 0;
    inConfilictTeilnehmerkreis = new HashSet<>();
    inConflictROPruefung = new HashSet<>();
  }


  public abstract Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung);


}
