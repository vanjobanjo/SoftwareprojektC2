package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.AssumptionViolatedException;

public class getGeplanteBloeckeSteps extends BaseSteps {

  @Und("es gibt die folgenden geplanten Bloecke:")
  public void esGibtDieBloecke(DataTable table) {
    throw new AssumptionViolatedException("Not implemented yet!");

  }

  @Wenn("ich alle geplanten Bloecke anfrage")
  public void ichAlleGeplantenBloeckeAnfrage() {
    try {
      state.results.put("bloecke",
          state.controller.getGeplanteBloecke());
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }
  }

  @Dann("erhalte ich die Bloecke {string}")
  public void erhalteIchDieBloecke(String bloecke) {
    List<String> expectedBlockIds = Arrays.asList(bloecke.split(", "));
    Set<ReadOnlyBlock> actual = (Set<ReadOnlyBlock>) state.results.get("bloecke");
    assertThat(actual).allMatch(x -> expectedBlockIds.contains(x.getName()));
    assertThat(actual).hasSameSizeAs(expectedBlockIds);
  }

  @Dann("erhalte ich keine Bloecke")
  public void erhalteIchKeineBloecke() {
    Set<ReadOnlyBlock> actual = (Set<ReadOnlyBlock>) state.results.get("bloecke");
    assertThat(actual).isEmpty();
  }

  @Und("es gibt keine geplanten Bloecke")
  public void esGibtKeineGeplantenBloecke() throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> geplanteBloecke = state.controller.getGeplanteBloecke();
    assertThat(geplanteBloecke).isEmpty();
  }

  @Und("es gibt die folgenden geplanten und ungeplanten Bloecke:")
  public void esGibtDieFolgendenGeplantenUndUngeplantenBloecke(DataTable table) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
