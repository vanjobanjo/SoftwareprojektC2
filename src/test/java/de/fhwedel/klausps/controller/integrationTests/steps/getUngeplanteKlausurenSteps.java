package de.fhwedel.klausps.controller.integrationTests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.junit.AssumptionViolatedException;

public class getUngeplanteKlausurenSteps extends BaseSteps {

    @Angenommen("es existieren die folgenden Klausuren:")
    public void esExistierenDieFolgendenKlausuren(DataTable dataTable) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("ich alle ungeplanten Klausuren anfrage")
    public void ichAlleUngeplantenKlausurenAnfrage() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("bekomme ich die Klausuren {string}")
    public void bekommeIchDieKlausuren(String klausuren) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("bekomme ich keine Klausuren")
    public void bekommeIchKeineKlausuren() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("es existieren keine Klausuren")
    public void esExistierenKeineKlausuren() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("bekomme ich den Block als Teil der ungeplanten Klausuren")
    public void bekommeIchDenBlockAlsTeilDerUngeplantenKlausuren(DataTable dataTable) {

    }
}
