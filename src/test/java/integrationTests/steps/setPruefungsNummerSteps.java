package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Set;

public class setPruefungsNummerSteps extends BaseSteps {

  @Angenommen("die Pruefung {string} hat die Nummer {string}")
  public void diePruefungHatDieNummer(String nameOfPruefung, String newPruefungsNummer)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(newPruefungsNummer, nameOfPruefung, newPruefungsNummer,
        emptySet(), Duration.ofMinutes(90), emptyMap());
  }

  @Wenn("ich die Nummer von {string} zu {string} ändere")
  public void ichDieNummerVonZuAendere(String nameOfPruefung, String newPruefungsNummer) {
    try {
      ReadOnlyPruefung pruefung = findPruefungWithName(nameOfPruefung);
      ReadOnlyPlanungseinheit result = state.controller.setPruefungsnummer(pruefung,
          newPruefungsNummer);
      state.results.put("planungseinheit", result);
    } catch (NoPruefungsPeriodeDefinedException | IllegalArgumentException | IllegalStateException e) {
      putExceptionInResult(e);
    }
  }

  @Dann("ist die Nummer von {string} {string}")
  public void istDieNummerVon(String nameOfPruefung, String pruefungsNummer) {
    ReadOnlyPruefung pruefung = findPruefungWithName(nameOfPruefung);

    assertThat(getExceptionFromResult()).isNull();
    assertThat(pruefung).isNotNull();
    assertThat(pruefung.getPruefungsnummer()).isEqualTo(pruefungsNummer);
  }

  @Angenommen("es existieren keine Pruefungen")
  public void esExistierenKeinePruefungen() {
  }

  @Wenn("ich versuche die Nummer einer Pruefung zu ändern")
  public void ichVersucheDieNummerEinerPruefungZuAendern() {
    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsNummer("123")
        .withPruefungsName("name").build();
    try {
      ReadOnlyPlanungseinheit result = state.controller.setPruefungsnummer(pruefung,
          "newPruefungsNummer");
      state.results.put("planungseinheit", result);
    } catch (NoPruefungsPeriodeDefinedException | IllegalStateException | IllegalArgumentException e) {
      putExceptionInResult(e);
    }

  }

  @Wenn("ich keine Pruefung nenne, dessen Nummer ich verändern möchte")
  public void ichKeinePruefungNenneDessenNummerIchVeraendernMoechte() {
    try {
      state.controller.setPruefungsnummer(null, "newNumber");
    } catch (NoPruefungsPeriodeDefinedException | IllegalStateException | IllegalArgumentException
        | NullPointerException e) {
      putExceptionInResult(e);
    }
  }

  @Wenn("ich die Nummer einer Pruefung ändere ohne eine neue Nummer anzugeben")
  public void ichDieNummerEinerPruefungAendereOhneEineNeueNummerAnzugeben() {
    try {
      state.controller.createPruefung("123", "pruefung", "ab1",
          emptySet(), Duration.ofMinutes(90), emptyMap());

    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("Unexpected Error, Pruefungsperiode should be defined", e);
    }
  }

  @Und("es existiert eine Pruefung mit der Nummer {string}")
  public void esExistiertEinePruefungMitDerNummer(String pruefungsNummer) {
    try {
      state.controller.createPruefung(pruefungsNummer, pruefungsNummer, pruefungsNummer,
          emptySet(), Duration.ofMinutes(120), emptyMap());
    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("Unexpected Error, Pruefungsperiode should be defined", e);
    }
  }

  @Und("die Nummer von {string} ist immer noch {string}")
  public void dieNummerVonIstImmerNoch(String nameOfPruefung, String pruefungsNummer) {
    ReadOnlyPruefung pruefung = findPruefungWithName(nameOfPruefung);

    assertThat(pruefung).isNotNull();
    assertThat(pruefung.getPruefungsnummer()).isEqualTo(pruefungsNummer);
  }


  @Wenn("ich die Nummer der Pruefung {string} aendere ohne eine neue Nummer anzugeben")
  public void ichDieNummerDerPruefungAendereOhneEineNeueNummerAnzugeben(String pruefungsName) {
    ReadOnlyPruefung pruefung = findPruefungWithName(pruefungsName);
    try {
      state.controller.setPruefungsnummer(pruefung, null);
    } catch (NoPruefungsPeriodeDefinedException | IllegalStateException | NullPointerException e) {
      putExceptionInResult(e);
    }
  }

  @Angenommen("die Pruefung {string} hat die Nummer {string} und ist im Block {string}")
  public void diePruefungHatDieNummerUndIstImBlock(String pruefungsName, String pruefungsNummer,
      String blockName) {
    try {
      ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungsNummer, pruefungsName,
          pruefungsNummer,
          emptySet(), Duration.ofMinutes(90), emptyMap());
      state.controller.createBlock(blockName, PARALLEL, pruefung);
    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("Unexpected Error, Pruefungsperiode should be defined", e);
    }

  }


  private Set<ReadOnlyPruefung> getAllPruefungen() throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> pruefungSet = state.controller.getUngeplantePruefungen();
    pruefungSet.addAll(state.controller.getGeplantePruefungen());
    return pruefungSet;
  }

  private ReadOnlyPruefung findPruefungWithName(String name) {
    try {
      Set<ReadOnlyPruefung> pruefungSet = getAllPruefungen();
      ReadOnlyPruefung pruefung = null;
      for (ReadOnlyPruefung p : pruefungSet) {
        if (p.getName().equals(name)) {
          pruefung = p;
          break;
        }
      }
      return pruefung;
    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("Unexpected Error, Pruefungsperiode should be defined", e);
    }
  }

  @Dann("erhalte ich eine Pruefung")
  public void erhalteIchEinePruefung() {
    assertThat(getExceptionFromResult()).isNull();
    assertThat(
        ((ReadOnlyPlanungseinheit) state.results.get("planungseinheit")).isBlock()).isFalse();
  }

  @Dann("erhalte ich einen Block {string} der die Pruefung {string} mit Nummer {string} enthaelt")
  public void erhalteIchEinenBlock(String blockName, String pruefungsName, String pruefungsNumber) {
    assertThat(getExceptionFromResult()).isNull();
    assertThat(((ReadOnlyPlanungseinheit) state.results.get("planungseinheit")).isBlock()).isTrue();
    assertThat(
        ((ReadOnlyPlanungseinheit) state.results.get("planungseinheit")).getName()).isEqualTo(
        blockName);
    ReadOnlyBlock block = (ReadOnlyBlock) state.results.get("planungseinheit");
    Set<ReadOnlyPruefung> pruefungen = block.getROPruefungen();
    int counter = 0;
    for (ReadOnlyPruefung pruefung : pruefungen) {
      if (pruefung.getName().equals(pruefungsName) && pruefung.getPruefungsnummer()
          .equals(pruefungsNumber)) {
        counter++;
      }
    }
    assertThat(counter).isEqualTo(1);
  }
}
