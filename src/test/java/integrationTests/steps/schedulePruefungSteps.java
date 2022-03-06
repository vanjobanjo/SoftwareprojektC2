package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class schedulePruefungSteps extends BaseSteps {


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

  @Und("es existiert die ungeplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester")
  public void esExistiertDieUngeplantePruefungMitDemTeilnehmerkreis(String pruefung,
      String teilnehmerkreis, int semester) throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung, teilnehmerkreis, semester);
  }

  @Und("es existiert die geplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester am {localDateTime}")
  public void esExistiertDieGeplantePruefungMitDemTeilnehmerkreisImSemesterAm(String pruefung,
      String teilnehmerkreisString,
      int semester, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreisString, semester);
    state.controller.schedulePruefung(roPruefung, termin);
  }


  @Dann("bekomme ich eine Fehlermeldung HartesKriteriumException")
  public void bekommeIchEineFehlermeldungHartesKriteriumException() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(HartesKriteriumException.class);
  }

  @Und("es existiert die geplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester am {localDateTime} um {int} Uhr")
  public void esExistiertDieGeplantePruefungMitDemTeilnehmerkreisImSemesterAmUm(String pruefung,
      String teilnehmerkreis, int semester, LocalDateTime termin, int time)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LocalDateTime newTermin = changeTime(termin, time);
    esExistiertDieGeplantePruefungMitDemTeilnehmerkreisImSemesterAm(pruefung, teilnehmerkreis,
        semester, newTermin);
  }


  /**
   * Hilfsmethode um aus einem Termin und einer Uhrzeit einen neuen Termin mit der neuen Uhrzeit zu
   * machen
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
    List<KriteriumsAnalyse> listWithWeicheKriterien = state.controller.analyseScoring(
        getPruefungFromModel(pruefung));
    assertThat(listWithWeicheKriterien).isNotEmpty();
    boolean finde = false;
    Iterator<KriteriumsAnalyse> it = listWithWeicheKriterien.iterator();
    KriteriumsAnalyse kA;
    while (!finde && it.hasNext()) {
      kA = it.next();
      if (kA.getKriterium().equals(WeichesKriterium.valueOf(kriterium))) {
        finde = true;
      }
    }
    assertTrue(finde);

  }

  @Und("die Pruefung {string} hat nicht das WeicheKriterium {string}")
  public void diePruefungHatNichtDasWeicheKriterium(String pruefung, String kriterium)
      throws NoPruefungsPeriodeDefinedException {
    List<KriteriumsAnalyse> listWithWeicheKriterien = state.controller.analyseScoring(
        getPruefungFromModel(pruefung));
    boolean finde = false;
    Iterator<KriteriumsAnalyse> it = listWithWeicheKriterien.iterator();
    KriteriumsAnalyse kA;
    while (!finde && it.hasNext()) {
      kA = it.next();
      if (kA.getKriterium().equals(WeichesKriterium.valueOf(kriterium))) {
        finde = true;
      }
    }
    assertFalse(finde);

  }

  @Und("es existiert die ungeplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester {string} und {int} Teilnehmer")
  public void esExistiertDieUngeplantePruefungMitDemTeilnehmerkreisImSemesterUndTeilnehmer(
      String pruefung, String teilnehmerkreis, int Semester, String ausbildungsGrad, int anzahl)
      throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung, teilnehmerkreis, Semester, ausbildungsGrad, anzahl);

  }

  @Und("es existiert die geplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester und {int} Teilnehmer am {localDateTime} um {int} Uhr")
  public void esExistiertDieGeplantePruefungMitDemTeilnehmerkreisImSemesterUndTeilnehmerAmUmUhr(
      String pruefung, String teilnehmerkreis, int Semester, int count, LocalDateTime termin,
      int time)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {

    LocalDateTime newTermin = changeTime(termin, time);
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreis, Semester, "BACHELOR",
        count);
    state.controller.schedulePruefung(roPruefung, newTermin);
  }

  @Und("es existiert die ungeplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester und Dauer von {int} Minuten")
  public void esExistiertDieUngeplantePruefungMitDemTeilnehmerkreisImSemesterUndDauerVonMinuten(
      String pruefung, String teilnehmerkreis, int Semester, int dauer)
      throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung, teilnehmerkreis, Semester, dauer);
  }

  @Und("es existiert die geplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester und Dauer von {int} Minuten am {localDateTime}")
  public void esExistiertDieGeplantePruefungMitDemTeilnehmerkreisImSemesterUndDauerVonMinutenAm(
      String pruefung, String teilnehmerkreis, int Semester, int dauer, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getOrCreate(pruefung, teilnehmerkreis, Semester, dauer);
    state.controller.schedulePruefung(roPruefung, termin);
  }

  @Und("ist die Dauer der Pruefung {string} {int} Minuten")
  public void istDieDauerDerPruefungMinuten(String pruefung, int duration)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    assertThat(roPruefung.getDauer()).hasMinutes(duration);
  }

  @Und("es existiert die ungeplante Pruefung {string} mit dem Teilnehmerkreis {string} und {string} im {int} Semester")
  public void esExistiertDieUngeplantePruefungMitDemTeilnehmerkreisUndImSemester(String pruefung,
      String teilnehmerkreis1, String teilnehmerkreis2, int semester)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    getOrCreate(pruefung, teilnehmerkreis1, semester);
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
        Ausbildungsgrad.BACHELOR, teilnehmerkreis2, teilnehmerkreis2, semester);

    state.controller.addTeilnehmerkreis(roPruefung, teilnehmerkreis, 10);
  }


  @Wenn("ich {string} am {localDateTime} einplanen moechte bugtest")
  public void ichAmUmUhrEinplanenMoechteBugtest(String pruefung, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    try {
      state.results.put("bugFix",state.controller.schedulePruefung(roPruefung, termin));
    } catch (HartesKriteriumException e) {
      state.results.put("exception", e);
    }
  }

  @Und("die Pruefung {string} hat nicht das WeicheKriterium {string} bugix")
  public void diePruefungHatNichtDasWeicheKriteriumBugix(String pruefung, String kriterium)
      throws NoPruefungsPeriodeDefinedException {
    List<KriteriumsAnalyse> listWithWeicheKriterien = state.controller.analyseScoring(
        getPruefungFromModel(pruefung));



    Object exception = state.results.get("bugFix");
    assertThat(exception).isNotNull();
    List<ReadOnlyPlanungseinheit> newList = (List<ReadOnlyPlanungseinheit>) exception;

    assertThat(newList.contains(getPruefungFromModel(pruefung))).isTrue();


    boolean finde = false;
    Iterator<KriteriumsAnalyse> it = listWithWeicheKriterien.iterator();
    KriteriumsAnalyse kA;
    while (!finde && it.hasNext()) {
      kA = it.next();
      if (kA.getKriterium().equals(WeichesKriterium.valueOf(kriterium))) {
        finde = true;
      }
    }
    assertFalse(finde);

  }
}
