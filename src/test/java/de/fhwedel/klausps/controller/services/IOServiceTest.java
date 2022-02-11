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
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createEmptyPeriode(getSemester(), LocalDate.of(1996, 9, 1),
            LocalDate.of(1997, 3, 23),
            LocalDate.of(1996, 9, 2), -1));
  }

  @Test
  void createEmptyPeriode_kapazitaetMustNotBeZero() {
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createEmptyPeriode(getSemester(), LocalDate.of(1996, 9, 1),
            LocalDate.of(1997, 3, 23),
            LocalDate.of(1996, 9, 2), 0));
  }

  @Test
  void createEmptyPeriode_startMustBeBeforeEnd() {
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createEmptyPeriode(getSemester(),
            LocalDate.of(1997, 3, 23),
            LocalDate.of(1996, 9, 1),
            LocalDate.of(1996, 9, 2), 0));
  }
  @Test
  void createEmptyPeriode_ankertagMustBeBeforeEnd() {
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createEmptyPeriode(getSemester(),
            LocalDate.of(1996, 9, 1),
            LocalDate.of(1997, 3, 23),
            LocalDate.of(1997, 9, 2), 0));
  }
@Test
  void createEmptyPeriode_ankertagMustBeAfterStart() {
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createEmptyPeriode(getSemester(),
            LocalDate.of(1996, 9, 1),
            LocalDate.of(1997, 3, 23),
            LocalDate.of(1996, 8, 2), 0));
  }

  private Semester getSemester() {
    return new SemesterImpl(Semestertyp.SOMMERSEMESTER, Year.of(1996));
  }
}
