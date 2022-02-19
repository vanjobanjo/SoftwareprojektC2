package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class deleteBlockSteps extends BaseSteps {

  @Wenn("ich den Block {string} loesche")
  public void ichDenBlockLoesche(String blockName) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = getBlockFromModel(blockName);
    try {
      List<ReadOnlyPruefung> pruefungen = state.controller.deleteBlock(block);
      state.results.put("pruefungen", pruefungen);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Dann("erhalte ich keine Pruefungen")
  public void erhalteIchKeinePruefungen() {
    List<ReadOnlyPruefung> pruefungen = (List<ReadOnlyPruefung>) state.results.get("pruefungen");
    assertThat(pruefungen).isEmpty();
  }

  @Dann("enthaelt das Ergebnis genau die Pruefung {string}")
  public void enthaeltDasErgebnisGenauDiePruefung(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung expected = getPruefungFromModel(pruefungName);
    List<ReadOnlyPruefung> actual = (List<ReadOnlyPruefung>) state.results.get("pruefungen");
    assertThat(actual).contains(expected);
  }

  @Wenn("ich den unbekannten Block {string} loesche")
  public void ichDenUnbekanntenBlockLoesche(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = new BlockDTO("name", null, Duration.ofHours(1), Collections.emptySet(),
        1998, Blocktyp.PARALLEL);
    try {
      List<ReadOnlyPruefung> pruefungen = state.controller.deleteBlock(block);
      state.results.put("pruefungen", pruefungen);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich den Block {string} mit einer unbekannten Pruefung loesche")
  public void ichDenBlockMitEinerUnbekanntenPruefungLoesche(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = getBlockFromModel(blockName);
    ReadOnlyPruefung unknownPruefung = new PruefungDTOBuilder()
        .withPruefungsNummer("unknown").withPruefungsName("unknown").withDauer(Duration.ofHours(1))
        .build();
    block = new BlockDTO(block.getName(), block.getTermin().orElse(null), block.getDauer(),
        Set.of(unknownPruefung), block.getBlockId(), block.getTyp());
    try {
      List<ReadOnlyPruefung> pruefungen = state.controller.deleteBlock(block);
      state.results.put("pruefungen", pruefungen);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }
}
