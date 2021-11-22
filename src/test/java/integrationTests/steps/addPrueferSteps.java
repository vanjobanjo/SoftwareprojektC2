package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class addPrueferSteps {

  @Angenommen("die Pruefung {string} hat keinen Pruefer als Pruefer")
  public void diePruefungHatKeinenPrueferAlsPruefer(String pruefung) {
  }

  @Wenn("ich der Pruefung {string} den Pruefer {string} hinzufuege")
  public void ichDerPruefungDenPrueferHinzufuege(String pruefung, String prueferName) {
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} eingetragen")
  public void hatDiePruefungDenPrueferEingetragen(String pruefung, String prueferName) {
  }

  @Angenommen("die Pruefung {string} hat {string} als Pruefer")
  public void diePruefungHatAlsPruefer(String pruefung, String prueferName) {
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} und {string} eingetragen")
  public void hatDiePruefungDenPrueferUndEingetragen(String pruefungsName, String prueferNameOne, String prueferNameTwo) {
  }

  @Angenommen("die Pruefung {string} hat {string} und {string} als Pruefer")
  public void diePruefungHatUndAlsPruefer(String pruefungsName, String prueferNameOne, String prueferNameTwo) {
  }

  @Dann("hat die Pruefung {string} den Pruefer {string} und {string} und {string} eingetragen")
  public void hatDiePruefungDenPrueferUndUndEingetragen(String pruefungsName, String prueferNameOne, String prueferNameTwo,
      String prueferNameThree) {
  }
}
