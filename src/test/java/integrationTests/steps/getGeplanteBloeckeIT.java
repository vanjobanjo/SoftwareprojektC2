package integrationTests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class getGeplanteBloeckeIT {

  @Und("es gibt die folgenden geplanten Bloecke:")
  public void esGibtDieBloecke(DataTable table) {
    throw new AssumptionViolatedException("Not implemented yet!");

  }

  @Wenn("ich alle geplanten Bloecke anfrage")
  public void ichAlleGeplantenBloeckeAnfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich die Bloecke {string}")
  public void erhalteIchDieBloecke(String bloecke) {
    throw new AssumptionViolatedException("Not implemented yet!");

  }

  @Dann("erhalte ich keine Bloecke")
  public void erhalteIchKeineBloecke() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Und("es gibt keine geplanten Bloecke")
  public void esGibtKeineGeplantenBloecke() {
    throw new AssumptionViolatedException("Not implemented yet!");

  }

  @Und("es gibt die folgenden geplanten und ungeplanten Bloecke:")
  public void esGibtDieFolgendenGeplantenUndUngeplantenBloecke(DataTable table) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }


}
