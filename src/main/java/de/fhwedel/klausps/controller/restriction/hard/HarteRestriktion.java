package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Optional;

public abstract class HarteRestriktion {


  protected final HartesKriterium hardRestriction;
  protected DataAccessService dataAccessService;

  HarteRestriktion(DataAccessService dataAccessService, HartesKriterium kriterium) {
    this.hardRestriction = kriterium;
    this.dataAccessService = dataAccessService;

  }


  public abstract Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung);


}
