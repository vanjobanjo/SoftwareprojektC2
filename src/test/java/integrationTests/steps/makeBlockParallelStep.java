package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class makeBlockParallelStep extends BaseSteps {

  @Wenn("ich den Block {string} auf parallel stelle")
  public void ichDenBlockAufParallelStelle(String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    List<ReadOnlyPlanungseinheit> result = state.controller.makeBlockParallel(
        getBlockFromModel(blockName));

  }

  @Dann("ist der Block {string} parallel")
  public void istDerBlockParallel(String blockName) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = getBlockFromModel(blockName);
    assertThat(block.getTyp()).isEqualTo(Blocktyp.PARALLEL);
  }
}

