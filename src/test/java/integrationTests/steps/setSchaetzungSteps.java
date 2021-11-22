package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class setSchaetzungSteps {
  @Angenommen("^die Prüfung \"([^\"]*)\" hat die Schaetzung (\\d+)$")
  public void diePruefungHatDieSchaetzung(String pruefung, int schaetzung) {
    // Write code here that turns the phrase above into concrete actions
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("^ich die Schaetzung von \"([^\"]*)\" zu (\\d+) aendere$")
  public void ichDieSchaetzungVonZuAendere(String pruefung, int schaetzung) {
    // Write code here that turns the phrase above into concrete actions
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich die Schaetzung von {string} zu {int} aendere")
  public void ichDieSchaetzungVonZuAendere_minusWert(String pruefung, int schaetzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("ist die Schaetzung von {string} den Wert {int}")
  public void istDieSchaetzungVonDenWert(String pruefung, int schaetzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Prüfung {string} noch keine Schaetzung")
  public void diePruefungNochKeineSchaetzung(String pruefung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {string}, {string}")
  public void diePruefungHatAlsTeilnehmerkreisschaetzungB_BWLB_WING(
      String pruefung, String hoererKreis, String hoererKreis2) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Und("es hat Auswirkungen auf das Scoring von {string}")
  public void esHatAuswirkungenAufDasScoringVon(String arg0) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {string}")
  public void diePruefungHatAlsTeilnehmerkreisschaetzung(String pruefung, String teilnehmerKreis) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn(
      "ich den Studiengang {string} Fachsemester {int} mit Ordnung {string} und {int} schaetze und in {string} hinzufuege")
  public void ichDenStudiengangFachsemesterMitOrdnungUndSchaetzeUndInHinzufuege(
      String studiengang, int fachSemester, String stdienOrdnung, int schaetzung, String pruefung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
