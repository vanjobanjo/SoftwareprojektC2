package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import integrationTests.state.State;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

public class setTerminSteps extends BaseSteps {

  /**
   * Wandelt ein String in Format dd.mm.yyyy in eine LocalDateTime mit der Uhrzeit 09:00
   *
   * @param datum das Datum fuer die neue LocalDateTime
   * @return den String als LocalDateTime
   */
  @ParameterType("(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d)?")
  public LocalDateTime localDateTime(String datum) {

    if (datum != null) {
      String[] split = datum.split("\\.");

      String str = split[2] + "-" + split[1] + "-" + split[0] + "T09:00:00";
      return LocalDateTime.parse(str);
    } else {
      return null;
    }
  }

  @Angenommen("die Pruefung {string} hat den Termin {localDateTime} und die Pruefungsperiode von {localDateTime} - {localDateTime} und es gibt noch keine Pruefungen")
  public void pruefungHatTerminAberKeineAnderePruefungVorhanden(String pruefung,
      LocalDateTime termin, LocalDateTime pruefStart, LocalDateTime pruefEnd)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung oldPruefung = getOrCreate(pruefung);

    if (termin != null) {
      state.controller.schedulePruefung(oldPruefung, termin);
    }

  }

  @Wenn("ich den Termin von {string} auf den {localDateTime} aendere")
  public void pruefungsTerminAEndern(String pruefung, LocalDateTime newTermin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung readPruefung = getPruefungFromModel(pruefung);
    try {
      state.controller.schedulePruefung(readPruefung, newTermin);
    } catch (IllegalArgumentException | HartesKriteriumException e) {
      state.results.put("exception", e);
    }

  }

  @Dann("ist der Termin von {string} am {localDateTime}")
  public void istDerTerminVonPruefungAmDannTermin(String pruefung, LocalDateTime resultTermin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung shouldresult = getPruefungFromModel(pruefung);

    assertThat(shouldresult.getTermin()).contains(resultTermin);

  }

  @Dann("ist der Termin von {string} {localDateTime} und bekommt eine Fehlermeldung")
  public void terminAenderungMitFehlermeldung(String pruefung, LocalDateTime resultTermin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung r = getOrCreate(pruefung);
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(IllegalArgumentException.class);
  }

  @Angenommen("die Pruefung {string} hat den Termin {localDateTime} und die Pruefungsperiode von {localDateTime} - {localDateTime} und es gibt schon Pruefungen")
  public void pruefungHatTerminAberAnderePruefungVorhanden(String pruefung, LocalDateTime termin,
      LocalDateTime pruefStart, LocalDateTime pruefEnd, DataTable pruefungMitTerminen)
      throws IllegalTimeSpanException, HartesKriteriumException, NoPruefungsPeriodeDefinedException {

    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(pruefStart.getYear()));
    state.controller.createEmptyPeriode(semester, pruefStart.toLocalDate(), pruefEnd.toLocalDate(),
        pruefStart.toLocalDate(), 400);
    schedulePruefungen(pruefungMitTerminen);

    ReadOnlyPruefung r = getOrCreate(pruefung);

    if (termin != null) {
      state.controller.schedulePruefung(r, termin);
    }

  }

  private void schedulePruefungen(DataTable pruefungMitTerminen)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    for (List<String> l : pruefungMitTerminen.cells()) {
      assertThat(l).hasSize(2);
      if (!l.get(1).equals("Termin")) {
        ReadOnlyPruefung r = getOrCreate(l.get(0));
        Teilnehmerkreis t = new TeilnehmerkreisImpl("Informatik", "1", 1, Ausbildungsgrad.BACHELOR);
        state.controller.addTeilnehmerkreis(r, t, 5);
        state.controller.schedulePruefung(r, localDateTime((l.get(1))));
      }
    }
  }


  @Dann("ist der Termin von {string} {localDateTime} und bekommt eine HarteKriterums Fehlermeldung")
  public void istDerTerminVonDannTerminUndBekommtEineHarteKriterumsFehlermeldung(String pruefung,
      LocalDateTime resultTermin)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung r = getOrCreate(pruefung);
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(HartesKriteriumException.class);
  }


  @Angenommen("es existiert keine Pruefungsperiode und die Pruefung {string} soll eingeplant werden")
  public void esExistiertKeinePruefungsperiodeUndDiePruefungSollEingeplantWerden(String pruefung) {

  }

  @Dann("bekomme ich eine Fehlermeldung NoPRuefungsPeriodeDefinedException")
  public void bekommeIchEineFehlermeldungNoPRuefungsPeriodeDefinedException() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(NoPruefungsPeriodeDefinedException.class);
  }

  @Wenn("ich der Pruefung {string} einen Termin gebe")
  public void ichDerPruefungEinenTerminGebe(String pruefung) {
    LocalDateTime time = LocalDateTime.of(2021, 12, 11, 9, 0);
    ReadOnlyPruefung r = new PruefungDTOBuilder().withPruefungsNummer(pruefung)
        .withPruefungsName(pruefung).build();
    try {
      state.controller.schedulePruefung(r, time);
    } catch (HartesKriteriumException | NoPruefungsPeriodeDefinedException | IllegalArgumentException | IllegalStateException e) {
      state.results.put("exception", e);
    }
  }

  @Dann("bekomme ich eine Fehlermeldung IlligaleArgumentException")
  public void bekommeIchEineFehlermeldungIlligaleArgumentExceptionDaSieInEinBlockLiegt() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(IllegalArgumentException.class);
  }

  @Und("es existiert die Pruefung {string}")
  public void esExistiertDiePruefung(String pruefung) throws NoPruefungsPeriodeDefinedException {
    getOrCreate(pruefung);
  }


  @Dann("bekomme ich eine Fehlermeldung IllegalStateException")
  public void bekommeIchEineFehlermeldungIllegalStateException() {
    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(IllegalStateException.class);
  }


  @Und("die Pruefung {string} soll eingeplant werden")
  public void diePruefungSollEingeplantWerden(String pruefung) {

  }
}
