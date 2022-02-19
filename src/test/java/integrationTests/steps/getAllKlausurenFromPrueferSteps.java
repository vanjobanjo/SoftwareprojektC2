package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class getAllKlausurenFromPrueferSteps extends BaseSteps {


  @Angenommen("es existieren die folgenden Klausuren mit Pruefer:")
  public void esExistierenDieFolgendenKlausurenMitPruefer(List<Map<String, String>> pruefungen)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    for (Map<String, String> pruefung : pruefungen) {
      String pruefungsnummer = String.valueOf(pruefung.get("Name").hashCode());
      ReadOnlyPruefung roPruefung = state.controller.createPruefung(pruefungsnummer,
          pruefung.get("Name"),
          pruefungsnummer,
          Set.of(pruefung.get("Pruefer").split(", ")),
          parseDuration(pruefung.get("Dauer")),
          emptyMap());
      String date = pruefung.get("Datum");
      String time = pruefung.get("StartZeit");
      if (date != null && time != null) {
        LocalDateTime start = parseDate(date).atTime(parseTime(time));
        state.controller.schedulePruefung(roPruefung, start);
      }
    }
  }

  @Wenn("ich alle Pruefungen des Pruefers {string} abfrage")
  public void ichAllePruefungenDesPruefersAbfrage(String pruefer) {
    try {
      Set<ReadOnlyPruefung> result = state.controller.getAllKlausurenFromPruefer(pruefer);
      state.results.put("pruefungen", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }
  }

  @Dann("bekomme ich keine Pruefungen")
  public void bekommeIchKeinePruefungen() {
    assertThat(getExceptionFromResult()).isNull();
    assertThat(state.results.get("pruefungen")).isNotNull();
  }


  @Dann("bekomme ich die Pruefungen {string}")
  public void bekommeIchDiePruefungen(String pruefungen) {
    assertThat(getExceptionFromResult()).isNull();
    List<Integer> pruefungsNums = new ArrayList<>();
    for (String name : pruefungen.split(", ")) {
      pruefungsNums.add(name.hashCode());
    }

    if (state.results.get("pruefungen") instanceof Set<?> result) {
      assertThat((result).size()).isEqualTo(pruefungsNums.size());
      for (ReadOnlyPruefung pruefung : (Set<ReadOnlyPruefung>) result) {
        assertThat(pruefungsNums).contains(Integer.valueOf(pruefung.getPruefungsnummer()));
      }
    } else {
      throw new AssertionError("Set erwartet, war aber kein Set");
    }
  }
}
