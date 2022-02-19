package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import org.junit.AssumptionViolatedException;

public class setNameBlockSteps extends BaseSteps {

  @Angenommen("es existiert der geplante Block {string}")
  public void esExistiertDerGeplanteBlock(String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = state.controller.createBlock(blockName, Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung("name", "name",
        "name", emptySet(), Duration.ofHours(1), emptyMap());
    state.controller.addPruefungToBlock(blockToChange, pruefungToChange);
    state.controller.scheduleBlock(blockToChange,
        state.controller.getStartDatumPeriode().atStartOfDay());
  }

  @Wenn("ich den Namen des Blocks auf {string} aendere")
  public void ichDenNamenDesBlocksAufAendere(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich einen Block mit dem Namen {string}")
  public void erhalteIchEinenBlockMitDemNamen(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existiert der ungeplante Block {string}")
  public void esExistiertDerUngeplanteBlock(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existiert kein Block mit dem Namen {string}")
  public void esExistiertKeinBlockMitDemNamen(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existieren die Bloecke {string} und {string}")
  public void esExistierenDieBloeckeUnd(String blockName1, String blockName2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Namen von {string} auf {string} aendere")
  public void ichDenNamenVonAufAendere(String blockName1, String blockName2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
