package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_ANALYSIS_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_DM_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_HASKELL_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.bwlMaster;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.infMaster;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomROPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
  private final ReadOnlyPruefung RO_ANALYSIS = new PruefungDTOBuilder().withPruefungsName(
      "Analysis").withPruefungsNummer("1").withDauer(Duration.ofMinutes(120)).build();
  private final ReadOnlyPruefung RO_DM = new PruefungDTOBuilder().withPruefungsName("DM")
      .withPruefungsNummer("2").withDauer(Duration.ofMinutes(120)).build();
  private final ReadOnlyPruefung RO_HASKELL = new PruefungDTOBuilder().withPruefungsName("HASKELL")
      .withPruefungsNummer("3").withDauer(Duration.ofMinutes(120)).build();
  private DataAccessService dataAccessService;
  private RestrictionService restrictionService;
  private ScheduleService deviceUnderTest;
  // TODO ScheduleService hat keine direkte Abhaengigkeit von der Pruefungsperiode, diese sollte
  //  fuer tests nicht existieren. Wenn Verhalten vom DataAccessService erwartet wird, was
  //  abhaengig von der Pruefungsperiode ist, dann ist das als einfaches Return des
  //  DataAccessService zu modellieren!
  private Pruefungsperiode pruefungsperiode;
  private Converter converter;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.restrictionService = mock(RestrictionService.class);
    converter = new Converter();
    this.deviceUnderTest = new ScheduleService(dataAccessService, restrictionService, converter);
    converter.setScheduleService(deviceUnderTest);
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    when(pruefungsperiode.getStartdatum()).thenReturn(START_PERIOD);
    when(pruefungsperiode.getEnddatum()).thenReturn(END_PERIOD);
    dataAccessService.setPruefungsperiode(this.pruefungsperiode);
  }

  @Test
  void scheduleBlockUnConstistentBlock() {
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM);
    Pruefung model_haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL);

    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    when(pruefungsperiode.pruefung(RO_ANALYSIS.getPruefungsnummer())).thenReturn(model_analysis);
    when(pruefungsperiode.pruefung(RO_DM.getPruefungsnummer())).thenReturn(model_dm);
    when(pruefungsperiode.pruefung(RO_HASKELL.getPruefungsnummer())).thenReturn(model_haskell);

    // in the data model analysis and dm are in a block. haskell is not part of the block
    Block blockWithAnalysisDM = getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS,
        RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock inCosistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM,
        RO_ANALYSIS, RO_HASKELL);

    //Block doesn't exist
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(inCosistentBlock, START_PERIOD.atTime(_1000)));
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

  private Block getModelBlockFromROPruefungen(String name, LocalDateTime start,
      ReadOnlyPruefung... pruefungen) {
    Block block = new BlockImpl(pruefungsperiode, 1, name, Blocktyp.SEQUENTIAL);
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

  @Test
  void scheduleBlockSuccessFull() throws HartesKriteriumException {

    LocalDateTime time = START_PERIOD.atTime(_1000);

    Block model = new BlockImpl(pruefungsperiode, "AnalysisAndDm", Blocktyp.SEQUENTIAL);
    model.addPruefung(getPruefungOfReadOnlyPruefung(RO_DM));
    model.addPruefung(getPruefungOfReadOnlyPruefung(RO_ANALYSIS));
    model.setStartzeitpunkt(time);

    when(dataAccessService.terminIsInPeriod(any())).thenReturn(true);
    when(dataAccessService.scheduleBlock(any(), any())).thenReturn(model);

    ReadOnlyBlock consistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM,
        RO_ANALYSIS);

    assertDoesNotThrow(() -> deviceUnderTest.scheduleBlock(consistentBlock, time));
  }

  @Test
  void scheduleBlock_invalidTimeOneDayAfterEnd() {
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM);
    Pruefung model_haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL);

    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    when(pruefungsperiode.pruefung(RO_ANALYSIS.getPruefungsnummer())).thenReturn(model_analysis);
    when(pruefungsperiode.pruefung(RO_DM.getPruefungsnummer())).thenReturn(model_dm);
    when(pruefungsperiode.pruefung(RO_HASKELL.getPruefungsnummer())).thenReturn(model_haskell);

    Block blockWithAnalysisDM = getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS,
        RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock consistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM,
        RO_ANALYSIS);

    LocalDateTime endOfPeriodPlus1 = END_PERIOD.plusDays(1).atTime(_1000);

    // Time is one day after of end
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(consistentBlock, endOfPeriodPlus1));
  }

  @Test
  void scheduleBlock_invalidDateOneDayBeforeStart() {
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM);
    Pruefung model_haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL);

    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    when(pruefungsperiode.pruefung(RO_ANALYSIS.getPruefungsnummer())).thenReturn(model_analysis);
    when(pruefungsperiode.pruefung(RO_DM.getPruefungsnummer())).thenReturn(model_dm);
    when(pruefungsperiode.pruefung(RO_HASKELL.getPruefungsnummer())).thenReturn(model_haskell);

    Block blockWithAnalysisDM = getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS,
        RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock consistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM,
        RO_ANALYSIS);

    LocalDateTime endOfPeriodPlus1 = START_PERIOD.minusDays(1).atTime(_1000);
    // Time is one day before start
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(consistentBlock, endOfPeriodPlus1));
  }

  @Test
  void scheduledBlockExamInBlockIsNotInPeriod() {
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM);

    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    when(pruefungsperiode.pruefung(RO_ANALYSIS.getPruefungsnummer())).thenReturn(model_analysis);
    when(pruefungsperiode.pruefung(RO_DM.getPruefungsnummer())).thenReturn(model_dm);
    // Haskell is not in period

    // in the data model analysis and dm are in a block. haskell is not part of the block
    Block blockWithAnalysisDM = getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS,
        RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock inCosistentBlock = getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM,
        RO_ANALYSIS, RO_HASKELL);

    //Exam 3 Haskell doesn't exist
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(inCosistentBlock, START_PERIOD.atTime(_1000)));
  }

  @Test
  void scheduleEmptyBlock() {
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM);

    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    when(pruefungsperiode.pruefung(RO_ANALYSIS.getPruefungsnummer())).thenReturn(model_analysis);
    when(pruefungsperiode.pruefung(RO_DM.getPruefungsnummer())).thenReturn(model_dm);
    // Haskell is not in period
    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);

    ReadOnlyBlock emptyROBlock = getROBlockFromROPruefungen("Empty", null);

    // Not allowed schedule empty block
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(emptyROBlock, START_PERIOD.atTime(_1000)));
  }

  @Test
  void unscheduleBlock() {

    LocalDateTime now = LocalDateTime.now();
    Pruefung model_analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS);
    Pruefung model_dm = getPruefungOfReadOnlyPruefung(RO_DM);

    model_analysis.setStartzeitpunkt(now);
    model_dm.setStartzeitpunkt(now);

    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    when(pruefungsperiode.pruefung(RO_ANALYSIS.getPruefungsnummer())).thenReturn(model_analysis);
    when(pruefungsperiode.pruefung(RO_DM.getPruefungsnummer())).thenReturn(model_dm);
    // Haskell is not in period

    ReadOnlyPruefung ro_analysis = new PruefungDTOBuilder(RO_ANALYSIS).withStartZeitpunkt(now)
        .build();
    ReadOnlyPruefung ro_dm = new PruefungDTOBuilder(RO_DM).withStartZeitpunkt(now).build();

    // in the data model analysis and dm are in a block. haskell is not part of the block
    Block blockWithAnalysisDM = new BlockImpl(pruefungsperiode, 1, "AnalysisAndDm", PARALLEL);

    blockWithAnalysisDM.addPruefung(model_analysis);
    blockWithAnalysisDM.addPruefung(model_dm);

    blockWithAnalysisDM.setStartzeitpunkt(
        now); //at first add pruefungen than set the startzeitpunkt. otherwise startzeitpunkt will always be null!!

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock block = getROBlockFromROPruefungen("AnalysisAndDm", now, ro_analysis, ro_dm);

    when(dataAccessService.unscheduleBlock(any())).thenReturn(blockWithAnalysisDM);
    when(dataAccessService.getModelBlock(any())).thenReturn(blockWithAnalysisDM);
    when(restrictionService.getAffectedPruefungen(any(Block.class))).thenReturn(
        Set.of(model_analysis, model_dm));

    List<ReadOnlyPlanungseinheit> result = deviceUnderTest.unscheduleBlock(block);
    blockWithAnalysisDM.setStartzeitpunkt(null);
    // todo auf Block testen wenn equals für block implementiert ist
    assertThat(result).contains(ro_analysis, ro_dm);
  }

  @Test
  void removeTeilnehmerkreis() {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);

    ReadOnlyPruefung roHaskel = new PruefungDTOBuilder().withPruefungsName("Haskel")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("haskel")
        .withAdditionalTeilnehmerkreis(informatik).build();

    Set<Teilnehmerkreis> haskelTeilnehmrekeis = new HashSet<>();
    haskelTeilnehmrekeis.add(informatik);

    when(this.dataAccessService.removeTeilnehmerkreis(any(), any())).thenReturn(true);
    when(dataAccessService.getPruefungWith(anyString())).thenReturn(
        getPruefungOfReadOnlyPruefung(roHaskel));

    assertThat(deviceUnderTest.removeTeilnehmerKreis(roHaskel, informatik)).isEmpty();
  }

  @Test
  void addTeilnehmerkreis_successful_withSoft() {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);

    LocalDateTime date = LocalDateTime.of(2021, 8, 11, 9, 0);

    ReadOnlyPruefung roHaskel = new PruefungDTOBuilder().withPruefungsName("Haskel")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("haskel").withStartZeitpunkt(date)
        .withAdditionalPruefer("Schmidt").build();

    ReadOnlyPruefung roDM = new PruefungDTOBuilder().withPruefungsName("roDM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("roDM")
        //Hiermit sollte dann aufjedenfall mehrePruefunganeinemTag verletzt werden
        .withAdditionalTeilnehmerkreis(informatik).withStartZeitpunkt(date.plusMinutes(180))
        .withAdditionalPruefer("Schmidt").build();

    Pruefung haskel = getPruefungOfReadOnlyPruefung(roHaskel);
    Pruefung dm = getPruefungOfReadOnlyPruefung(roDM);

    List<ReadOnlyPlanungseinheit> listPlanungseinheit = new ArrayList<>();
    listPlanungseinheit.add(roHaskel);
    listPlanungseinheit.add(roDM);

    Set<Teilnehmerkreis> haskelTeilnehmrekeis = new HashSet<>();
    haskelTeilnehmrekeis.add(informatik);
    int schaetzungInformatik = 8;

    Set<Pruefung> conflictedPruefung = new HashSet<>();
    conflictedPruefung.add(haskel);
    conflictedPruefung.add(dm);

    when(restrictionService.getAffectedPruefungen(haskel)).thenReturn(conflictedPruefung);
    when(this.dataAccessService.getPruefungWith(roHaskel.getPruefungsnummer())).thenReturn(haskel);
    when(this.dataAccessService.addTeilnehmerkreis(haskel, informatik,
        schaetzungInformatik)).thenReturn(true);

    try {
      assertThat(deviceUnderTest.addTeilnehmerkreis(roHaskel, informatik,
          schaetzungInformatik)).containsAll(listPlanungseinheit);
    } catch (HartesKriteriumException e) {
      //Sollte hier nicht passieren, deshalb wird sie hier verworfen
      e.printStackTrace();
    }
  }

  @Test
  void addTeilnehmerkreis_hart() {
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);

    LocalDateTime date = LocalDateTime.of(2021, 8, 11, 9, 0);

    ReadOnlyPruefung roHaskel = new PruefungDTOBuilder().withPruefungsName("Haskel")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("haskel").withStartZeitpunkt(date)
        .withAdditionalPruefer("Schmidt").build();

    ReadOnlyPruefung roDM = new PruefungDTOBuilder().withPruefungsName("roDM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("roDM")
        //Hiermit sollte dann aufjedenfall mehrePruefunganeinemTag verletzt werden
        .withAdditionalTeilnehmerkreis(informatik).withStartZeitpunkt(date)
        .withAdditionalPruefer("Schmidt").build();

    Pruefung haskel = getPruefungOfReadOnlyPruefung(roHaskel);
    Pruefung dm = getPruefungOfReadOnlyPruefung(roDM);

    List<ReadOnlyPlanungseinheit> listPlanungseinheit = new ArrayList<>();
    listPlanungseinheit.add(roHaskel);
    listPlanungseinheit.add(roDM);

    Set<Teilnehmerkreis> haskelTeilnehmrekeis = new HashSet<>();
    haskelTeilnehmrekeis.add(informatik);
    int schaetzungInformatik = 8;

    Set<Pruefung> conflictedPruefung = new HashSet<>();
    conflictedPruefung.add(haskel);
    conflictedPruefung.add(dm);

    HartesKriteriumAnalyse hKA = new HartesKriteriumAnalyse(conflictedPruefung,
        haskelTeilnehmrekeis, schaetzungInformatik, ZWEI_KLAUSUREN_GLEICHZEITIG);

    List<HartesKriteriumAnalyse> listHard = new ArrayList<>();
    listHard.add(hKA);
    when(restrictionService.checkHarteKriterien(haskel)).thenReturn(listHard);
    when(restrictionService.getAffectedPruefungen(haskel)).thenReturn(conflictedPruefung);
    when(this.dataAccessService.getPruefungWith(roHaskel.getPruefungsnummer())).thenReturn(haskel);
    when(this.dataAccessService.addTeilnehmerkreis(haskel, informatik,
        schaetzungInformatik)).thenReturn(true);

    assertThrows(HartesKriteriumException.class,
        () -> deviceUnderTest.addTeilnehmerkreis(roHaskel, informatik, schaetzungInformatik));
    assertThat(haskel.getTeilnehmerkreise()).isEmpty();
  }

  @Test
  void setTeilnehmerkreisSchaetzung_not_planned() {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(infBachelor);
    when(dataAccessService.getPruefungWith(analysis.getPruefungsnummer())).thenReturn(analysis);

    int newSchaetzung = 300;
    deviceUnderTest.setTeilnehmerkreisSchaetzung(RO_ANALYSIS_UNPLANNED, infBachelor, newSchaetzung);
    assertThat(analysis.getSchaetzungen()).containsEntry(infBachelor, newSchaetzung);
  }

  @Test
  void setTeilnehmerkreisSchaetzung_teilnehmerkreis_not_part_of_pruefung() {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlMaster);
    when(dataAccessService.getPruefungWith(analysis.getPruefungsnummer())).thenReturn(analysis);

    int newSchaetzung = 300;
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.setTeilnehmerkreisSchaetzung(RO_ANALYSIS_UNPLANNED, infBachelor,
            newSchaetzung));
  }

  @Test
  void setTeilnehmerkreisSchaetzung_no_conflicts() {
    LocalDateTime time = LocalDateTime.now();

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlMaster);
    analysis.setStartzeitpunkt(time);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infMaster);
    dm.setStartzeitpunkt(time);

    when(dataAccessService.getPruefungWith(analysis.getPruefungsnummer())).thenReturn(analysis);
    when(dataAccessService.getPruefungWith(dm.getPruefungsnummer())).thenReturn(dm);

    int schaetzung = 20;
    assertThat(deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
        infMaster, schaetzung)).isEmpty();
  }

  @Test
  void setTeilnehmerkreisSchaetzung_change_does_not_affect_other_pruefung() {
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

    when(dataAccessService.getPruefungWith(analysis.getPruefungsnummer())).thenReturn(analysis);
    when(dataAccessService.getPruefungWith(dm.getPruefungsnummer())).thenReturn(dm);

    int newSchaetzung = 20;
    assertThat(deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
        teilnehmerkreis, newSchaetzung)).isEmpty();
    assertThat(analysis.getSchaetzungen()).containsEntry(teilnehmerkreis, oldSchaetzung);

    assertThat(dm.getSchaetzungen()).containsEntry(teilnehmerkreis, newSchaetzung);
  }

  @Test
  void setTeilnehmerkreisSchaetzung_too_many_students() {
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

    when(dataAccessService.getPruefungWith(analysis.getPruefungsnummer())).thenReturn(analysis);
    when(dataAccessService.getPruefungWith(dm.getPruefungsnummer())).thenReturn(dm);

    int newSchaetzung = Integer.MAX_VALUE;
    when(restrictionService.getAffectedPruefungen(dm)).thenReturn(Set.of(dm, analysis));
    assertThat(deviceUnderTest.setTeilnehmerkreisSchaetzung(converter.convertToReadOnlyPruefung(dm),
        teilnehmerkreis, newSchaetzung)).containsAll(Set.of(converter.convertToReadOnlyPruefung(dm),
        converter.convertToReadOnlyPruefung(analysis)));
    assertThat(analysis.getSchaetzungen()).containsEntry(teilnehmerkreis, oldSchaetzung);
    assertThat(dm.getSchaetzungen()).containsEntry(teilnehmerkreis, newSchaetzung);
  }

  @Test
  void addPruefungToBlock_blockMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.addPruefungToBlock(null, getRandomROPruefung(12L)));
  }

  @Test
  void addPruefungToBlock_pruefungMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.addPruefungToBlock(getEmptyROBlock(), null));
  }

  private ReadOnlyBlock getEmptyROBlock() {
    LocalDateTime startTime = LocalDateTime.of(2022, 1, 7, 11, 11);
    Set<ReadOnlyPruefung> noPruefungen = emptySet();
    return new BlockDTO("someName", startTime, Duration.ZERO, noPruefungen, 123456, PARALLEL);
  }

  @Test
  void addPruefungToBlock_plannedPruefungenMayNotBeAddedToBlocks() {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(getEmptyROBlock(),
            converter.convertToReadOnlyPruefung(pruefung)));
  }

  @Test
  void addPruefungToBlock_pruefungIsPartOfOtherBlock() {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock otherBlock = getUnplannedBlockWith1RandomPruefung();

    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(
        Optional.of(otherBlock));

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(getEmptyROBlock(), pruefung));
  }

  private ReadOnlyBlock getUnplannedBlockWith1RandomPruefung() {
    LocalDateTime startTime = LocalDateTime.of(2022, 1, 7, 11, 11);
    return new BlockDTO("someName", startTime, Duration.ZERO, Set.of(getRandomROPruefung(1L)),
        654321, PARALLEL);
  }

  @Test
  void addPruefungToBlock_pruefungIsAlreadyInSameBlock() throws HartesKriteriumException {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock block = getBlockWith(pruefung);

    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(Optional.of(block));

    assertDoesNotThrow(() -> deviceUnderTest.addPruefungToBlock(block, pruefung));
  }

  private ReadOnlyBlock getBlockWith(ReadOnlyPruefung... pruefungen) {
    LocalDateTime startTime = LocalDateTime.of(2022, 1, 7, 11, 11);
    return new BlockDTO("someName", startTime, Duration.ZERO, Set.of(pruefungen), 123456, PARALLEL);
  }

  @Test
  void addPruefungToBlock_pruefungIsAlreadyInSameBlock_empty_result()
      throws HartesKriteriumException {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock block = getBlockWith(pruefung);

    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(Optional.of(block));

    assertThat(deviceUnderTest.addPruefungToBlock(block, pruefung)).isEmpty();
  }

  @Test
  void addPruefungToBlock_pruefung_block_is_not_planned_no_affected_pruefungen()
      throws HartesKriteriumException {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock block = getUnplannedBlockWith();
    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.addPruefungToBlock(block, pruefung)).isEmpty();
  }

  private ReadOnlyBlock getUnplannedBlockWith(ReadOnlyPruefung... pruefungen) {
    return new BlockDTO("someName", null, Duration.ZERO, Set.of(pruefungen), 123456, PARALLEL);
  }

  @Test
  void addPruefungToBlock_pruefung_block_is_not_planned_no_further_calculation()
      throws HartesKriteriumException {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock block = getUnplannedBlockWith();
    when(dataAccessService.getBlockTo(any(ReadOnlyPruefung.class))).thenReturn(Optional.empty());

    deviceUnderTest.addPruefungToBlock(block, pruefung);
    verify(dataAccessService, times(0)).addPruefungToBlock(any(), any());
  }

  @Test
  void addPruefungToBlock_pruefung_with_same_teilnehmerkreis_at_same_time() {
    LocalDateTime termin = LocalDateTime.of(2022, 1, 2, 8, 0);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    haskell.addTeilnehmerkreis(infBachelor, 13);
    analysis.addTeilnehmerkreis(infBachelor, 13);

    Block block = getModelBlockWithPruefungen(pruefungsperiode, "block", termin, haskell);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);

    when(dataAccessService.getBlockTo(haskell)).thenReturn(Optional.of(block));
    when(restrictionService.checkHarteKriterien(any())).thenReturn(
        List.of(getNewHartesKriteriumAnalyse()));
    when(dataAccessService.addPruefungToBlock(roBlock, RO_ANALYSIS_UNPLANNED)).thenReturn(block);

    assertThrows(HartesKriteriumException.class, () -> deviceUnderTest.addPruefungToBlock(roBlock,
        converter.convertToReadOnlyPruefung(analysis)));
  }

  private Block getModelBlockWithPruefungen(Pruefungsperiode pruefungsperiode, String name,
      LocalDateTime termin, Pruefung... pruefungen) {
    Block result = new BlockImpl(pruefungsperiode, name, SEQUENTIAL);
    for (Pruefung pruefung : pruefungen) {
      result.addPruefung(pruefung);
      when(dataAccessService.getBlockTo(pruefung)).thenReturn(Optional.of(result));
    }
    if (termin != null) {
      result.setStartzeitpunkt(termin);
    }
    return result;
  }

  private HartesKriteriumAnalyse getNewHartesKriteriumAnalyse() {
    Random random = new Random(1L);
    int next = random.nextInt();
    return new HartesKriteriumAnalyse(
        Set.of(getPruefungOfReadOnlyPruefung(getRandomROPruefung(1L))),
        Set.of(getRandomTeilnehmerkreis(1L)), next, ZWEI_KLAUSUREN_GLEICHZEITIG);
  }

  @Test
  @DisplayName("addPruefungToBlock - hard restriction - check that pruefung does not get planned")
  void addPruefungToBlock_pruefung_with_same_teilnehmerkreis_hard_restriction() {
    LocalDateTime termin = LocalDateTime.of(2022, 1, 2, 8, 0);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    haskell.addTeilnehmerkreis(infBachelor, 13);
    analysis.addTeilnehmerkreis(infBachelor, 13);

    Block block = getModelBlockWithPruefungen(pruefungsperiode, "block", termin, haskell);
    ReadOnlyBlock roBlock = converter.convertToROBlock(block);

    when(dataAccessService.getBlockTo(haskell)).thenReturn(Optional.of(block));
    when(dataAccessService.getBlockTo(analysis)).thenReturn(Optional.of(block));
    when(restrictionService.checkHarteKriterien(any())).thenReturn(
        List.of(getNewHartesKriteriumAnalyse()));
    when(dataAccessService.addPruefungToBlock(roBlock, RO_ANALYSIS_UNPLANNED)).thenReturn(block);
    when(dataAccessService.getPruefungWith(haskell.getPruefungsnummer())).thenReturn(haskell);
    when(dataAccessService.getPruefungWith(analysis.getPruefungsnummer())).thenReturn(analysis);
    when(dataAccessService.removePruefungFromBlock(any(), any())).thenReturn(block);
    try {
      deviceUnderTest.addPruefungToBlock(roBlock, converter.convertToReadOnlyPruefung(analysis));

    } catch (HartesKriteriumException hardViolation) {
      verify(dataAccessService, times(1)).removePruefungFromBlock(roBlock, RO_ANALYSIS_UNPLANNED);
    }
  }

  @Test
  void removePruefungFromBlock_pruefung_empty_block() {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock block = getBlockWith();
    assertThat(deviceUnderTest.removePruefungFromBlock(block, pruefung)).isEmpty();
  }

  @Test
  void removePruefungFromBlock_pruefung_empty_block_remove_pruefung_data_access_doesnt_get_called() {
    ReadOnlyPruefung pruefung = getRandomROPruefung(1L);
    ReadOnlyBlock block = getBlockWith();
    deviceUnderTest.removePruefungFromBlock(block, pruefung);
    verify(dataAccessService, times(0)).removePruefungFromBlock(block, pruefung);
  }

  @Test
  void removePruefungFromBlock_pruefung_not_in_block() {
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    ReadOnlyPruefung pruefungInBlock = getRandomROPruefung(1L);
    ReadOnlyBlock block = getBlockWith(pruefungInBlock);
    when(dataAccessService.getPruefungWith(RO_HASKELL_UNPLANNED.getPruefungsnummer())).thenReturn(
        haskell);
    assertThat(deviceUnderTest.removePruefungFromBlock(block, RO_HASKELL_UNPLANNED)).isEmpty();

  }

  @Test
  @DisplayName("remove pruefung from block - dataAccessService removePruefungFromBlock does not get called")
  void removePruefungFromBlock_pruefung_not_in_block_verify() {
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    ReadOnlyPruefung pruefungInBlock = getRandomROPruefung(1L);
    ReadOnlyBlock block = getBlockWith(pruefungInBlock);
    when(dataAccessService.getPruefungWith(RO_HASKELL_UNPLANNED.getPruefungsnummer())).thenReturn(
        haskell);
    deviceUnderTest.removePruefungFromBlock(block, RO_HASKELL_UNPLANNED);
    verify(dataAccessService, times(0)).removePruefungFromBlock(any(), any());

  }

  @Test
  void getGeplantePruefungenWithKonflikt_noNullParametersAllowed() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(null));
  }
}
