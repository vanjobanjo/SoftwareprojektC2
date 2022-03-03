package integrationTests.steps;

import static integrationTests.DataTypes.parseDate;
import static integrationTests.DataTypes.parseTime;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BlockSteps extends BaseSteps {

  @Angenommen("es existieren die folgenden Bloecke:")
  public void esExistierenDieFolgendenBloecke(List<Map<String, String>> bloecke)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    for (Map<String, String> block : bloecke) {
      String name = block.get("Block");
      String date = block.get("Datum");
      String time = block.get("StartZeit");
      LocalDateTime termin = null;
      if (date != null && time != null) {
        termin = parseDate(date).atTime(parseTime(time));
      }
      ReadOnlyPruefung[] pruefungen = state.controller.getUngeplantePruefungen().stream()
          .filter((ReadOnlyPruefung x) -> block.get("Pruefungen").contains(x.getName()))
          .toArray(ReadOnlyPruefung[]::new);
      ReadOnlyBlock result = state.controller.createBlock(name, Blocktyp.PARALLEL, pruefungen);
      if (termin != null) {
        state.controller.scheduleBlock(result, termin);
      }
    }
  }

  @Wenn("ich alle ungeplanten Bloecke anfrage")
  public void ichAlleUngeplantenBloeckeAnfrage() {
    try {
      state.results.put("bloecke",
          state.controller.getUngeplanteBloecke());
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }
  }
}
