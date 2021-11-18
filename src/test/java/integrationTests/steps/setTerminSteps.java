package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class setTerminSteps {

  @Angenommen("die Prüfung {string} hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt noch keine Prüfungen")
  public void diePruefungPruefungHatDenTerminAktuellerTerminUndDiePruefungsperiodeVonPruefungsperiodenstartPruefungsperiodenendeUndEsGibtNochKeinePruefungen() {
  }

  @Wenn("ich den Termin von {string} auf den <neuer Termin> ändere")
  public void ichDenTerminVonPruefungAufDenNeuerTerminAEndere() {
  }

  @Dann("ist der Termin von {string} am <dann Termin>")
  public void istDerTerminVonPruefungAmDannTermin() {
  }

  @Dann("ist der Termin von {string} <dann Termin> und bekommt eine Fehlermeldung")
  public void istDerTerminVonPruefungDannTerminUndBekommtEineFehlermeldung() {
  }

  @Angenommen("die Prüfung {string} hat den Termin <aktueller Termin> und die Prüfungsperiode von <Pruefungsperiodenstart> - <Pruefungsperiodenende> und es gibt schon Prüfungen")
  public void diePruefungPruefungHatDenTerminAktuellerTerminUndDiePruefungsperiodeVonPruefungsperiodenstartPruefungsperiodenendeUndEsGibtSchonPruefungen() {
  }
}
