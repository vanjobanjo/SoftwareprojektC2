package integrationTests.steps;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Set;

public class getGeplantePruefungenWithKonfliktSteps extends BaseSteps {

  @Wenn("ich Konflikte mit geplanten Pruefungen abfrage fuer {string}")
  public void ichKonflikteMitGeplantenPruefungenAbfrageFuer(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    Set<ReadOnlyPruefung> result = state.controller.getGeplantePruefungenWithKonflikt(pruefung);
    state.results.put("pruefungen", result);
  }

  @Wenn("ich Konflikte mit geplanten Pruefungen fuer eine ungeplante Pruefung abfrage")
  public void ichKonflikteMitGeplantenPruefungenFurDieUngeplantePruefungAbfrage()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = new PruefungDTOBuilder()
        .withPruefungsName("unknowon")
        .withPruefungsNummer("unknown")
        .withDauer(Duration.ofHours(1))
        .build();
    Set<ReadOnlyPruefung> result = null;
    try {
      state.controller.getGeplantePruefungenWithKonflikt(pruefung);
    } catch (IllegalStateException exception) {
      putExceptionInResult(exception);
    }
  }
}
