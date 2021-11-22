package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class addPrueferSteps {

  @Angenommen("die Pruefung {string} hat keinen Pruefer als Pruefer")
  public void diePruefungHatKeinenPrueferAlsPruefer(String pruefung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich der Pruefung {string} den Pruefer {string} hinzufuege")
  public void ichDerPruefungDenPrueferHinzufuege(String pruefung, String prueferName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} eingetragen")
  public void hatDiePruefungDenPrueferEingetragen(String pruefung, String prueferName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat {string} als Pruefer")
  public void diePruefungHatAlsPruefer(String pruefung, String prueferName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} und {string} eingetragen")
  public void hatDiePruefungDenPrueferUndEingetragen(String pruefungsName, String prueferNameOne, String prueferNameTwo) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat {string} und {string} als Pruefer")
  public void diePruefungHatUndAlsPruefer(String pruefungsName, String prueferNameOne, String prueferNameTwo) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} und {string} und {string} eingetragen")
  public void hatDiePruefungDenPrueferUndUndEingetragen(String pruefungsName, String prueferNameOne, String prueferNameTwo,
      String prueferNameThree) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
