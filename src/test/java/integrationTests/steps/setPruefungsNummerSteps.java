package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class setPruefungsNummerSteps {

  @Angenommen("die Prüfung {string} hat die Nummer {string}")
  public void diePruefungHatDieNummer(String nameOfPruefung, String newPruefungsNummer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich die Nummer von {string} zu {string} ändere")
  public void ichDieNummerVonZuAEndere(String nameOfPruefung, String newPruefungsNummer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("ist die Nummer von {string} {string}")
  public void istDieNummerVon(String nameOfPruefung, String pruefungsNummer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existieren keine Prüfungen")
  public void esExistierenKeinePruefungen() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich versuche die Nummer einer Prüfung zu ändern")
  public void ichVersucheDieNummerEinerPruefungZuAEndern() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("bekomme ich eine Fehlermeldung")
  public void bekommeIchEineFehlermeldung() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich keine Prüfung nenne, dessen Nummer ich verändern möchte")
  public void ichKeinePruefungNenneDessenNummerIchVeraendernMoechte() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich die Nummer einer Prüfung ändere ohne eine neue Nummer anzugeben")
  public void ichDieNummerEinerPruefungAEndereOhneEineNeueNummerAnzugeben() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Und("es existiert eine Prüfung mit der Nummer {string}")
  public void esExistiertEinePruefungMitDerNummer(String pruefungsNummer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Und("die Nummer von {string} ist immernoch {string}")
  public void dieNummerVonIstImmernoch(String nameOfPruefung, String pruefungsNummer) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
