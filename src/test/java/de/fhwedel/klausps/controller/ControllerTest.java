package de.fhwedel.klausps.controller;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomDate;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedROPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTime;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedROPruefung;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.Converter;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class ControllerTest {

  Controller deviceUnderTest;
  DataAccessService dataAccessService;
  IOService ioService;
  ScheduleService scheduleService;
  Converter converter;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.ioService = mock(IOService.class);
    this.scheduleService = mock(ScheduleService.class);
    this.converter = mock(Converter.class);
    this.deviceUnderTest = new Controller(dataAccessService, ioService, scheduleService,
        converter);
  }

  @Test
  void getGeplantePruefungenWithKonflikt_noNullParametersAllowed() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(null));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_noPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException {
    when(scheduleService.getGeplantePruefungenWithKonflikt(any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(getRandomPlannedROPruefung(1L)));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_missingPruefungsperiodeIsDetected()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);

    when(scheduleService.getGeplantePruefungenWithKonflikt(any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);

    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor));
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
  void createSemester_semesterTypeMustNotBeNull() {
    Year year = Year.of(2022);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createSemester(null, year));
  }

  @Test
  void createSemester_yearMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.createSemester(Semestertyp.SOMMERSEMESTER, null));
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
    ReadOnlyPruefung pruefung = getRandomPlannedROPruefung(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getHardConflictedTimes(null, pruefung));
  }

  @Test
  void getHardConflictedTimes_planungseinheitMustNotBeNull() {
    Set<LocalDateTime> times = emptySet();
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getHardConflictedTimes(times, null));
  }

  @Test
  void getHardConflictedTimes_zeitpunktOutOfPeriodeIsContained()
      throws NoPruefungsPeriodeDefinedException {
    pruefungsperiodeIsSet();
    when(scheduleService.getHardConflictedTimes(any(), any())).thenThrow(
        IllegalArgumentException.class);

    Set<LocalDateTime> times = emptySet();
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.getHardConflictedTimes(times, pruefung));
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

    Set<LocalDateTime> times = emptySet();
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.getHardConflictedTimes(times, pruefung));
  }

  @Test
  void getAllPlanungseinheitenBetween_no_period()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(1L);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getPlanungseinheitenInZeitraum(start, end));
  }

  @Test
  void getAllPlanungseinheitenBetween_all_planned_pruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    LocalDateTime start = pruefung.getStartzeitpunkt();
    LocalDateTime end = start.plusDays(1L);
    when(dataAccessService.getAllPlanungseinheitenBetween(start, end)).thenReturn(
        Set.of(pruefung));
    when(converter.convertToROPlanungseinheitSet(anyCollection())).thenCallRealMethod();
    deviceUnderTest.getPlanungseinheitenInZeitraum(start, end);
    verify(converter).convertToROPlanungseinheitSet(anyCollection());
    assertThat(deviceUnderTest.getPlanungseinheitenInZeitraum(start, end)).hasSize(1);
  }

  @Test
  void getPlanungseinheitenInZeitraum_startMustNotBeNull() {
    LocalDateTime end = getRandomTime(2L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getPlanungseinheitenInZeitraum(null, end));
  }

  @Test
  void getPlanungseinheitenInZeitraum_endMustNotBeNull() {
    LocalDateTime start = getRandomTime(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getPlanungseinheitenInZeitraum(start, null));
  }

  @Test
  void getPlanungseinheitenInZeitraum_noPruefungsperiode()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    LocalDateTime start = getRandomTime(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getPlanungseinheitenInZeitraum(start, null));
  }

  @Test
  void getAllPlanungseinheitenBetween_illegalTimeSpan()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.minusDays(1L);
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenThrow(
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
    LocalDate end = getRandomDate(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.setDatumPeriode(null, end));
  }

  @Test
  void setDatumPeriode_endDatumMustNotBeNull() {
    LocalDate start = getRandomDate(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.setDatumPeriode(start, null));
  }

  @Test
  void setDatumPeriode_delegateToScheduleService()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDate start = getRandomDate(1L);
    LocalDate end = start.plusDays(28);
    deviceUnderTest.setDatumPeriode(start, end);
    verify(scheduleService).setDatumPeriode(start, end);
    verifyNoInteractions(dataAccessService);
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

  @Test
  void getKapazitaetPeriode_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPeriodenKapazitaet()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getKapazitaetPeriode());
  }

  @Test
  void getSemester_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getSemester()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getSemester());
  }

  @Test
  void getAllKlausurenFromPruefer_prueferMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAllKlausurenFromPruefer(null));
  }

  @Test
  void getAllKlausurenFromPruefer_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAllKlausurenFromPruefer(anyString())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAllKlausurenFromPruefer("name"));
  }

  @Test
  void getAllTeilnehmerKreise_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAllTeilnehmerkreise()).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAllTeilnehmerKreise());
  }

  @Test
  void getGeplantePruefungenForTeilnehmer_teilnehmerMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenForTeilnehmer(null));
  }

  @Test
  void getGeplantePruefungenForTeilnehmer_noPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.geplantePruefungenForTeilnehmerkreis(any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungenForTeilnehmer(getRandomTeilnehmerkreis(1L)));
  }

  @Test
  void getUngeplantePruefungenForTeilnehmer_teilnehmerMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getUngeplantePruefungenForTeilnehmer(null));
  }

  @Test
  void getUngeplantePruefungenForTeilnehmer_noPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.ungeplantePruefungenForTeilnehmerkreis(any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getUngeplantePruefungenForTeilnehmer(getRandomTeilnehmerkreis(1L)));
  }

  @Test
  void getAnzahlStudentenZeitpunkt_zeitpunktMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAnzahlStudentenZeitpunkt(null));
  }

  @Test
  void getAnzahlStudentenZeitpunkt_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAnzahlStudentenZeitpunkt(any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAnzahlStudentenZeitpunkt(getRandomTime(1L)));
  }

  @Test
  void getPruefungenInZeitraum_startpunktMustNotBeNull() {
    LocalDateTime end = getRandomTime(2L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getPruefungenInZeitraum(null, end));
  }

  @Test
  void getPruefungenInZeitraum_endpunktMustNotBeNull() {
    LocalDateTime start = getRandomTime(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getPruefungenInZeitraum(start, null));
  }

  @Test
  void getPruefungenInZeitraum_noPruefungsperiode()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getPruefungenInZeitraum(getRandomTime(1L),
            getRandomTime(1L).plusHours(1)));
  }

  @Test
  void getBlockOfPruefung_pruefungMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.getBlockOfPruefung(null));
  }

  @Test
  void getBlockOfPruefung_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getBlockOfPruefung(pruefung));
  }

  @Test
  void getBlockOfPruefung_pruefungNotInBlock() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(Optional.empty());
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThat(deviceUnderTest.getBlockOfPruefung(pruefung)).isNotPresent();
  }

  @Test
  void getBlockOfPruefung_pruefungIsInBlock() throws NoPruefungsPeriodeDefinedException {
    Block block = emptyBlock();
    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(Optional.of(block));
    when(converter.convertToROBlock(any())).thenCallRealMethod();
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThat(deviceUnderTest.getBlockOfPruefung(pruefung)).isPresent();
  }

  private Block emptyBlock() {
    return new BlockImpl(mock(Pruefungsperiode.class), 42, "blockName", Blocktyp.PARALLEL);
  }

  @Test
  void setDauer_pruefungMustNotBeNull() {
    Duration duration = Duration.ZERO;
    assertThrows(NullPointerException.class, () -> deviceUnderTest.setDauer(null, duration));
  }

  @Test
  void setDauer_dauerMustNotBeNull() {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThrows(NullPointerException.class, () -> deviceUnderTest.setDauer(pruefung, null));
  }

  @Test
  void setDauer_noPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    when(scheduleService.setDauer(any(), any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    Duration duration = Duration.ZERO;
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setDauer(pruefung, duration));
  }

  @Test
  void setKapazitaetPeriode_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(scheduleService.setKapazitaetPeriode(anyInt())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setKapazitaetPeriode(111));
  }

  @Test
  void setPruefungsnummer_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.setPruefungsnummer(any(), any())).thenThrow(
        NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setPruefungsnummer(mock(ReadOnlyPruefung.class), "pruefungsnummer"));
  }

  @Test
  void setPruefungsnummer_noNullParameters() {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    Duration dauer = Duration.ZERO;
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.setPruefungsnummer(pruefung, null));
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.setPruefungsnummer(null, "pruefungsnummer"));
  }

  @Test
  void setPruefungsnummer_delegateMethod() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    String pruefungsnummer = "pruefungsnummer";
    deviceUnderTest.setPruefungsnummer(pruefung, pruefungsnummer);
    verify(dataAccessService).setPruefungsnummer(pruefung, pruefungsnummer);
  }

  @Test
  void setPruefungsnummer_resultPresent() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    String pruefungsnummer = "pruefungsnummer";
    when(dataAccessService.setPruefungsnummer(pruefung, pruefungsnummer)).thenReturn(
        getRandomUnplannedPruefung(1L));
    when(converter.convertToReadOnlyPlanungseinheit(any())).thenReturn(pruefung);
    assertThat(deviceUnderTest.setPruefungsnummer(pruefung, pruefungsnummer)).isNotNull();
  }

  @Test
  void setName_pruefungMustNotBeNull() {
    ReadOnlyPruefung pruefung = null;
    String name = "name";
    assertThrows(NullPointerException.class, () -> deviceUnderTest.setName(pruefung, name));
  }

  @Test
  void setName_nameMustNotBeNull() {
    ReadOnlyPruefung pruefung = mock(ReadOnlyPruefung.class);
    String name = null;
    assertThrows(NullPointerException.class, () -> deviceUnderTest.setName(pruefung, name));
  }

  @Test
  void setName_nameMustNotBeEmpty() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = mock(ReadOnlyPruefung.class);
    String name = "";
    when(dataAccessService.changeNameOf(any(), any())).thenThrow(IllegalArgumentException.class);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.setName(pruefung, name));
  }

  @Test
  void setName_correctReturn() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = mock(ReadOnlyPruefung.class);
    String name = "";
    Pruefung modelPruefung = mock(Pruefung.class);
    when(dataAccessService.changeNameOf(any(), any())).thenThrow(IllegalArgumentException.class);
    when(converter.convertToReadOnlyPlanungseinheit(modelPruefung)).thenReturn(pruefung);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.setName(pruefung, name));
  }

}
