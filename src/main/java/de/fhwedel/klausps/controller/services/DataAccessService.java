package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.api.Pruefung;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.Map;

public class DataAccessService {

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr,
      String pruefer, Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreis) {

    return new Pruefung(name, pruefungsNr, pruefer, duration, teilnehmerkreis);
  }
}
