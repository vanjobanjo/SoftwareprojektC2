package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import java.util.List;


public abstract class WeicheRestriktion {

  protected final DataAccessService dataAccessService;
  protected final WeichesKriterium kriterium;

  protected WeicheRestriktion(
      DataAccessService dataAccessService,
      WeichesKriterium kriterium) {
    this.dataAccessService = dataAccessService;
    this.kriterium = kriterium;
  }

  public List<KriteriumsAnalyse> evaluate() {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

}
