package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class unscheduleBlockSteps extends BaseSteps {

  @Und("es existiert ein geplanter Block {string} mit der Pruefung {string} am {localDateTime}")
  public void esExistiertDerGeplanteBlockMitDerPruefung(String blockName, String pruefungName,
      LocalDateTime localDateTime)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofMinutes(120), emptyMap());

    ReadOnlyBlock block = state.controller.createBlock(blockName, PARALLEL, pruefung);
    state.controller.scheduleBlock(block, localDateTime);

  }

  @Wenn("ich den Block ausplane")
  public void ichDenBlockAusplane() throws NoPruefungsPeriodeDefinedException {
    List<ReadOnlyPlanungseinheit> result = state.controller.unscheduleBlock(
        getBlockFromModel("block"));
    state.results.put("affected", result);
  }


  @Dann("ist der Block ungeplant")
  public void istDerBlockUngeplant() throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isNull();
    ReadOnlyBlock block = getBlockFromModel("block");
    assertThat(block.getTermin()).isEmpty();
    List<ReadOnlyPlanungseinheit> affected = (List<ReadOnlyPlanungseinheit>) state.results.get(
        "affected");
    assertThat(affected).contains(block);

  }

  @Dann("ist die Pruefung {string} ungeplant")
  public void istDiePruefungUngeplant(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isNull();
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    assertThat(pruefung.getTermin()).isEmpty();
    List<ReadOnlyPlanungseinheit> affected = (List<ReadOnlyPlanungseinheit>) state.results.get(
        "affected");
    assertThat(affected).contains(pruefung);

  }

  @Dann("hat sich das Scoring der Pruefung {string} veraendert")
  public void hatSichDasScoringDerPruefungVeraendert(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isNull();
    List<ReadOnlyPlanungseinheit> affected = (List<ReadOnlyPlanungseinheit>) state.results.get(
        "affected");
    assertThat(affected).contains(getPruefungFromModel(pruefungName));
  }

  @Wenn("ich versuche den Block {string} auszuplanen")
  public void ichVersucheDenBlockAuszuplanen(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = new BlockDTO(blockName, LocalDateTime.of(2022, 5, 1, 12, 1),
        Duration.ofMinutes(12), emptySet(), 1, SEQUENTIAL);
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.unscheduleBlock(block);
      state.results.put("affected", result);
    } catch (IllegalStateException e) {
      putExceptionInResult(e);
    }

  }

  @Wenn("ich versuche den Block {string} mit der Pruefung {string} auszuplanen")
  public void ichVersucheDenBlockMitDerPruefungAuszuplanen(String blockName, String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime localDateTime = LocalDateTime.of(2022, 2, 22, 14, 0);
    ReadOnlyPruefung pruefung = new PruefungDTO(pruefungName, pruefungName, localDateTime,
        Duration.ofMinutes(10), emptyMap(), emptySet(), 2);
    ReadOnlyBlock block = new BlockDTO(blockName, localDateTime, Duration.ofMinutes(10),
        Set.of(pruefung), 2, SEQUENTIAL);
    try {
    List<ReadOnlyPlanungseinheit> result = state.controller.unscheduleBlock(block);
    state.results.put("affected", result);
    } catch (IllegalStateException e) {
      putExceptionInResult(e);
    }
  }
}
