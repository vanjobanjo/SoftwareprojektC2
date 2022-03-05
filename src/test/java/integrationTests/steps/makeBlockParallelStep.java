package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.List;

public class makeBlockParallelStep extends BaseSteps {

  @Wenn("ich den Block {string} auf parallel stelle")
  public void ichDenBlockAufParallelStelle(String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    List<ReadOnlyPlanungseinheit> result = state.controller.makeBlockParallel(
        getBlockFromModel(blockName));
    state.results.put("pruefungen", result);
  }

  @Dann("ist der Block {string} parallel")
  public void istDerBlockParallel(String blockName) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = getBlockFromModel(blockName);
    assertThat(block.getTyp()).isEqualTo(Blocktyp.PARALLEL);
  }

  @Wenn("ich einen unbekannten Block auf parallel stelle")
  public void ichEinenUnbekanntenBlockAufParallelStelle()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = new BlockDTO("unknown", null, Duration.ZERO, emptySet(), 0, SEQUENTIAL);
    try {
      state.controller.makeBlockParallel(block);
    } catch (IllegalStateException exception) {
      putExceptionInResult(exception);
    }
  }
}

