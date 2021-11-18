package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class setTerminSteps {
    @Angenommen("die Prüfung {string} hat den Termin {double}{double} und die Prüfungsperiode von {double}{double} - {double}{double}")
    public void diePruefungHatDenTerminUndDiePruefungsperiodeVon(String arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9) {
    }

    @Wenn("ich den Termin von {string} auf den {double}{double} ändere")
    public void ichDenTerminVonAufDenAendere(String arg0, int arg1, int arg2, int arg3) {
    }

    @Dann("ist der Termin von {string} {double}{double}")
    public void istDerTerminVon(String arg0, int arg1, int arg2, int arg3) {
    }

    @Angenommen("die Prüfung {string} hat den Termin keinWert und die Prüfungsperiode von {double}{double} - {double}{double}")
    public void diePruefungHatDenTerminKeinWertUndDiePruefungsperiodeVon(String arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6) {
    }

    @Dann("ist der Termin von {string} keinWert")
    public void istDerTerminVonKeinWert(String arg0) {
    }
}
