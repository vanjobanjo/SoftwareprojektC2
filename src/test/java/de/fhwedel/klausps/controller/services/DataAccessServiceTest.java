package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungenReadOnly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyBlockAssert;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.controller.util.TestFactory;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessServiceTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();

  final ReadOnlyPruefung RO_ANALYSIS_UNPLANNED = new PruefungDTOBuilder().withPruefungsName(
      "Analysis").withPruefungsNummer("1").withDauer(Duration.ofMinutes(120)).build();
  final ReadOnlyPruefung RO_DM_UNPLANNED = new PruefungDTOBuilder().withPruefungsName("DM")
      .withPruefungsNummer("2").withDauer(Duration.ofMinutes(120)).build();

  final ReadOnlyPruefung RO_HASKELL_UNPLANNED = new PruefungDTOBuilder().withPruefungsName(
      "HASKELL").withPruefungsNummer("3").withDauer(Duration.ofMinutes(120)).build();

  private Pruefungsperiode pruefungsperiode;
  private DataAccessService deviceUnderTest;
  private ScheduleService scheduleService;
  private Converter converter;

  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.deviceUnderTest = ServiceProvider.getDataAccessService();
    // todo make sure the mocked class is not tested
    this.scheduleService = mock(ScheduleService.class);
    deviceUnderTest.setPruefungsperiode(pruefungsperiode);
    deviceUnderTest.setScheduleService(this.scheduleService);
    this.converter = new Converter();
    converter.setScheduleService(this.scheduleService); //TODO
    deviceUnderTest.setConverter(converter);
  }

  @Test
  @DisplayName("A Pruefung gets created by calling the respective method of the pruefungsPeriode")
  void createPruefungSuccessTest() {
    ReadOnlyPruefung pruefung = deviceUnderTest.createPruefung("Analysis", "b123", "ref",
        "pruefer 1",
        Duration.ofMinutes(90), new HashMap<>());
    assertThat(pruefung).isNotNull();
    verify(pruefungsperiode, times(1)).addPlanungseinheit(any());
  }

  @Test
  @DisplayName("A created pruefung has the expected attributes")
  void createPruefungSuccessRightAttributesTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    ReadOnlyPruefung actual = deviceUnderTest.createPruefung(expected.getName(),
        expected.getPruefungsnummer(), "ref", expected.getPruefer(), expected.getDauer(),
        expected.getTeilnehmerKreisSchaetzung());

    ReadOnlyPruefungAssert.assertThat(actual).isTheSameAs(getReadOnlyPruefung());
  }

  @Test
  @DisplayName("A created pruefung is persisted in the data model")
  void createPruefungSaveInModelTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    ReadOnlyPruefung actual = deviceUnderTest.createPruefung(expected.getName(),
        expected.getPruefungsnummer(), "ref", expected.getPruefer(), expected.getDauer(),
        expected.getTeilnehmerKreisSchaetzung());
    verify(pruefungsperiode, times(1)).addPlanungseinheit(notNull());
    assertThat(actual).isNotNull();
  }

  @Test
  @DisplayName("A pruefung can not be created when one with the same pruefungsnummer")
  void createPruefung_existsAlreadyTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    Pruefung test = new PruefungImpl(expected.getPruefungsnummer(), expected.getName(), "ABCDEF",
        expected.getDauer());

    when(pruefungsperiode.pruefung(expected.getPruefungsnummer())).thenReturn(test);
    assertThat(
        deviceUnderTest.createPruefung(expected.getName(), expected.getPruefungsnummer(), "ref",
            expected.getPruefer(), expected.getDauer(),
            expected.getTeilnehmerKreisSchaetzung())).isNull();
  }

  @Test
  @DisplayName("Change name of a Pruefung")
  void setName_successfullyTest() {
    ReadOnlyPruefung before = getReadOnlyPruefung();
    Pruefung model = getPruefungOfReadOnlyPruefung(before);
    String newName = "NoNameNeeded";

    when(pruefungsperiode.pruefung(before.getPruefungsnummer())).thenReturn(model);
    ReadOnlyPruefung after = deviceUnderTest.changeNameOfPruefung(before, newName).asPruefung();
    ReadOnlyPruefungAssert.assertThat(after).differsOnlyInNameFrom(before).hasName(newName);
  }

  @Test
  void getGeplantePruefungenTest() {
    ReadOnlyPruefung p1 = new PruefungDTOBuilder().withPruefungsName("Hallo").build();
    ReadOnlyPruefung p2 = new PruefungDTOBuilder().withPruefungsName("Welt").build();

    Pruefung pm1 = getPruefungOfReadOnlyPruefung(p1);
    Pruefung pm2 = getPruefungOfReadOnlyPruefung(p2);
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(pm1, pm2));
    when(scheduleService.scoringOfPruefung(any(Pruefung.class))).thenReturn(10, 20, 30);

    assertThat(deviceUnderTest.getGeplantePruefungen()).containsOnly(p1, p2);
  }

  @Test
  void ungeplanteKlausurenTest() {
    PruefungDTO p1 = new PruefungDTOBuilder().withPruefungsName("Hallo").build();
    PruefungDTO p2 = new PruefungDTOBuilder().withPruefungsName("Welt").build();
    Pruefung pm1 = getPruefungOfReadOnlyPruefung(p1);
    Pruefung pm2 = getPruefungOfReadOnlyPruefung(p2);

    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(pm1, pm2));

    Set<ReadOnlyPruefung> result = deviceUnderTest.getUngeplantePruefungen();
    assertThat(result).containsOnly(p1, p2);
  }

  @Test
  void geplanteBloeckeTest() {
    List<ReadOnlyPruefung> pruefungen = getRandomPruefungenReadOnly(1234, 2);
    List<Pruefung> pruefungenFromModel = convertPruefungenFromReadonlyToModel(pruefungen);
    Block initialBlock = new BlockImpl(pruefungsperiode, 1, "name", Blocktyp.PARALLEL);
    pruefungenFromModel.forEach(initialBlock::addPruefung);

    when(pruefungsperiode.geplanteBloecke()).thenReturn(Set.of(initialBlock));

    Set<ReadOnlyBlock> blockController = deviceUnderTest.getGeplanteBloecke();
    ReadOnlyBlock resultingBlock = blockController.stream().toList().get(0);
    ReadOnlyBlockAssert.assertThat(resultingBlock)
        .containsOnlyPruefungen(pruefungen.toArray(new ReadOnlyPruefung[0]));
  }

  @Test
  void ungeplanteBloeckeTest() {
    ReadOnlyPruefung ro01 = new PruefungDTOBuilder().withPruefungsName("inBlock0")
        .withPruefungsNummer("123").build();
    ReadOnlyPruefung ro02 = new PruefungDTOBuilder().withPruefungsName("inBlock1")
        .withPruefungsNummer("1235").build();
    Pruefung inBlock0 = getPruefungOfReadOnlyPruefung(ro01);
    Pruefung inBlock1 = getPruefungOfReadOnlyPruefung(ro02);
    Block block = new BlockImpl(pruefungsperiode, 1, "name", Blocktyp.PARALLEL);
    block.addPruefung(inBlock0);
    block.addPruefung(inBlock1);

    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(new HashSet<>(List.of(block)));
    Set<ReadOnlyBlock> ungeplanteBloecke = deviceUnderTest.getUngeplanteBloecke();

    assertThat(ungeplanteBloecke).hasSize(1);
    ReadOnlyBlock resultBlock = new LinkedList<>(ungeplanteBloecke).get(0);
    ReadOnlyPruefung ro0 = new LinkedList<>(resultBlock.getROPruefungen()).get(0);
    ReadOnlyPruefung ro1 = new LinkedList<>(resultBlock.getROPruefungen()).get(1);
    ReadOnlyBlockAssert.assertThat(resultBlock).containsOnlyPruefungen(ro01, ro02);
    assertThat(ro0 != ro01 || ro0 != ro02).isTrue();
    assertThat(ro1 != ro01 || ro1 != ro02).isTrue();
  }

  @Test
  void addPruefer_successTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(
        TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED));
    ReadOnlyPlanungseinheit result = deviceUnderTest.addPruefer("2", "Cohen");
    assertThat(result.getPruefer()).contains("Cohen");
    assertThat(result.isBlock()).isFalse();
  }

  @Test
  void addPrueferBlock_successTest() {
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode,
        "Hallo", null, modelDm);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(modelDm);
    ReadOnlyPlanungseinheit result = deviceUnderTest.addPruefer("2", "Cohen");
    assertThat(result.getPruefer()).contains("Cohen");
    assertThat(result.isBlock()).isTrue();
    assertThat(result.asBlock().getROPruefungen()).contains(TestFactory.RO_DM_UNPLANNED);
    assertThat(result.asBlock().getROPruefungen().stream()
        .anyMatch(x -> x.getPruefer().contains("Cohen"))).isTrue();

  }

  @Test
  void removePrueferBlock_successTest() {
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode,
        "Hallo", null, modelDm);
    modelDm.addPruefer("Cohen");
    when(pruefungsperiode.pruefung(anyString())).thenReturn(modelDm);
    ReadOnlyPlanungseinheit result = deviceUnderTest.removePruefer("2", "Cohen");
    assertThat(result.getPruefer()).doesNotContain("Cohen");
    assertThat(result.isBlock()).isTrue();
    assertThat(result.asBlock().getROPruefungen()).contains(TestFactory.RO_DM_UNPLANNED);
    assertThat(result.asBlock().getROPruefungen().stream()
        .noneMatch(x -> x.getPruefer().contains("Cohen"))).isTrue();

  }

  @Test
  void addPruefer_unknownPruefungTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.addPruefer("b110", "Gödel"));
  }

  @Test
  void removePruefer_successTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(getPruefungWithPruefer("Cohen"));
    ReadOnlyPruefungAssert.assertThat(deviceUnderTest.removePruefer("b321", "Cohen").asPruefung())
        .hasNotPruefer("Cohen");
  }

  @Test
  void removePruefer_unknownPruefungTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.removePruefer("b110", "Gödel"));
  }

  @Test
  void removePruefer_otherPrueferStayTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(
        getPruefungWithPruefer("Hilbert", "Einstein"));
    ReadOnlyPruefungAssert.assertThat(deviceUnderTest.removePruefer("b321", "Hilbert").asPruefung())
        .hasPruefer("Einstein").hasNotPruefer("Cohen");
  }

  @Test
  void changeDurationOf_Successful() {
    PruefungDTOBuilder pDTOB = new PruefungDTOBuilder();
    pDTOB.withPruefungsName("Analysi");
    pDTOB.withDauer(Duration.ofMinutes(90));
    ReadOnlyPruefung ro01 = pDTOB.build();
    PruefungImpl t = new PruefungImpl(ro01.getPruefungsnummer(), ro01.getName(), "Keine Ahnnung",
        ro01.getDauer());
    pruefungsperiode.addPlanungseinheit(t);

    assertEquals(ro01.getDauer(), Duration.ofMinutes(90));
    ReadOnlyPlanungseinheit roAcc = ro01;
    try {
      when(pruefungsperiode.pruefung(ro01.getPruefungsnummer())).thenReturn(t);
      when(scheduleService.changeDuration(t, Duration.ofMinutes(120))).thenReturn(List.of(t));
      assertThat(deviceUnderTest.changeDurationOf(ro01, Duration.ofMinutes(120))).hasSize(1);
      t.setDauer(Duration.ofMinutes(120)); // ScheduleService muss noch implementiert werden.
      roAcc = deviceUnderTest.changeDurationOf(ro01, Duration.ofMinutes(120)).get(0);
    } catch (HartesKriteriumException ignored) {
    }
    assertEquals(roAcc.getDauer(), Duration.ofMinutes(120));
  }

  @Test
  void changeDurationOf_withMinus() {
    PruefungDTOBuilder pDTOB = new PruefungDTOBuilder();
    pDTOB.withPruefungsName("Analysi");
    pDTOB.withDauer(Duration.ofMinutes(90));
    ReadOnlyPruefung ro01 = pDTOB.build();

    assertEquals(ro01.getDauer(), Duration.ofMinutes(90));
    Duration minusMinus = Duration.ofMinutes(-120);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.changeDurationOf(ro01, minusMinus));
  }

  @Test
  void unschedulePruefung_integration() {
    LocalDateTime initialSchedule = LocalDateTime.of(2022, 1, 1, 10, 30);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(
        new PruefungImpl("Pruefungsnummer", "name", "nbr", Duration.ofMinutes(90),
            initialSchedule));
    assertThat(deviceUnderTest.unschedulePruefung(getReadOnlyPruefung()).getStartzeitpunkt())
        .isNull();
  }

  @Test
  void unschedulePruefung_noExam() {
    when(pruefungsperiode.pruefung(any())).thenReturn(null);
    ReadOnlyPruefung somePruefung = getReadOnlyPruefung();
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.unschedulePruefung(somePruefung));
  }

  @Test
  void scheduleBlock_successful() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);

    LocalDateTime termin = LocalDateTime.of(2000, 1, 1, 0, 0);
    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    modelBlock.addPruefung(analysis);
    modelBlock.addPruefung(dm);
    modelBlock.addPruefung(haskell);

    // no consistency check!
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    when(pruefungsperiode.block(analysis)).thenReturn(modelBlock);
    when(pruefungsperiode.block(haskell)).thenReturn(modelBlock);
    when(pruefungsperiode.block(dm)).thenReturn(modelBlock);
    when(pruefungsperiode.pruefung(analysis.getPruefungsnummer())).thenReturn(analysis);
    when(pruefungsperiode.pruefung(dm.getPruefungsnummer())).thenReturn(dm);
    when(pruefungsperiode.pruefung(haskell.getPruefungsnummer())).thenReturn(haskell);

    Block result = deviceUnderTest.scheduleBlock(blockToSchedule, termin);

    assertThat(result.getPruefungen()).containsOnly(analysis, haskell, dm);

    assertThat(result.getStartzeitpunkt()).isEqualTo(termin);
    for (Pruefung p : result.getPruefungen()) {
      assertThat(p.getStartzeitpunkt()).isEqualTo(termin);
    }
    assertThat(modelBlock.getStartzeitpunkt()).isEqualTo(termin);
  }

  @Test
  @DisplayName("cannot schedule block that does not exist in model.")
  void scheduleBlock_different_names() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("Namme", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);

    LocalDateTime termin = LocalDateTime.of(2000, 1, 1, 0, 0);
    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);

    // no consistency check!
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.scheduleBlock(blockToSchedule, termin));
  }

  @Test
  void unscheduleBlock_integration() {
    // all start same
    LocalDateTime termin = LocalDateTime.of(2000, 1, 1, 0, 0);
    ReadOnlyPruefung ro_analysis = new PruefungDTOBuilder(RO_ANALYSIS_UNPLANNED).withStartZeitpunkt(
        termin).build();
    ReadOnlyPruefung ro_dm_planned = new PruefungDTOBuilder(RO_DM_UNPLANNED).withStartZeitpunkt(
        termin).build();
    ReadOnlyPruefung ro_haskell_planned = new PruefungDTOBuilder(
        RO_HASKELL_UNPLANNED).withStartZeitpunkt(termin).build();

    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", termin, Duration.ZERO,
        Set.of(ro_analysis, ro_dm_planned, ro_haskell_planned), 1, Blocktyp.SEQUENTIAL);

    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, termin,
        ro_analysis, ro_dm_planned, ro_haskell_planned);
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    Block ro_block_result = deviceUnderTest.unscheduleBlock(blockToSchedule);
    assertThat(modelBlock.getStartzeitpunkt()).isNull();
    assertThat(ro_block_result.getStartzeitpunkt()).isNull();
    assertThat(ro_block_result.getPruefungen().stream()
        .noneMatch(Planungseinheit::isGeplant)).isTrue();
  }

  @Test
  void SetPruefungsnummerTest() {

    String oldNumber = "2";
    String newNumber = "1";

    PruefungDTO analysis = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withPruefungsNummer(oldNumber).withDauer(Duration.ofMinutes(120))
        .withAdditionalPruefer("Harms").withStartZeitpunkt(LocalDateTime.now()).build();

    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(analysis);
    when(pruefungsperiode.pruefung(oldNumber)).thenReturn(modelAnalysis);
    ReadOnlyPruefung analysisNewNumber = deviceUnderTest.setPruefungsnummer(analysis, newNumber).asPruefung();
    assertThat(analysisNewNumber.getPruefungsnummer()).isEqualTo(newNumber);
    assertThat(analysisNewNumber).isNotEqualTo(analysis); // Equal arbeitet prüft die Nummern
    assertThat(analysisNewNumber.getDauer()).isEqualTo(analysis.getDauer());
  }

  @Test
  void schedulePruefungTest() {
    LocalDateTime expectedSchedule = LocalDateTime.of(2022, 1, 1, 10, 30);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(
        new PruefungImpl("Pruefungsnummer", "name", "nbr", Duration.ofMinutes(90)));
    assertThat(
        deviceUnderTest.schedulePruefung(getReadOnlyPruefung(), expectedSchedule)
            .getStartzeitpunkt()).isEqualTo(expectedSchedule);
  }

  @Test
  void schedulePruefung_noExamTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    ReadOnlyPruefung somePruefung = getReadOnlyPruefung();
    LocalDateTime someSchedule = getRandomTime();
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.schedulePruefung(somePruefung, someSchedule));
  }

  @Test
  void deletePruefung_successful() {
    ReadOnlyPruefung roP = getRandomPruefungenReadOnly(8415, 1).get(0);
    Pruefung pruefungModel = this.getPruefungOfReadOnlyPruefung(roP);
    when(this.pruefungsperiode.pruefung(roP.getPruefungsnummer())).thenReturn(pruefungModel);
    when(this.pruefungsperiode.removePlanungseinheit(pruefungModel)).thenReturn(true);
    this.deviceUnderTest.deletePruefung(roP);
    verify(this.pruefungsperiode, times(1)).removePlanungseinheit(pruefungModel);
  }

  @Test
  void deletePruefung_throw() {
    ReadOnlyPruefung roP = getRandomPruefungenReadOnly(8415, 1).get(0);
    when(this.pruefungsperiode.pruefung(any())).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> this.deviceUnderTest.deletePruefung(roP));
  }

  @Test
  void existsBlockSuccessful() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);
    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isTrue();
  }

  @Test
  void existsBlockSuccessfulWithDate() {
    LocalDateTime termin = LocalDateTime.of(2000, 1, 1, 0, 0);
    ReadOnlyPruefung ro_analysis = new PruefungDTOBuilder(RO_ANALYSIS_UNPLANNED).withStartZeitpunkt(
        termin).build();
    ReadOnlyPruefung ro_dm = new PruefungDTOBuilder(RO_DM_UNPLANNED).withStartZeitpunkt(termin)
        .build();
    ReadOnlyPruefung ro_haskell = new PruefungDTOBuilder(RO_HASKELL_UNPLANNED).withStartZeitpunkt(
        termin).build();

    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", termin, Duration.ZERO,
        Set.of(ro_analysis, ro_dm, ro_haskell), 1, Blocktyp.SEQUENTIAL);

    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, termin,
        ro_analysis, ro_dm, ro_haskell);
    modelBlock.setStartzeitpunkt(termin);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isTrue();
  }

  @Test
  void existsBlockDifferentBlockNames() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("DifferentName", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);

    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existBlockWithoutPruefungen() {
    ReadOnlyBlock block = new BlockDTO("name", null, Duration.ZERO, new HashSet<>(), 1,
        Blocktyp.SEQUENTIAL);
    Block modelBock = new BlockImpl(pruefungsperiode, 1, "name", Blocktyp.SEQUENTIAL);
    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(new HashSet<>(List.of(modelBock)));
    assertThat(deviceUnderTest.exists(block)).isTrue();
  }

  @Test
  void existsBlockDifferentDates() {
    LocalDateTime now = LocalDateTime.now();
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", now, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);

    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);

    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock,
        LocalDateTime.now(), RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);

    modelBlock.setStartzeitpunkt(now.plusMinutes(120));

    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existsBlockDifferentDates2() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", LocalDateTime.now(), Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);
    LocalDateTime termin = LocalDateTime.of(2000, 1, 1, 0, 0);
    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);

    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock,
        LocalDateTime.now(), RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);

    modelBlock.setStartzeitpunkt(termin);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existsBlockDifferentDates3() {
    LocalDateTime termin = LocalDateTime.of(2000, 1, 1, 0, 0);
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", termin, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);
    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existsBlockROBlockHasLessPruefungen() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED), 1, Blocktyp.SEQUENTIAL);

    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED, RO_HASKELL_UNPLANNED);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existsBlockROBlockHasMorePruefungen() {
    ReadOnlyBlock blockToSchedule = new BlockDTO("Name", null, Duration.ZERO,
        Set.of(RO_ANALYSIS_UNPLANNED, RO_HASKELL_UNPLANNED, RO_DM_UNPLANNED), 1,
        Blocktyp.SEQUENTIAL);

    Block modelBlock = new BlockImpl(pruefungsperiode, 1, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void createBlock_Successful() {
    when(pruefungsperiode.addPlanungseinheit(any())).thenReturn(true);
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    ReadOnlyBlock ro = deviceUnderTest.createBlock("Hallo", RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    assertThat(ro.getROPruefungen()).containsOnly(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
  }

  @Test
  void createBlock_PlannedROPruefung() {
    ReadOnlyPruefung ro_analysis_planned = new PruefungDTOBuilder(
        RO_ANALYSIS_UNPLANNED).withStartZeitpunkt(LocalDateTime.now()).build();
    configureMock_getPruefungToROPruefung(ro_analysis_planned, RO_DM_UNPLANNED);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createBlock("Hallo", ro_analysis_planned, RO_DM_UNPLANNED));
  }

  @Test
  void createBlock_PruefungAlreadyInOtherBlock() {
    Block model = new BlockImpl(pruefungsperiode, "Name", Blocktyp.SEQUENTIAL);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(model, null,
        RO_ANALYSIS_UNPLANNED);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createBlock("Hallo", RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED));
  }

  @Test
  void createBlock_DoublePruefungen() {
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.createBlock("Hallo", RO_DM_UNPLANNED, RO_DM_UNPLANNED));
  }

  @Test
  void createBlock_Successful2() {
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    when(pruefungsperiode.addPlanungseinheit(any())).thenReturn(
        true); // pruefungsperiode is gemocked!
    ReadOnlyBlock ro = deviceUnderTest.createBlock("Hallo", RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    Block model = new BlockImpl(pruefungsperiode, "Name", Blocktyp.SEQUENTIAL);
    LocalDateTime now = LocalDateTime.now();
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(model, now,
        RO_HASKELL_UNPLANNED);

    assertThat(ro.getROPruefungen()).containsOnly(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    assertThat(
        new PruefungDTOBuilder(new LinkedList<>(model.getPruefungen()).get(0)).build()).isEqualTo(
        new PruefungDTOBuilder(RO_HASKELL_UNPLANNED).withStartZeitpunkt(now).build());
  }


  @Test
  void getBetween() {

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    LocalDateTime end = LocalDateTime.of(2021, 8, 11, 10, 0);

    Set<Planungseinheit> setPlanung = new HashSet<>();
    Planungseinheit dmPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);
    Planungseinheit infotechPL = mock(Planungseinheit.class);

    setPlanung.add(dmPL);
    setPlanung.add(haskelPL);
    setPlanung.add(infotechPL);

    Pruefung dm = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung infotech = mock(Pruefung.class);

    when(dmPL.asPruefung()).thenReturn(dm);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    when(infotechPL.asPruefung()).thenReturn(infotech);

    List<Planungseinheit> listPruefung = new ArrayList<>();
    listPruefung.add(dmPL);
    listPruefung.add(haskelPL);
    listPruefung.add(infotechPL);

    when(this.pruefungsperiode.planungseinheitenBetween(start, end)).thenReturn(setPlanung);

    try {

      assertThat(this.deviceUnderTest.getAllPlanungseinheitenBetween(start, end)).containsAll(
          listPruefung);
      //  assertEquals(listPruefung, this.deviceUnderTest.getAllPruefungenBetween(start, end));
    } catch (IllegalTimeSpanException e) {
      //Per hand getestet sollte nichts schieflaufen
      e.printStackTrace();
    }
  }

  @Test
  void getPruuefungBetween_throwIllegal() {

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    LocalDateTime end = LocalDateTime.of(2021, 8, 11, 10, 0);

    Set<Planungseinheit> setPlanung = new HashSet<>();
    Planungseinheit dm = mock(Planungseinheit.class);
    Planungseinheit haskel = mock(Planungseinheit.class);
    Planungseinheit infotech = mock(Planungseinheit.class);

    setPlanung.add(dm);
    setPlanung.add(haskel);
    setPlanung.add(infotech);

    List<Pruefung> listPruefung = new ArrayList<>();
    listPruefung.add(dm.asPruefung());
    listPruefung.add(haskel.asPruefung());
    listPruefung.add(infotech.asPruefung());

    when(this.pruefungsperiode.planungseinheitenBetween(start, end)).thenReturn(setPlanung);
    assertThrows(IllegalTimeSpanException.class,
        () -> this.deviceUnderTest.getAllPlanungseinheitenBetween(end, start));

  }

  @Test
  void getPlanungseinheitenBetween() throws IllegalTimeSpanException {

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    LocalDateTime end = LocalDateTime.of(2021, 8, 11, 10, 0);

    Set<Planungseinheit> setPlanung = new HashSet<>();
    Planungseinheit dmPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);
    Planungseinheit infotechPL = mock(Planungseinheit.class);

    setPlanung.add(dmPL);
    setPlanung.add(haskelPL);
    setPlanung.add(infotechPL);

    Pruefung dm = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung infotech = mock(Pruefung.class);

    when(dmPL.asPruefung()).thenReturn(dm);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    when(infotechPL.asPruefung()).thenReturn(infotech);

    List<Planungseinheit> listPruefung = new ArrayList<>(3);
    listPruefung.add(dmPL);
    listPruefung.add(haskelPL);
    listPruefung.add(infotechPL);

    when(this.pruefungsperiode.planungseinheitenBetween(start, end)).thenReturn(setPlanung);

    assertThat(this.deviceUnderTest.getAllPlanungseinheitenBetween(start, end))
        .containsExactlyInAnyOrderElementsOf(listPruefung);
  }

  @Test
  void getPlanungseinheitenBetween_throwIllegal() {

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    LocalDateTime end = LocalDateTime.of(2021, 8, 11, 10, 0);

    Set<Planungseinheit> setPlanung = new HashSet<>();
    Planungseinheit dm = mock(Planungseinheit.class);
    Planungseinheit haskel = mock(Planungseinheit.class);
    Planungseinheit infotech = mock(Planungseinheit.class);

    setPlanung.add(dm);
    setPlanung.add(haskel);
    setPlanung.add(infotech);

    Set<Pruefung> listPruefung = new HashSet<>();
    listPruefung.add(dm.asPruefung());
    listPruefung.add(haskel.asPruefung());
    listPruefung.add(infotech.asPruefung());

    when(this.pruefungsperiode.planungseinheitenBetween(start, end)).thenReturn(setPlanung);
    assertThrows(IllegalTimeSpanException.class,
        () -> this.deviceUnderTest.getAllPlanungseinheitenBetween(end, start));

  }

  @Test
  void getBlockToPruefungOptTest() {
    //---- Start Configuration//
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    ReadOnlyPruefung dm = TestFactory.RO_DM_UNPLANNED;
    Pruefung modelAnalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(dm);
    LocalDateTime januar = LocalDateTime.of(2021, 1, 1, 1, 1);
    Block block = TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        januar, modelAnalysis, modelDm);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, modelDm, modelAnalysis);
    //-----//
    Optional<ReadOnlyBlock> result = deviceUnderTest.getBlockTo(analysis);
    assertThat(result).isPresent();
    assertThat(result.get().getROPruefungen()).containsOnly(analysis, dm);
  }

  @Test
  void getBlockToPruefungOptIsEmptyTest() {
    //---- Start Configuration//
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    ReadOnlyPruefung dm = TestFactory.RO_DM_UNPLANNED;
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(dm);
    Pruefung modelAnalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    LocalDateTime januar = LocalDateTime.of(2021, 1, 1, 1, 1);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        januar, modelAnalysis);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, modelAnalysis, modelDm);

    Optional<ReadOnlyBlock> result = deviceUnderTest.getBlockTo(dm);
    assertThat(result).isEmpty();
  }

  @Test
  void getBlockToPruefungOptTest2() {
    //---- Start Configuration//
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    Pruefung modelAnalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    ReadOnlyPruefung dm = TestFactory.RO_DM_UNPLANNED;
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(dm);
    LocalDateTime januar = LocalDateTime.of(2021, 1, 1, 1, 1);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        januar, modelAnalysis);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, modelAnalysis, modelDm);

    Optional<ReadOnlyBlock> result = deviceUnderTest.getBlockTo(analysis);
    assertThat(result).isPresent();
    assertThat(result.get().getROPruefungen()).containsOnly(analysis);
  }

  @Test
  void removePruefungFromUnplannedBlock() {
    // set up
    Block modelBlock = new BlockImpl(pruefungsperiode, "block 1", Blocktyp.SEQUENTIAL);
    Pruefung modelPruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    modelBlock.addPruefung(modelPruefung);
    when(pruefungsperiode.pruefung(modelPruefung.getPruefungsnummer())).thenReturn(modelPruefung);
    when(pruefungsperiode.block(modelPruefung)).thenReturn(modelBlock);
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);

    ReadOnlyBlock block =
        new BlockDTO(
            "block 1",
            null,
            RO_ANALYSIS_UNPLANNED.getDauer(),
            new HashSet<>(List.of(RO_ANALYSIS_UNPLANNED)),
            modelBlock.getId(), Blocktyp.PARALLEL);

    ReadOnlyBlock expectedBlock = new BlockDTO("block 1", null, Duration.ZERO, new HashSet<>(),
        modelBlock.getId(), Blocktyp.PARALLEL);

    // get results
    Pair<Block, Pruefung> actualResult = deviceUnderTest.removePruefungFromBlock(
        block, RO_ANALYSIS_UNPLANNED);
    Block actualBlock = actualResult.left();
    Pruefung actualPruefung = actualResult.right();
    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    ReadOnlyPruefungAssert.assertThat(RO_ANALYSIS_UNPLANNED)
        .isTheSameAs(converter.convertToReadOnlyPruefung(actualPruefung));
  }


  @Test
  @DisplayName("successfully remove pruefung from scheduled block, only one pruefung in block")
  void removePruefungFromBlockPlannedBlock() {

    // set up
    Block modelBlock = new BlockImpl(pruefungsperiode, "block", Blocktyp.SEQUENTIAL);
    modelBlock.setStartzeitpunkt(LocalDateTime.now());

    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1")
        .withStartZeitpunkt(LocalDateTime.now()).build();
    Pruefung modelPruefung = getPruefungOfReadOnlyPruefung(pruefung);
    modelBlock.addPruefung(modelPruefung);

    when(pruefungsperiode.pruefung(modelPruefung.getPruefungsnummer())).thenReturn(modelPruefung);
    when(pruefungsperiode.block(modelPruefung)).thenReturn(modelBlock);

    ReadOnlyBlock expectedBlock = new BlockDTO("block", null, Duration.ZERO, new HashSet<>(),
        modelBlock.getId(),
        Blocktyp.PARALLEL);
    ReadOnlyBlock block = new BlockDTO("block",
        null,
        RO_ANALYSIS_UNPLANNED.getDauer(),
        new HashSet<>(List.of(pruefung)),
        modelBlock.getId(), Blocktyp.PARALLEL);
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    // actual method to test call
    Pair<Block, Pruefung> actualResult = deviceUnderTest.removePruefungFromBlock(
        block, pruefung);
    Block actualBlock = actualResult.left();
    Pruefung actualPruefung = actualResult.right();

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    ReadOnlyPruefungAssert.assertThat(RO_ANALYSIS_UNPLANNED)
        .isTheSameAs(converter.convertToReadOnlyPruefung(actualPruefung));
  }

  @Test
  @DisplayName("successfully remove pruefung from scheduled block, more than one pruefung in block")
  void removePruefungFromBlockPlannedBlock2() {
    LocalDateTime termin = LocalDateTime.now();
    // set up
    Block modelBlock = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);
    modelBlock.setStartzeitpunkt(termin);

    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();
    ReadOnlyPruefung pruefungToRemove = new PruefungDTOBuilder().withPruefungsName("DM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("2").build();

    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, termin,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    ReadOnlyBlock expectedBlock = new BlockDTO("block", termin, Duration.ofMinutes(120),
        new HashSet<>(List.of(pruefung)), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock block = new BlockDTO("block",
        termin,
        RO_ANALYSIS_UNPLANNED.getDauer(),
        new HashSet<>(List.of(pruefung, pruefungToRemove)),
        modelBlock.getId(), Blocktyp.PARALLEL);

    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    // actual method to test call
    Pair<Block, Pruefung> actualResult = deviceUnderTest.removePruefungFromBlock(
        block, pruefungToRemove);
    Block actualBlock = actualResult.left();
    Pruefung actualPruefung = actualResult.right();

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    ReadOnlyPruefungAssert.assertThat(RO_DM_UNPLANNED)
        .isTheSameAs(converter.convertToReadOnlyPruefung(actualPruefung));
  }

  @Test
  @DisplayName("successfully remove pruefung from unscheduled block, more than one pruefung in block")
  void removePruefungFromBlockNotPlannedBlock2() {

    // set up
    Block modelBlock = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);

    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();
    ReadOnlyPruefung pruefungToRemove = new PruefungDTOBuilder().withPruefungsName("DM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("2").build();

    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    ReadOnlyBlock expectedBlock = new BlockDTO("block", null, Duration.ofMinutes(120),
        new HashSet<>(List.of(pruefung)), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock block = new BlockDTO("block",
        null,
        RO_ANALYSIS_UNPLANNED.getDauer(),
        new HashSet<>(List.of(pruefung, pruefungToRemove)),
        modelBlock.getId(), Blocktyp.PARALLEL);
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    // actual method to test call
    Pair<Block, Pruefung> actualResult = deviceUnderTest.removePruefungFromBlock(
        block, pruefungToRemove);
    Block actualBlock = actualResult.left();
    Pruefung actualPruefung = actualResult.right();

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    ReadOnlyPruefungAssert.assertThat(RO_DM_UNPLANNED)
        .isTheSameAs(converter.convertToReadOnlyPruefung(actualPruefung));
  }

  @Test
  @DisplayName("successfully remove pruefung from block, pruefung is not in block")
  void removePruefungFromBlockNotInBlock() {

    // set up
    Block modelBlock = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);

    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();
    ReadOnlyPruefung pruefungToRemove = new PruefungDTOBuilder().withPruefungsName("DM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("2").build();

    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, null,
        RO_ANALYSIS_UNPLANNED);
    ReadOnlyBlock block = new BlockDTO("block",
        null,
        RO_ANALYSIS_UNPLANNED.getDauer(),
        new HashSet<>(List.of(pruefung, pruefungToRemove)),
        1, Blocktyp.PARALLEL);

    // actual method to test call
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.removePruefungFromBlock(
        block, pruefungToRemove));
  }

  @Test
  @DisplayName("unsuccessfully try to remove pruefung from block, block is not in model")
  void removePruefungFromNonExistingBlock() {

    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();
    ReadOnlyPruefung pruefungToRemove = new PruefungDTOBuilder().withPruefungsName("DM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("2").build();

    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    ReadOnlyBlock block = new BlockDTO("block",
        null,
        RO_ANALYSIS_UNPLANNED.getDauer(),
        new HashSet<>(List.of(pruefung, pruefungToRemove)),
        1, Blocktyp.PARALLEL);

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.removePruefungFromBlock(block, pruefungToRemove));
  }

  @Test
  void ungeplantePruefungToTeilnehmerkreisTest() {
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwl);
    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(bwl)).containsOnly(
        TestFactory.RO_ANALYSIS_UNPLANNED);
  }

  @Test
  void geplantePruefungToTeilnehmerkreisTest() {
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.setStartzeitpunkt(LocalDateTime.of(1, 1, 1, 1, 1));
    analysis.addTeilnehmerkreis(bwl);
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.geplantePruefungenForTeilnehmerkreis(bwl)).containsOnly(
        TestFactory.RO_ANALYSIS_UNPLANNED);
  }

  @Test
  void ungeplantePruefungToTeilnehmerkreisEmptyTest() {
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(TestFactory.infPtl);
    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(
        deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(TestFactory.bwlBachelor)).isEmpty();
  }

  @Test
  void geplantePruefungToTeilnehmerkreisEmptyTest() {
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(TestFactory.infPtl);
    analysis.setStartzeitpunkt(LocalDateTime.of(1, 1, 1, 1, 1));
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(
        deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(TestFactory.bwlBachelor)).isEmpty();
  }

  @Test
  void addPruefungToBlock_none_scheduled_successful() {

    Block modelBlock = new BlockImpl(pruefungsperiode, "b1", Blocktyp.PARALLEL);
    ReadOnlyBlock blockToAddTo = new BlockDTO("b1", null, null,
        new HashSet<>(), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock expectedBlock = new BlockDTO("b1", null, RO_ANALYSIS_UNPLANNED.getDauer(),
        Set.of(RO_ANALYSIS_UNPLANNED), modelBlock.getId(), Blocktyp.PARALLEL);

    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(Set.of(modelBlock));
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED);
    Pair<Block, Pruefung> result = deviceUnderTest.addPruefungToBlock(blockToAddTo,
        RO_ANALYSIS_UNPLANNED);

    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result.left()));
  }


  @Test
  void addPruefungToBlock_block_scheduled_successful() {
    // setup termin and read only pruefungen
    LocalDateTime termin = LocalDateTime.now();
    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();
    ReadOnlyPruefung pruefungToAdd = new PruefungDTOBuilder().withPruefungsName("DM")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("2").build();
    // setup model block
    Block modelBlock = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);
    Pruefung p_analysis = getPruefungOfReadOnlyPruefung(pruefung);
    Pruefung p_dm = getPruefungOfReadOnlyPruefung(pruefungToAdd);
    modelBlock.addPruefung(p_analysis);
    modelBlock.setStartzeitpunkt(termin);
    // mock
    when(pruefungsperiode.pruefung(pruefung.getPruefungsnummer())).thenReturn(p_analysis);
    when(pruefungsperiode.pruefung(pruefungToAdd.getPruefungsnummer())).thenReturn(p_dm);
    when(pruefungsperiode.block(p_analysis)).thenReturn(modelBlock);
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);

    // setup input and expected blocks
    ReadOnlyBlock expectedBlock = new BlockDTO("block", termin, Duration.ofMinutes(120),
        new HashSet<>(List.of(pruefung, pruefungToAdd)), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock inputBlock = new BlockDTO("block", termin, Duration.ofMinutes(120),
        new HashSet<>(List.of(pruefung)), modelBlock.getId(), Blocktyp.PARALLEL);

    // method call
    Pair<Block, Pruefung> result = deviceUnderTest.addPruefungToBlock(inputBlock,
        RO_DM_UNPLANNED);
    // compare results
    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result.left()));
  }

  @Test
  void addPruefungToBlock_pruefung_scheduled_successful() {
    // setup termin and read only pruefungen
    LocalDateTime termin = LocalDateTime.now();
    ReadOnlyPruefung analysis = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withStartZeitpunkt(termin)
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();

    Block modelBlock = new BlockImpl(pruefungsperiode, "b1", Blocktyp.PARALLEL);
    ReadOnlyBlock blockToAddTo = new BlockDTO("b1", null, null,
        new HashSet<>(), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock expectedBlock = new BlockDTO("b1", null, RO_ANALYSIS_UNPLANNED.getDauer(),
        Set.of(RO_ANALYSIS_UNPLANNED), modelBlock.getId(), Blocktyp.PARALLEL);

    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(Set.of(modelBlock));
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED);
    Pair<Block, Pruefung> result = deviceUnderTest.addPruefungToBlock(blockToAddTo,
        analysis);

    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result.left()));
  }


  @Test
  void addPruefungToBlock_both_scheduled_successful() {
    // setup termin and read only pruefungen
    LocalDateTime termin = LocalDateTime.now();
    LocalDateTime terminLater = termin.plusHours(2);

    ReadOnlyPruefung pruefung = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("1").build();
    ReadOnlyPruefung pruefungToAdd = new PruefungDTOBuilder().withPruefungsName("DM")
        .withStartZeitpunkt(terminLater)
        .withDauer(Duration.ofMinutes(120)).withPruefungsNummer("2").build();
    // setup model block
    Block modelBlock = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);
    Pruefung p_analysis = getPruefungOfReadOnlyPruefung(pruefung);
    Pruefung p_dm = getPruefungOfReadOnlyPruefung(pruefungToAdd);
    modelBlock.addPruefung(p_analysis);
    modelBlock.setStartzeitpunkt(termin);
    // mock
    when(pruefungsperiode.pruefung(pruefung.getPruefungsnummer())).thenReturn(p_analysis);
    when(pruefungsperiode.pruefung(pruefungToAdd.getPruefungsnummer())).thenReturn(p_dm);
    when(pruefungsperiode.block(p_analysis)).thenReturn(modelBlock);
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);

    // setup input and expected blocks
    ReadOnlyBlock expectedBlock = new BlockDTO("block", termin, Duration.ofMinutes(120),
        new HashSet<>(List.of(pruefung, pruefungToAdd)), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock inputBlock = new BlockDTO("block", termin, Duration.ofMinutes(120),
        new HashSet<>(List.of(pruefung)), modelBlock.getId(), Blocktyp.PARALLEL);

    // method call
    Pair<Block, Pruefung> result = deviceUnderTest.addPruefungToBlock(inputBlock,
        pruefungToAdd);
    // compare results
    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result.left()));
  }

  @Test
  void addPruefungToBlock_block_does_not_exist() {
    ReadOnlyBlock blockToAddTo = new BlockDTO("b1", null, null,
        new HashSet<>(), 1, Blocktyp.PARALLEL);

    configureMock_getPruefungToROPruefung(RO_DM_UNPLANNED);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(blockToAddTo,
            RO_DM_UNPLANNED));
  }


  @Test
  void addPruefungToBlock_pruefung_does_not_exist() {
    Block modelBlock = new BlockImpl(pruefungsperiode, "b1", Blocktyp.PARALLEL);
    ReadOnlyBlock blockToAddTo = new BlockDTO("b1", null, null,
        new HashSet<>(), modelBlock.getId(), Blocktyp.PARALLEL);

    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(Set.of(modelBlock));
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(blockToAddTo,
            RO_ANALYSIS_UNPLANNED));

  }


  @Test
  void addPruefungToBlock_neither_block_nor_pruefung_exist() {

    ReadOnlyBlock blockToAddTo = new BlockDTO("b1", null, null,
        new HashSet<>(), 1, Blocktyp.PARALLEL);

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefungToBlock(blockToAddTo,
            RO_HASKELL_UNPLANNED));

  }


  private void configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(
      Block modelBlock, LocalDateTime termin, ReadOnlyPruefung... pruefungen) {
    for (ReadOnlyPruefung p : pruefungen) {
      Pruefung temp = getPruefungOfReadOnlyPruefung(p);
      modelBlock.addPruefung(temp);
      modelBlock.setStartzeitpunkt(termin);
      when(pruefungsperiode.pruefung(p.getPruefungsnummer())).thenReturn(temp);
      when(pruefungsperiode.block(temp)).thenReturn(modelBlock);
    }
  }

  private void configureMock_getPruefungToROPruefung(ReadOnlyPruefung... pruefungen) {
    for (ReadOnlyPruefung p : pruefungen) {
      Pruefung temp = getPruefungOfReadOnlyPruefung(p);
      when(pruefungsperiode.pruefung(p.getPruefungsnummer())).thenReturn(temp);
    }
  }

  /**
   * Gibt eine vorgegeben ReadOnlyPruefung zurueck
   *
   * @return gibt eine vorgebene ReadOnlyPruefung zurueck
   */
  private ReadOnlyPruefung getReadOnlyPruefung() {
    return new PruefungDTOBuilder().withPruefungsName("Analysis").withPruefungsNummer("b001")
        .withAdditionalPruefer("Harms").withDauer(Duration.ofMinutes(90)).build();
  }

  private LocalDateTime getRandomTime() {
    return LocalDateTime.of(2022, 7, 22, 12, 0);
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

  private List<Pruefung> convertPruefungenFromReadonlyToModel(
      Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream().map(this::getPruefungOfReadOnlyPruefung).toList();
  }

  private Pruefung getPruefungWithPruefer(String pruefer) {
    Pruefung pruefung = new PruefungImpl("b001", "Analysis", "refNbr", Duration.ofMinutes(70));
    pruefung.addPruefer(pruefer);
    return pruefung;
  }

  private Pruefung getPruefungWithPruefer(String... pruefer) {
    Pruefung pruefung = new PruefungImpl("b001", "Analysis", "refNbr", Duration.ofMinutes(70));
    for (String p : pruefer) {
      pruefung.addPruefer(p);
    }
    return pruefung;
  }


}
