package integrationTests.steps;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.Set;

public class getPruefungenInZeitraumSteps extends BaseSteps {

  @Wenn("ich alle Pruefungen im Zeitraum {localDateTime} - {localDateTime} anfrage")
  public void ichAllePruefungenImZeitraumAnfrage(LocalDateTime start, LocalDateTime end) {
    try {
      Set<ReadOnlyPruefung> result = state.controller.getPruefungenInZeitraum(start, end);
      state.results.put("pruefungen", result);
    } catch (NoPruefungsPeriodeDefinedException | IllegalTimeSpanException e) {
      putExceptionInResult(e);
    }
  }
}
