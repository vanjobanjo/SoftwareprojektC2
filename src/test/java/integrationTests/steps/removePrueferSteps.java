package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class removePrueferSteps {

  @Angenommen("die Pruefung {string} hat den Pruefer {string}")
  public void diePruefungPruefungHatDenPrueferPrueferEins(String pruefung, String pruefer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Pruefer {string} entferne")
  public void ichDenPrueferPrueferentfernenEntferne(String pruefer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} {string}")
  public void hatDiePruefungPruefungResult(String pruefung, String pruefer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat den Pruefer {string} und {string}")
  public void diePruefungPruefungHatDenPrueferPrueferEinsUndPrueferZwei(String pruefung, String prueferOne, String prueferTwo) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} {string} und {string}")
  public void hatDiePruefungPruefungResultUndResultTwo(String pruefung, String resultOne, String resultTwo) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat keinen Pruefer eingetragen")
  public void diePruefungPruefungHatKeinenPrueferEingetragen(String pruefung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
