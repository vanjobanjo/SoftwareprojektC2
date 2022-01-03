package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomDuration;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

  private final LocalDate START_PERIOD = LocalDate.of(2000, 1, 1);
  private final LocalDate END_PERIOD = LocalDate.of(2000, 1, 31);
  private final LocalTime _1000 = LocalTime.of(10, 0);
  private final ReadOnlyPruefung RO_ANALYSIS =
      new PruefungDTOBuilder()
          .withPruefungsName("Analysis")
          .withPruefungsNummer("1")
          .withDauer(Duration.ofMinutes(120))
          .build();
  private final ReadOnlyPruefung RO_DM =
      new PruefungDTOBuilder()
          .withPruefungsName("DM")
          .withPruefungsNummer("2")
          .withDauer(Duration.ofMinutes(120))
          .build();
  private final ReadOnlyPruefung RO_HASKELL =
      new PruefungDTOBuilder()
          .withPruefungsName("HASKELL")
          .withPruefungsNummer("3")
          .withDauer(Duration.ofMinutes(120))
          .build();
  private DataAccessService dataAccessService;
  private RestrictionService restrictionService;
  private ScheduleService deviceUnderTest;
  // TODO ScheduleService hat keine direkte Abhaengigkeit von der Pruefungsperiode, diese sollte
  //  fuer tests nicht existieren. Wenn Verhalten vom DataAccessService erwartet wird, was
  //  abhaengig von der Pruefungsperiode ist, dann ist das als einfaches Return des
  //  DataAccessService zu modellieren!
  private Pruefungsperiode pruefungsperiode;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.restrictionService = mock(RestrictionService.class);
    this.deviceUnderTest = new ScheduleService(dataAccessService, restrictionService);

    this.pruefungsperiode = mock(Pruefungsperiode.class);
    when(pruefungsperiode.getStartdatum()).thenReturn(START_PERIOD);
    when(pruefungsperiode.getEnddatum()).thenReturn(END_PERIOD);
    dataAccessService.setPruefungsperiode(this.pruefungsperiode);
    dataAccessService.setScheduleService(this.deviceUnderTest);


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
    Block blockWithAnalysisDM =
        getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS, RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock inCosistentBlock =
        getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM, RO_ANALYSIS, RO_HASKELL);

    //Block doesn't exist
    assertThrows(
        IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(inCosistentBlock, START_PERIOD.atTime(_1000)));
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

    ReadOnlyBlock consistentBlock =
        getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM, RO_ANALYSIS);

    // check no exceptions
    List<ReadOnlyPlanungseinheit> result =
        deviceUnderTest.scheduleBlock(consistentBlock, time);
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

    Block blockWithAnalysisDM =
        getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS, RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock consistentBlock =
        getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM, RO_ANALYSIS);

    LocalDateTime endOfPeriodPlus1 = END_PERIOD.plusDays(1).atTime(_1000);

    // Time is one day after of end
    assertThrows(
        IllegalArgumentException.class,
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

    Block blockWithAnalysisDM =
        getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS, RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock consistentBlock =
        getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM, RO_ANALYSIS);

    LocalDateTime endOfPeriodPlus1 = START_PERIOD.minusDays(1).atTime(_1000);
    // Time is one day before start
    assertThrows(
        IllegalArgumentException.class,
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
    Block blockWithAnalysisDM =
        getModelBlockFromROPruefungen("AnalysisAndDm", null, RO_ANALYSIS, RO_DM);

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock inCosistentBlock =
        getROBlockFromROPruefungen("AnalysisAndDm", null, RO_DM, RO_ANALYSIS, RO_HASKELL);

    //Exam 3 Haskell doesn't exist
    assertThrows(
        IllegalArgumentException.class,
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

    ReadOnlyBlock emptyROBlock =
        getROBlockFromROPruefungen("Empty", null);

    // Not allowed schedule empty block
    assertThrows(
        IllegalArgumentException.class,
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
    Block blockWithAnalysisDM = new BlockImpl(pruefungsperiode, 1, "AnalysisAndDm",
        Blocktyp.PARALLEL);

    blockWithAnalysisDM.addPruefung(model_analysis);
    blockWithAnalysisDM.addPruefung(model_dm);

    blockWithAnalysisDM.setStartzeitpunkt(
        now); //at first add pruefungen than set the startzeitpunkt. otherwise startzeitpunkt will always be null!!

    when(pruefungsperiode.block(any(Pruefung.class))).thenReturn(null);
    when(pruefungsperiode.block(model_analysis)).thenReturn(blockWithAnalysisDM);
    when(pruefungsperiode.block(model_dm)).thenReturn(blockWithAnalysisDM);

    ReadOnlyBlock block =
        getROBlockFromROPruefungen("AnalysisAndDm", now, ro_analysis, ro_dm);

    when(dataAccessService.unscheduleBlock(any())).thenReturn(blockWithAnalysisDM);
    when(restrictionService.getAffectedPruefungen(any())).thenReturn(
        Set.of(model_analysis, model_dm));

    List<ReadOnlyPlanungseinheit> result = deviceUnderTest.unscheduleBlock(block);
    assertThat(result).contains(ro_analysis, ro_dm);
  }

  /*
  //TODO movePruefung umschreiben
  @Test
  void movePruefung_unknownPruefung() throws HartesKriteriumException {
    when(dataAccessService.getStartOfPruefungWith(anyString())).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.movePruefung(getRandomPruefung(111L), LocalDateTime.now()));
  }*/
/*
  @Test
  void movePruefung_hartesKriteriumVerletzt() throws HartesKriteriumException {
    when(dataAccessService.getStartOfPruefungWith(anyString()))
        .thenReturn(Optional.of(LocalDateTime.now()));
    when(restrictionService.checkHarteKriterien()).thenReturn(getHartesKriteriumAnalyses());
    assertThrows(HartesKriteriumException.class,
        () -> deviceUnderTest.movePruefung(getRandomPruefung(111L), LocalDateTime.now()));
    verify(dataAccessService, times(2)).schedulePruefung(any(), any());
  }*/
/*
  private List<HartesKriteriumAnalyse> getHartesKriteriumAnalyses() {
    return List.of(new HartesKriteriumAnalyse(Set.copyOf(getRandomPruefungen(342L, 5)),
        Collections.emptySet(), 999));
  }*/

  private Pruefung getPruefungOfReadOnlyPruefung(ReadOnlyPruefung roPruefung) {
    PruefungImpl modelPruefung =
        new PruefungImpl(
            roPruefung.getPruefungsnummer(),
            roPruefung.getName(),
            "",
            roPruefung.getDauer(),
            roPruefung.getTermin().orElse(null));
    for (String pruefer : roPruefung.getPruefer()) {
      modelPruefung.addPruefer(pruefer);
    }
    roPruefung.getTeilnehmerKreisSchaetzung().forEach(modelPruefung::setSchaetzung);
    return modelPruefung;
  }

  private Block getModelBlockFromROPruefungen(
      String name, LocalDateTime start, ReadOnlyPruefung... pruefungen) {
    Block block = new BlockImpl(pruefungsperiode, 1, name, Blocktyp.SEQUENTIAL);
    block.setStartzeitpunkt(start);
    for (ReadOnlyPruefung p : pruefungen) {
      block.addPruefung(getPruefungOfReadOnlyPruefung(p));
    }
    return block;
  }


  private ReadOnlyBlock getROBlockFromROPruefungen(
      String name, LocalDateTime start, ReadOnlyPruefung... pruefungen) {
    return new BlockDTO(name, start, Duration.ZERO,
        Set.of(pruefungen), 1, Blocktyp.PARALLEL);
  }

  private ReadOnlyPruefung getRandomPruefung(long seed) {
    return getRandomPruefungen(seed, 1).get(0);
  }

  private List<ReadOnlyPruefung> getRandomPruefungen(long seed, int amount) {
    Random random = new Random(seed);
    List<ReadOnlyPruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(
          new PruefungDTOBuilder()
              .withPruefungsName(getRandomString(random, 5))
              .withDauer(getRandomDuration(random, 120))
              .withPruefungsNummer(getRandomString(random, 4))
              .build());
    }
    return randomPruefungen;
  }

}
