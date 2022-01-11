package de.fhwedel.klausps.controller;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedROPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedROPruefung;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.controller.util.TestUtils;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControllerTest {

  Controller deviceUnderTest;
  DataAccessService dataAccessService;
  IOService ioService;
  ScheduleService scheduleService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.ioService = mock(IOService.class);
    this.scheduleService = mock(ScheduleService.class);
    this.deviceUnderTest = new Controller(dataAccessService, ioService, scheduleService);
  }

  @Test
  void getGeplantePruefungenWithKonflikt_noNullParametersAllowed() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(null));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_missingPruefungsperiodeIsDetected() {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);

    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(false);

    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_useDataAccessServiceOnlyForCheckForPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);

    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);

    deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor);
    verify(dataAccessService, only()).isPruefungsperiodeSet();
  }

  @Test
  void getGeplantePruefungenWithKonflikt_delegateToScheduleService()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);
    pruefungsperiodeIsSet();

    deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor);
    verify(scheduleService, times(1)).getGeplantePruefungenWithKonflikt(any());
  }

  private void pruefungsperiodeIsSet() {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
  }

  @Test
  void createTeilnehmerkreis_successful() {
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl("Informatik", "14.0", 1,
        Ausbildungsgrad.BACHELOR);
    assertThat(deviceUnderTest.createTeilnehmerkreis(Ausbildungsgrad.BACHELOR, "Informatik", "14.0",
        1)).isEqualTo(teilnehmerkreis);
  }

  @Test
  void createTeilnehmerkreis_unsuccessful() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createTeilnehmerkreis(null, "Informatik", "b", 1));
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createTeilnehmerkreis(Ausbildungsgrad.BACHELOR, null, "b", 1));
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createTeilnehmerkreis(Ausbildungsgrad.BACHELOR, "BWL", null, 1));
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createTeilnehmerkreis(null, null, null, 1));
  }

  @Test
  void createSemester_successful() {
    Semester createdSemester = deviceUnderTest.createSemester(Semestertyp.WINTERSEMESTER,
        Year.of(2022));
    assertThat(createdSemester).isNotNull();
    assertThat(createdSemester.getJahr()).isEqualTo(Year.of(2022));
    assertThat(createdSemester.getTyp()).isEqualTo(Semestertyp.WINTERSEMESTER);

  }

  @Test
  void createSemester_unsuccessful() {
    Year year = Year.of(2);
    assertThrows(NullPointerException.class, () -> deviceUnderTest.createSemester(Semestertyp.SOMMERSEMESTER, null));
    assertThrows(NullPointerException.class, () -> deviceUnderTest.createSemester(null, year));
    assertThrows(NullPointerException.class, () -> deviceUnderTest.createSemester(null, null));
  }

  @Test
  void getHardConflictedTimes_zeitpunkteMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.getHardConflictedTimes(null,
        TestUtils.getRandomPlannedROPruefung(1L)));
  }

  @Test
  void getHardConflictedTimes_planungseinheitMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getHardConflictedTimes(emptySet(), null));
  }

  @Test
  void getHardConflictedTimes_zeitpunktOutOfPeriodeIsContained()
      throws NoPruefungsPeriodeDefinedException {
    pruefungsperiodeIsSet();
    when(scheduleService.getHardConflictedTimes(any(), any())).thenThrow(
        IllegalArgumentException.class);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.getHardConflictedTimes(emptySet(), getRandomUnplannedROPruefung(1L)));
  }

  @Test
  void getHardConflictedTimes_missingPruefungsperiodeIsDetected() {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(false);

    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getHardConflictedTimes(emptySet(), getRandomUnplannedROPruefung(1L)));
  }

  @Test
  void getHardConflictedTimes_unknwonPruefung() throws NoPruefungsPeriodeDefinedException {
    pruefungsperiodeIsSet();
    when(dataAccessService.existsPruefungWith(anyString())).thenReturn(false);
    when(scheduleService.getHardConflictedTimes(any(), any())).thenThrow(
        IllegalArgumentException.class);

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.getHardConflictedTimes(emptySet(), getRandomUnplannedROPruefung(1L)));
  }

}
