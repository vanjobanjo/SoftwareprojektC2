package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class removePrueferSteps {

  @Angenommen("die Pruefung <Pruefung> hat den Pruefer <PrueferEins>")
  public void diePruefungPruefungHatDenPrueferPrueferEins() {
  }

  @Wenn("ich den Pruefer <Prueferentfernen> entferne")
  public void ichDenPrueferPrueferentfernenEntferne() {
  }

  @Dann("hat die Pruefung <Pruefung> <result>")
  public void hatDiePruefungPruefungResult() {
  }

  @Angenommen("die Pruefung <Pruefung> hat den Pruefer <PrueferEins> und <PrueferZwei>")
  public void diePruefungPruefungHatDenPrueferPrueferEinsUndPrueferZwei() {
  }

  @Dann("hat die Pruefung <Pruefung> <result> und <resultTwo>")
  public void hatDiePruefungPruefungResultUndResultTwo() {
  }

  @Angenommen("die Pruefung <Pruefung> hat keinen Pruefer eingetragen")
  public void diePruefungPruefungHatKeinenPrueferEingetragen() {
  }
}
