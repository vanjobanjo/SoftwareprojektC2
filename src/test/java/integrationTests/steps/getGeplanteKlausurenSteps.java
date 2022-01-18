package integrationTests.steps;

import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import io.cucumber.java.de.Wenn;

public class getGeplanteKlausurenSteps extends BaseSteps {

  @Wenn("ich alle geplanten Klausuren anfrage")
  public void ichAlleGeplantenKlausurenAnfrage() throws NoPruefungsPeriodeDefinedException {
    getState().results.put("pruefungen",
        getState().controller.getGeplantePruefungen());
  }

}
