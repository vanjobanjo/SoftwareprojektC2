package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;


public class periodeGetterSetterSteps {
    @Angenommen("Es ist kein Semester geplant")
    public void esIstKeinSemesterGeplant() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Wenn("ich das Startdatum der Periode anfrage")
    public void ichDasStartdatumDerPeriodeAnfrage() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Dann("Erhalte ich einen Fehler")
    public void erhalteIchEinenFehler() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Dann("erhalte ich das Startdatum")
    public void erhalteIchDasStartdatum() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Angenommen("Es ist ein Semester geplant")
    public void esIstEinSemesterGeplant() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Wenn("ich das Startdatum der Periode ändere")
    public void ichDasStartdatumDerPeriodeAendere() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Dann("wird das Startdatum auf das angegebene Datum geändert")
    public void wirdDasStartdatumAufDasAngegebeneDatumGeaendert() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Wenn("ich das Enddatum der Periode ändere")
    public void ichDasEnddatumDerPeriodeAendere() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    
    @Wenn("ich das {string} und das {string} der Periode aendere")
    public void ichDasStartdatumUndDasEnddatumDerPeriodeAendere(String start, String end) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Dann("werden die Daten entsprechend geaendert")
    public void werdenDieDatenEntsprechendGeaendert() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
    
    @Angenommen("es existieren folgende Teilnehmerkreise:")
    public void esExistierenFolgendeTeilnehmerkreise() {
    }
    
    @Wenn("ich alle Teilnehmerkreise anfrage")
    public void ichAlleTeilnehmerkreiseAnfrage() {
    }
    
    @Dann("bekomme ich die Teilnehmerkreise {string}")
    public void bekommeIchDieTeilnehmerkreise(String arg0) {
    }
}
