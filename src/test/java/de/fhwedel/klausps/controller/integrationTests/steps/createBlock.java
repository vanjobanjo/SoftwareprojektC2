package de.fhwedel.klausps.controller.integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class createBlock {

  @Wenn("ich einen Block mit den Pruefungen {string} und {string} erstelle")
  public void erstelleBlockMitPruefungen(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }


  @Angenommen("es existieren die geplanten Klausuren {string} und {string}")
  public void esExistierenDieGeplantenKlausurenUnd(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }


  @Angenommen("es existieren die geplanten Pruefungen {string} und {string}")
  public void esExistierenDieGeplantenPruefungenUnd(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
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
}
