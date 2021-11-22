package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class removePrueferSteps {

  @Angenommen("die Prüfung <Prüfung> hat den Prüfer <PrüferEins> und <PrüferZwei>")
  public void diePruefungHatdiesePruefer() {
  }

  @Wenn("ich den Prüfer <Prüferentfernen> entferne")
  public void ichDenPrueferentferne() {
  }

  @Dann("hat die Prüfung <Prüfung> <result> und <resultTwo>")
  public void hatDiePruefungResultUndResultTwo() {
  }
}
