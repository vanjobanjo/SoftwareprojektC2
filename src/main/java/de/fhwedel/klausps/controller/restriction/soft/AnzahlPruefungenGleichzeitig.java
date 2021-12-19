package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Optional;

public class AnzahlPruefungenGleichzeitig extends WeicheRestriktion {

  protected AnzahlPruefungenGleichzeitig(DataAccessService dataAccessService) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH);
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    return Optional.empty();
  }

}
