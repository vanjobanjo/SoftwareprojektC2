package de.fhwedel.klausps.controller.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IOServiceTest {

  private IOService deviceUnderTest;
  private DataAccessService dataAccessService;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new IOService(dataAccessService);
  }

  @Test
  void createEmptyPeriodeTest() throws IllegalTimeSpanException {
    deviceUnderTest.createEmptyPeriode(
        getSemester(), LocalDate.of(1996, 9, 1), LocalDate.of(1997, 3, 23),
        LocalDate.of(1996, 9, 2), 300);
    verify(dataAccessService, times(1)).setPruefungsperiode(any(Pruefungsperiode.class));
  }

  @Test
  void createEmptyPeriode_kapazitaetMustNotBeNegative() {
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1996, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createEmptyPeriode(semester, start,
            end, ankertag, -1));
  }

  @Test
  void createEmptyPeriode_kapazitaetMustNotBeZero() {
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1996, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createEmptyPeriode(semester, start, end,
            ankertag, 0));
  }

  @Test
  void createEmptyPeriode_startMustBeBeforeEnd() {
    LocalDate start = LocalDate.of(1997, 3, 23);
    LocalDate end = LocalDate.of(1996, 9, 1);
    LocalDate ankertag = LocalDate.of(1996, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createEmptyPeriode(semester, start, end, ankertag, 1233));
  }

  @Test
  void createEmptyPeriode_ankertagMustBeBeforeEnd() {
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1997, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createEmptyPeriode(semester, start, end, ankertag, 332));
  }

  @Test
  void createEmptyPeriode_ankertagMustBeAfterStart() {
    Semester semester = getSemester();
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1996, 8, 2);
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createEmptyPeriode(semester, start, end, ankertag, 12));
  }

  // ---
  @Test
  void createNewPeriodeWithData_kapazitaetMustNotBeNegative() {
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1996, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(semester, start,
            end, ankertag, -1, mock(Path.class), mock(Path.class)));
  }

  @Test
  void createNewPeriodeWith_kapazitaetMustNotBeZero() {
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1996, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(semester, start,
            end, ankertag, 0, mock(Path.class), mock(Path.class)));
  }

  @Test
  void createNewPeriodeWithData_startMustBeBeforeEnd() {
    LocalDate start = LocalDate.of(1997, 3, 23);
    LocalDate end = LocalDate.of(1996, 9, 1);
    LocalDate ankertag = LocalDate.of(1996, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(semester, start, end, ankertag, 23,
            mock(Path.class), mock(Path.class)));
  }

  @Test
  void createNewPeriodeWithData_ankertagMustBeBeforeEnd() {
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1997, 9, 2);
    Semester semester = getSemester();
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(semester, start, end, ankertag, 123,
            mock(Path.class), mock(Path.class)));
  }

  @Test
  void createNewPeriodeWithData_ankertagMustBeAfterStart() {
    Semester semester = getSemester();
    LocalDate start = LocalDate.of(1996, 9, 1);
    LocalDate end = LocalDate.of(1997, 3, 23);
    LocalDate ankertag = LocalDate.of(1996, 8, 2);
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(semester, start, end, ankertag, 123,
            mock(Path.class), mock(Path.class)));
  }

  private Semester getSemester() {
    return new SemesterImpl(Semestertyp.SOMMERSEMESTER, Year.of(1996));
  }
}
