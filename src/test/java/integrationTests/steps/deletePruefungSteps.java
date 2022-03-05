package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class deletePruefungSteps extends BaseSteps {

  @Wenn("ich die Pruefung {string} loesche")
  public void ichDiePruefungLoesche(String pruefungName) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    try {
      Optional<ReadOnlyBlock> block = state.controller.deletePruefung(pruefung);
      state.results.put("result", block);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }

  @Dann("existiert die Pruefung {string} nicht mehr")
  public void existiertDiePruefungNichtMehr(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(existsPruefungWith(pruefungName)).isFalse();
  }

  @Dann("ist das Ergebnis leer")
  public void istDasErgebnisLeer() {
    Optional<Object> optional = (Optional<Object>) state.results.get("result");
    assertThat(optional).isEmpty();
  }

  @Dann("enthaelt das Ergebnis den Block {string}")
  public void enthaeltDasErgebnisDenBlock(String blockName) {
    Optional<ReadOnlyBlock> optional = (Optional<ReadOnlyBlock>) state.results.get("result");
    assertThat(optional).isPresent();
    assertThat(optional.get().getName()).isEqualTo(blockName);
  }

  @Wenn("ich die unbekannte Pruefung {string} loesche")
  public void ichDieUnbekanntePruefungLoesche(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = new PruefungDTO(pruefungName, pruefungName,
        Duration.ofHours(1), emptyMap(), emptySet(), 12);
    try {
      Optional<ReadOnlyBlock> block = state.controller.deletePruefung(pruefung);
      state.results.put("result", block);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }

  @Dann("enthaelt das Ergebnis den Block {string} mit genau den Pruefungen {stringList}")
  public void enthaeltDasErgebnisDenBlockMitGenauDenPruefungen(String blockName,
      List<String> pruefungNames) {
    Optional<ReadOnlyBlock> optional = (Optional<ReadOnlyBlock>) state.results.get("result");
    assertThat(optional).isPresent();
    ReadOnlyBlock block = optional.get();
    assertThat(block.getName()).isEqualTo(blockName);
    assertThat(block.getROPruefungen()).hasSameSizeAs(pruefungNames);
    assertThat(block.getROPruefungen()).allMatch(
        pruefung -> pruefungNames.contains(pruefung.getName()));
  }
}
