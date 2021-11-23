package de.fhwedel.klausps.controller.integrationTests.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;

public class getGeplanteBloecke {

  @Und("es gibt die folgenden geplanten Bloecke:")
  public void esGibtDieBloecke(DataTable table) {

  }

  @Wenn("ich alle geplanten Bloecke anfrage")
  public void ichAlleGeplantenBloeckeAnfrage() {
  }

  @Dann("erhalte ich die Bloecke {string}")
  public void erhalteIchDieBloecke(String bloecke) {

  }

  @Dann("erhalte ich keine Bloecke")
  public void erhalteIchKeineBloecke() {
  }

  @Und("es gibt keine geplanten Bloecke")
  public void esGibtKeineGeplantenBloecke() {

  }
}
