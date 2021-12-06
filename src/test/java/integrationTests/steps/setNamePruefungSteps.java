package integrationTests.steps;


import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import org.junit.AssumptionViolatedException;

public class setNamePruefungSteps {

  @Angenommen("ich den Namen der Pruefung auf {string} aendere")
  public void ichDenNamenDerPruefungAendere(String pruefungsname) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }


  @Dann("erhalte ich eine Pruefung mit dem Namen {string}")
  public void erhalteIchEinePruefungMitDemNamen(String pruefungsname) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existiert keine Pruefung mit dem Namen {string}")
  public void esExistiertKeinePruefungMitDemNamen(String pruefungsname) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existieren die Pruefungen {string} und {string}")
  public void esExistierenDiePruefungenUnd(String pruefungsname1, String pruefungsname2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
