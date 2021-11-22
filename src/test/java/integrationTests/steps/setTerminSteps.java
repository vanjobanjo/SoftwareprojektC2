package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class setTerminSteps {

  @Angenommen("die Prüfung <Pruefung> hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt noch keine Prüfungen")
  public void pruefungHatTerminAberKeineAnderePruefungVorhanden() {
  }

  @Wenn("ich den Termin von <Pruefung> auf den <neuer Termin> ändere")
  public void pruefungsTerminAEndern() {
  }

  @Dann("ist der Termin von <Pruefung> am <dann Termin>")
  public void istDerTerminVonPruefungAmDannTermin() {
  }

  @Dann("ist der Termin von <Pruefung> <dann Termin> und bekommt eine Fehlermeldung")
  public void terminAenderungMitFehlermeldung() {
  }


}
