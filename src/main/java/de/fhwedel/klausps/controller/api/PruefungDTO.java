package de.fhwedel.klausps.controller.api;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import java.util.Set;

public class PruefungDTO extends ReadOnlyPruefung {

  public PruefungDTO(String pruefungsnummer,
                     String name,
                     LocalDateTime termin,
                     Duration dauer,
                     Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung,
                     Set<String> pruefer,
                     int scoring,
                     boolean geplant) {
    super(pruefungsnummer, name, termin, dauer, teilnehmerkreisSchaetzung, pruefer, scoring, geplant);
  }

  public int getScoring() {
    return 0;
  }
}
