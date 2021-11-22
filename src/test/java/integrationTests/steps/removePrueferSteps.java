package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class removePrueferSteps {

  @Angenommen("die Pruefung <Pruefung> hat den Pruefer <PrueferEins>")
  public void diePruefungPruefungHatDenPrueferPrueferEins() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Pruefer <Prueferentfernen> entferne")
  public void ichDenPrueferPrueferentfernenEntferne() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung <Pruefung> <result>")
  public void hatDiePruefungPruefungResult() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung <Pruefung> hat den Pruefer <PrueferEins> und <PrueferZwei>")
  public void diePruefungPruefungHatDenPrueferPrueferEinsUndPrueferZwei() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung <Pruefung> <result> und <resultTwo>")
  public void hatDiePruefungPruefungResultUndResultTwo() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung <Pruefung> hat keinen Pruefer eingetragen")
  public void diePruefungPruefungHatKeinenPrueferEingetragen() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
