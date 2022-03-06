package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;
import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_ANALYSIS_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_DM_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_HASKELL_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.bwlMaster;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.infMaster;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedROPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungWith;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedROPruefung;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;
import static java.time.Month.FEBRUARY;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.Controller;
import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.matchers.IsNotMatcher;
import de.fhwedel.klausps.controller.util.TestFactory;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.api.importer.ImportException;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.PruefungsperiodeImpl;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

  private final LocalDate START_PERIOD = LocalDate.of(2000, 1, 1);
  private final LocalDate END_PERIOD = LocalDate.of(2000, 1, 31);
  private final LocalTime _1000 = LocalTime.of(10, 0);

  private DataAccessService dataAccessService;
  private RestrictionService restrictionService;
  private ScheduleService deviceUnderTest;
  private Converter converter;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.restrictionService = mock(RestrictionService.class);
    converter = new Converter();
    this.deviceUnderTest = new ScheduleService(dataAccessService, restrictionService, converter);
    converter.setScheduleService(deviceUnderTest);
  }

  @Test
  void scheduleBlockUnConsistentBlock() throws NoPruefungsPeriodeDefinedException {
    Pruefung model_analysis = TestFactory.getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung model_dm = TestFactory.getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(model_analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(model_dm);
    when(dataAccessService.getPruefung(RO_HASKELL_UNPLANNED)).thenThrow(
        IllegalStateException.class);

    Block blockWithAnalysisDM = getModelBlockFromROPruefungen("AnalysisAndDm", null,
        RO_ANALYSIS_UNPLANNED,
        RO_DM_UNPLANNED);

    when(dataAccessService.getBlockTo(model_analysis)).thenReturn(Optional.of(blockWithAnalysisDM));
    when(dataAccessService.getBlockTo(model_dm)).thenReturn(Optional.of(blockWithAnalysisDM));

    ReadOnlyBlock inConsistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null,
        RO_DM_UNPLANNED,
        RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED);

    //Block doesn't exist
    LocalDateTime termin = START_PERIOD.atTime(_1000);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.scheduleBlock(inConsistentBlock, termin));
  }

  private Block getModelBlockFromROPruefungen(String name, LocalDateTime start,
      ReadOnlyPruefung... pruefungen) {
    Block block = new BlockImpl(mock(Pruefungsperiode.class), 1, name, Blocktyp.SEQUENTIAL);
    block.setStartzeitpunkt(start);
    for (ReadOnlyPruefung p : pruefungen) {
      block.addPruefung(getPruefungOfReadOnlyPruefung(p));
    }
    return block;
  }

  private ReadOnlyBlock getROBlockFromROPruefungen(String name, LocalDateTime start,
      ReadOnlyPruefung... pruefungen) {
    return new BlockDTO(name, start, Duration.ZERO, Set.of(pruefungen), 1, PARALLEL);
  }

  private Pruefung getPruefungOfReadOnlyPruefung(ReadOnlyPruefung roPruefung) {
    PruefungImpl modelPruefung = new PruefungImpl(roPruefung.getPruefungsnummer(),
        roPruefung.getName(), "", roPruefung.getDauer(), roPruefung.getTermin().orElse(null));
    for (String pruefer : roPruefung.getPruefer()) {
      modelPruefung.addPruefer(pruefer);
    }
    roPruefung.getTeilnehmerKreisSchaetzung().forEach(modelPruefung::setSchaetzung);
    return modelPruefung;
  }

  @Test
  void scheduleBlockSuccessFull() throws NoPruefungsPeriodeDefinedException {

    LocalDateTime time = START_PERIOD.atTime(_1000);

    Block model = new BlockImpl(mock(Pruefungsperiode.class), "AnalysisAndDm", Blocktyp.SEQUENTIAL);
    model.addPruefung(getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED));
    model.addPruefung(getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED));
    model.setStartzeitpunkt(time);

    when(dataAccessService.terminIsInPeriod(any())).thenReturn(true);
    when(dataAccessService.scheduleBlock(any(), any())).thenReturn(model);

    ReadOnlyBlock consistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null,
        RO_DM_UNPLANNED,
        RO_ANALYSIS_UNPLANNED);

    assertDoesNotThrow(() -> deviceUnderTest.scheduleBlock(consistentBlock, time));
  }

  @Test
  void scheduledBlockExamInBlockIsNotInPeriod() throws NoPruefungsPeriodeDefinedException {
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenThrow(
        IllegalStateException.class);
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    dataAccessService = new DataAccessService(pruefungsperiode);
    deviceUnderTest = new ScheduleService(dataAccessService, restrictionService, converter);
    Block block = new BlockImpl(mock(Pruefungsperiode.class), 1, "AnalysisAndDm", PARALLEL);
    block.addPruefung(model_analysis);
    ReadOnlyBlock readOnlyBlock = new BlockDTO("block", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED), 1, PARALLEL);
    when(pruefungsperiode.block(readOnlyBlock.getBlockId())).thenReturn(block);
    ReadOnlyBlock inConsistentBlock = new BlockDTO("block", null,
        RO_ANALYSIS_UNPLANNED.getDauer(), Set.of(RO_ANALYSIS_UNPLANNED), 1, PARALLEL);

    when(pruefungsperiode.getStartdatum()).thenReturn(LocalDate.of(2022, 1, 1));
    when(pruefungsperiode.getStartdatum()).thenReturn(LocalDate.of(2022, 2, 1));

    LocalDateTime termin = LocalDateTime.of(2022, 1, 12, 10, 0);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.scheduleBlock(inConsistentBlock, termin));
  }


  @Test
  void unscheduleBlock() throws NoPruefungsPeriodeDefinedException {

    LocalDateTime now = LocalDateTime.now();
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    model_analysis.setStartzeitpunkt(now);
    model_dm.setStartzeitpunkt(now);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(model_analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(model_dm);

    ReadOnlyPruefung ro_analysis = new PruefungDTOBuilder(RO_ANALYSIS_UNPLANNED).withStartZeitpunkt(
            now)
        .build();
    ReadOnlyPruefung ro_dm = new PruefungDTOBuilder(RO_DM_UNPLANNED).withStartZeitpunkt(now)
        .build();

    // in the data model analysis and dm are in a block. haskell is not part of the block
    Block blockWithAnalysisDM = new BlockImpl(mock(Pruefungsperiode.class), 1, "AnalysisAndDm",
        PARALLEL);

    blockWithAnalysisDM.addPruefung(model_analysis);
    blockWithAnalysisDM.addPruefung(model_dm);

    blockWithAnalysisDM.setStartzeitpunkt(
        now); //at first add pruefungen than set the startzeitpunkt. otherwise startzeitpunkt will always be null!!

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());
    when(dataAccessService.getBlockTo(model_analysis)).thenReturn(Optional.of(blockWithAnalysisDM));
    when(dataAccessService.getBlockTo(model_dm)).thenReturn(Optional.of(blockWithAnalysisDM));

    ReadOnlyBlock block = getROBlockFromROPruefungen("AnalysisAndDm", now, ro_analysis, ro_dm);

    when(dataAccessService.unscheduleBlock(any())).thenReturn(blockWithAnalysisDM);
    when(dataAccessService.getBlock(any())).thenReturn(blockWithAnalysisDM);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        Set.of(model_analysis, model_dm));

    List<ReadOnlyPlanungseinheit> result = deviceUnderTest.unscheduleBlock(block);
    blockWithAnalysisDM.setStartzeitpunkt(null);
    // todo auf Block testen wenn equals für block implementiert ist
    assertThat(result).contains(ro_analysis, ro_dm);
  }

  @Test
  void removeTeilnehmerkreis() throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);

    ReadOnlyPruefung roHaskel = new PruefungDTOBuilder().withPruefungsName("Haskel")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("haskell")
        .withAdditionalTeilnehmerkreis(informatik).build();

    when(this.dataAccessService.removeTeilnehmerkreis(any(), any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(
        (getPruefungOfReadOnlyPruefung(roHaskel)));

    assertThat(deviceUnderTest.removeTeilnehmerKreis(roHaskel, informatik)).isEmpty();
  }

  @Test
  void removeTeilnehmerkreis_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    ReadOnlyPruefung pruefung = converter.convertToReadOnlyPruefung(
        getRandomPruefungWith(1L, informatik));

    when(this.dataAccessService.removeTeilnehmerkreis(any(), any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);

    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.removeTeilnehmerKreis(pruefung, informatik));
  }

  @Test
  void removeTeilnehmerkreis_pruefungDoesNotExist() throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    ReadOnlyPruefung pruefung = converter.convertToReadOnlyPruefung(
        getRandomPruefungWith(1L, informatik));

    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);

    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.removeTeilnehmerKreis(pruefung, informatik));
  }

  @Test
  void addTeilnehmerkreis_successful_withSoft()
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);

    LocalDateTime date = LocalDateTime.of(2021, 8, 11, 9, 0);

    ReadOnlyPruefung roHaskel = new PruefungDTOBuilder().withPruefungsName("Haskel")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("haskell").withStartZeitpunkt(date)
        .withAdditionalPruefer("Schmidt").build();

    ReadOnlyPruefung roDM = new PruefungDTOBuilder().withPruefungsName("roDM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("roDM")
        //Hiermit sollte dann aufjedenfall mehrePruefunganeinemTag verletzt werden
        .withAdditionalTeilnehmerkreis(informatik).withStartZeitpunkt(date.plusMinutes(180))
        .withAdditionalPruefer("Schmidt").build();

    Pruefung haskell = getPruefungOfReadOnlyPruefung(roHaskel);
    Pruefung dm = getPruefungOfReadOnlyPruefung(roDM);

    int schaetzungInformatik = 8;

    Set<Pruefung> conflictedPruefung = new HashSet<>();
    conflictedPruefung.add(haskell);
    conflictedPruefung.add(dm);

    when(restrictionService.getPruefungenAffectedBy(haskell)).thenReturn(conflictedPruefung);
    when(this.dataAccessService.getPruefung(roHaskel)).thenReturn(haskell);
    when(this.dataAccessService.setTeilnehmerkreis(haskell, informatik,
        schaetzungInformatik)).thenReturn(true);

    assertThat(deviceUnderTest.addTeilnehmerkreis(roHaskel, informatik,
        schaetzungInformatik)).containsAll(conflictedPruefung);
  }

  @Test
  void addTeilnehmerkreis_hart() throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);

    LocalDateTime date = LocalDateTime.of(2021, 8, 11, 9, 0);

    ReadOnlyPruefung roHaskel = new PruefungDTOBuilder().withPruefungsName("Haskel")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("haskell").withStartZeitpunkt(date)
        .withAdditionalPruefer("Schmidt").build();

    ReadOnlyPruefung roDM = new PruefungDTOBuilder().withPruefungsName("roDM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("roDM")
        //Hiermit sollte dann aufjedenfall mehrePruefunganeinemTag verletzt werden
        .withAdditionalTeilnehmerkreis(informatik).withStartZeitpunkt(date)
        .withAdditionalPruefer("Schmidt").build();

    Pruefung haskell = getPruefungOfReadOnlyPruefung(roHaskel);
    Pruefung dm = getPruefungOfReadOnlyPruefung(roDM);

    int schaetzungInformatik = 8;
    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, schaetzungInformatik);
    Set<Pruefung> conflictedPruefung = new HashSet<>();
    conflictedPruefung.add(haskell);
    conflictedPruefung.add(dm);

    HardRestrictionAnalysis hKA = new HardRestrictionAnalysis(conflictedPruefung,
        ZWEI_KLAUSUREN_GLEICHZEITIG, teilnehmerCount);

    List<HardRestrictionAnalysis> listHard = new ArrayList<>();
    listHard.add(hKA);
    when(restrictionService.checkHardRestrictions(haskell)).thenReturn(listHard);
    when(restrictionService.getPruefungenAffectedBy(haskell)).thenReturn(conflictedPruefung);
    when(this.dataAccessService.getPruefung(roHaskel)).thenReturn(haskell);
    when(this.dataAccessService.setTeilnehmerkreis(haskell, informatik,
        schaetzungInformatik)).thenReturn(true);

    assertThrows(HartesKriteriumException.class,
        () -> deviceUnderTest.addTeilnehmerkreis(roHaskel, informatik, schaetzungInformatik));
    assertThat(haskell.getTeilnehmerkreise()).isEmpty();
  }

  @Test
  void addTeilnehmerkreis_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.addTeilnehmerkreis(getRandomUnplannedROPruefung(1L),
            mock(Teilnehmerkreis.class), 42));
  }

  @Test
  void setTeilnehmerkreisSchaetzung_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setTeilnehmerkreisSchaetzung(getRandomUnplannedROPruefung(1L),
            mock(Teilnehmerkreis.class), 42));
  }

  @Test
  void setTeilnehmerkreisSchaetzung_PruefungDoesNotExist()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.setTeilnehmerkreisSchaetzung(getRandomUnplannedROPruefung(1L),
            mock(Teilnehmerkreis.class), 42));
  }


  @Test
  void setTeilnehmerkreisSchaetzung_not_planned() throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor);
    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);

    int newSchaetzung = 300;
    deviceUnderTest.setTeilnehmerkreisSchaetzung(RO_ANALYSIS_UNPLANNED, infBachelor, newSchaetzung);
    assertThat(analysis.getSchaetzungen()).containsEntry(infBachelor, newSchaetzung);
  }

  @Test
  void setTeilnehmerkreisSchaetzung_teilnehmerkreis_not_part_of_pruefung()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlMaster);
    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    int newSchaetzung = 300;
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setTeilnehmerkreisSchaetzung(RO_ANALYSIS_UNPLANNED, infBachelor,
            newSchaetzung));
  }

  @Test
  void setTeilnehmerkreisSchaetzung_no_conflicts() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime time = LocalDateTime.of(2022, 2, 10, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlMaster);
    analysis.setStartzeitpunkt(time);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infMaster);
    dm.setStartzeitpunkt(time);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);

    int schaetzung = 20;
    assertThat(deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
        infMaster, schaetzung)).isEmpty();
  }

  @Test
  void setTeilnehmerkreisSchaetzung_change_does_not_affect_other_pruefung()
      throws NoPruefungsPeriodeDefinedException {
    // make sure date is not a Sunday, otherwise restriction could be violated
    LocalDateTime day = LocalDateTime.of(2022, 1, 5, 10, 0);
    LocalDateTime dayAfter = LocalDateTime.now().plusDays(1);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl("t", "ord", 1,
        Ausbildungsgrad.MASTER);

    int oldSchaetzung = 12;
    analysis.addTeilnehmerkreis(teilnehmerkreis, oldSchaetzung);
    analysis.setStartzeitpunkt(day);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(teilnehmerkreis, oldSchaetzung);
    dm.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);

    int newSchaetzung = 20;
    assertThat(deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
        teilnehmerkreis, newSchaetzung)).isEmpty();
    assertThat(analysis.getSchaetzungen()).containsEntry(teilnehmerkreis, oldSchaetzung);

    assertThat(dm.getSchaetzungen()).containsEntry(teilnehmerkreis, newSchaetzung);
  }

  @Test
  void setTeilnehmerkreisSchaetzung_too_many_students() throws NoPruefungsPeriodeDefinedException {
    // make sure date is not a Sunday, otherwise restriction could be violated
    LocalDateTime day = LocalDateTime.of(2022, 1, 5, 10, 0);
    LocalDateTime dayAfter = LocalDateTime.now().plusDays(1);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl("t", "ord", 1,
        Ausbildungsgrad.MASTER);

    int oldSchaetzung = 12;
    analysis.addTeilnehmerkreis(teilnehmerkreis, oldSchaetzung);
    analysis.setStartzeitpunkt(day);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(teilnehmerkreis, oldSchaetzung);
    dm.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);

    int newSchaetzung = Integer.MAX_VALUE;
    when(restrictionService.getPruefungenAffectedBy(dm)).thenReturn(Set.of(dm, analysis));
    assertThat(deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
        teilnehmerkreis, newSchaetzung)).containsAll(Set.of(dm, analysis));
    assertThat(analysis.getSchaetzungen()).containsEntry(teilnehmerkreis, oldSchaetzung);
    assertThat(dm.getSchaetzungen()).containsEntry(teilnehmerkreis, newSchaetzung);
  }

  @Test
  void setTeilnehmerkreisSchaetzung_negativeSchaetzung() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime time = LocalDateTime.of(2022, 2, 10, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlMaster);
    analysis.setStartzeitpunkt(time);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infMaster);
    dm.setStartzeitpunkt(time);

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);

    int schaetzung = -10;
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
            infMaster, schaetzung));
  }

  @Test
  void addPruefungToBlock_blockMustNotBeNull() {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(12L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.addPruefungToBlock(null, pruefung));
  }

  @Test
  void addPruefungToBlock_pruefungMustNotBeNull() {
    ReadOnlyBlock emptyBlock = getEmptyROBlock();
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.addPruefungToBlock(emptyBlock, null));
  }

  private ReadOnlyBlock getEmptyROBlock() {
    LocalDateTime startTime = LocalDateTime.of(2022, 1, 7, 11, 11);
    Set<ReadOnlyPruefung> noPruefungen = emptySet();
    return new BlockDTO("someName", startTime, Duration.ZERO, noPruefungen, 123456, PARALLEL);
  }

  @Test
  void addPruefungToBlock_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.addPruefungToBlock(getEmptyROBlock(), pruefung));
  }

  @Test
  void addPruefungToBlock_plannedPruefungenMayNotBeAddedToBlocks()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung readOnlyPruefung = new PruefungDTOBuilder().withPruefungsNummer(
            RO_DM_UNPLANNED.getPruefungsnummer())
        .withStartZeitpunkt(LocalDateTime.of(2022, 2, 10, 10, 0))
        .build();
    Pruefung pruefung = getPruefungOfReadOnlyPruefung(readOnlyPruefung);
    when(dataAccessService.getPruefung(readOnlyPruefung)).thenReturn(pruefung);
    ReadOnlyBlock emptyBlock = getEmptyROBlock();
    when(dataAccessService.getBlock(emptyBlock)).thenReturn(
        new BlockImpl(mock(Pruefungsperiode.class), 1, "block", PARALLEL));
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(emptyBlock, readOnlyPruefung));
  }

  @Test
  void addPruefungToBlock_pruefungIsPartOfOtherBlock() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getRandomUnplannedROPruefung(1L);
    Block otherBlock = getUnplannedBlockWith1RandomPruefung();
    Pruefung pruefung = getPruefungOfReadOnlyPruefung(roPruefung);
    when(dataAccessService.getBlockTo(pruefung)).thenReturn(
        Optional.of(otherBlock));
    when(dataAccessService.getPruefung(roPruefung)).thenReturn(pruefung);
    ReadOnlyBlock emptyBlock = getEmptyROBlock();
    when(dataAccessService.getBlock(emptyBlock)).thenReturn(
        new BlockImpl(mock(Pruefungsperiode.class), 1, "name", PARALLEL));
    when(dataAccessService.getBlock(emptyBlock)).thenReturn(
        new BlockImpl(mock(Pruefungsperiode.class), 1, "test", PARALLEL));
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(emptyBlock, roPruefung));
  }

  private Block getUnplannedBlockWith1RandomPruefung() {
    return new BlockImpl(mock(Pruefungsperiode.class), 19982022, "someName", PARALLEL);
  }

  @Test
  void addPruefungToBlock_pruefungIsAlreadyInSameBlock()
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung roPruefung = getRandomUnplannedROPruefung(1L);
    Block block = getUnplannedBlockWith1RandomPruefung();
    ReadOnlyBlock readOnlyBlock = new BlockDTO("block", null, roPruefung.getDauer(),
        Set.of(roPruefung), 1, PARALLEL);
    Pruefung pruefung = getPruefungOfReadOnlyPruefung(roPruefung);
    block.addPruefung(pruefung);
    when(dataAccessService.getBlockTo(pruefung)).thenReturn(Optional.of(block));
    when(dataAccessService.getPruefung(roPruefung)).thenReturn(pruefung);
    when(dataAccessService.getBlock(readOnlyBlock)).thenReturn(block);
    assertThat(deviceUnderTest.addPruefungToBlock(readOnlyBlock, roPruefung)).isEmpty();
  }

  private ReadOnlyBlock getPlannedBlockWith(ReadOnlyPruefung pruefung) {
    LocalDateTime start = LocalDateTime.of(2022, 2, 7, 22, 54, 0);
    return new BlockDTO("name", start, Duration.ofHours(2), Set.of(pruefung), 9753, PARALLEL);
  }

  @Test
  void addPruefungToBlock_pruefungIsAlreadyInSameBlock_empty_result()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    ReadOnlyBlock block = getPlannedBlockWith(RO_ANALYSIS_UNPLANNED);
    Block modelBlock = new BlockImpl(mock(Pruefungsperiode.class), 1, "block", PARALLEL);
    modelBlock.addPruefung(pruefung);

    when(dataAccessService.getBlock(block)).thenReturn(modelBlock);
    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(
        pruefung);

    assertThat(deviceUnderTest.addPruefungToBlock(block, RO_ANALYSIS_UNPLANNED)).isEmpty();
  }

  @Test
  void addPruefungToBlock_unplanned_ContainsAtLeastChangedBlock()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    ReadOnlyBlock block = getUnplannedBlockWith();
    Block modelBlock = new BlockImpl(mock(Pruefungsperiode.class), block.getBlockId(),
        block.getName(), block.getTyp());
    when(dataAccessService.getBlock(block)).thenReturn(modelBlock);
    when(dataAccessService.getBlockTo(pruefung)).thenReturn(Optional.of(modelBlock));
    when(dataAccessService.getBlock(block)).thenReturn(modelBlock);
    when(dataAccessService.getBlockTo(argThat(new IsNotMatcher<>(pruefung)))).thenReturn(
        Optional.empty());
    when(dataAccessService.addPruefungToBlock(any(), any())).thenReturn(modelBlock);
    when(dataAccessService.getPruefung(pruefung)).thenReturn(mock(Pruefung.class));

    assertThat(deviceUnderTest.addPruefungToBlock(block, pruefung)).contains(block);
  }

  @Test
  void addPruefungToBlock_planned_ContainsAtLeastChangedBlock()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    ReadOnlyBlock block = getPlannedBlockWith(getRandomUnplannedROPruefung(2L));
    Block modelBlock = mock(Block.class);
    when(modelBlock.getId()).thenReturn(block.getBlockId());
    when(modelBlock.isGeplant()).thenReturn(true);

    when(dataAccessService.getBlockTo(pruefung)).thenReturn(Optional.of(modelBlock));
    when(dataAccessService.getBlock(block)).thenReturn(
        modelBlock);
    when(dataAccessService.getBlockTo(argThat(new IsNotMatcher<>(pruefung)))).thenReturn(
        Optional.empty());
    when(dataAccessService.addPruefungToBlock(any(), any())).thenReturn(modelBlock);
    when(dataAccessService.getPruefung(pruefung)).thenReturn(mock(Pruefung.class));

    assertThat(deviceUnderTest.addPruefungToBlock(block, pruefung)).contains(block);
  }

  @Test
  void removePruefungFromBlock_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    ReadOnlyBlock block = getPlannedBlockWith();
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.removePruefungFromBlock(block, RO_HASKELL_UNPLANNED));
  }

  private ReadOnlyBlock getUnplannedBlockWith(ReadOnlyPruefung... pruefungen) {
    return new BlockDTO("someName", null, Duration.ZERO, Set.of(pruefungen), 123456, PARALLEL);
  }

  @Test
  void addPruefungToBlock_pruefung_with_same_teilnehmerkreis_at_same_time()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 1, 2, 8, 0);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    haskell.addTeilnehmerkreis(infBachelor, 13);
    analysis.addTeilnehmerkreis(infBachelor, 13);

    Block block = getModelBlockWithPruefungen("block", termin, haskell);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);

    when(dataAccessService.getBlockTo(haskell)).thenReturn(Optional.of(block));
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(restrictionService.checkHardRestrictions(any(Pruefung.class))).thenReturn(
        List.of(getNewHartesKriteriumAnalyse()));
    when(dataAccessService.addPruefungToBlock(roBlock, RO_ANALYSIS_UNPLANNED)).thenReturn(block);
    when(dataAccessService.getPruefung(any())).thenReturn(analysis);
    when(dataAccessService.removePruefungFromBlock(roBlock,
        converter.convertToReadOnlyPruefung(analysis)))
        .thenReturn(block);

    assertThrows(HartesKriteriumException.class, () -> deviceUnderTest.addPruefungToBlock(roBlock,
        converter.convertToReadOnlyPruefung(analysis)));
  }

  private Block getModelBlockWithPruefungen(String name,
      LocalDateTime termin, Pruefung... pruefungen) throws NoPruefungsPeriodeDefinedException {
    Block result = new BlockImpl(mock(Pruefungsperiode.class), name, SEQUENTIAL);
    for (Pruefung pruefung : pruefungen) {
      result.addPruefung(pruefung);
      when(dataAccessService.getBlockTo(pruefung)).thenReturn(Optional.of(result));
    }
    if (termin != null) {
      result.setStartzeitpunkt(termin);
    }
    return result;
  }

  private HardRestrictionAnalysis getNewHartesKriteriumAnalyse() {
    Random random = new Random(1L);
    int next = random.nextInt();
    Map<Teilnehmerkreis, Integer> teilnehmercount = new HashMap<>();
    teilnehmercount.put(getRandomTeilnehmerkreis(1L), next);
    return new HardRestrictionAnalysis(
        Set.of(getPruefungOfReadOnlyPruefung(getRandomUnplannedROPruefung(1L)))
        , ZWEI_KLAUSUREN_GLEICHZEITIG,
        teilnehmercount);
  }

  @Test
  @DisplayName("addPruefungToBlock - hard restriction - check that pruefung does not get planned")
  void addPruefungToBlock_pruefung_with_same_teilnehmerkreis_hard_restriction()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 1, 2, 8, 0);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    haskell.addTeilnehmerkreis(infBachelor, 13);
    analysis.addTeilnehmerkreis(infBachelor, 13);

    Block block = getModelBlockWithPruefungen("block", termin, haskell);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);

    when(dataAccessService.getBlockTo(haskell)).thenReturn(Optional.of(block));
    when(dataAccessService.getBlockTo(analysis)).thenReturn(Optional.of(block));
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(restrictionService.checkHardRestrictions(anySet())).thenReturn(
        List.of(getNewHartesKriteriumAnalyse()));
    when(dataAccessService.addPruefungToBlock(roBlock, RO_ANALYSIS_UNPLANNED)).thenReturn(block);
    when(dataAccessService.getPruefung(RO_HASKELL_UNPLANNED)).thenReturn(haskell);
    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.removePruefungFromBlock(any(), any())).thenReturn(block);
    try {
      deviceUnderTest.addPruefungToBlock(roBlock, converter.convertToReadOnlyPruefung(analysis));
    } catch (HartesKriteriumException hardViolation) {
      verify(dataAccessService, times(1)).removePruefungFromBlock(roBlock, RO_ANALYSIS_UNPLANNED);
    }
  }

  private ReadOnlyBlock getPlannedBlockWith(ReadOnlyPruefung... pruefungen) {
    LocalDateTime startTime = LocalDateTime.of(2022, 1, 7, 11, 11);
    return new BlockDTO("someName", startTime, Duration.ZERO, Set.of(pruefungen), 123456, PARALLEL);
  }

  @Test
  void removePruefungFromBlock_pruefung_empty_block() throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomUnplannedPruefung(1L);
    ReadOnlyBlock block = getPlannedBlockWith();
    when(dataAccessService.getPruefung(any())).thenReturn(pruefung);
    assertThat(deviceUnderTest.removePruefungFromBlock(block,
        converter.convertToReadOnlyPruefung(pruefung))).isEmpty();
  }

  @Test
  void removePruefungFromBlock_pruefung_empty_block_remove_pruefung_data_access_doesnt_get_called()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomUnplannedPruefung(1L);
    ReadOnlyBlock block = getPlannedBlockWith();
    when(dataAccessService.getPruefung(any())).thenReturn(pruefung);
    deviceUnderTest.removePruefungFromBlock(block, converter.convertToReadOnlyPruefung(pruefung));
    verify(dataAccessService, times(0)).removePruefungFromBlock(block,
        converter.convertToReadOnlyPruefung(pruefung));
  }

  @Test
  void removePruefungFromBlock_pruefung_not_in_block() throws NoPruefungsPeriodeDefinedException {
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    ReadOnlyPruefung pruefungInBlock = getRandomUnplannedROPruefung(1L);
    ReadOnlyBlock block = getPlannedBlockWith(pruefungInBlock);
    when(dataAccessService.getPruefung(RO_HASKELL_UNPLANNED)).thenReturn(haskell);
    assertThat(deviceUnderTest.removePruefungFromBlock(block, RO_HASKELL_UNPLANNED)).isEmpty();

  }

  @Test
  @DisplayName("remove pruefung from block - dataAccessService removePruefungFromBlock does not get called")
  void removePruefungFromBlock_pruefung_not_in_block_verify()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    ReadOnlyPruefung pruefungInBlock = getRandomUnplannedROPruefung(1L);
    ReadOnlyBlock block = getPlannedBlockWith(pruefungInBlock);
    when(dataAccessService.getPruefung(RO_HASKELL_UNPLANNED)).thenReturn(haskell);
    deviceUnderTest.removePruefungFromBlock(block, RO_HASKELL_UNPLANNED);
    verify(dataAccessService, times(0)).removePruefungFromBlock(any(), any());
  }

  @Test
  void removePruefungFromBlock_aPruefungInROBlockDoesntExistInModel()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    ReadOnlyBlock roBlock = new BlockDTO("block", null, RO_ANALYSIS_UNPLANNED.getDauer(),
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED), 1, PARALLEL);
    Block block = new BlockImpl(mock(Pruefungsperiode.class), 1, "name", PARALLEL);
    block.addPruefung(haskell);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(dataAccessService.getPruefung(RO_HASKELL_UNPLANNED)).thenReturn(haskell);
    when(dataAccessService.getBlockTo(haskell)).thenReturn(Optional.of(block));
    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenThrow(
        IllegalStateException.class);

    assertThrows(IllegalStateException.class, () -> deviceUnderTest.removePruefungFromBlock(roBlock,
        RO_HASKELL_UNPLANNED));

  }

  @Test
  void removePruefungFromBlock_BlockDoesNotExist()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    ReadOnlyBlock roBlock = new BlockDTO("block", null, RO_ANALYSIS_UNPLANNED.getDauer(),
        Set.of(RO_HASKELL_UNPLANNED), 1, PARALLEL);
    when(dataAccessService.getBlock(roBlock)).thenThrow(IllegalStateException.class);
    when(dataAccessService.getPruefung(RO_HASKELL_UNPLANNED)).thenReturn(haskell);
    when(dataAccessService.getBlockTo(haskell)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class, () -> deviceUnderTest.removePruefungFromBlock(roBlock,
        RO_HASKELL_UNPLANNED));

  }


  @Test
  void getGeplantePruefungenWithKonflikt_noNullParametersAllowed() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(null));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_noPruefungsPeriode()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(null));
  }

  @Test
  void setDauer_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setDauer(mock(ReadOnlyPruefung.class), Duration.ofMinutes(230)));
  }

  @Test
  void setDauer_Successful() throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LocalDateTime time = LocalDateTime.of(2021, 8, 12, 8, 0);
    LocalDateTime timeb = LocalDateTime.of(2021, 8, 11, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);

    analysis.setStartzeitpunkt(time);
    dm.setStartzeitpunkt(timeb);

    assertThat(analysis.getDauer()).isEqualTo(RO_ANALYSIS_UNPLANNED.getDauer());
    assertThat(deviceUnderTest.setDauer(RO_ANALYSIS_UNPLANNED, Duration.ofMinutes(90))).isEmpty();
  }

  @Test
  void setDauer_Successful_checkSoft()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LocalDateTime time = LocalDateTime.of(2021, 8, 12, 8, 0);
    LocalDateTime timeb = LocalDateTime.of(2021, 8, 11, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);
    analysis.setStartzeitpunkt(time);
    dm.setStartzeitpunkt(timeb);

    assertThat(analysis.getDauer()).isEqualTo(RO_ANALYSIS_UNPLANNED.getDauer());
    assertThat(deviceUnderTest.setDauer(RO_ANALYSIS_UNPLANNED, Duration.ofMinutes(90))).isEmpty();
  }

  @Test
  void setDauer_Unsuccessful() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime time = LocalDateTime.of(2021, 8, 12, 8, 0);
    LocalDateTime timeb = LocalDateTime.of(2021, 8, 12, 10, 30);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.setStartzeitpunkt(time);
    dm.setStartzeitpunkt(timeb);
    analysis.addTeilnehmerkreis(infBachelor, 8);
    dm.addTeilnehmerkreis(infBachelor, 8);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));

    when(dataAccessService.getPruefung(RO_ANALYSIS_UNPLANNED)).thenReturn(analysis);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(infBachelor, 8);
    HardRestrictionAnalysis hka = new HardRestrictionAnalysis(Set.of(analysis, dm),
        ZWEI_KLAUSUREN_GLEICHZEITIG, teilnehmerCount);
    when(restrictionService.checkHardRestrictions(any(Pruefung.class))).thenReturn(List.of(hka));

    assertThat(analysis.getDauer()).isEqualTo(RO_ANALYSIS_UNPLANNED.getDauer());
    assertThrows(HartesKriteriumException.class,
        () -> deviceUnderTest.setDauer(RO_ANALYSIS_UNPLANNED, Duration.ofMinutes(150)));
  }

  @Test
  void setDauer_negativeDuration() {
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setDauer(mock(ReadOnlyPruefung.class), Duration.ofMinutes(-1)));
  }

  @Test
  void setDauer_durationZero() {
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setDauer(mock(ReadOnlyPruefung.class), Duration.ofMinutes(0)));
  }


  @Test
  void analyseScoring_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(NoPruefungsPeriodeDefinedException.class);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.analyseScoring(getRandomPlannedROPruefung(1L)));
  }

  @Test
  void analyseScoring_no_restriction_violations() throws NoPruefungsPeriodeDefinedException {
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    when(restrictionService.checkWeicheKriterien(dm)).thenReturn(Collections.emptyList());
    when(dataAccessService.getPruefung(any())).thenReturn(dm);
    assertThat(deviceUnderTest.analyseScoring(RO_DM_UNPLANNED)).isEmpty();
  }

  @Test
  void analyseScoring_restrictions_violated() throws NoPruefungsPeriodeDefinedException {
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    SoftRestrictionAnalysis softRestrictionAnalysis = new SoftRestrictionAnalysis(Set.of(analysis),
        UNIFORME_ZEITSLOTS,
        Set.of(infBachelor), 10, 100);
    when(dataAccessService.getPruefung(RO_DM_UNPLANNED)).thenReturn(dm);
    when(restrictionService.checkWeicheKriterien(dm)).thenReturn(List.of(softRestrictionAnalysis));

    List<KriteriumsAnalyse> result = deviceUnderTest.analyseScoring(RO_DM_UNPLANNED);
    assertThat(result).isNotEmpty().hasSize(1);
    assertThat(result.get(0)).isNotNull();
    KriteriumsAnalyse resultAnalyse = result.get(0);
    assertThat(resultAnalyse.getBetroffenePruefungen()).containsOnly(RO_ANALYSIS_UNPLANNED);
    assertThat(resultAnalyse.getKriterium()).isEqualTo(UNIFORME_ZEITSLOTS);
    assertThat(resultAnalyse.getTeilnehmer()).containsOnly(infBachelor);
    assertThat(resultAnalyse.getAnzahlBetroffenerStudenten()).isEqualTo(10);
  }

  @Test
  void analyseScoring_pruefungDoesNotExist() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.analyseScoring(RO_ANALYSIS_UNPLANNED));
  }

  @Test
  void getHardConflictedTimes_timesToCheckMustNotBeNull() {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
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
  void getHardConflictedTimes_planungseinheitIsUnknownPruefung()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);
    Set<LocalDateTime> times = emptySet();
    ReadOnlyPruefung pruefung = getRandomPlannedROPruefung(1L);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.getHardConflictedTimes(times, pruefung));
  }

  @Test
  void getHardConflictedTimes_planungseinheitIsUnknownBlock()
      throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getBlock(any())).thenThrow(IllegalStateException.class);
    Set<LocalDateTime> times = emptySet();
    ReadOnlyBlock block = getEmptyROBlock();
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.getHardConflictedTimes(times, block));
  }

  @Test
  void getHardConflictedTimes_checkingUnplannedPruefungResultsInNoBlocking()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung planungseinheit = getRandomUnplannedPruefung(1L);
    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(planungseinheit);
    assertThat(deviceUnderTest.getHardConflictedTimes(emptySet(),
        getRandomUnplannedROPruefung(1L))).isEmpty();
  }

  @Test
  void getHardConflictedTimes_checkHardCriteriaForEachTimeToCheck_oneTime()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungToCheckFor = getRandomPlannedPruefung(1L);
    LocalDateTime start = LocalDateTime.of(2022, 3, 4, 12, 12);

    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(pruefungToCheckFor);
    when(dataAccessService.getStartOfPeriode()).thenReturn(LocalDate.of(2022, 3, 3));
    when(dataAccessService.getEndOfPeriode()).thenReturn(LocalDate.of(2022, 3, 10));

    deviceUnderTest.getHardConflictedTimes(Set.of(start),
        converter.convertToReadOnlyPruefung(pruefungToCheckFor));
    verify(restrictionService, times(1)).wouldBeHardConflictIfStartedAt(any(), any());
  }

  @Test
  void getHardConflictedTimes_checkHardCriteriaForEachTimeToCheck_multipleTimesToCheck()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungToCheckFor = getRandomPlannedPruefung(1L);
    LocalDateTime start = LocalDateTime.of(2022, 3, 4, 12, 12);

    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(pruefungToCheckFor);
    when(dataAccessService.getStartOfPeriode()).thenReturn(LocalDate.of(2022, 3, 3));
    when(dataAccessService.getEndOfPeriode()).thenReturn(LocalDate.of(2022, 3, 10));

    deviceUnderTest.getHardConflictedTimes(Set.of(start, start.plusDays(1)),
        converter.convertToReadOnlyPruefung(pruefungToCheckFor));
    verify(restrictionService, times(2)).wouldBeHardConflictIfStartedAt(any(), any());
  }

  @Test
  void getHardConflictedTimes_checkHardCriteriaForEachTimeToCheck_noTimes()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung planungseinheit = getRandomUnplannedPruefung(1L);

    when(dataAccessService.getPruefung(any())).thenReturn(planungseinheit);
    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);

    deviceUnderTest.getHardConflictedTimes(emptySet(), getRandomUnplannedROPruefung(1L));
    verify(restrictionService, never()).wouldBeHardConflictIfStartedAt(any(), any());
  }

  @Test
  void getHardConflictedTimes_resultContainsAsManyEntriesAsConflicts_none()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungToCheckFor = getRandomPlannedPruefung(1L);
    LocalDateTime start = LocalDateTime.of(2022, 3, 4, 12, 12);

    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(pruefungToCheckFor);
    when(restrictionService.wouldBeHardConflictIfStartedAt(any(), any())).thenReturn(false);
    when(dataAccessService.getStartOfPeriode()).thenReturn(LocalDate.of(2022, 3, 3));
    when(dataAccessService.getEndOfPeriode()).thenReturn(LocalDate.of(2022, 3, 10));

    assertThat(deviceUnderTest.getHardConflictedTimes(
        Set.of(start, start.plusDays(1), start.plusDays(2)),
        converter.convertToReadOnlyPruefung(pruefungToCheckFor))).isEmpty();
  }

  @Test
  void getHardConflictedTimes_resultContainsAsManyEntriesAsConflicts_one()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungToCheckFor = getRandomPlannedPruefung(1L);
    LocalDateTime start = LocalDateTime.of(2022, 3, 4, 12, 12);

    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(pruefungToCheckFor);
    when(restrictionService.wouldBeHardConflictIfStartedAt(any(), any())).thenReturn(false, true,
        false);
    when(dataAccessService.getStartOfPeriode()).thenReturn(LocalDate.of(2022, 3, 3));
    when(dataAccessService.getEndOfPeriode()).thenReturn(LocalDate.of(2022, 3, 10));

    assertThat(deviceUnderTest.getHardConflictedTimes(
        Set.of(start, start.plusDays(1), start.plusDays(2)),
        converter.convertToReadOnlyPruefung(pruefungToCheckFor))).hasSize(1);
  }

  @Test
  void getHardConflictedTimes_resultContainsAsManyEntriesAsConflicts_multiple()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungToCheckFor = getRandomPlannedPruefung(1L);
    LocalDateTime start = LocalDateTime.of(2022, 3, 4, 12, 12);

    when(dataAccessService.existsPruefungWith(any())).thenReturn(true);
    when(dataAccessService.getPruefung(any())).thenReturn(pruefungToCheckFor);
    when(restrictionService.wouldBeHardConflictIfStartedAt(any(), any())).thenReturn(true, true,
        true);
    when(dataAccessService.getStartOfPeriode()).thenReturn(LocalDate.of(2022, 3, 3));
    when(dataAccessService.getEndOfPeriode()).thenReturn(LocalDate.of(2022, 3, 10));

    assertThat(deviceUnderTest.getHardConflictedTimes(
        Set.of(start, start.plusDays(1), start.plusDays(2)),
        converter.convertToReadOnlyPruefung(pruefungToCheckFor))).hasSize(3);
  }

  @Test
  void makeBlockSequential_block_is_already_sequential()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(SEQUENTIAL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);

    assertThat(deviceUnderTest.setBlockType(roBlock, SEQUENTIAL)).isEmpty();
  }

  @Test
  void makeBlockSequential_block_is_parallel_but_not_planned()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);

    Block block = getModelBlockWithPruefungen("block", null, analysis, haskell);
    block.setTyp(PARALLEL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);

    assertThat(deviceUnderTest.setBlockType(roBlock, SEQUENTIAL)).contains(roBlock);
  }

  @Test
  void makeBlockSequential_block_does_not_exist() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getBlock(any())).thenThrow(IllegalStateException.class);
    ReadOnlyBlock block = getEmptyROBlock();
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.setBlockType(block, SEQUENTIAL));
  }

  @Test
  void makeBlockSequential_pruefungInBlockDoesNotExist() throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = new BlockImpl(mock(Pruefungsperiode.class), 1, "name", PARALLEL);
    when(dataAccessService.getBlock(any())).thenReturn(modelBlock);
    modelBlock.addPruefung(getRandomUnplannedPruefung(1));
    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);
    ReadOnlyBlock block = new BlockDTO("name", null, RO_DM_UNPLANNED.getDauer(),
        Set.of(RO_DM_UNPLANNED), 1, PARALLEL);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.setBlockType(block, SEQUENTIAL));
  }

  @Test
  void makeBlockParallel_pruefungInBlockDoesNotExist() throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = new BlockImpl(mock(Pruefungsperiode.class), 1, "name", SEQUENTIAL);
    when(dataAccessService.getBlock(any())).thenReturn(modelBlock);
    modelBlock.addPruefung(getRandomUnplannedPruefung(1));
    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);
    ReadOnlyBlock block = new BlockDTO("name", null, RO_DM_UNPLANNED.getDauer(),
        Set.of(RO_DM_UNPLANNED), 1, SEQUENTIAL);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.setBlockType(block, PARALLEL));
  }


  @Test
  void makeBlockSequential_hard_restriction_failures() throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(PARALLEL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    List<HardRestrictionAnalysis> hardKriterien = new LinkedList<>();

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    hardKriterien.add(new HardRestrictionAnalysis(Collections.emptySet(),
        ZWEI_KLAUSUREN_GLEICHZEITIG, teilnehmerCount));
    when(restrictionService.checkHardRestrictions(Set.of(analysis, haskell))).thenReturn(
        hardKriterien);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        new HashSet<>(Set.of(analysis, haskell)));
    assertThrows(HartesKriteriumException.class,
        () -> deviceUnderTest.setBlockType(roBlock, SEQUENTIAL));
  }

  @Test
  void makeBlockSequential_successful()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(PARALLEL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        Collections.emptySet());
    when(restrictionService.checkHardRestrictions(Set.of(analysis, haskell))).thenReturn(
        Collections.emptyList());

    assertThat(deviceUnderTest.setBlockType(roBlock, SEQUENTIAL)).containsExactly(roBlock);
  }

  @Test
  void makeBlockSequential_soft_restrictions()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(PARALLEL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        new HashSet<>(Set.of(analysis)));
    when(restrictionService.checkHardRestrictions(Set.of(analysis, haskell))).thenReturn(
        Collections.emptyList());

    assertThat(deviceUnderTest.setBlockType(roBlock, SEQUENTIAL)).containsExactlyInAnyOrder(
        roBlock, RO_ANALYSIS_UNPLANNED);
  }

  @Test
  void makeBlockParallel_block_is_already_Parallel()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(PARALLEL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);

    assertThat(deviceUnderTest.setBlockType(roBlock, PARALLEL)).isEmpty();
  }

  @Test
  void makeBlockParallel_block_is_sequential_but_not_planned()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);

    Block block = getModelBlockWithPruefungen("block", null, analysis, haskell);
    block.setTyp(SEQUENTIAL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);

    assertThat(deviceUnderTest.setBlockType(roBlock, PARALLEL)).contains(roBlock);
  }

  @Test
  void makeBlockParallel_block_does_not_exist() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getBlock(any())).thenThrow(IllegalStateException.class);
    ReadOnlyBlock block = getEmptyROBlock();
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.setBlockType(block, PARALLEL));
  }

  @Test
  void makeBlockParallel_hard_restriction_failures() throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(SEQUENTIAL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    List<HardRestrictionAnalysis> hardKriterien = new LinkedList<>();

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();

    hardKriterien.add(new HardRestrictionAnalysis(Collections.emptySet(),
        ZWEI_KLAUSUREN_GLEICHZEITIG, teilnehmerCount));
    when(restrictionService.checkHardRestrictions(Set.of(analysis, haskell))).thenReturn(
        hardKriterien);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        new HashSet<>(Set.of(analysis, haskell)));
    assertThrows(HartesKriteriumException.class,
        () -> deviceUnderTest.setBlockType(roBlock, PARALLEL));
  }

  @Test
  void makeBlockParallel_successful()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(SEQUENTIAL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        Collections.emptySet());
    when(restrictionService.checkHardRestrictions(Set.of(analysis, haskell))).thenReturn(
        Collections.emptyList());

    assertThat(deviceUnderTest.setBlockType(roBlock, PARALLEL)).containsExactly(roBlock);
  }

  @Test
  void makeBlockParallel_soft_restrictions()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2, 2);
    Block block = getModelBlockWithPruefungen("block", termin, analysis, haskell);
    block.setTyp(SEQUENTIAL);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);
    when(dataAccessService.getBlock(roBlock)).thenReturn(block);
    when(restrictionService.getPruefungenAffectedByAnyBlock(any(Block.class))).thenReturn(
        new HashSet<>(Set.of(analysis)));
    when(restrictionService.checkHardRestrictions(Set.of(analysis, haskell))).thenReturn(
        Collections.emptyList());

    assertThat(deviceUnderTest.setBlockType(roBlock, PARALLEL)).containsExactlyInAnyOrder(
        roBlock, RO_ANALYSIS_UNPLANNED);
  }

  @Test
  void unschedulePruefung_pruefungDoesNotExist() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPruefung(any())).thenThrow(IllegalStateException.class);
    ReadOnlyPruefung pruefung = getRandomPlannedROPruefung(1L);
    assertThrows(IllegalStateException.class,
        () -> deviceUnderTest.unschedulePruefung(pruefung));
  }


  @Test
  void createNewPeriodeWithDataTest_kapazitaet_too_low()
      throws NoPruefungsPeriodeDefinedException {
    IOService ioService = new IOService(dataAccessService);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 9);
    int kapazitaet = -1;
    Path path = mock(Path.class);
    Path path2 = mock(Path.class);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));
    when(dataAccessService.getGeplanteBloecke()).thenReturn(Collections.emptySet());
    when(restrictionService.checkHardRestrictions(analysis)).thenReturn(
        List.of(mock(HardRestrictionAnalysis.class)));

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, path2));
  }

  @Test
  void createNewPeriodeWithDataTest_startMustBeBeforeEnd()
      throws NoPruefungsPeriodeDefinedException {
    IOService ioService = new IOService(dataAccessService);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate end = LocalDate.of(2022, 2, 2);
    LocalDate start = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 9);
    int kapazitaet = 100;
    Path path = mock(Path.class);
    Path path2 = mock(Path.class);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));
    when(dataAccessService.getGeplanteBloecke()).thenReturn(Collections.emptySet());
    when(restrictionService.checkHardRestrictions(analysis)).thenReturn(
        List.of(mock(HardRestrictionAnalysis.class)));

    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, path2));
  }

  @Test
  void createNewPeriodeWithDataTest_ankertagMustBeAfterStart()
      throws NoPruefungsPeriodeDefinedException {
    IOService ioService = new IOService(dataAccessService);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 1);
    int kapazitaet = 100;
    Path path = mock(Path.class);
    Path path2 = mock(Path.class);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));
    when(dataAccessService.getGeplanteBloecke()).thenReturn(Collections.emptySet());
    when(restrictionService.checkHardRestrictions(analysis)).thenReturn(
        List.of(mock(HardRestrictionAnalysis.class)));

    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, path2));
  }

  @Test
  void createNewPeriodeWithDataTest_ankertagMustBeforeEnd()
      throws NoPruefungsPeriodeDefinedException {
    IOService ioService = new IOService(dataAccessService);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 26);
    int kapazitaet = 100;
    Path path = mock(Path.class);
    Path path2 = mock(Path.class);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));
    when(dataAccessService.getGeplanteBloecke()).thenReturn(Collections.emptySet());
    when(restrictionService.checkHardRestrictions(analysis)).thenReturn(
        List.of(mock(HardRestrictionAnalysis.class)));

    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, path2));
  }

  @Test
  void createNewPeriodeWithDataTest_two_at_same_time()
      throws ImportException, IOException, NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    IOService ioService = mock(IOService.class);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 9);
    int kapazitaet = 300;
    Path path = mock(Path.class);
    Path path2 = mock(Path.class);

    doNothing().when(ioService)
        .createNewPeriodeWithData(semester, start, end, ankerTag, kapazitaet,
            path, path2);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));
    when(dataAccessService.getGeplanteBloecke()).thenReturn(Collections.emptySet());
    when(restrictionService.checkHardRestrictions(dm)).thenReturn(
        List.of(mock(HardRestrictionAnalysis.class)));

    try {
      deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
          kapazitaet, path, path2);

    } catch (ImportException | IllegalTimeSpanException e) {
      assertThat(dm.isGeplant()).isFalse();
      assertThat(analysis.isGeplant()).isTrue();
    }
  }

  @Test
  void createNewPeriodeWithDataTest_none_at_same_time()
      throws ImportException, IOException, NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    IOService ioService = mock(IOService.class);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 9);
    int kapazitaet = 300;
    Path path = mock(Path.class);

    doNothing().when(ioService)
        .createNewPeriodeWithData(semester, start, end, ankerTag, kapazitaet,
            path, null);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    LocalDateTime termin2 = LocalDateTime.of(2022, 2, 13, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin2);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, dm));
    when(dataAccessService.getGeplanteBloecke()).thenReturn(Collections.emptySet());
    when(restrictionService.checkHardRestrictions(dm)).thenReturn(Collections.emptyList());

    assertDoesNotThrow(
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, null));
    assertThat(dm.isGeplant()).isTrue();
    assertThat(analysis.isGeplant()).isTrue();
  }

  @Test
  void bulkTest_createNewPeriodeWithData()
      throws ImportException, IOException, IllegalTimeSpanException {
    int errorCount = 0;
    for (int i = 0; i < 100; i++) {
      try {
        createNewPeriodeWithDataTest_more_than_two_at_same_time();
      } catch (AssertionError e) {
        errorCount++;
      }
    }
  }

  @Test
  void createNewPeriodeWithDataTest_more_than_two_at_same_time()
      throws ImportException, IOException, IllegalTimeSpanException {

    IOService ioService = mock(IOService.class);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 9);
    int kapazitaet = 300;
    Path path = mock(Path.class);
    Pruefungsperiode pruefungsperiode = setUpPruefungsperiodeAndIgnoreIOService(semester, start,
        end, ankerTag, kapazitaet, path, ioService);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    addPruefungenToPruefungsperiode(pruefungsperiode, Set.of(analysis, dm, haskell));

    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    haskell.addTeilnehmerkreis(infBachelor, 40);
    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);
    haskell.setStartzeitpunkt(termin);

    assertThrows(ImportException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, null));
    assertThat(dm.isGeplant()).isFalse();
    assertThat(analysis.isGeplant()).isFalse();
    assertThat(haskell.isGeplant()).isTrue();
  }

  private Pruefungsperiode setUpPruefungsperiodeAndIgnoreIOService(Semester semester,
      LocalDate start, LocalDate end, LocalDate ankerTag, int kapazitaet, Path path,
      IOService ioService) throws ImportException, IOException, IllegalTimeSpanException {
    doNothing().when(ioService)
        .createNewPeriodeWithData(semester, start, end, ankerTag, kapazitaet,
            path, null);

    Pruefungsperiode pruefungsperiode = new PruefungsperiodeImpl(semester, start, end, ankerTag,
        kapazitaet);

    dataAccessService = ServiceProvider.getDataAccessService();
    dataAccessService.setPruefungsperiode(pruefungsperiode);
    restrictionService = ServiceProvider.getRestrictionService();
    deviceUnderTest = new ScheduleService(dataAccessService, restrictionService,
        ServiceProvider.getConverter());

    return pruefungsperiode;
  }

  private void addPruefungenToPruefungsperiode(Pruefungsperiode periode, Set<Pruefung> toAdd) {
    for (Pruefung pruefung : toAdd) {
      periode.addPlanungseinheit(pruefung);
    }
  }

  @Test
  void createNewPeriodeWithDataTest_two_at_same_time_different_days()
      throws ImportException, IOException, IllegalTimeSpanException {

    IOService ioService = mock(IOService.class);
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 2, 2);
    LocalDate end = LocalDate.of(2022, 2, 25);
    LocalDate ankerTag = LocalDate.of(2022, 2, 9);
    int kapazitaet = 300;
    Path path = mock(Path.class);
    Pruefungsperiode pruefungsperiode = setUpPruefungsperiodeAndIgnoreIOService(semester, start,
        end, ankerTag, kapazitaet, path, ioService);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);

    LocalDateTime termin = LocalDateTime.of(2022, 2, 12, 8, 0);
    LocalDateTime termin2 = LocalDateTime.of(2022, 2, 14, 8, 0);

    Pruefung pruefung4 = new PruefungImpl("4", "pruefung4", "ref", Duration.ofMinutes(120),
        null);
    addPruefungenToPruefungsperiode(pruefungsperiode, Set.of(analysis, dm, haskell, pruefung4));
    analysis.addTeilnehmerkreis(infBachelor, 30);
    dm.addTeilnehmerkreis(infBachelor, 12);
    haskell.addTeilnehmerkreis(infBachelor, 40);
    pruefung4.addTeilnehmerkreis(infBachelor, 50);

    analysis.setStartzeitpunkt(termin);
    dm.setStartzeitpunkt(termin);
    haskell.setStartzeitpunkt(termin2);
    pruefung4.setStartzeitpunkt(termin2);

    assertThrows(ImportException.class,
        () -> deviceUnderTest.createNewPeriodeWithData(ioService, semester, start, end, ankerTag,
            kapazitaet, path, null));
    assertThat(dm.isGeplant()).isFalse();
    assertThat(analysis.isGeplant()).isTrue();
    assertThat(haskell.isGeplant()).isFalse();
    assertThat(pruefung4.isGeplant()).isTrue();
  }

  @Test
  void setKapazitaetPeriode_noPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    doThrow(new NoPruefungsPeriodeDefinedException()).when(dataAccessService)
        .setKapazitaetStudents(anyInt());
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setKapazitaetPeriode(1));
  }

  @Test
  void setKapazitaetPeriode_checkFor0PlannedPruefung() throws NoPruefungsPeriodeDefinedException {
    int amountPlannedPruefungen = 0;
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, amountPlannedPruefungen);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(new HashSet<>(pruefungen));
    deviceUnderTest.setKapazitaetPeriode(1);
    verify(restrictionService, times(amountPlannedPruefungen)).getPruefungenAffectedBy(
        any(Pruefung.class));
  }

  @Test
  void setKapazitaetPeriode_checkFor1PlannedPruefung() throws NoPruefungsPeriodeDefinedException {
    int amountPlannedPruefungen = 1;
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, amountPlannedPruefungen);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(new HashSet<>(pruefungen));
    deviceUnderTest.setKapazitaetPeriode(1);
    verify(restrictionService, times(amountPlannedPruefungen)).getPruefungenAffectedBy(
        any(Pruefung.class));
  }

  @Test
  void setKapazitaetPeriode_checkFor2PlannedPruefung() throws NoPruefungsPeriodeDefinedException {
    int amountPlannedPruefungen = 2;
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, amountPlannedPruefungen);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(new HashSet<>(pruefungen));
    deviceUnderTest.setKapazitaetPeriode(1);
    verify(restrictionService, times(amountPlannedPruefungen)).getPruefungenAffectedBy(
        any(Pruefung.class));
  }

  @Test
  void unscheduleUnschedulePruefungTest() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Collections.emptySet());
    Pruefung unscheduledPruefung = TestFactory.P_ANALYSIS_UNPLANNED;
    ReadOnlyPruefung unscheduledRoPruefung = new PruefungDTOBuilder(unscheduledPruefung).build();
    when(dataAccessService.getPruefung(unscheduledRoPruefung)).thenReturn(unscheduledPruefung);

    deviceUnderTest.unschedulePruefung(unscheduledRoPruefung);
  }

  @Test
  void setKapazitaetPeriode_resultContainsAllAffected() throws NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 2);
    List<Pruefung> affectedPruefungen = getRandomPlannedPruefungen(2L, 7);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(new HashSet<>(pruefungen));
    when(restrictionService.getPruefungenAffectedBy(any(Pruefung.class))).thenReturn(
        new HashSet<>(affectedPruefungen.subList(0, 5)),
        new HashSet<>(affectedPruefungen.subList(5, 7)));
    assertThat(deviceUnderTest.setKapazitaetPeriode(11)).containsExactlyInAnyOrderElementsOf(
        affectedPruefungen);
  }

  @Test
  void setDatumPeriode_startAfterAnkertag() throws NoPruefungsPeriodeDefinedException {
    LocalDate start = LocalDate.of(2022, FEBRUARY, 7);
    LocalDate ankerTag = start.minusDays(1);
    LocalDate end = start.plusWeeks(1);

    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);

    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.setDatumPeriode(start, end));
  }

  @Test
  void setDatumPeriode_ankertagAfterEnd() throws NoPruefungsPeriodeDefinedException {
    LocalDate start = LocalDate.of(2022, FEBRUARY, 7);
    LocalDate end = start.plusWeeks(1);
    LocalDate ankerTag = end.plusDays(1);

    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);

    assertThrows(IllegalTimeSpanException.class,
        () -> deviceUnderTest.setDatumPeriode(start, end));
  }

  @Test
  void setDatumPeriode_startAfterPlannedPruefung() throws NoPruefungsPeriodeDefinedException {
    LocalDate start = LocalDate.of(2022, FEBRUARY, 7);
    LocalDate end = start.plusWeeks(1);
    Pruefung plannedPruefung = randomPruefungAt(start.atStartOfDay().minus(1, MINUTES));

    when(dataAccessService.getAnkertag()).thenReturn(start.plusDays(1));
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(plannedPruefung));

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setDatumPeriode(start, end));
  }

  private Pruefung randomPruefungAt(LocalDateTime schedule) {
    Pruefung result = getRandomPlannedPruefung(1L);
    result.setStartzeitpunkt(schedule);
    return result;
  }

  @Test
  void setDatumPeriode_endBeforePlannedPruefung() throws NoPruefungsPeriodeDefinedException {
    LocalDate start = LocalDate.of(2022, FEBRUARY, 7);
    LocalDate end = start.plusWeeks(1);
    Pruefung plannedPruefung = randomPruefungAt(end.plusDays(1).atStartOfDay());

    when(dataAccessService.getAnkertag()).thenReturn(start.plusDays(1));
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(plannedPruefung));

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setDatumPeriode(start, end));
  }

  @Test
  void scheduleBlockWithOnePruefungAtSameTime() {
    // test hard restriction because of integration test fail (this test is really an integration test)

    // controller preparation
    Semester semester = new SemesterImpl(Semestertyp.WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 1);
    LocalDate end = LocalDate.of(2022, 4, 1);
    LocalDate anker = LocalDate.of(2022, 2, 1);
    Pruefungsperiode pruefungsperiode = new PruefungsperiodeImpl(semester, start, end, anker, 400);
    dataAccessService = ServiceProvider.getDataAccessService();
    dataAccessService.setPruefungsperiode(pruefungsperiode);
    RestrictionService restrictionService = new RestrictionService();
    Converter converter = new Converter();
    ScheduleService scheduleService = new ScheduleService(dataAccessService, restrictionService,
        converter);
    converter.setScheduleService(scheduleService);
    IOService ioService = new IOService(dataAccessService);

    Controller controller = new Controller(dataAccessService, ioService, scheduleService,
        converter);

    // test preparation
    String pruefungString = "pruefung";
    String pruefung2String = "pruefung2";
    String blockString = "block";
    TeilnehmerkreisImpl t1 = new TeilnehmerkreisImpl("test", "test", 1, Ausbildungsgrad.BACHELOR);
    TeilnehmerkreisImpl t2 = new TeilnehmerkreisImpl("test", "test", 1, Ausbildungsgrad.BACHELOR);
    ReadOnlyBlock block;
    try {
      // scheduled pruefung
      ReadOnlyPruefung scheduledPruefung = controller.createPruefung(pruefungString, pruefungString,
          pruefungString,
          emptySet(), Duration.ofMinutes(90), Map.of(t1, 3));
      controller.schedulePruefung(scheduledPruefung, LocalDateTime.of(2022, 3, 20, 8, 0));

      // not scheduled pruefung
      ReadOnlyPruefung notScheduledPruefung = controller.createPruefung(pruefung2String,
          pruefung2String, pruefung2String, emptySet(), Duration.ofMinutes(90), Map.of(t2, 10));

      // create Block
      block = controller.createBlock(blockString, PARALLEL, notScheduledPruefung);

    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("keine Pruefungsperiode");

    } catch (HartesKriteriumException f) {
      throw new AssertionError("hard criteria");
    }

    // test restriction
    assertThat(block).isNotNull();

    assertThrows(HartesKriteriumException.class,
        () -> controller.scheduleBlock(block, LocalDateTime.of(2022, 3, 20, 8, 0)));

  }

  @Test
  void schedulePruefungWithOnePruefungAtSameTime() {
    // test hard restriction because of integration test fail (this test is really an integration test)
    Semester semester = new SemesterImpl(Semestertyp.WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 1);
    LocalDate end = LocalDate.of(2022, 4, 1);
    LocalDate anker = LocalDate.of(2022, 2, 1);
    Pruefungsperiode pruefungsperiode = new PruefungsperiodeImpl(semester, start, end, anker, 400);
    dataAccessService = ServiceProvider.getDataAccessService();
    dataAccessService.setPruefungsperiode(pruefungsperiode);
    RestrictionService restrictionService = new RestrictionService();
    Converter converter = new Converter();
    ScheduleService scheduleService = new ScheduleService(dataAccessService, restrictionService,
        converter);
    converter.setScheduleService(scheduleService);
    IOService ioService = new IOService(dataAccessService);

    Controller controller = new Controller(dataAccessService, ioService, scheduleService,
        converter);
    String pruefungString = "pruefung";
    String pruefung2String = "pruefung2";

    TeilnehmerkreisImpl t1 = new TeilnehmerkreisImpl("test", "test", 1, Ausbildungsgrad.BACHELOR);
    TeilnehmerkreisImpl t2 = new TeilnehmerkreisImpl("test", "test", 1, Ausbildungsgrad.BACHELOR);
    ReadOnlyPruefung pruefungToTest;
    try {
      ReadOnlyPruefung scheduledPruefung = controller.createPruefung(pruefungString, pruefungString,
          pruefungString,
          emptySet(), Duration.ofMinutes(90), Map.of(t1, 3));
      controller.schedulePruefung(scheduledPruefung, LocalDateTime.of(2022, 3, 20, 8, 0));

      pruefungToTest = controller.createPruefung(pruefung2String,
          pruefung2String, pruefung2String, emptySet(), Duration.ofMinutes(90), Map.of(t2, 10));


    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("keine Pruefungsperiode");

    } catch (HartesKriteriumException f) {
      throw new AssertionError("hard criteria");
    }

    assertThat(pruefungToTest).isNotNull();

    assertThrows(HartesKriteriumException.class,
        () -> controller.schedulePruefung(pruefungToTest, LocalDateTime.of(2022, 3, 20, 8, 0)));

  }

}
