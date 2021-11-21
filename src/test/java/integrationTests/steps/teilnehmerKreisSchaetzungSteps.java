package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;

public class teilnehmerKreisSchaetzungSteps {

    @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {string}")
    public void diePruefungHatAlsTeilnehmerkreisschaetzung(String pruefung, String teilnehmerKreisSchatzung) {

    }

    @Wenn("ich den Studiengang {string} Fachsemester {int} mit Ordnung {string} und {int} schaetze und in {string} hinzufuege")
    public void teilnehmerKreisInKlausurHinzufuegen(
            String studiengang, int sememster, String ordnung, int schaetzung, String klausur) {
    }


    @Dann("hat die Pruefung {string} die Teilnehmerkreischaetzungen: {string}")
    public void pruefungHatTeilnehmerkreis(String pruefung, String teilnehmerKreisSchaetzung) {
    }

    @Dann("werfe IllegalArgumentException")
    public void werfeIllegalArgumentException() {
    }

    @Und("es hat Auswirkungen auf das Scoring von {string}")
    public void veraendertesScoring(String klausur) {

    }
}
