package de.fhwedel.klausps.controller;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomDate;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedROPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTime;
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

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.Converter;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.controller.util.TestUtils;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
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
    this.deviceUnderTest = new Controller(dataAccessService, ioService, scheduleService,
        new Converter());
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
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createSemester(Semestertyp.SOMMERSEMESTER, null));
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
  void getHardConflictedTimes_missingPruefungsperiodeIsDetected()
      throws NoPruefungsPeriodeDefinedException {
    when(scheduleService.getHardConflictedTimes(any(), any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);

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

  @Test
  void getAllPlanungseinheitenBetween_no_period()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAllROPlanungseinheitenBetween(any(), any())).thenReturn(null);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(1L);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getPlanungseinheitenInZeitraum(start, end));
  }

  @Test
  void getAllPlanungseinheitenBetween_all_planned_pruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
    ReadOnlyPruefung roPruefung = getRandomPlannedROPruefung(1L);
    LocalDateTime start = roPruefung.getTermin().get();
    LocalDateTime end = start.plusDays(1L);
    when(dataAccessService.getAllROPlanungseinheitenBetween(start, end)).thenReturn(
        Set.of(roPruefung));
    assertThat(deviceUnderTest.getPlanungseinheitenInZeitraum(start, end)).containsOnly(roPruefung);
  }

  @Test
  void getAllPlanungseinheitenBetween_illegalTimeSpan()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(1L);
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
    when(dataAccessService.getAllROPlanungseinheitenBetween(any(), any())).thenThrow(
        IllegalTimeSpanException.class);
    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.getPlanungseinheitenInZeitraum(start, end));
  }

  @Test
  void setAnkerTagPeriode_ankerTagMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.setAnkerTagPeriode(null));
  }

  @Test
  void setAnkerTagPeriode_delegateToDataAccessService()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    deviceUnderTest.setAnkerTagPeriode(getRandomTime(1998L).toLocalDate());
    verify(dataAccessService).setAnkertag(any(LocalDate.class));
  }

  @Test
  void setDatumPeriode_startDatumMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.setDatumPeriode(null, getRandomDate(1L)));
  }

  @Test
  void setDatumPeriode_endDatumMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.setDatumPeriode(getRandomDate(1L), null));
  }


  @Test
  void makeBlockSequential_parameters_must_not_be_null() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.makeBlockSequential(null));
  }

  @Test
  void makeBlockSequential_no_pruefungsperiode_defined() {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(false);
    ReadOnlyBlock block = mock(ReadOnlyBlock.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.makeBlockSequential(block));
  }

  @Test
  void makeBlockParallel_parameters_must_not_be_null() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.makeBlockParallel(null));
  }


  @Test
  void makeBlockParallel__no_pruefungsperiode_defined() {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(false);
    ReadOnlyBlock block = mock(ReadOnlyBlock.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.makeBlockParallel(block));
  }

  @Test
  void getGeplantePruefungen_delegateToDataAccessService()
      throws NoPruefungsPeriodeDefinedException {
    deviceUnderTest.getGeplantePruefungen();
    verify(dataAccessService).getGeplantePruefungen();
  }

  @Test
  void getGeplantePruefungen_noPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getGeplantePruefungen()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungen());
  }

  @Test
  void getUngeplantePruefungen_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getUngeplantePruefungen()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getUngeplantePruefungen());
  }

  @Test
  void getGeplanteBloecke_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getGeplanteBloecke()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplanteBloecke());
  }

  @Test
  void getUngeplanteBloecke_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getUngeplanteBloecke()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getUngeplanteBloecke());
  }

  @Test
  void getStartDatumPeriode_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getStartOfPeriode()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getStartDatumPeriode());
  }

  @Test
  void getEndDatumPeriode_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getEndOfPeriode()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getEndDatumPeriode());
  }

  @Test
  void getAnkerPeriode_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAnkertag()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAnkerPeriode());
  }

}
