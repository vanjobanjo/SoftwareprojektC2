package de.fhwedel.klausps.controller.integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;

public class setPruefungsNummerSteps {

  @Angenommen("die Prüfung {string} hat die Nummer {string}")
  public void diePruefungHatDieNummer(String nameOfPruefung, String newPruefungsNummer) {
  }

  @Wenn("ich die Nummer von {string} zu {string} ändere")
  public void ichDieNummerVonZuAEndere(String nameOfPruefung, String newPruefungsNummer) {
  }

  @Dann("ist die Nummer von {string} {string}")
  public void istDieNummerVon(String nameOfPruefung, String pruefungsNummer) {
  }

  @Angenommen("es existieren keine Prüfungen")
  public void esExistierenKeinePruefungen() {
  }

  @Wenn("ich versuche die Nummer einer Prüfung zu ändern")
  public void ichVersucheDieNummerEinerPruefungZuAEndern() {
  }

  @Dann("bekomme ich eine Fehlermeldung")
  public void bekommeIchEineFehlermeldung() {
  }

  @Wenn("ich keine Prüfung nenne, dessen Nummer ich verändern möchte")
  public void ichKeinePruefungNenneDessenNummerIchVeraendernMoechte() {
  }

  @Wenn("ich die Nummer einer Prüfung ändere ohne eine neue Nummer anzugeben")
  public void ichDieNummerEinerPruefungAEndereOhneEineNeueNummerAnzugeben() {
  }

  @Und("es existiert eine Prüfung mit der Nummer {string}")
  public void esExistiertEinePruefungMitDerNummer(String pruefungsNummer) {
  }

  @Und("die Nummer von {string} ist immernoch {string}")
  public void dieNummerVonIstImmernoch(String nameOfPruefung, String pruefungsNummer) {
  }
}
