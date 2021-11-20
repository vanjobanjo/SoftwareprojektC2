package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class teilnehmerKreisSchaetzungSteps {

    @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {string}")
    public void diePruefungHatAlsTeilnehmerkreisschaetzung(String pruefung, String teilnehmerKreisSchatzung) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("ich den Studiengang {string} Fachsemester {int} mit Ordnung {string} und {int} schaetze und hinzufuege")
    public void ichDenStudiengangFachsemesterMitOrdnungUndSchaetzeUndHinzufuege(String studiengang, int sememster, String ordnung, int schaetzung) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }


    @Dann("hat die Pruefung {string} die Teilnehmerkreischaetzungen: {string}")
    public void hatDiePruefungDieTeilnehmerkreischaetzungen(String pruefung, String teilnehmerKreisSchaetzung) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("werfe IllegalArgumentException")
    public void werfeIllegalArgumentException() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
}
