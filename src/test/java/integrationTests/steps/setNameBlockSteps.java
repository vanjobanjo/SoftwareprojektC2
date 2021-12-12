package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class setNameBlockSteps {

  @Angenommen("es existiert der geplante Block {string}")
  public void esExistiertDerGeplanteBlock(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Namen des Blocks auf {string} aendere")
  public void ichDenNamenDesBlocksAufAendere(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich einen Block mit dem Namen {string}")
  public void erhalteIchEinenBlockMitDemNamen(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existiert der ungeplante Block {string}")
  public void esExistiertDerUngeplanteBlock(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existiert kein Block mit dem Namen {string}")
  public void esExistiertKeinBlockMitDemNamen(String blockName) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existieren die Bloecke {string} und {string}")
  public void esExistierenDieBloeckeUnd(String blockName1, String blockName2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Namen von {string} auf {string} aendere")
  public void ichDenNamenVonAufAendere(String blockName1, String blockName2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
