package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class getPlanungseinheitenInZeitraumSteps extends BaseSteps {

  @Wenn("ich alle Planungseinheiten im Zeitraum {localDateTime} - {localDateTime} anfrage")
  public void ichAllePlanungseinheitenInZeitraumAnfrage(LocalDateTime start, LocalDateTime end) {
    try {
      Set<ReadOnlyPlanungseinheit> result = state.controller
          .getPlanungseinheitenInZeitraum(start, end);
      state.results.put("planungseinheiten", result);
    } catch (NoPruefungsPeriodeDefinedException | IllegalTimeSpanException e) {
      putExceptionInResult(e);
    }
  }

  @Dann("erhalte ich die Pruefungen {string} und die Bloecke {string}")
  public void erhalteIchDiePruefungenUndDenBlock(String pruefungen,
      String bloecke) {
    List<String> splitPruefungen = List.of(pruefungen.split(", "));
    List<String> splitBloecke = List.of(bloecke.split(", "));
    int blockCount = 0;
    int pruefungsCount = 0;
    assertThat(getExceptionFromResult()).isNull();
    assertThat(state.results.get("planungseinheiten")).isNotNull();

    if (state.results.get("planungseinheiten") instanceof Set<?> result) {
      assertThat((result).size()).isEqualTo(splitPruefungen.size() + splitBloecke.size());
      for (ReadOnlyPlanungseinheit plan : (Set<ReadOnlyPlanungseinheit>) result) {
        if (plan.isBlock()) {
          blockCount++;
          assertThat(splitBloecke).contains(plan.asBlock().getName());
        } else {
          pruefungsCount++;
          assertThat(splitPruefungen).contains(plan.asPruefung().getName());
        }
      }
      assertThat(blockCount)
          .withFailMessage("Erwartete Anzahl Pruefungen {}, waren allerdings {}",
              splitBloecke.size(), blockCount).isEqualTo(splitBloecke.size());

      assertThat(pruefungsCount)
          .withFailMessage("Erwartete Anzahl Pruefungen {}, waren allerdings {}",
              splitPruefungen.size(), pruefungsCount).isEqualTo(splitPruefungen.size());
    } else {
      throw new AssertionError();
    }
  }

  @Dann("erhalte ich keine Planungseinheiten")
  public void dannErhalteIchKeinePlanungseinheiten() {
    assertThat(getExceptionFromResult()).isNull();
    assertThat(state.results.get("planungseinheiten")).isNotNull();
    assertThat((Set<ReadOnlyPlanungseinheit>) state.results.get("planungseinheiten")).isEmpty();
  }

  @Dann("erhalte ich die Pruefungen {string}")
  public void erhalteIchDiePruefungen(String pruefungen) {
    List<String> splitPruefungen = List.of(pruefungen.split(", "));
    assertThat(getExceptionFromResult()).isNull();
    assertThat(state.results.get("planungseinheiten")).isNotNull();

    if (state.results.get("planungseinheiten") instanceof Set<?> result) {
      for (ReadOnlyPlanungseinheit plan : (Set<ReadOnlyPlanungseinheit>) result) {
        assertThat(plan.isBlock()).isFalse();
        assertThat(splitPruefungen).contains(plan.asPruefung().getName());
      }
      assertThat(result.size()).isEqualTo(splitPruefungen.size());
    }
  }
}