package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import integrationTests.state.State;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * A common point for helper methods required in many step definitions.
 */
public class BaseSteps {

  public static State state;

  protected void createSemester() throws IllegalTimeSpanException {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 31);
    LocalDate end = LocalDate.of(2022, 2, 27);
    LocalDate ankertag = start.plusDays(7);
    state.controller.createEmptyPeriode(semester, start, end, ankertag, 400);
  }

  protected LocalDate parseDate(String dateTxt) {
    String[] tmp = dateTxt.split("\\.");
    int day = Integer.parseInt(tmp[0]);
    int month = Integer.parseInt(tmp[1]);
    int year = Integer.parseInt(tmp[2]);
    return LocalDate.of(year, month, day);
  }

  protected LocalTime parseTime(String timeTxt) {
    String[] tmp = timeTxt.split(":");
    int hours = Integer.parseInt(tmp[0]);
    int minutes = Integer.parseInt(tmp[1]);
    return LocalTime.of(hours, minutes);
  }

  @NotNull
  protected ReadOnlyPruefung getPruefungFromControllerWith(String pruefungsName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getAllPruefungen().stream().filter(
            (ReadOnlyPruefung readOnlyPruefung) -> readOnlyPruefung.getPruefungsnummer()
                .equals(pruefungsName))
        .findFirst().get();
    return pruefung;
  }

  protected Set<ReadOnlyPruefung> getAllPruefungen() throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> allPruefungen = new HashSet<>();
    allPruefungen.addAll(state.controller.getGeplantePruefungen());
    allPruefungen.addAll(state.controller.getUngeplantePruefungen());
    return allPruefungen;
  }

  protected boolean existsPruefungWith(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    return getAllPruefungen().stream().filter(
            (ReadOnlyPruefung readOnlyPruefung) -> readOnlyPruefung.getPruefungsnummer()
                .equals(pruefungName))
        .findFirst().isPresent();
  }

}
