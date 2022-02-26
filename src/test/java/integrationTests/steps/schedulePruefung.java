package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;

public class schedulePruefung extends BaseSteps {


  @Wenn("ich {string} am {localDateTime} einplanen moechte")
  public void ichAmEinplanenMoechte(String pruefung, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    try {
      state.controller.schedulePruefung(roPruefung, termin);
    } catch (HartesKriteriumException e) {
      state.results.put("exception", e);
    }
  }

  @Dann("ist die Pruefung {string} am {localDateTime} eingeplant")
  public void istDiePruefungAmEingeplant(String pruefung, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);

    assertThat(roPruefung.geplant()).isTrue();
    assertThat(roPruefung.getTermin()).isNotEmpty();
    assertThat(roPruefung.getTermin()).get().isEqualTo(termin);

  }

  @Und("es existiert die ungeplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster")
  public void esExistiertDieUngeplantePruefungMitDenTeilnehmerkreis(String pruefung,
      String teilnehmerkreis, int semster) throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreis, semster);

  }

  @Und("es existiert die geplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster am {localDateTime}")
  public void esExistiertDieGeplantePruefungMitDenTeilnehmerkreisImSemsterAm(String pruefung,
      String teilnehmerkeisString,
      int semster, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkeisString, semster);
    state.controller.schedulePruefung(roPruefung, termin);
  }



  @Dann("bekomme ich eine Fehlermeldung HartesKriteriumException")
  public void bekommeIchEineFehlermeldungHartesKriteriumException() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(HartesKriteriumException.class);
  }
}
