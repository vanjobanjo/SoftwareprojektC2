package integrationTests.steps;

import static integrationTests.steps.BaseSteps.state;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import org.junit.AssumptionViolatedException;

public class createBlockSteps {

  @Wenn("ich einen Block mit den Pruefungen {string} und {string} erstelle")
  public void erstelleBlockMitPruefungen(String pruefung1, String pruefung2)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung rp1 = new PruefungDTOBuilder().withPruefungsNummer(pruefung1)
        .withPruefungsName(pruefung1).withStartZeitpunkt(LocalDateTime.MIN).build();
    ReadOnlyPruefung rp2 = new PruefungDTOBuilder().withPruefungsNummer(pruefung2)
        .withPruefungsName(pruefung2).withStartZeitpunkt(LocalDateTime.MIN).build();
    try {
      state.controller.createBlock("Hallo", Blocktyp.SEQUENTIAL, rp1, rp2);
    } catch (IllegalArgumentException e) {
      state.results.put("Eine der übergebenen Prüfungen ist geplant.",
          e);
    }
  }

  @Angenommen("es existieren die geplanten Pruefungen {string} und {string}")
  public void esExistierenDieGeplantenPruefungenUnd(String pruefung1, String pruefung2)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung rp1 = state.controller.createPruefung(pruefung1, pruefung1, pruefung1,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());

    ReadOnlyPruefung rp2 = state.controller.createPruefung(pruefung2, pruefung2, pruefung2,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());
    LocalDate.of(2022, 1, 31);
    state.controller.schedulePruefung(rp1, LocalDate.of(2022, 1, 31).atTime(LocalTime.MAX));
    state.controller.schedulePruefung(rp2, LocalDate.of(2022, 1, 31).atTime(LocalTime.MAX));
  }

  @Angenommen("es existieren die ungeplanten Klausuren {string} und {string}")
  public void esExistierenDieUngeplantenKlausurenUnd(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es existieren die ungeplanten Pruefungen {string} und {string}")
  public void esExistierenDieUngeplantenPruefungenUnd(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es existiert die geplante Pruefung {string} und die ungeplante Pruefung {string}")
  public void esExistiertDieGeplantePruefungUndDieUngeplantePruefung(String pruefung1,
      String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es existiert die ungeplante Pruefung {string}")
  public void esExistiertDieUngeplantePruefung(String pruefung) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Wenn("ich einen Block mit der Pruefung {string} erstelle")
  public void ichEinenBlockMitDerPruefungErstelle(String pruefung) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("erhalte ich einen Block mit der Pruefung {string}")
  public void erhalteIchEinenBlockMitDerPruefung(String pruefung) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es existiert die geplante Pruefung {string}")
  public void esExistiertDieGeplantePruefung(String pruefung) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("erhalte ich einen Block mit den Pruefungen {string} und {string}")
  public void erhalteIchEinenBlockMitDenPruefungenUnd(String pruefung1, String pruefung2) {
  }


  @Dann("erhalte ich ein Fehlermeldung Pruefungen sind geplant")
  public void erhalteIchEinFehlermeldungPruefungenSindGeplant() {
    Object obj = state.results.get("Eine der übergebenen Prüfungen ist geplant.");
    assertThat(obj).isInstanceOf(IllegalArgumentException.class);
  }
}
