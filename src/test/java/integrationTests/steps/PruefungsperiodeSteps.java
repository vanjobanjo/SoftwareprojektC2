package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;
import static integrationTests.steps.BaseSteps.state;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import io.cucumber.java.de.Angenommen;
import java.time.LocalDate;
import java.time.Year;
import org.junit.AssumptionViolatedException;

public class PruefungsperiodeSteps {

  @Angenommen("es existiert eine Pruefungsperiode")
  public void esExistiertEinePruefungsperiode() throws IllegalTimeSpanException {
    createSemester();
  }

  @Angenommen("es existiert keine Pruefungsperiode")
  public void esExistiertKeinePruefungsperiode() {
    throw new AssumptionViolatedException("not implemented");
  }


  protected void createSemester() throws IllegalTimeSpanException {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 31);
    LocalDate end = LocalDate.of(2022, 2, 27);
    LocalDate ankertag = start.plusDays(7);
    state.controller.createEmptyPeriode(semester, start, end, ankertag, 200);
  }

  @Angenommen("es existiert eine Pruefungsperiode von {localDate} - {localDate}")
  public void esExistiertEinePruefungsperiodeVon(LocalDate start, LocalDate ende)
      throws IllegalTimeSpanException {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    state.controller.createEmptyPeriode(semester, start, ende, start, 200);
  }

  @Angenommen("es existiert eine Pruefungsperiode von {localDate} - {localDate} mit dem Ankertag {localDate}")
  public void esExistiertEinePruefungsperiodeVon(LocalDate start, LocalDate end, LocalDate ankertag)
      throws IllegalTimeSpanException {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    state.controller.createEmptyPeriode(semester, start, end, ankertag, 200);
  }
}
