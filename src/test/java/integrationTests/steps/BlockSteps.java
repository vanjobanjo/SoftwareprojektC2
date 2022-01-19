package integrationTests.steps;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Angenommen;
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
      ReadOnlyBlock result = state.controller.createBlock(name, Blocktyp.PARALLEL,
          state.controller.getUngeplantePruefungen().stream()
              .filter(x -> block.get("Pruefungen").contains(x.getName()))
              .toArray(ReadOnlyPruefung[]::new));
      if (termin != null) {
        state.controller.scheduleBlock(result, termin);
      }
    }
  }
}
