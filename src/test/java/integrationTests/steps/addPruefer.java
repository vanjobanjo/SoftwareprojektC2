package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class addPruefer {
    @Angenommen("die Prüfung {string} hat keinen Prüfer als Prüfer")
    public void diePruefungHatKeinenPrueferAlsPruefer(String arg0) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("ich der Prüfung {string} den Prüfer {string} hinzufüge")
    public void ichDerPruefungDenPrueferHinzufuege(String arg0, String arg1) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("hat die Prüfung {string} den Prüfer {string} eingetragen")
    public void hatDiePruefungDenPrueferEingetragen(String arg0, String arg1) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("die Prüfung {string} hat {string} als Prüfer")
    public void diePruefungHatAlsPruefer(String arg0, String arg1) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("hat die Prüfung {string} den Prüfer {string} und {string} eingetragen")
    public void hatDiePruefungDenPrueferUndEingetragen(String arg0, String arg1, String arg2) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("die Prüfung {string} hat {string} und {string} als Prüfer")
    public void diePruefungHatUndAlsPruefer(String arg0, String arg1, String arg2) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("hat die Prüfung {string} den Prüfer {string} und {string} und {string} eingetragen")
    public void hatDiePruefungDenPrueferUndUndEingetragen(String arg0, String arg1, String arg2, String arg3) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
}
