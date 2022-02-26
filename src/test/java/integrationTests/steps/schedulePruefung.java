package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

public class schedulePruefung extends BaseSteps {


  @Wenn("ich {string} am {localDateTime} einplanen moechte")
  public void ichAmEinplanenMoechte(String pruefung, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    try {
      state.controller.schedulePruefung(roPruefung, termin);
    } catch (HartesKriteriumException e) {
      state.results.put("exception", e);
    }
  }

  @Dann("ist die Pruefung {string} am {localDateTime} eingeplant")
  public void istDiePruefungAmEingeplant(String pruefung, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);

    assertThat(roPruefung.geplant()).isTrue();
    assertThat(roPruefung.getTermin()).isNotEmpty();
    assertThat(roPruefung.getTermin()).get().isEqualTo(termin);

  }

  @Und("es existiert die ungeplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster")
  public void esExistiertDieUngeplantePruefungMitDenTeilnehmerkreis(String pruefung,
      String teilnehmerkreis, int semster) throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreis, semster);

  }

  @Und("es existiert die geplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster am {localDateTime}")
  public void esExistiertDieGeplantePruefungMitDenTeilnehmerkreisImSemsterAm(String pruefung,
      String teilnehmerkeisString,
      int semster, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkeisString, semster);
    state.controller.schedulePruefung(roPruefung, termin);
  }


  @Dann("bekomme ich eine Fehlermeldung HartesKriteriumException")
  public void bekommeIchEineFehlermeldungHartesKriteriumException() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(HartesKriteriumException.class);
  }

  @Und("es existiert die geplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster am {localDateTime} um {int} Uhr")
  public void esExistiertDieGeplantePruefungMitDenTeilnehmerkreisImSemsterAmUm(String pruefung,
      String teilnehmerkreis, int semster, LocalDateTime termin, int time)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LocalDateTime newTermin = changeTime(termin, time);
    esExistiertDieGeplantePruefungMitDenTeilnehmerkreisImSemsterAm(pruefung, teilnehmerkreis,
        semster, newTermin);
  }


  /**
   * Hilfsmethode um aus einen Termin und einer Uhrzeit einen neuen Termin mit der neuen Uhrzeit zz
   * machenb
   *
   * @param termin Jahr, monat tag des neuen termins
   * @param time   Uhrzeit des neuen termins (als Stundenzahl)
   * @return Jahr, Monat, Tag und Uhrzeit
   */
  private LocalDateTime changeTime(LocalDateTime termin, int time) {
    return LocalDateTime.of(termin.getYear(), termin.getMonth(),
        termin.getDayOfMonth(), time, 0);
  }

  @Wenn("ich {string} am {localDateTime} um {int} Uhr einplanen moechte")
  public void ichAmUmUhrEinplanenMoechte(String pruefung, LocalDateTime termin, int time)
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime newTermin = changeTime(termin, time);
    ichAmEinplanenMoechte(pruefung, newTermin);
  }


  @Dann("ist die Pruefung {string} am {localDateTime} um {int} Uhr eingeplant")
  public void istDiePruefungAmUmUhrEingeplant(String pruefung, LocalDateTime termin, int time)
      throws NoPruefungsPeriodeDefinedException {

    LocalDateTime newTermin = changeTime(termin, time);
    istDiePruefungAmEingeplant(pruefung, newTermin);
  }

  @Und("die Pruefung {string} hat das WeicheKriterium {string}")
  public void diePruefungHatDasWeicheKriterium(String pruefung, String kriterium)
      throws NoPruefungsPeriodeDefinedException {
    List<KriteriumsAnalyse> listWithWeicheKriteren = state.controller.analyseScoring(
        getPruefungFromModel(pruefung));
    assertThat(listWithWeicheKriteren).isNotEmpty();
    boolean finde = false;
    Iterator<KriteriumsAnalyse> it = listWithWeicheKriteren.iterator();
    KriteriumsAnalyse kA;
    while(!finde  && it.hasNext()){
        kA= it.next();
      if(kA.getKriterium().equals(WeichesKriterium.valueOf(kriterium))){
        finde = true;
      }
    }
    assertTrue(finde);

  }

  @Und("die Pruefung {string} hat nicht das WeicheKriterium {string}")
  public void diePruefungHatNichtDasWeicheKriterium(String pruefung, String kriterium)
      throws NoPruefungsPeriodeDefinedException {
    List<KriteriumsAnalyse> listWithWeicheKriteren = state.controller.analyseScoring(
        getPruefungFromModel(pruefung));
    boolean finde = false;
    Iterator<KriteriumsAnalyse> it = listWithWeicheKriteren.iterator();
    KriteriumsAnalyse kA;
    while(!finde  && it.hasNext()){
      kA= it.next();
      if(kA.getKriterium().equals(WeichesKriterium.valueOf(kriterium))){
        finde = true;
      }
    }
    assertFalse(finde);

  }

  @Und("es existiert die ungeplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster {string} und {int} Teilnehmer")
  public void esExistiertDieUngeplantePruefungMitDenTeilnehmerkreisImSemsterUndTeilnehmer(
      String pruefung, String teilnehmerkreis, int semster,String ausbildunggrade,  int anzahl)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreis, semster,ausbildunggrade, anzahl);

  }

  @Und("es existiert die geplante Pruefung {string} mit den Teilnehmerkreis {string} im {int} semster und {int} Teilnehmer am {localDateTime} um {int} Uhr")
  public void esExistiertDieGeplantePruefungMitDenTeilnehmerkreisImSemsterUndTeilnehmerAmUmUhr(
      String pruefung, String teilnehmerkreis, int semster, int count , LocalDateTime termin,  int time)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {

    LocalDateTime newTermin = changeTime(termin, time);
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreis, semster, "BACHELOR", count);
    state.controller.schedulePruefung(roPruefung,newTermin);
  }
}
