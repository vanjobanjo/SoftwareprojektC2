package integrationTests.steps;


import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class setSchaetzungSteps {
    @Angenommen("^die Prüfung \"([^\"]*)\" hat die Schätzung (\\d+)$")
    public void diePruefungHatDieSchaetzung(String pruefung, int schaetzung)  {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("^ich die Schätzung von \"([^\"]*)\" zu (\\d+) ändere$")
    public void ichDieSchaetzungVonZuAendere(String pruefung, int schaetzung) {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("ich die Schätzung von {string} zu {int} ändere")
    public void ichDieSchaetzungVonZuAendere_minusWert(String pruefung, int schaetzung) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("ist die Schätzung von {string} den Wert {int}")
    public void istDieSchaetzungVonDenWert(String pruefung, int schaetzung) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("die Prüfung {string} noch keine Schätzung")
    public void diePruefungNochKeineSchaetzung(String pruefung) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
}
