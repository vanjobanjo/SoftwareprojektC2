package integrationTests.steps;

import static integrationTests.steps.BaseSteps.state;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
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

public class createBlockSteps {

  private static final String RESULT_BLOCK = "Block";
  private static final String RESULT_EXCEPTION = "Eine der übergebenen Prüfungen ist geplant.";

  @Wenn("ich einen Block mit den geplanten Pruefungen {string} und {string} erstelle")
  public void erstelleBlockMitPruefungen(String pruefung1, String pruefung2)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung rp1 = new PruefungDTOBuilder().withPruefungsNummer(pruefung1)
        .withPruefungsName(pruefung1).withStartZeitpunkt(LocalDateTime.MIN).build();
    ReadOnlyPruefung rp2 = new PruefungDTOBuilder().withPruefungsNummer(pruefung2)
        .withPruefungsName(pruefung2).withStartZeitpunkt(LocalDateTime.MIN).build();
    try {
      state.controller.createBlock("Hallo", Blocktyp.SEQUENTIAL, rp1, rp2);
    } catch (IllegalArgumentException e) {
      state.results.put(RESULT_EXCEPTION,
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
  public void esExistierenDieUngeplantenKlausurenUnd(String pruefung1, String pruefung2)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(pruefung1, pruefung1, pruefung1,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());

    state.controller.createPruefung(pruefung2, pruefung2, pruefung2,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());
  }

  @Angenommen("es existiert die geplante Pruefung {string} und die ungeplante Pruefung {string}")
  public void esExistiertDieGeplantePruefungUndDieUngeplantePruefung(String pruefung1,
      String pruefung2) throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung rp1 = state.controller.createPruefung(pruefung1, pruefung1, pruefung1,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());

    ReadOnlyPruefung rp2 = state.controller.createPruefung(pruefung2, pruefung2, pruefung2,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());
    LocalDate.of(2022, 1, 31);
    state.controller.schedulePruefung(rp1, LocalDate.of(2022, 1, 31).atTime(LocalTime.MAX));
  }

  @Angenommen("es existiert die ungeplante Pruefung {string}")
  public void esExistiertDieUngeplantePruefung(String pruefung1)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(pruefung1, pruefung1, pruefung1,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());
  }

  @Wenn("ich einen Block mit der Pruefung {string} erstelle")
  public void ichEinenBlockMitDerPruefungErstelle(String pruefung1)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung rp1 = new PruefungDTOBuilder().withPruefungsNummer(pruefung1)
        .withPruefungsName(pruefung1).build();
    ReadOnlyBlock result;
    result = state.controller.createBlock("Hallo", Blocktyp.SEQUENTIAL, rp1);
    state.results.put(RESULT_BLOCK, result);
  }

  @Dann("erhalte ich einen Block mit der Pruefung {string}")
  public void erhalteIchEinenBlockMitDerPruefung(String pruefung1) {
    ReadOnlyBlock result = (ReadOnlyBlock) state.results.get(RESULT_BLOCK);
    assertThat(result.getROPruefungen().stream()
        .anyMatch(pruefung -> pruefung.getName().equals(pruefung1))).isTrue();
  }

  @Angenommen("es existiert die geplante Pruefung {string}")
  public void esExistiertDieGeplantePruefung(String pruefung1)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung rp1 = state.controller.createPruefung(pruefung1, pruefung1, pruefung1,
        Collections.emptySet(),
        Duration.ofMinutes(120), Collections.emptyMap());
    LocalDate.of(2022, 1, 31);
    state.controller.schedulePruefung(rp1, LocalDate.of(2022, 1, 31).atTime(LocalTime.MAX));
  }

  @Dann("erhalte ich einen Block mit den Pruefungen {string} und {string}")
  public void erhalteIchEinenBlockMitDenPruefungenUnd(String pruefung1, String pruefung2) {
    ReadOnlyBlock result = (ReadOnlyBlock) state.results.get(RESULT_BLOCK);
    assertThat(result.getROPruefungen().stream()
        .anyMatch(pruefung -> pruefung.getName().equals(pruefung1))).isTrue();
    assertThat(result.getROPruefungen().stream()
        .anyMatch(pruefung -> pruefung.getName().equals(pruefung2))).isTrue();
  }

  @Dann("erhalte ich ein Fehlermeldung Pruefungen sind geplant")
  public void erhalteIchEinFehlermeldungPruefungenSindGeplant() {
    Object obj = state.results.get(RESULT_EXCEPTION);
    assertThat(obj).isInstanceOf(IllegalArgumentException.class);
  }

  @Wenn("ich einen Block mit den ungeplanten Pruefungen {string} und {string} erstelle")
  public void ichEinenBlockMitDenUngeplantenPruefungenUndErstelle(String pruefung1,
      String pruefung2) throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung rp1 = new PruefungDTOBuilder().withPruefungsNummer(pruefung1)
        .withPruefungsName(pruefung1).build();
    ReadOnlyPruefung rp2 = new PruefungDTOBuilder().withPruefungsNummer(pruefung2)
        .withPruefungsName(pruefung2).build();

    ReadOnlyBlock result;

    result = state.controller.createBlock("Hallo", Blocktyp.SEQUENTIAL, rp1, rp2);
    state.results.put(RESULT_BLOCK, result);
  }

  @Wenn("ich einen gemischten Block mit der geplanten Pruefung {string} und ungeplanten {string} erstelle")
  public void ichEinenGemischtenBlockMitDerGeplantenPruefungUndUngeplantenErstelle(String pruefung1,
      String pruefung2) {

    ReadOnlyPruefung rp1 = new PruefungDTOBuilder().withPruefungsNummer(pruefung1)
        .withPruefungsName(pruefung1).withStartZeitpunkt(LocalDateTime.MIN).build();
    ReadOnlyPruefung rp2 = new PruefungDTOBuilder().withPruefungsNummer(pruefung2)
        .withPruefungsName(pruefung2).build();
    try {
      state.controller.createBlock("Hallo", Blocktyp.SEQUENTIAL, rp1, rp2);
    } catch (IllegalArgumentException | NoPruefungsPeriodeDefinedException e) {
      state.results.put(RESULT_EXCEPTION,
          e);
    }
  }

  @Wenn("ich einen Block mit der geplanten Pruefung {string} erstelle")
  public void ichEinenBlockMitDerGeplantenPruefungErstelle(String pruefung1) {

    ReadOnlyPruefung rp1 = new PruefungDTOBuilder().withPruefungsNummer(pruefung1)
        .withPruefungsName(pruefung1).withStartZeitpunkt(LocalDateTime.MIN).build();
    try {
      state.controller.createBlock("Hallo", Blocktyp.SEQUENTIAL, rp1);
    } catch (IllegalArgumentException | NoPruefungsPeriodeDefinedException e) {
      state.results.put(RESULT_EXCEPTION,
          e);
    }
  }
}
