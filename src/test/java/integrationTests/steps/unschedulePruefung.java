package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class unschedulePruefung extends BaseSteps {

  @Wenn("ich die Pruefung {string} ausplanen moechte")
  public void ichDiePruefungAusplanenMoechte(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.unschedulePruefung(pruefung);
      state.results.put("pruefungen", result);
    } catch (IllegalArgumentException exception) {
      putExceptionInResult(exception);
    }
  }

  @Dann("ist die Pruefung {string} nicht eingeplant")
  public void istDiePruefungNichtEingeplant(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    assertThat(pruefung.ungeplant()).isTrue();
  }

  @Dann("enthaelt das Ergebnis als einzige Pruefungen {stringList}")
  public void enthaeltDasErgebnisDiePruefungenAnalysisBWL(List<String> pruefungNames) {
    Collection<ReadOnlyPlanungseinheit> actual = (Collection<ReadOnlyPlanungseinheit>) state.results.get(
        "pruefungen");
    List<ReadOnlyPruefung> pruefungen =
        actual.stream().filter(x -> !x.isBlock()).map(ReadOnlyPlanungseinheit::asPruefung).toList();
    assertThat(pruefungen).hasSize(pruefungNames.size());
    assertThat(pruefungen).allMatch((pruefung) -> pruefungNames.contains(pruefung.getName()));
  }

  @Dann("enthaelt das Ergebnis als einzigen Block {string}")
  public void enthaeltDasErgebnisAlsEinzigenBlock(String blockName) {
    List<ReadOnlyPlanungseinheit> actual = (List<ReadOnlyPlanungseinheit>) state.results.get(
        "pruefungen");
    List<ReadOnlyBlock> bloecke =
        actual.stream()
            .filter(ReadOnlyPlanungseinheit::isBlock)
            .map(ReadOnlyPlanungseinheit::asBlock).toList();
    assertThat(bloecke).hasSize(1);
    assertThat(bloecke).allMatch((pruefung) -> blockName.equals((pruefung.getName())));
  }

  @Wenn("ich die unbekannte Pruefung {string} ausplane")
  public void ichDieUnbekanntePruefungAusplane(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = new PruefungDTO(pruefungName, pruefungName, Duration.ofHours(1),
        Collections.emptyMap(), Collections.emptySet(), 0);
    try {
      state.controller.unschedulePruefung(pruefung);
    } catch (IllegalStateException exception) {
      putExceptionInResult(exception);
    }
  }
}
