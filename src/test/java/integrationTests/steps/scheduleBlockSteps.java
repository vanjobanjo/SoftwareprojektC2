package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class scheduleBlockSteps extends BaseSteps {

  @Und("es existiert ein ungeplanter Block {string} mit der Pruefung {string}")
  public void esExistiertEinUngeplanterBlockMitPruefung(String block, String pruefung) {
    try {
      ReadOnlyPruefung p = state.controller.createPruefung(pruefung, pruefung, pruefung, emptySet(),
          Duration.ofMinutes(90), emptyMap());
      state.controller.createBlock(block, PARALLEL, p);
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }
  }

  @Und("es existiert ein ungeplanter Block {string} mit der Pruefung {string} und dem Teilnehmerkreis {string}")
  public void esExistiertEinUngeplanterBlockMitPruefung(String block, String pruefung,
      String teilnehmerkreis) {
    try {
      ReadOnlyPruefung p = state.controller.createPruefung(pruefung, pruefung, pruefung, emptySet(),
          Duration.ofMinutes(90), Map.of(createTeilnehmerkreis(teilnehmerkreis), 10));
      state.controller.createBlock(block, PARALLEL, p);
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }

  }


  @Wenn("ich den Block am {localDateTime} einplane")
  public void ichDenBlockAmEinplane(LocalDateTime dateTime) {
    try {
      ReadOnlyBlock b = getBlockFromModel("block");
      List<ReadOnlyPlanungseinheit> result = state.controller.scheduleBlock(b, dateTime);
      state.results.put("affected", result);

    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException e) {
      putExceptionInResult(e);
    }
  }

  @Dann("ist der Block {string} am {localDateTime} geplant")
  public void wirdDerBlockAmGeplant(String block, LocalDateTime dateTime)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("affected")).isNotNull();
    Set<ReadOnlyBlock> planned = state.controller.getGeplanteBloecke();
    assertThat(planned).hasSize(1);
    ReadOnlyBlock resultBlock = planned.toArray(new ReadOnlyBlock[0])[0];
    Optional<LocalDateTime> resultTermin = resultBlock.getTermin();
    assertThat(resultBlock.getName()).isEqualTo(block);
    assertThat(resultTermin).isPresent();
    assertThat(resultTermin).contains(dateTime);
  }

  @Und("es werden Restriktionen verletzt")
  public void esWerdenRestriktionenVerletzt() {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("affected")).isNotNull();
    assertThat((List<ReadOnlyPlanungseinheit>)state.results.get("affected")).hasSizeGreaterThan(2);

  }


  @Und("es existiert die geplante Pruefung {string} am {localDateTime} mit dem Teilnehmerkreis {string}")
  public void esExistiertDieGeplantePruefungAmMitDemTeilnehmerkreis(String pruefung,
      LocalDateTime dateTime, String teilnehmerkreis) {

    try {
      ReadOnlyPruefung p = state.controller.createPruefung(pruefung, pruefung, pruefung, emptySet(),
          Duration.ofMinutes(90), Map.of(createTeilnehmerkreis(teilnehmerkreis), 2));
      state.controller.schedulePruefung(p, dateTime);
    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException e) {
      putExceptionInResult(e);
    }
  }

  @Und("es werden keine Restriktionen verletzt")
  public void esWerdenKeineRestriktionenVerletzt() {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("affected")).isNotNull();
    List<ReadOnlyPlanungseinheit> affected = (List<ReadOnlyPlanungseinheit>) state.results.get(
        "affected");
    assertThat(affected).hasSize(2);
    for (ReadOnlyPlanungseinheit p : affected) {
      if (p.isBlock()) {
        assertThat(p.asBlock().getName()).isEqualTo("block");
      } else {
        assertThat(p.asPruefung().getName()).isEqualTo("pruefung");
      }
    }
  }

  @Dann("wird eine harte Restriktion verletzt")
  public void wirdEineHarteRestriktionVerletzt() {
    assertThat(state.results.get(EXCEPTION)).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isInstanceOf(HartesKriteriumException.class);
  }
}
