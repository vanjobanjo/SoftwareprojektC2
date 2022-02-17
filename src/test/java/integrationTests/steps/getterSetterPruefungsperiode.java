package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Ausbildungsgrad.BACHELOR;
import static integrationTests.steps.BaseSteps.state;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AssumptionViolatedException;


public class getterSetterPruefungsperiode {

  private static final String EXCEPTION = "exception";

  /**
   * Wandelt ein String in Format dd.mm.yyyy in eine LocalDate um
   *
   * @param datum das Datum fuer die neue LocalDateTime
   * @return den String als LocalDateTime
   */
  @ParameterType("(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d)?")
  public LocalDate localDate(String datum) {

    if (datum != null) {
      String[] split = datum.split("\\.");

      String str = split[2] + "-" + split[1] + "-" + split[0];
      return LocalDate.parse(str);
    } else {
      return null;
    }
  }

  @ParameterType("[A-Z][a-z]*")
  public Semestertyp semestertyp(String semestertyp) {

    if (semestertyp != null) {
      if (semestertyp.equalsIgnoreCase("sommersemester")) {
        return Semestertyp.SOMMERSEMESTER;
      } else if (semestertyp.equalsIgnoreCase("wintersemester")) {
        return Semestertyp.WINTERSEMESTER;
      }
    }
    throw new IllegalArgumentException("Parse Error for Semestertyp in Tests");
  }

  @ParameterType("(\\d\\d\\d\\d)")
  public Year year(String year) {
    return Year.of(Integer.parseInt(year));
  }

  // ------------------------------------------------------------
  // ------------------------- allgemein ------------------------
  // ------------------------------------------------------------

  @Dann("erhalte ich einen Fehler")
  public void erhalteIchEinenFehler() {
    assertThat(state.results.get(EXCEPTION)).isNotNull();
  }
  // ------------------------------------------------------------
  // ------------------ Start- und Enddatum ---------------------
  // ------------------------------------------------------------

