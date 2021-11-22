package de.fhwedel.klausps.controller.integrationTests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import org.junit.AssumptionViolatedException;

public class setTerminSteps {

  /**
   *  Wandelt ein String in Format dd.mm.yyyy in eine LocalDateTime mit der Uhrzeit 09:00
   * @param datum das Datum fuer die neue LocalDateTime
   * @return den String als LocalDateTime
   */
  @ParameterType("(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d)?")
  public LocalDateTime localDateTime(String datum){

    if(datum != null) {
      String[] split = datum.split("\\.");

      String str = split[2] + "-" + split[1] + "-" + split[0] + "T09:00:00";
      return LocalDateTime.parse(str);
    }else{
      return null;
    }
  }

  @Angenommen("die Pruefung {string} hat den Termin {localDateTime} und die Pruefungsperiode von {localDateTime} - {localDateTime} und es gibt noch keine Pruefungen")
  public void pruefungHatTerminAberKeineAnderePruefungVorhanden(String pruefung, LocalDateTime termin, LocalDateTime pruefStart, LocalDateTime pruefEnd) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich den Termin von {string} auf den {localDateTime} aendere")
  public void pruefungsTerminAEndern(String pruefung, LocalDateTime newTermin) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("ist der Termin von {string} am {localDateTime}")
  public void istDerTerminVonPruefungAmDannTermin(String pruefung, LocalDateTime resultTermin) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("ist der Termin von {string} {localDateTime} und bekommt eine Fehlermeldung")
  public void terminAenderungMitFehlermeldung(String pruefung, LocalDateTime resultTermin) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat den Termin {localDateTime} und die Pruefungsperiode von {localDateTime} - {localDateTime} und es gibt schon Pruefungen")
  public void pruefungHatTerminAberAnderePruefungVorhanden(String pruefung, LocalDateTime termin, LocalDateTime pruefStart, LocalDateTime pruefEnd, DataTable pruefungMitTerminen) {
  }
}
