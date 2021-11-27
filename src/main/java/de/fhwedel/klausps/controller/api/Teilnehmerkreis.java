package de.fhwedel.klausps.controller.api;

public class Teilnehmerkreis implements de.fhwedel.klausps.model.api.Teilnehmerkreis {

  private int fachsemster;
  private String studiengang;
  private String pruefungsordnung;


  Teilnehmerkreis(String studiengang, String pruefungsordung, int fachsemster) {
    this.studiengang = studiengang;
    this.pruefungsordnung = pruefungsordung;
    this.fachsemster = fachsemster;
  }

  @Override
  public String getStudiengang() {
    return this.studiengang;
  }

  @Override
  public String getPruefungsordnung() {
    return this.pruefungsordnung;
  }

  @Override
  public int getFachsemester() {
    return this.fachsemster;
  }

  public void setFachsemster(int newFachsemster) {
    this.fachsemster = newFachsemster;
  }

  public void setStudiengang(String newStudiengang) {
    this.studiengang = newStudiengang;
  }

  public void setPruefungsordnung(String newPruefungsOrdnung) {
    this.pruefungsordnung = newPruefungsOrdnung;
  }

  public void setOrdungSemster(String newPruefungsOrdung, int newFachsemster) {
    this.pruefungsordnung = newPruefungsOrdung;
    this.fachsemster = newFachsemster;
  }
}
