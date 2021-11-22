package integrationTests.steps;


import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class setSchaetzungSteps {
    @Angenommen("^die Prüfung \"([^\"]*)\" hat die Schätzung (\\d+)$")
    public void diePruefungHatDieSchaetzung(String arg0, int arg1)  {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("^ich die Schätzung von \"([^\"]*)\" zu (\\d+) ändere$")
    public void ichDieSchaetzungVonZuAendere(String arg0, int arg1) {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }


    @Dann("ist die Schätzung von {string} den Wert {int}")
    public void istDieSchaetzungVonDenWert(String arg0, int arg1) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("die Prüfung {string} noch keine Schätzung")
    public void diePruefungNochKeineSchaetzung(String arg0) {
    }
}
