package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class hardConflictSteps extends BaseSteps {


  @Und("es existiert die geplante Pruefung {string} mit den Teilnehmerkreisen am {localDateTime}")
  public void esExistiertDieGeplantePruefungMitDenTeilnehmerkreisenAmUmUhr(String pruefung,
      LocalDateTime termin, List<List<String>> table)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {

    ReadOnlyPruefung roPruefung = getOrCreate(pruefung);
    state.controller.schedulePruefung(roPruefung, termin);

    getTeilnehmerMap(roPruefung, table);

  }

  private void getTeilnehmerMap(ReadOnlyPruefung roPruefung, List<List<String>> table)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {

    table.remove(0);
    for (List<String> t : table) {
      Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl(t.get(0), t.get(0),
          Integer.parseInt(t.get(1)),
          Ausbildungsgrad.BACHELOR);

      state.controller.addTeilnehmerkreis(roPruefung, teilnehmerkreis, Integer.parseInt(t.get(2)));
    }

  }

  @Und("es existiert die ungeplante Pruefung {string} mit den Teilnehmerkreisen")
  public void esExistiertDieUngeplantePruefungMitDenTeilnehmerkreisen(String pruefung,
      List<List<String>> table)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {

    ReadOnlyPruefung roPruefung = getOrCreate(pruefung);
    getTeilnehmerMap(roPruefung, table);
  }

  @Wenn("ich die Pruefung {string} am {localDateTime} einplanen moechte")
  public void ichDiePruefungAmUmUhrEinplanenMoechte(String pruefung, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    try {
      state.controller.schedulePruefung(getPruefungFromModel(pruefung), termin);
    } catch (HartesKriteriumException e) {
      state.results.put("exception", e);
    }

  }

  @Dann("habe ich eine HartesKriteriumException mit den Teilnehmerkreisen")
  public void habeIchEineHartesKriteriumExceptionMitDenTeilnehmerkreisen(List<List<String>> table) {

    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(HartesKriteriumException.class);
    HartesKriteriumException h = (HartesKriteriumException) exception;

    table.remove(0);
    int betroffeneStudenten = 0;
    for (List<String> t : table) {
      betroffeneStudenten += Integer.parseInt(t.get(2));
    }

    assertThat(h.getAnzahlBetroffenerStudenten()).isEqualTo(betroffeneStudenten);

    Teilnehmerkreis testTeilnehmerkreis ;

    for (List<String> t : table) {
     testTeilnehmerkreis = new TeilnehmerkreisImpl(t.get(0),t.get(0),Integer.parseInt(t.get(1)),Ausbildungsgrad.BACHELOR);
     assertThat(h.getTeilnehmerkreis()).contains(testTeilnehmerkreis);
    }


  }
}
