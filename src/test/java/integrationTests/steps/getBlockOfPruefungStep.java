package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Optional;

public class getBlockOfPruefungStep extends BaseSteps {

  @Wenn("ich den Block zu der Pruefung {string} abfrage")
  public void ichDenBlockZuDerPruefungAbfrage(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    Optional<ReadOnlyBlock> block = state.controller.getBlockOfPruefung(pruefung);
    state.results.put("result", block);
  }

  @Wenn("ich den Block zu der unbekannten Pruefung {string} abfrage")
  public void ichDenBlockZuDerUnbekanntenPruefungAbfrage(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = new PruefungDTO(pruefungName, pruefungName,
        Duration.ofHours(1), emptyMap(), emptySet(), 12);
    try {
      Optional<ReadOnlyBlock> block = state.controller.getBlockOfPruefung(pruefung);
      state.results.put("result", block);
    } catch (IllegalStateException exception) {
      putExceptionInResult(exception);
    }
  }
}
