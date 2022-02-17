package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class removePrueferSteps extends BaseSteps {


  @Angenommen("die Pruefung {string} hat den Pruefer {string}")
  public void diePruefungPruefungHatDenPrueferPrueferEins(String pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung);

    state.controller.addPruefer(getOrCreate(pruefung), pruefer);
  }


  @Dann("hat die Pruefung {string} {string}")
  public void hatDiePruefungPruefungResult(String pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung readPruefung = getPruefungFromModel(pruefung);
    if (pruefer.equals("\"\"")) {
      pruefer = null;
    }

    assertThat(readPruefung.getPruefer()).containsOnly(pruefer);
  }


  @Angenommen("die Pruefung {string} hat den Pruefer {string} und {string} als Pruefer")
  public void diePruefungPruefungHatDenPrueferPrueferEinsUndPrueferZwei(String pruefung,
      String prueferOne, String prueferTwo) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung readPruefung = getOrCreate(pruefung);
state.controller.addPruefer(readPruefung, prueferOne);
state.controller.addPruefer(readPruefung, prueferTwo);
  }

  @Dann("hat die Pruefung {string} {string} und {string}")
  public void hatDiePruefungPruefungResultUndResultTwo(String pruefung, String resultOne,
      String resultTwo) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat keinen Pruefer eingetragen")
  public void diePruefungPruefungHatKeinenPrueferEingetragen(String pruefung)
      throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung);
  }

  @Wenn("ich den Pruefer {string} entferne von der {string} entferne")
  public void ichDenPrueferPrueferentfernenEntferneVonDerPruefungEntferne(String pruefer,
      String pruefung)
      throws NoPruefungsPeriodeDefinedException {

    state.controller.removePruefer(getPruefungFromModel(pruefung), pruefer);
  }


  @Dann("hat die Pruefung {string} {string} als Pruefer")
  public void hatDiePruefungPruefungResultAlsPruefer(String pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung readOnlyPruefung = getPruefungFromModel(pruefung);
    if (pruefer.equals("")) {
      assertThat(readOnlyPruefung.getPruefer()).isEmpty();
    } else {
      assertThat(readOnlyPruefung.getPruefer()).containsOnly(pruefer);
    }
  }


  @Wenn("ich den Pruefer {string} von der {string} entferne")
  public void ichDenPrueferPrueferentfernenVonDerPruefungEntferne(String pruefer, String pruefung)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.removePruefer(getPruefungFromModel(pruefung), pruefer);
  }

  @Dann("hat die Pruefung {string} {string} und {string} als Pruefer")
  public void hatDiePruefungPruefungResultUndResultTwoAlsPruefer(String pruefung,
      String prueferOne, String prueferTwo) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung readPruefung = getPruefungFromModel(pruefung);

    assertThat(readPruefung.getPruefer()).contains(prueferOne);

    if(!prueferTwo.equals("")){
      assertThat(readPruefung.getPruefer()).contains(prueferTwo);
    }
  }

  @Und("es soll eine Pruefer von einer Pruefung entfernt werden")
  public void esSollEinePrueferVonEinerPruefungEntferntWerden() {
    //Soll leer bleiben
  }

  @Wenn("ich ein Pruefer {string} von einer Pruefung {string} entfernen möchte")
  public void ichEinPrueferVonEinerPruefungEntfernenMöchte(String pruefer, String pruefung) {
    ReadOnlyPruefung r = new  PruefungDTOBuilder().withPruefungsName(pruefung).withPruefungsNummer(pruefung).build();
    try {
      state.controller.removePruefer(r, pruefer);
    } catch (IllegalArgumentException  | NoPruefungsPeriodeDefinedException | IllegalStateException e) {
      state.results.put("exception", e);
    }
  }

  @Und("es soll probiert werden eine Pruefer von einer Pruefung entfernt")
  public void esSollProbiertWerdenEinePrueferVonEinerPruefungEntfernt() {
    //SOll leer bleiben
  }

  @Und("es soll eine Pruefer von einer Pruefung {string} entfernt werden")
  public void esSollEinePrueferVonEinerPruefungEntferntWerden(String pruefung)
      throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung);
  }

  @Wenn("ich einen leeren Pruefer {string} von einer Pruefung {string} entfernen möchte")
  public void ichEinenLeerenPrueferVonEinerPruefungEntfernenMöchte(String pruefer, String pruefung)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung r =getOrCreate(pruefung);
    try {
      state.controller.removePruefer(r, pruefer);
    } catch (IllegalArgumentException  | NoPruefungsPeriodeDefinedException | IllegalStateException e) {
      state.results.put("exception", e);
    }
  }
}