  @Wenn("ich das Startdatum der Periode anfrage")
  public void ichDasStartdatumDerPeriodeAnfrage() {
    try {
      LocalDate result = state.controller.getStartDatumPeriode();
      state.results.put("startDate", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("erhalte ich das Startdatum")
  public void erhalteIchDasStartdatum() {
    assertThat(state.results.get("startDate")).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isNull();
  }

  @Wenn("ich das Enddatum der Periode anfrage")
  public void ichDasEnddatumDerPeriodeAnfrage() {
    try {
      LocalDate result = state.controller.getEndDatumPeriode();
      state.results.put("endDate", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("erhalte ich das Enddatum")
  public void erhalteIchDasEnddatum() {
    assertThat(state.results.get("endDate")).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isNull();
  }

  @Wenn("ich das {localDate} und das {localDate} der Periode aendere")
  public void ichDasStartdatumUndDasEnddatumDerPeriodeAendere(LocalDate start,
      LocalDate end) {
    try {
      state.controller.setDatumPeriode(start, end);
    } catch (NoPruefungsPeriodeDefinedException | IllegalTimeSpanException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("werden die Daten auf {localDate} und {localDate} geaendert")
  public void werdenDieDatenEntsprechendGeaendert(LocalDate start, LocalDate end)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.controller.getStartDatumPeriode()).isEqualTo(start);
    assertThat(state.controller.getEndDatumPeriode()).isEqualTo(end);
    assertThat(state.results.get(EXCEPTION)).isNull();
  }

  @Wenn("ich das Startdatum auf {localDate} und das Enddatum auf {localDate} setze")
  public void ichDasStartdatumAufUndDasEnddatumAuf(LocalDate start, LocalDate end) {
    try {
      state.controller.setDatumPeriode(start, end);
    } catch (NoPruefungsPeriodeDefinedException | IllegalTimeSpanException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  // ------------------------------------------------------------
  // ------------------------ Ankertag --------------------------
  // ------------------------------------------------------------

  @Wenn("ich den Ankertag der Periode anfrage")
  public void ichDenAnkertagDerPeriodeAnfrage() {
    try {
      LocalDate result = state.controller.getAnkerPeriode();
      state.results.put("ankertag", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("erhalte ich den Ankertag")
  public void erhalteIchDenAnkertag() {
    assertThat(state.results.get("ankertag")).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isNull();
  }

  @Wenn("ich den Ankertag auf {localDate} setze")
  public void ichDenAnkertagAufSetze(LocalDate ankertag) {
    try {
      state.controller.setAnkerTagPeriode(ankertag);
    } catch (IllegalTimeSpanException | NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("wird der Ankertag auf {localDate} geaendert")
  public void wirdDerAnkertagAufGeaendert(LocalDate ankertag)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.controller.getAnkerPeriode()).isEqualTo(ankertag);
  }

  // ------------------------------------------------------------
  // ----------------------- Kapazität --------------------------
  // ------------------------------------------------------------

  @Wenn("ich die Kapazitaet anfrage")
  public void ichDieKapazitaetAnfrage() {
    try {
      int result = state.controller.getKapazitaetPeriode();
      state.results.put("kapazitaet", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("erhalte ich die Kapazitaet")
  public void erhalteIchDieKapazitaet() {
    assertThat(state.results.get("kapazitaet")).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isNull();
  }

  @Wenn("ich die Kapazitaet auf den Wert {int} setze")
  public void ichDieKapazitaetAufSetze(int kapazitaet) {
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.setKapazitaetPeriode(kapazitaet);
      state.results.put("changed", result);
    } catch (NoPruefungsPeriodeDefinedException | IllegalArgumentException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("erhalte ich eine Liste mit Klausuren deren Bewertung veraendert wurde")
  public void erhalteListeMitGeaenderterBewertung() {
    assertThat(state.results.get("changed")).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isNull();
  }

  @Und("es sind Pruefungen geplant")
  public void esSindPruefungenGeplant() {
    try {
      Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl("inf",
          "20.0", 1, BACHELOR);
      Map<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
      schaetzungen.put(teilnehmerkreis, 200);
      state.controller.createPruefung("abc", "abc", "abc", emptySet(), Duration.ofMinutes(120),
          schaetzungen);
      Set<Teilnehmerkreis> result = new HashSet<>();
      result.add(teilnehmerkreis);
      state.results.put("changed", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Dann("erhalte ich eine Liste ohne Klausuren")
  public void erhalteIchEineListeOhneKlausuren() {
    assertThat(state.results.get("changed")).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isNull();
    if (state.results.get("changed") instanceof List<?> changed) {
      assertThat(changed).isEmpty();
    } else {
      throw new AssertionError("List should have been empty.");
    }
  }

  @Und("es sind keine Pruefungen geplant")
  public void esSindKeinePruefungenGeplant() throws IllegalTimeSpanException {
    Semester semester = new SemesterImpl(Semestertyp.WINTERSEMESTER, Year.of(2022));
    state.controller.createEmptyPeriode(semester,
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 2, 2),
        LocalDate.of(2022, 1, 4),
        200);
  }

  // ------------------------------------------------------------
  // ------------------------- Semester -------------------------
  // ------------------------------------------------------------

  @Wenn("ich das Semester abfrage")
  public void ichDasSemesterAbfrage() {
    try {
      Semester result = state.controller.getSemester();
      state.results.put("semester", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Wenn("ich ein neues Semester mit dem Jahr {year} und dem Typ {semestertyp} erstelle")
  public void ichDasSemesterVonAltAufNeuAendere(Year year, Semestertyp semestertyp) {
    Semester result = state.controller.createSemester(semestertyp, year);
    state.results.put("semester", result);
  }


  @Dann("erhalte ich das Semester")
  public void erhalteIchDasSemester() {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("semester")).isNotNull();
  }

  @Dann("erhalte ich das Semester {semestertyp} {year}")
  public void erhalteIchDasSemesterSommersemester(Semestertyp semestertyp, Year year) {
    if (state.results.get("semester") instanceof Semester semester) {
      assertThat(semester.getTyp()).isEqualTo(semestertyp);
      assertThat(semester.getJahr()).isEqualTo(year);
    } else {
      throw new AssertionError("Result should have been a semester, but was not.");
    }
  }

  // ------------------------------------------------------------
  // --------------------- Teilnehmerkreise ---------------------
  // ------------------------------------------------------------

  @Wenn("ich alle Teilnehmerkreise anfrage")
  public void ichAlleTeilnehmerkreiseAnfrage() {
    try {
      Set<Teilnehmerkreis> result = state.controller.getAllTeilnehmerKreise();
      state.results.put("teilnehmerkreise", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      state.results.put(EXCEPTION, e);
    }
  }

  @Und("es existieren die Teilnehmerkreise {listOfTeilnehmerKreis}")
  public void esExistierenDieTeilnehmerkreise(List<Teilnehmerkreis> teilnehmerkreise)
      throws NoPruefungsPeriodeDefinedException {
    for (int i = 0; i < teilnehmerkreise.size(); i++) {
      Map<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
      schaetzungen.put(teilnehmerkreise.get(i), 1);
      state.controller.createPruefung(String.valueOf(i), String.valueOf(i), String.valueOf(i),
          emptySet(), Duration.ofMinutes(90), schaetzungen);
    }
  }

  @Dann("bekomme ich eine leere Liste")
  public void bekommeIchEineLeereListe() {
    Set<Teilnehmerkreis> result = castToSetOfTk();
    assertThat(result).isEmpty();
    assertThat(state.results.get("exception")).isNull();
  }


  @Dann("bekomme ich die Teilnehmerkreise {listOfTeilnehmerKreis}")
  public void bekommeIchDieTeilnehmerkreise(List<Teilnehmerkreis> teilnehmerkreise) {
    Set<Teilnehmerkreis> result = castToSetOfTk();
    for (Teilnehmerkreis tk : teilnehmerkreise) {
      assertThat(tkExists(result, tk)).isTrue();
    }
    assertThat(state.results.get("exception")).isNull();
  }

  @Und("es gibt keine Teilnehmerkreise")
  public void esGibtKeineTeilnehmerkreise()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Semester semester = new SemesterImpl(Semestertyp.SOMMERSEMESTER, Year.of(2022));
    state.controller.createEmptyPeriode(semester,
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 2, 1),
        LocalDate.of(2022, 1, 10),
        100);
    state.results.put("teilnehmerkreise", state.controller.getAllTeilnehmerKreise());
  }


  private Set<Teilnehmerkreis> castToSetOfTk() {
    Set<Teilnehmerkreis> castSet = new HashSet<>();
    if (state.results.get("teilnehmerkreise") instanceof Set<?> lst) {
      for (Object o : lst) {
        if (o instanceof Teilnehmerkreis tk) {
          castSet.add(tk);
        } else {
          throw new AssertionError("found non Teilnehmerkreis Object in getAllTeilnehmerkreise");
        }
      }
    } else {
      throw new AssertionError(
          "result of getAllTeilnehmerkreise was not a Set of Teilnehmerkreise");
    }
    return castSet;
  }


  private boolean tkExists(Set<Teilnehmerkreis> tks, Teilnehmerkreis toTest) {
    for (Teilnehmerkreis tk : tks) {
      if (tk.getAusbildungsgrad().equals(toTest.getAusbildungsgrad())
          && tk.getPruefungsordnung().equals(toTest.getPruefungsordnung())
          && tk.getFachsemester() == toTest.getFachsemester()
          && tk.getStudiengang().equals(toTest.getStudiengang())
      ) {
        return true;
      }
    }
    return false;
  }

}
