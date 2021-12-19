package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AnzahlPruefungenGleichzeitigRestriktion extends WeicheRestriktion {

  protected AnzahlPruefungenGleichzeitigRestriktion(DataAccessService dataAccessService) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH);
  }

  @Override
  public Optional<KriteriumsAnalyse> evaluate(Pruefung pruefung) {
    LocalDateTime pruefungsEnde = pruefung.getStartzeitpunkt().plus(pruefung.getDauer());
    try {
      List<Pruefung> simultaneousPruefungen = dataAccessService.getAllPruefungenBetween(pruefung.getStartzeitpunkt(), pruefungsEnde);
    } catch (IllegalTimeSpanException e) {

    }
    return Optional.empty();
  }

}
