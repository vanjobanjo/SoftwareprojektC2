package restriction;


import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DataAccessServiceTest {

  @Test
  public void createPruefungSuccessTest() {
    DataAccessService service = new DataAccessService();
    ReadOnlyPruefung pruefung = service.createPruefung("Analysis",
        "b123", "pruefer 1", Duration.ofMinutes(90),
        new HashMap<>());
    Assert.assertNotNull("Pruefung ist nicht null", pruefung);
  }


  @Test
  public void createPruefungSuccessRightAttributesTest() {
    DataAccessService service = new DataAccessService();
    Map<Teilnehmerkreis, Integer> teilnehmerkreise = new HashMap<>();
    ReadOnlyPruefung pruefung = service.createPruefung("abc",
        "b123", "pruefer 1", Duration.ofMinutes(90),
        teilnehmerkreise);

    assertThat(pruefung.getName()).isEqualTo("abc");
    assertThat(pruefung.getPruefungsnummer()).isEqualTo("b123");
    assertThat(pruefung.getPruefer()).containsExactly("pruefer 1");
    assertThat(pruefung.getDauer()).hasMinutes(90);
    assertThat(pruefung.getTeilnehmerkreise()).isEqualTo(teilnehmerkreise.keySet());
  }


}
