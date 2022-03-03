package integrationTests.steps;


import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Set;

public class setNamePruefungSteps extends BaseSteps {

  @Wenn("ich den Namen der Pruefung auf {string} aendere")
  public void ichDenNamenDerPruefungAendere(String pruefungsName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel("pruefung");
    ReadOnlyPlanungseinheit result = state.controller.setName(pruefung, pruefungsName);
    state.results.put("changed", result);
  }


  @Dann("erhalte ich eine Pruefung mit dem Namen {string}")
  public void erhalteIchEinePruefungMitDemNamen(String pruefungsName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungsName);
    assertThat(pruefung).isNotNull();
    assertThat(pruefung.getName()).isEqualTo(pruefungsName);
  }

  @Und("es existiert keine Pruefung mit dem Namen {string}")
  public void esExistiertKeinePruefungMitDemNamen(String pruefungsName)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> pruefungen = state.controller.getGeplantePruefungen();
    pruefungen.addAll(state.controller.getUngeplantePruefungen());
    int flag = 0;
    for (ReadOnlyPruefung pruefung : pruefungen) {
      if (pruefung.getName().equals(pruefungsName)) {
        flag++;
      }
    }
    assertThat(flag).isZero();
  }

  @Und("es existieren die Pruefungen {string} und {string}")
  public void esExistierenDiePruefungenUnd(String pruefungsName, String otherPruefungsName)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(pruefungsName, pruefungsName, pruefungsName, emptySet(),
        Duration.ofMinutes(120), emptyMap());
    state.controller.createPruefung(otherPruefungsName, otherPruefungsName, otherPruefungsName,
        emptySet(), Duration.ofMinutes(120), emptyMap());

  }

  @Dann("erhalte ich den Block {string} mit der geaenderten Pruefung {string}")
  public void erhalteIchDenBlockMitDerGeaendertenPruefung(String blockName, String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(getBlockFromModel(blockName)).isNotNull();
    ReadOnlyPlanungseinheit result = (ReadOnlyPlanungseinheit) state.results.get("changed");
    assertThat(result.isBlock()).isTrue();
    ReadOnlyBlock block = result.asBlock();
    int flag = 0;
    for (ReadOnlyPruefung pruefung : block.getROPruefungen()) {
      if (pruefung.getName().equals(pruefungName)) {
        flag++;
      }
    }
    assertThat(flag).isEqualTo(1);
  }

  @Wenn("ich versuche den Namen der Pruefung auf {string} zu aendern")
  public void ichVersucheDenNamenDerPruefungAufZuAendern(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsNummer(pruefungName)
        .withPruefungsNummer(pruefungName).build();
    try {
      state.controller.setName(pruefung, pruefungName);
    } catch (IllegalStateException e) {
      putExceptionInResult(e);
    }
  }

}
