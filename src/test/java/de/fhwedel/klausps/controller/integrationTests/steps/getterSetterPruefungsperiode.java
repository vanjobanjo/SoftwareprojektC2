package de.fhwedel.klausps.controller.integrationTests.steps;

import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.AssumptionViolatedException;


public class getterSetterPruefungsperiode {

  // ------------------------------------------------------------
  // ------------------------- allgemein ------------------------
  // ------------------------------------------------------------


  @Angenommen("es ist eine Pruefungsperiode geplant")
  public void esIstEinSemesterGeplant() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es ist keine Pruefungsperiode geplant")
  public void esIstKeinSemesterGeplant() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich einen Fehler")
  public void erhalteIchEinenFehler() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
  // ------------------------------------------------------------
  // ------------------ Start- und Enddatum ---------------------
  // ------------------------------------------------------------

  @Wenn("ich das Startdatum der Periode anfrage")
  public void ichDasStartdatumDerPeriodeAnfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich das Startdatum")
  public void erhalteIchDasStartdatum() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich das Enddatum der Periode anfrage")
  public void ichDasEnddatumDerPeriodeAnfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich das Enddatum")
  public void erhalteIchDasEnddatum() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich das {localDateTime} und das {localDateTime} der Periode aendere")
  public void ichDasStartdatumUndDasEnddatumDerPeriodeAendere(LocalDateTime start,
      LocalDateTime end) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("werden die Daten entsprechend geaendert")
  public void werdenDieDatenEntsprechendGeaendert() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  // ------------------------------------------------------------
  // ----------------------- Kapazität --------------------------
  // ------------------------------------------------------------


  @Wenn("ich die Kapazitaet anfrage")
  public void ichDieKapazitaetAnfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich die Kapazitaet")
  public void erhalteIchDieKapazitaet() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }


  @Wenn("ich die {int} aendere")
  public void ichDieKapazitaetAendere(int kapazitaet) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich die Kapazitaet auf den Wert {int} setze")
  public void ichDieKapazitaetAufSetze(int kapazitaet) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es ist eine Pruefungsperiode geplant und es sind Pruefungen geplant")
  public void semesterUndPruefungenGeplant() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }


  @Dann("erhalte ich eine Liste mit Klausuren deren Bewertung veraendert wurde")
  public void erhalteListeMitGeaenderterBewertung() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es ist eine Pruefungsperiode geplant und es sind keine Pruefungen geplant")
  public void semesterOhnePruefungenGeplant() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }


  @Dann("erhalte ich eine Liste ohne Klausuren")
  public void erhalteIchEineListeOhneKlausuren() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  // ------------------------------------------------------------
  // ------------------------- Semester -------------------------
  // ------------------------------------------------------------

  @Wenn("ich das Semester abfrage")
  public void ichDasSemesterAbfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn("ich das Semester von {int} {string} auf {int} {string} aendere")
  public void ichDasSemesterVonAltAufNeuAendere(int oldYear, String oldSemester,
      int newYear, String newSemester) {
  }

  @Wenn("ich das Semester auf {int} {string} aendere")
  public void ichDasSemesterAendere(int year, String semester) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("erhalte ich das Semester")
  public void erhalteIchDasSemester() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("ist am Ende das Semester {int} {string} eingetragen")
  public void istAmEndeDasNeueSemesterEingetragen(int year, String semester) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  // ------------------------------------------------------------
  // --------------------- Teilnehmerkreise ---------------------
  // ------------------------------------------------------------


  @Wenn("ich alle Teilnehmerkreise anfrage")
  public void ichAlleTeilnehmerkreiseAnfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es existieren die Teilnehmerkreise {listOfTeilnehmerKreis}")
  public void esExistierenDieTeilnehmerkreise(List<Teilnehmerkreis> teilnehmerkreis) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("es ist eine Pruefungsperiode geplant und es gibt keine Teilnehmerkreise")
  public void semesterGeplantKeinTeilnehmerkreis() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("bekomme ich eine leere Liste")
  public void bekommeIchEineLeereListe() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("bekomme ich die Teilnehmerkreise {string}")
  public void bekommeIchDieTeilnehmerkreise(String teilnehmerkreise) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

}
