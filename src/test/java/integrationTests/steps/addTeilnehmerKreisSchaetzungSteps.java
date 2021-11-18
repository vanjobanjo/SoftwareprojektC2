package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class addTeilnehmerKreisSchaetzungSteps {

    @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {string}")
    public void diePruefungHatAlsTeilnehmerkreisschaetzung(String pruefung, String teilnehmerKreisSchatzung) {

    }

    @Wenn("ich den Studiengang {string} Fachsemester {int} mit Ordnung {string} und {int} schaetze und hinzufuege")
    public void ichDenStudiengangFachsemesterMitOrdnungUndSchaetzeUndHinzufuege(String studiengang, int sememster, String ordnung, int schaetzung) {
    }


    @Dann("hat die Pruefung {string} die Teilnehmerkreischaetzungen: {string}")
    public void hatDiePruefungDieTeilnehmerkreischaetzungen(String pruefung, String teilnehmerKreisSchaetzung) {
    }

}
