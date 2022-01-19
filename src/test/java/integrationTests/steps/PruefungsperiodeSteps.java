package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;
import static integrationTests.steps.BaseSteps.state;

import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import io.cucumber.java.de.Angenommen;
import java.time.LocalDate;
import java.time.Year;

public class PruefungsperiodeSteps {

  @Angenommen("es existiert eine Pruefungsperiode")
  public void esExistiertEinePruefungsperiode() {
    createSemester();
  }

  protected void createSemester() {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 31);
    LocalDate end = LocalDate.of(2022, 2, 27);
    LocalDate ankertag = start.plusDays(7);
    state.controller.createEmptyPeriode(semester, start, end, ankertag, 400);
  }

}
