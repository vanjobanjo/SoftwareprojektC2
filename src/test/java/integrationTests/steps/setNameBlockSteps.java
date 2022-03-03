package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
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
  public void ichDenNamenDesBlocksAufAendere(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = getBlockFromModel("block");

    try {
      ReadOnlyBlock result = state.controller.setName(block, blockName);
      state.results.put("changed", result);
    } catch (IllegalStateException e) {
      putExceptionInResult(e);
    }

  }

  @Dann("erhalte ich einen Block mit dem Namen {string}")
  public void erhalteIchEinenBlockMitDemNamen(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> bloecke = state.controller.getGeplanteBloecke();
    bloecke.addAll(state.controller.getUngeplanteBloecke());
    int flag = 0;
    for (ReadOnlyBlock block : bloecke) {
      if (block.getName().equals(blockName)) {
        flag++;
      }
    }
    assertThat(flag).isPositive();

  }

  @Angenommen("es existiert der ungeplante Block {string}")
  public void esExistiertDerUngeplanteBlock(String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = state.controller.createBlock(blockName, Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung("name", "name",
        "name", emptySet(), Duration.ofHours(1), emptyMap());
    state.controller.addPruefungToBlock(blockToChange, pruefungToChange);
  }

  @Angenommen("es existiert kein Block mit dem Namen {string}")
  public void esExistiertKeinBlockMitDemNamen(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> bloecke = state.controller.getGeplanteBloecke();
    bloecke.addAll(state.controller.getUngeplanteBloecke());
    int flag = 0;
    for (ReadOnlyBlock block : bloecke) {
      if (block.getName().equals(blockName)) {
        flag++;
      }
    }
    assertThat(flag).isZero();
  }

  @Angenommen("es existieren die Bloecke {string} und {string}")
  public void esExistierenDieBloeckeUnd(String blockName1, String blockName2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Namen von {string} auf {string} aendere")
  public void ichDenNamenVonAufAendere(String blockName1, String blockName2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich versuche den Namen des Blocks auf {string} zu aendern")
  public void ichVersucheDenNamenDesBlocksAufZuAendern(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = new BlockDTO("block", LocalDateTime.of(1997, 1, 1, 22, 0),
        Duration.ofMinutes(10), emptySet(), 1, SEQUENTIAL);

    try {
      state.controller.setName(block, blockName);
    } catch (IllegalStateException | IllegalArgumentException e) {
      putExceptionInResult(e);
    }
  }
}

