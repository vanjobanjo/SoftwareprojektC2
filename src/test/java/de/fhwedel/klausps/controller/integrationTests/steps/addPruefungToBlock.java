package de.fhwedel.klausps.controller.integrationTests.steps;


import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class addPruefungToBlock {

  @Angenommen("es existiert der leere Block {string}")
  public void esExistiertDerLeereBlock(String block) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Wenn("ich die Pruefung {string} hinzufuege")
  public void ichDiePruefungHinzufuege(String pruefung) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("erhalte ich einen Block der die Pruefung {string} enthaelt")
  public void erhalteIchEinenBlockDerDiePruefungEnthaelt(String Pruefung) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es existiert der Block {string} mit der Pruefung {string}")
  public void esExistiertDerBlockMitDerPruefung(String block, String string) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("erhalte ich einen Block mit den Pruefungen {string} und {string}")
  public void erhalteIchEinenBlockMitDenPruefungenUnd(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es gibt am selben Tag einen geplanten Block {string} und die geplante Pruefung {string}")
  public void esGibtAmSelbenTagEinenGeplantenBlockUndDieGeplantePruefung(String block,
      String klausur) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Wenn("ich die Pruefung {string} zum Block {string} hinzufuege")
  public void ichDiePruefungZuHinzufuege(String pruefung, String block) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("erhalte ich eine Fehlermeldung")
  public void erhalteIchEineFehlermeldung() {
    throw new AssumptionViolatedException("not implemented");
  }


  @Angenommen("es existiert keine Pruefungsperiode")
  public void esExistiertKeinePruefungsperiode() {
    throw new AssumptionViolatedException("not implemented");
  }

}
