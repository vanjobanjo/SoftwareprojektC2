package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.AssumptionViolatedException;

public class addPrueferSteps extends BaseSteps {

  @ParameterType("(\\\".+\\\")")
  public Pruefung pruefung(String name) {
    Duration duration = Duration.ofHours(2);
    name = name.substring(1, name.length() - 1);
    return new PruefungImpl(name, name, name, duration);
  }

  @Wenn("ich der Pruefung {string} den Pruefer {string} hinzufuege")
  public void ichDerPruefungDenPrueferHinzufuege(String pruefung, String prueferName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung oldPruefung = new PruefungDTOBuilder()
        .withPruefungsNummer(pruefung)
        .withPruefungsName(pruefung)
        .build();
    try {
      ReadOnlyPlanungseinheit result = state.controller.addPruefer(oldPruefung, prueferName);
      state.results.put("planungseinheit", result);
    } catch (IllegalStateException e) {
      state.results.put("exception", e);
    }
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} eingetragen")
  public void hatDiePruefungDenPrueferEingetragen(String pruefungsName, String prueferName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungsName);
    assertThat(pruefung.getPruefer()).containsExactly(prueferName);
  }

  @Angenommen("die Pruefung {pruefung} hat {string} als Pruefer")
  public void diePruefungHatAlsPruefer(Pruefung pruefung, String prueferName)
      throws NoPruefungsPeriodeDefinedException {
    pruefung.addPruefer(prueferName);
    state.controller.createPruefung(pruefung.getReferenzVerwaltungsystem(),
        pruefung.getName(),
        pruefung.getPruefungsnummer(),
        pruefung.getPruefer(),
        pruefung.getDauer(),
        pruefung.getSchaetzungen());
  }

  @Angenommen("die Pruefung {string} hat {string} und {string} als Pruefer")
  public void diePruefungHatUndAlsPruefer(String pruefungsName, String prueferNameOne,
      String prueferNameTwo) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} und {string} und {string} eingetragen")
  public void hatDiePruefungDenPrueferUndUndEingetragen(String pruefungsName, String prueferNameOne,
      String prueferNameTwo,
      String prueferNameThree) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Und("die Pruefung {pruefung} hat keinen Pruefer")
  public void diePruefungHatKeinenPruefer(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(pruefung.getReferenzVerwaltungsystem(),
        pruefung.getName(),
        pruefung.getPruefungsnummer(),
        pruefung.getPruefer(),
        pruefung.getDauer(),
        pruefung.getSchaetzungen());
  }

  @Dann("hat die Pruefung {string} die Pruefer {stringList}")
  public void hatDiePruefungDiePrueferProfDrDennisSaeringBirgerWolter(String pruefungsName,
      List<String> pruefer)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungsName);
    assertThat(pruefung.getPruefer()).containsExactlyInAnyOrderElementsOf(pruefer);
  }

  @Dann("hat die Pruefung {string} nur den Pruefer {string} eingetragen")
  public void hatDiePruefungNurDenPrueferEingetragen(String pruefungsName, String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungsName);
    assertThat(pruefung.getPruefer()).hasSize(1);
    assertThat(pruefung.getPruefer()).containsExactly(pruefer);
  }

  @Und("die Pruefung {string} ist im Block {string}")
  public void diePruefungIstImBlock(String pruefungsName, String blockName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = state.controller.createBlock(blockName, Blocktyp.PARALLEL,
        getPruefungFromModel(pruefungsName));
  }

  @Dann("erhalte ich den Block {string} zurueck")
  public void erhalteIchDenBlockZurueck(String blockName) {
    ReadOnlyPlanungseinheit result = (ReadOnlyPlanungseinheit) state.results.get("planungseinheit");
    assertThat(result.isBlock()).isTrue();
    assertThat(result.getName()).isEqualTo(blockName);
  }

  @Und("es existiert keine Pruefung {string}")
  public void esExistiertKeinePruefung(String pruefungsName) {
    assertThrows(NoSuchElementException.class,
        () -> getPruefungFromModel(pruefungsName));
  }

}
