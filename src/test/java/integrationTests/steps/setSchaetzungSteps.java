package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class setSchaetzungSteps {
    @io.cucumber.java.de.Angenommen("^die Prüfung \"([^\"]*)\" hat die Schätzung (\\d+)$")
    public void diePruefungHatDieSchaetzung(String arg0, int arg1)  {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }

    @io.cucumber.java.de.Wenn("^ich die Schätzung von \"([^\"]*)\" zu (\\d+) ändere$")
    public void ichDieSchaetzungVonZuAendere(String arg0, int arg1) {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }

    @io.cucumber.java.de.Dann("^ist die Schätzung von \"([^\"]*)\" (\\d+)$")
    public void istDieSchaetzungVon(String arg0, int arg1)  {
        // Write code here that turns the phrase above into concrete actions
           throw new AssumptionViolatedException("Not implemented yet!");
    }


    @Wenn("ich die Schätzung von {string} zu {int} ändere")
    public void ichDieSchaetzungVonZuAendere_minusWert(String arg0, int arg1) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
}
