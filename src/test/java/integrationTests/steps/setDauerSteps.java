package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;

public class setDauerSteps extends BaseSteps {

  @Wenn("ich die Dauer der Pruefung {string} auf {int} Minuten ändere")
  public void ichDieDauerDerPruefungAufMinutenÄndere(String pruefung, int dauer)
      throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    try {
      state.controller.setDauer(roPruefung, Duration.ofMinutes(dauer));
    } catch (HartesKriteriumException | IllegalArgumentException e) {
      state.results.put("exception", e);
    }
  }

  @Dann("hat die Pruefung {string} die Dauer von {int} Minuten")
  public void hatDiePruefungDieDauerVonMinuten(String pruefung, int dauer)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    assertThat(roPruefung.getDauer()).hasMinutes(dauer);
  }

  @Und("bekomme ich eine Fehlermeldung IllegalArgumentException")
  public void bekommeIchEineFehlermeldungIllegalArgumentException() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(IllegalArgumentException.class);

  }

  @Wenn("ich eine Pruefung eine dauer aendermoechte die nicht existiert")
  public void ichEinePruefungEineDauerAendermoechteDieNichtExistiert()
      throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = new PruefungDTOBuilder().withPruefungsNummer("wichitg")
        .withPruefungsName("wichitg").build();
    try {
      state.controller.setDauer(roPruefung, Duration.ofMinutes(90));
    } catch (HartesKriteriumException | IllegalArgumentException | IllegalStateException e) {
      state.results.put("exception", e);
    }

  }
}
