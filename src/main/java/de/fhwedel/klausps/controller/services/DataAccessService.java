package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.api.Pruefung;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

public class DataAccessService {

  private Pruefungsperiode pruefungsperiode;

  public DataAccessService(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr,
      String pruefer, Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreis) {

    Set<Planungseinheit> filtered = pruefungsperiode.filteredPlanungseinheiten(
        planungseinheit -> planungseinheit instanceof de.fhwedel.klausps.model.api.Pruefung
            && ((de.fhwedel.klausps.model.api.Pruefung) planungseinheit)
            .getPruefungsnummer().equals(pruefungsNr));

    if (filtered.isEmpty()) {
      // todo add pruefung to model, can't implement without acutal structures from model
      //pruefungsperiode.addPlanungseinheit(new de.fhwedel.klausps.model.api.Pruefung());
      return new Pruefung(name, pruefungsNr, pruefer, duration, teilnehmerkreis);
    }
    return null;
  }
}

