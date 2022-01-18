package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.AssumptionViolatedException;

public class getUngeplanteKlausurenSteps extends BaseSteps {

  @DataTableType
  public Pruefung pruefungEntryTransformer(Map<String, String> entry) {
    String pruefungsnummer = String.valueOf(entry.get("Name").hashCode());
    return new PruefungImpl(pruefungsnummer,
        entry.get("Name"),
        pruefungsnummer,
        parseDuration(entry.get("Dauer")));
  }

  private Duration parseDuration(String durationTxt) {
    String[] tmp = durationTxt.split(":");
    return Duration.ofHours(Integer.parseInt(tmp[0]))
        .plus(Duration.ofMinutes(Integer.parseInt(tmp[1])));
  }

  @Angenommen("es existieren die folgenden Klausuren:")
  public void esExistierenDieFolgendenKlausuren(List<Map<String, String>> pruefungen)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    createSemester();
    for (Map<String, String> pruefung : pruefungen) {
      String pruefungsnummer = String.valueOf(pruefung.get("Name").hashCode());
      ReadOnlyPruefung roPruefung = getState().controller.createPruefung(pruefungsnummer,
          pruefung.get("Name"),
          pruefungsnummer,
          emptySet(),
          parseDuration(pruefung.get("Dauer")),
          emptyMap());
      String date = pruefung.get("Datum");
      String time = pruefung.get("StartZeit");
      if (date != null && time != null) {
        LocalDateTime start = parseDate(date).atTime(parseTime(time));
        getState().controller.schedulePruefung(roPruefung, start);
      }
    }
  }

  private void createSemester() {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 31);
    LocalDate end = LocalDate.of(2022, 2, 27);
    LocalDate ankertag = start.plusDays(7);
    getState().controller.createEmptyPeriode(semester, start, end, ankertag, 400);
  }

  private LocalDate parseDate(String dateTxt) {
    String[] tmp = dateTxt.split("\\.");
    int day = Integer.parseInt(tmp[0]);
    int month = Integer.parseInt(tmp[1]);
    int year = Integer.parseInt(tmp[2]);
    return LocalDate.of(year, month, day);
  }

  private LocalTime parseTime(String timeTxt) {
    String[] tmp = timeTxt.split(":");
    int hours = Integer.parseInt(tmp[0]);
    int minutes = Integer.parseInt(tmp[1]);
    return LocalTime.of(hours, minutes);
  }

  @Wenn("ich alle ungeplanten Klausuren anfrage")
  public void ichAlleUngeplantenKlausurenAnfrage() {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("bekomme ich die Klausuren {string}")
  public void bekommeIchDieKlausuren(String klausuren) {
    Collection<ReadOnlyPruefung> result = (Collection<ReadOnlyPruefung>) getState().results.get(
        "pruefungen");
    List<String> pruefungsNamen = Arrays.asList(klausuren.split(", "));
    assertThat(result).hasSameSizeAs(pruefungsNamen);
    assertThat(result).allMatch(
        (ReadOnlyPruefung pruefung) -> pruefungsNamen.stream().anyMatch(
            (String name) -> pruefung.getName().equals(name)));
  }

  @Dann("bekomme ich keine Klausuren")
  public void bekommeIchKeineKlausuren() {
    assertThat((Collection<ReadOnlyPruefung>) getState().results.get("pruefungen")).isEmpty();
  }

  @Angenommen("es existieren keine Klausuren")
  public void esExistierenKeineKlausuren() {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 31);
    LocalDate end = LocalDate.of(2022, 2, 27);
    LocalDate ankertag = start.plusDays(7);
    getState().controller.createEmptyPeriode(semester, start, end, ankertag, 400);
  }

  @Dann("bekomme ich den Block als Teil der ungeplanten Klausuren")
  public void bekommeIchDenBlockAlsTeilDerUngeplantenKlausuren(DataTable dataTable) {

  }
}
