package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.TestFactory.bwlBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.infMaster;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungenReadOnly;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTime;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedROPruefung;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyBlockAssert;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessServiceTest {

  final ReadOnlyPruefung RO_ANALYSIS_UNPLANNED = new PruefungDTOBuilder().withPruefungsName(
      "Analysis").withPruefungsNummer("1").withDauer(Duration.ofMinutes(120)).build();
  final ReadOnlyPruefung RO_DM_UNPLANNED = new PruefungDTOBuilder().withPruefungsName("DM")
      .withPruefungsNummer("2").withDauer(Duration.ofMinutes(120)).build();

  final ReadOnlyPruefung RO_HASKELL_UNPLANNED = new PruefungDTOBuilder().withPruefungsName(
      "HASKELL").withPruefungsNummer("3").withDauer(Duration.ofMinutes(120)).build();

  private Pruefungsperiode pruefungsperiode;
  private DataAccessService deviceUnderTest;
  private Converter converter;

  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.deviceUnderTest = ServiceProvider.getDataAccessService();
    deviceUnderTest.setPruefungsperiode(pruefungsperiode);
    this.converter = new Converter();
    converter.setScheduleService(mock(ScheduleService.class));
  }

  @Test
  @DisplayName("A Pruefung gets created by calling the respective method of the pruefungsPeriode")
  void createPruefungSuccessTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = deviceUnderTest.createPruefung("Analysis", "b123", "ref",
        "pruefer 1",
        Duration.ofMinutes(90), new HashMap<>());
    assertThat(pruefung).isNotNull();
    verify(pruefungsperiode, times(1)).addPlanungseinheit(any());
  }

  @Test
  @DisplayName("A created pruefung has the expected attributes")
  void createPruefungSuccessRightAttributesTest() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    Pruefung actual = deviceUnderTest.createPruefung(expected.getName(),
        expected.getPruefungsnummer(), "ref", expected.getPruefer(), expected.getDauer(),
        expected.getTeilnehmerKreisSchaetzung());

    assertThat(actual.getSchaetzungen()).containsAllEntriesOf(
        expected.getTeilnehmerKreisSchaetzung());
    assertThat(actual.getPruefungsnummer()).isEqualTo(expected.getPruefungsnummer());
    assertThat(actual.getName()).isEqualTo(expected.getName());
    assertThat(actual.getPruefer()).containsExactlyInAnyOrderElementsOf(expected.getPruefer());
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

  @Test
  @DisplayName("A created pruefung is persisted in the data model")
  void createPruefungSaveInModelTest() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    Pruefung actual = deviceUnderTest.createPruefung(expected.getName(),
        expected.getPruefungsnummer(), "ref", expected.getPruefer(), expected.getDauer(),
        expected.getTeilnehmerKreisSchaetzung());
    verify(pruefungsperiode, times(1)).addPlanungseinheit(notNull());
    assertThat(actual).isNotNull();
  }

  @Test
  @DisplayName("A pruefung can not be created when one with the same pruefungsnummer")
  void createPruefung_existsAlreadyTest() throws NoPruefungsPeriodeDefinedException {
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
  void changeNameOf_successfullyTest() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung before = getReadOnlyPruefung();
    Pruefung model = getPruefungOfReadOnlyPruefung(before);
    String newName = "NoNameNeeded";

    when(pruefungsperiode.pruefung(before.getPruefungsnummer())).thenReturn(model);
    Pruefung after = deviceUnderTest.changeNameOf(before, newName).asPruefung();
    ReadOnlyPruefungAssert.assertThat(converter.convertToReadOnlyPruefung(after))
        .differsOnlyInNameFrom(before).hasName(newName);
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
  void changeNameOf_unknownPruefung() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.changeNameOf(pruefung, "name"));
  }

  @Test
  void changeNameOf_reallyChangeName() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    Pruefung pruefungToChange = mock(Pruefung.class);
    String name = "name";
    when(pruefungsperiode.pruefung(anyString())).thenReturn(pruefungToChange);
    deviceUnderTest.changeNameOf(pruefung, name);
    verify(pruefungToChange).setName(name);
  }

  @Test
  void getGeplantePruefungenTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung p1 = getRandomPlannedPruefung(1L);
    Pruefung p2 = getRandomPlannedPruefung(2L);

    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(p1, p2));

    assertThat(deviceUnderTest.getGeplantePruefungen()).containsOnly(p1, p2);
  }

  @Test
  void getGeplantePruefungen_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungen());
  }

  @Test
  void getUngeplantePruefungen_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getUngeplantePruefungen());
  }

  @Test
  void ungeplanteKlausurenTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung pm1 = getRandomUnplannedPruefung(1L);
    Pruefung pm2 = getRandomUnplannedPruefung(2L);

    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(pm1, pm2));

    Set<Pruefung> result = deviceUnderTest.getUngeplantePruefungen();
    assertThat(result).containsOnly(pm1, pm2);
  }

  @Test
  void geplanteBloeckeTest() throws NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1234, 2);
    Block block = new BlockImpl(pruefungsperiode, 1, "name", Blocktyp.PARALLEL);
    pruefungen.forEach(block::addPruefung);

    when(pruefungsperiode.geplanteBloecke()).thenReturn(Set.of(block));
    assertThat(deviceUnderTest.getGeplanteBloecke()).hasSize(1);
  }

  @Test
  void ungeplanteBloeckeTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung inBlock0 = getRandomUnplannedPruefung(1L);
    Pruefung inBlock1 = getRandomUnplannedPruefung(2L);
    Block block = new BlockImpl(pruefungsperiode, 1, "name", Blocktyp.PARALLEL);
    block.addPruefung(inBlock0);
    block.addPruefung(inBlock1);

    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(Set.of(block));

    assertThat(deviceUnderTest.getUngeplanteBloecke()).hasSize(1);
  }

  @Test
  void getUngeplanteBloecke_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getUngeplanteBloecke());
  }

  @Test
  void addPruefer_successTest() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(
        TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED));
    Planungseinheit result = deviceUnderTest.addPruefer(RO_DM_UNPLANNED, "Cohen");
    assertThat(result.getPruefer()).contains("Cohen");
    assertThat(result.isBlock()).isFalse();
  }

  @Test
  void addPrueferBlock_successTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED);
    Block result = TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode,
        "Hallo", null, modelDm);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(modelDm);
    deviceUnderTest.addPruefer(RO_DM_UNPLANNED, "Cohen");
    assertThat(result.getPruefer()).contains("Cohen");
    assertThat(result.isBlock()).isTrue();
    assertThat(result.asBlock().getPruefungen()).contains(modelDm);
    assertThat(result.asBlock().getPruefungen().stream()
        .anyMatch(x -> x.getPruefer().contains("Cohen"))).isTrue();

  }

  @Test
  void removePrueferBlock_successTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED);
    Block result = TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode,
        "Hallo", null, modelDm);
    modelDm.addPruefer("Cohen");
    when(pruefungsperiode.pruefung(anyString())).thenReturn(modelDm);
    deviceUnderTest.removePruefer(RO_DM_UNPLANNED, "Cohen");
    assertThat(result.getPruefer()).doesNotContain("Cohen");
    assertThat(result.isBlock()).isTrue();
    assertThat(result.asBlock().getPruefungen()).contains(modelDm);
    assertThat(result.asBlock().getPruefungen().stream()
        .noneMatch(x -> x.getPruefer().contains("Cohen"))).isTrue();
  }

  @Test
  void addPruefer_unknownPruefungTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.addPruefer(RO_HASKELL_UNPLANNED, "Gödel"));
  }

  @Test
  void removePruefer_successTest() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(getPruefungWithPruefer("Cohen"));
    assertThat(deviceUnderTest.removePruefer(RO_DM_UNPLANNED, "Cohen").asPruefung()
        .getPruefer()).doesNotContain("Cohen");
  }

  private Pruefung getPruefungWithPruefer(String... pruefer) {
    Pruefung pruefung = new PruefungImpl("b001", "Analysis", "refNbr", Duration.ofMinutes(70));
    for (String p : pruefer) {
      pruefung.addPruefer(p);
    }
    return pruefung;
  }

  private List<Pruefung> convertPruefungenFromReadonlyToModel(
      Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream().map(this::getPruefungOfReadOnlyPruefung).toList();
  }

  @Test
  void removePruefer_unknownPruefungTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.removePruefer(RO_DM_UNPLANNED, "Gödel"));
  }

  @Test
  void removePruefer_otherPrueferStayTest() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(
        getPruefungWithPruefer("Hilbert", "Einstein"));
    Planungseinheit result = deviceUnderTest.removePruefer(RO_DM_UNPLANNED, "Hilbert");
    assertThat(result.asPruefung().getPruefer()).contains("Einstein");
    assertThat(result.asPruefung().getPruefer()).doesNotContain("Hilbert");

  }

  @Test
  void unschedulePruefung_integration() throws NoPruefungsPeriodeDefinedException {
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
  void SetPruefungsnummerTest() throws NoPruefungsPeriodeDefinedException {

    String oldNumber = "2";
    String newNumber = "1";

    PruefungDTO analysis = new PruefungDTOBuilder().withPruefungsName("Analysis")
        .withPruefungsNummer(oldNumber).withDauer(Duration.ofMinutes(120))
        .withAdditionalPruefer("Harms").withStartZeitpunkt(LocalDateTime.now()).build();

    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(analysis);
    when(pruefungsperiode.pruefung(oldNumber)).thenReturn(modelAnalysis);
    Pruefung analysisNewNumber = deviceUnderTest.setPruefungsnummer(analysis, newNumber)
        .asPruefung();
    assertThat(analysisNewNumber.getPruefungsnummer()).isEqualTo(newNumber);
    assertThat(analysisNewNumber.getPruefungsnummer()).isNotEqualTo(analysis.getPruefungsnummer());
    assertThat(analysisNewNumber.getDauer()).isEqualTo(analysis.getDauer());
  }

  @Test
  void schedulePruefungTest() throws NoPruefungsPeriodeDefinedException {
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
    LocalDateTime someSchedule = getRandomTime(1L);
    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.schedulePruefung(somePruefung, someSchedule));
  }

  @Test
  void deletePruefung_successful() throws NoPruefungsPeriodeDefinedException {
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
    when(pruefungsperiode.block(modelBock.getId())).thenReturn(modelBock);
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
  void createBlock_Successful() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.addPlanungseinheit(any())).thenReturn(true);
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    Block ro = deviceUnderTest.createBlock("Hallo", RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    assertThat(ro.getPruefungen().stream().map(Pruefung::getPruefungsnummer)
        .collect(Collectors.toSet())).containsOnly(RO_ANALYSIS_UNPLANNED.getPruefungsnummer(),
        RO_DM_UNPLANNED.getPruefungsnummer());
  }

  private void configureMock_getPruefungToROPruefung(ReadOnlyPruefung... pruefungen) {
    for (ReadOnlyPruefung p : pruefungen) {
      Pruefung temp = getPruefungOfReadOnlyPruefung(p);
      when(pruefungsperiode.pruefung(p.getPruefungsnummer())).thenReturn(temp);
    }
  }

  @Test
  @DisplayName("successfully remove pruefung from scheduled block, only one pruefung in block")
  void removePruefungFromBlockPlannedBlock() throws NoPruefungsPeriodeDefinedException {

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
    Block actualBlock = deviceUnderTest.removePruefungFromBlock(
        block, pruefung);

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    assertThat(actualBlock.isGeplant()).isFalse();
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
  void createBlock_Successful2() throws NoPruefungsPeriodeDefinedException {
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    when(pruefungsperiode.addPlanungseinheit(any())).thenReturn(
        true); // pruefungsperiode is gemocked!

    Block ro = deviceUnderTest.createBlock("Hallo", RO_ANALYSIS_UNPLANNED, RO_DM_UNPLANNED);
    Block model = new BlockImpl(pruefungsperiode, "Name", Blocktyp.SEQUENTIAL);
    LocalDateTime now = LocalDateTime.now();
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(model, now,
        RO_HASKELL_UNPLANNED);

    assertThat(ro.getPruefungen().stream().map(Pruefung::getPruefungsnummer)
        .collect(Collectors.toSet())).containsOnly(RO_ANALYSIS_UNPLANNED.getPruefungsnummer(),
        RO_DM_UNPLANNED.getPruefungsnummer());
    assertThat(
        new PruefungDTOBuilder(new LinkedList<>(model.getPruefungen()).get(0)).build()).isEqualTo(
        new PruefungDTOBuilder(RO_HASKELL_UNPLANNED).withStartZeitpunkt(now).build());
  }

  @Test
  void getBetween() throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {

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
    assertThat(this.deviceUnderTest.getAllPlanungseinheitenBetween(start, end)).containsAll(
        listPruefung);
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

    when(this.pruefungsperiode.planungseinheitenBetween(start, end)).thenReturn(setPlanung);
    assertThrows(IllegalTimeSpanException.class,
        () -> this.deviceUnderTest.getAllPlanungseinheitenBetween(end, start));

  }

  @Test
  void getPlanungseinheitenBetween()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {

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

    when(this.pruefungsperiode.planungseinheitenBetween(start, end)).thenReturn(setPlanung);
    assertThrows(IllegalTimeSpanException.class,
        () -> this.deviceUnderTest.getAllPlanungseinheitenBetween(end, start));

  }

  @Test
  void getBlockToPruefungOptTest() throws NoPruefungsPeriodeDefinedException {
    //---- Start Configuration//
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    ReadOnlyPruefung dm = TestFactory.RO_DM_UNPLANNED;
    Pruefung modelAnalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(dm);
    LocalDateTime januar = LocalDateTime.of(2021, 1, 1, 1, 1);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        januar, modelAnalysis, modelDm);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, modelDm, modelAnalysis);
    //-----//
    Optional<Block> result = deviceUnderTest.getBlockTo(analysis);
    assertThat(result).isPresent();
    assertThat(result.get().getPruefungen()).containsOnly(modelAnalysis, modelDm);
  }

  @Test
  void getBlockToPruefungOptIsEmptyTest() throws NoPruefungsPeriodeDefinedException {
    //---- Start Configuration//
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    ReadOnlyPruefung dm = TestFactory.RO_DM_UNPLANNED;
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(dm);
    Pruefung modelAnalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    LocalDateTime januar = LocalDateTime.of(2021, 1, 1, 1, 1);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        januar, modelAnalysis);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, modelAnalysis, modelDm);

    Optional<Block> result = deviceUnderTest.getBlockTo(dm);
    assertThat(result).isEmpty();
  }

  @Test
  void getBlockToPruefungOptTest2() throws NoPruefungsPeriodeDefinedException {
    //---- Start Configuration//
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    Pruefung modelAnalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    ReadOnlyPruefung dm = TestFactory.RO_DM_UNPLANNED;
    Pruefung modelDm = TestFactory.getPruefungOfReadOnlyPruefung(dm);
    LocalDateTime januar = LocalDateTime.of(2021, 1, 1, 1, 1);
    TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        januar, modelAnalysis);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, modelAnalysis, modelDm);

    Optional<Block> result = deviceUnderTest.getBlockTo(analysis);
    assertThat(result).isPresent();
    assertThat(result.get().getPruefungen()).containsOnly(modelAnalysis);
  }

  @Test
  void removePruefungFromUnplannedBlock() throws NoPruefungsPeriodeDefinedException {
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
    Block actualBlock = deviceUnderTest.removePruefungFromBlock(
        block, RO_ANALYSIS_UNPLANNED);

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    assertThat(actualBlock.isGeplant()).isFalse();

  }

  @Test
  @DisplayName("successfully remove pruefung from scheduled block, more than one pruefung in block")
  void removePruefungFromBlockPlannedBlock2() throws NoPruefungsPeriodeDefinedException {
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
    Block actualBlock = deviceUnderTest.removePruefungFromBlock(
        block, pruefungToRemove);

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    assertThat(actualBlock.isGeplant()).isTrue();
  }

  @Test
  @DisplayName("successfully remove pruefung from unscheduled block, more than one pruefung in block")
  void removePruefungFromBlockNotPlannedBlock2() throws NoPruefungsPeriodeDefinedException {

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
    Block actualBlock = deviceUnderTest.removePruefungFromBlock(
        block, pruefungToRemove);

    // assertions
    ReadOnlyBlockAssert.assertThat(expectedBlock).isSameAs(converter.convertToROBlock(actualBlock));
    assertThat(actualBlock.isGeplant()).isFalse();
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
  void getBlockTo_pruefungMustNotBeNull() {
    ReadOnlyPruefung pruefung = null;
    assertThrows(NullPointerException.class, () -> deviceUnderTest.getBlockTo(pruefung));
  }

  @Test
  void getBlockTo_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getBlockTo(pruefung));
  }

  @Test
  void getBlockTo_unknownPruefung() {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.getBlockTo(pruefung));
  }

  @Test
  void getBlockToP_pruefungMustNotBeNull() {
    Pruefung pruefung = null;
    assertThrows(NullPointerException.class, () -> deviceUnderTest.getBlockTo(pruefung));
  }

  @Test
  void getBlockToP_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    Pruefung pruefung = getRandomUnplannedPruefung(1L);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getBlockTo(pruefung));
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
  void ungeplantePruefungToTeilnehmerkreisTest() throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwl);
    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(bwl)).containsOnly(analysis);
  }

  @Test
  void geplantePruefungToTeilnehmerkreisTest() throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.setStartzeitpunkt(LocalDateTime.of(1, 1, 1, 1, 1));
    analysis.addTeilnehmerkreis(bwl);
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.geplantePruefungenForTeilnehmerkreis(bwl)).containsOnly(analysis);
  }

  @Test
  void ungeplantePruefungToTeilnehmerkreisEmptyTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(TestFactory.infPtl);
    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(
        deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(TestFactory.bwlBachelor)).isEmpty();
  }

  @Test
  void geplantePruefungToTeilnehmerkreisEmptyTest() throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(TestFactory.infPtl);
    analysis.setStartzeitpunkt(LocalDateTime.of(1, 1, 1, 1, 1));
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(
        deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(TestFactory.bwlBachelor)).isEmpty();
  }

  @Test
  void geplantePruefungenForTeilnehmerkreis_teilnehmerkreisMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.geplantePruefungenForTeilnehmerkreis(null));
  }

  @Test
  void geplantePruefungenForTeilnehmerkreis_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.geplantePruefungenForTeilnehmerkreis(getRandomTeilnehmerkreis(1L)));
  }

  @Test
  void addPruefungToBlock_none_scheduled_successful() throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = new BlockImpl(pruefungsperiode, "b1", Blocktyp.PARALLEL);
    ReadOnlyBlock blockToAddTo = new BlockDTO("b1", null, null,
        new HashSet<>(), modelBlock.getId(), Blocktyp.PARALLEL);
    ReadOnlyBlock expectedBlock = new BlockDTO("b1", null, RO_ANALYSIS_UNPLANNED.getDauer(),
        Set.of(RO_ANALYSIS_UNPLANNED), modelBlock.getId(), Blocktyp.PARALLEL);

    when(pruefungsperiode.ungeplanteBloecke()).thenReturn(Set.of(modelBlock));
    when(pruefungsperiode.block(modelBlock.getId())).thenReturn(modelBlock);
    configureMock_getPruefungToROPruefung(RO_ANALYSIS_UNPLANNED);
    Block result = deviceUnderTest.addPruefungToBlock(blockToAddTo,
        RO_ANALYSIS_UNPLANNED);

    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result));
  }

  @Test
  void addPruefungToBlock_block_scheduled_successful() throws NoPruefungsPeriodeDefinedException {
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
    Block result = deviceUnderTest.addPruefungToBlock(inputBlock,
        RO_DM_UNPLANNED);
    // compare results
    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result));
  }

  @Test
  void addPruefungToBlock_pruefung_scheduled_successful()
      throws NoPruefungsPeriodeDefinedException {
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
    Block result = deviceUnderTest.addPruefungToBlock(blockToAddTo,
        analysis);

    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result));
  }

  @Test
  void setNameOfBlockTest() throws NoPruefungsPeriodeDefinedException {

    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED);
    Block model = TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        LocalDateTime.MIN, analysis);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, analysis);

    when(pruefungsperiode.block(anyInt())).thenReturn(model);
    deviceUnderTest.setNameOfBlock(converter.convertToROBlock(model), "Ciao");

    assertThat(model.getName()).isEqualTo("Ciao");
  }

  @Test
  void setNameOfBlockWithScoringTest() throws NoPruefungsPeriodeDefinedException {

    Pruefung analysis = TestFactory.getPruefungOfReadOnlyPruefung(TestFactory.RO_DM_UNPLANNED);
    Block model = TestFactory.configureMock_addPruefungToBlockModel(pruefungsperiode, "Hallo",
        LocalDateTime.MIN, analysis);
    TestFactory.configureMock_getPruefungFromPeriode(pruefungsperiode, analysis);

    when(pruefungsperiode.block(anyInt())).thenReturn(model);
    deviceUnderTest.setNameOfBlock(converter.convertToROBlock(model), "Ciao");
    assertThat(model.getName()).isEqualTo("Ciao");
    assertThat(model.getPruefungen()).containsOnly(analysis);


  }

  @Test
  void addPruefungToBlock_both_scheduled_successful() throws NoPruefungsPeriodeDefinedException {
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
    Block result = deviceUnderTest.addPruefungToBlock(inputBlock,
        pruefungToAdd);
    // compare results
    ReadOnlyBlockAssert.assertThat(expectedBlock)
        .isSameAs(converter.convertToROBlock(result));
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

  @Test
  void getAllKlausurenFromPruefer_prueferMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAllKlausurenFromPruefer(null));
  }

  @Test
  void getAllKlausurenFromPruefer_no_pruefungen() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Collections.emptySet());
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("test")).isEmpty();
  }

  @Test
  void getAllKlausurenFromPruefer_no_pruefungen_with_pruefer()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Set.of(analysis, haskell, dm));
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("test")).isEmpty();
  }

  @Test
  void getAllKlausurenFromPruefer_only_one_pruefer() throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addPruefer("test");
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("test")).contains(analysis);
  }

  @Test
  void getAllKlausurenFromPruefer_more_than_one_pruefer()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addPruefer("harms");
    analysis.addPruefer("test");
    analysis.addPruefer("pruefer2");
    analysis.addPruefer("pruefer3");
    analysis.addPruefer("pruefer4");
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Set.of(analysis, haskell, dm));
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("test")).contains(analysis);
  }

  @Test
  void getAllKlausurenFromPruefer_more_than_one_pruefer_different_pruefungen()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addPruefer("harms");
    analysis.addPruefer("test");
    analysis.addPruefer("pruefer2");
    analysis.addPruefer("pruefer3");
    analysis.addPruefer("pruefer4");
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addPruefer("t");
    haskell.addPruefer("e");
    haskell.addPruefer("s");
    haskell.addPruefer("t");
    haskell.addPruefer("test");
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addPruefer("te");
    dm.addPruefer("st");
    dm.addPruefer("test");
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Set.of(analysis, haskell, dm));
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("test")).contains(analysis,
        haskell, dm);
  }

  @Test
  void getAllKlausurenFromPruefer_more_than_one_pruefer_block()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addPruefer("harms");
    analysis.addPruefer("test");
    analysis.addPruefer("pruefer2");
    analysis.addPruefer("pruefer3");
    analysis.addPruefer("pruefer4");
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addPruefer("t");
    haskell.addPruefer("e");
    haskell.addPruefer("s");
    haskell.addPruefer("t");
    haskell.addPruefer("test");
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addPruefer("te");
    dm.addPruefer("st");
    dm.addPruefer("test");
    Block block = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);
    block.addPruefung(analysis);
    block.addPruefung(dm);
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Set.of(analysis, haskell, dm));
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("test")).contains(analysis,
        haskell, dm);
  }

  @Test
  void getAllKlausurenFromPruefer_planned_and_not_planned()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addPruefer("harms");
    analysis.addPruefer("pruefer");
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addPruefer("harms");
    haskell.addPruefer("e");
    haskell.addPruefer("s");
    haskell.addPruefer("t");
    haskell.addPruefer("pruefer");
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addPruefer("te");
    dm.addPruefer("st");
    dm.addPruefer("pruefer");
    Block block = new BlockImpl(pruefungsperiode, "block", Blocktyp.PARALLEL);
    block.addPruefung(analysis);
    block.addPruefung(dm);
    block.setStartzeitpunkt(LocalDateTime.of(2022, 1, 2, 12, 1));
    when(pruefungsperiode.getPlanungseinheiten()).thenReturn(Set.of(analysis, haskell, dm));
    assertThat(deviceUnderTest.getAllKlausurenFromPruefer("pruefer")).contains(
        analysis,
        haskell, dm);
  }

  @Test
  void getAnkertag_test() throws NoPruefungsPeriodeDefinedException {
    LocalDate ankertag = LocalDate.of(2022, 2, 2);
    when(pruefungsperiode.getAnkertag()).thenReturn(ankertag);
    assertThat(deviceUnderTest.getAnkertag()).isEqualTo(ankertag);
  }

  @Test
  void getAnkertag_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class, () -> deviceUnderTest.getAnkertag());
  }

  @Test
  void getAnzahlStudentenZeitpunkt_zeitpunktMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAnzahlStudentenZeitpunkt(null));
  }

  @Test
  void getAnzahlStudentenZeitpunkt_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAnzahlStudentenZeitpunkt(getRandomTime(1L)));
  }

  @Test
  void getAnzahlStudentenZeitpunkt_no_exam_at_zeitpunkt()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 1, 1, 0, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.setSchaetzung(infBachelor, 20);
    analysis.setSchaetzung(infMaster, 10);
    analysis.setSchaetzung(bwlBachelor, 40);
    analysis.setStartzeitpunkt(LocalDateTime.of(2022, 2, 2, 2, 2));
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.getAnzahlStudentenZeitpunkt(termin)).isZero();
  }

  @Test
  void getAnzahlStudentenZeitpunkt_one_exam_at_time_multiple_teilnehmerkreise()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int infB = 20;
    int infM = 10;
    int bwlB = 40;
    int expectedResult = infB + infM + bwlB;
    analysis.addTeilnehmerkreis(infBachelor, infB);
    analysis.addTeilnehmerkreis(infMaster, infM);
    analysis.addTeilnehmerkreis(bwlBachelor, bwlB);
    analysis.setStartzeitpunkt(termin);
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.getAnzahlStudentenZeitpunkt(termin)).isEqualTo(expectedResult);
  }

  @Test
  void getAnzahlStudentenZeitpunkt_different_exams() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    int infB_haskell = 20;
    int infB_analysis = 10;
    int offset = 5;
    analysis.addTeilnehmerkreis(infBachelor, infB_analysis);
    haskell.addTeilnehmerkreis(infMaster, infB_haskell);

    analysis.setStartzeitpunkt(termin);
    haskell.setStartzeitpunkt(termin.plusMinutes(offset));

    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis, haskell));
    assertThat(deviceUnderTest.getAnzahlStudentenZeitpunkt(termin.plusMinutes(2 * offset)))
        .isEqualTo(infB_analysis + infB_haskell);
  }

  @Test
  void getAnzahlStudentenZeitpunkt_different_exams_second_has_not_started_yet()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    int infB_haskell = 20;
    int infB_analysis = 10;
    int offset = 10;
    analysis.addTeilnehmerkreis(infBachelor, infB_analysis);
    haskell.addTeilnehmerkreis(infMaster, infB_haskell);

    analysis.setStartzeitpunkt(termin);
    haskell.setStartzeitpunkt(termin.plusMinutes(offset));

    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis, haskell));
    assertThat(deviceUnderTest.getAnzahlStudentenZeitpunkt(termin.plusMinutes(offset / 2)))
        .isEqualTo(infB_analysis);
  }

  @Test
  void getAnzahlStudentenZeitpunkt_on_endzeitpunkt() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int infB_analysis = 10;
    analysis.addTeilnehmerkreis(infBachelor, infB_analysis);

    analysis.setStartzeitpunkt(termin);

    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis));
    assertThat(deviceUnderTest.getAnzahlStudentenZeitpunkt(analysis.endzeitpunkt()))
        .isEqualTo(infB_analysis);
  }

  @Test
  void getAnzahlStudentenZeitpunkt_in_Block() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime termin = LocalDateTime.of(2022, 2, 2, 2, 2);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    int infB_haskell = 20;
    int infB_analysis = 10;
    analysis.addTeilnehmerkreis(infBachelor, infB_analysis);
    haskell.addTeilnehmerkreis(infBachelor, infB_haskell);

    Block block = new BlockImpl(pruefungsperiode, "block", PARALLEL);
    block.addPruefung(analysis);
    block.addPruefung(haskell);

    block.setStartzeitpunkt(termin);
    when(pruefungsperiode.geplantePruefungen()).thenReturn(Set.of(analysis, haskell));
    assertThat(deviceUnderTest.getAnzahlStudentenZeitpunkt(termin))
        .isEqualTo(infB_haskell);
  }

  @Test
  void existsBlockWith_exists() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.block(anyInt())).thenReturn(mock(Block.class));
    assertThat(deviceUnderTest.existsBlockWith(1234)).isTrue();
  }

  @Test
  void existsBlockWith_doesNotExist() throws NoPruefungsPeriodeDefinedException {
    when(pruefungsperiode.block(anyInt())).thenReturn(null);
    assertThat(deviceUnderTest.existsBlockWith(1234)).isFalse();
  }

  @Test
  void existsBlockWith_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.existsBlockWith(1));
  }

  @Test
  void getPlanungseinheitenAt_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getPlanungseinheitenAt(getRandomTime(1L)));
  }

  @Test
  void getPlanungseinheitenAt_timeMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.getPlanungseinheitenAt(null));
  }

  @Test
  void setAnkertag_ankertagMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.setAnkertag(null));
  }

  @Test
  void setAnkertag_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setAnkertag(getRandomTime(1L).toLocalDate()));
  }

  @Test
  void setAnkertag_beforeStartOfPruefungsperiode() {
    LocalDate newAnkertag = getRandomTime(1L).toLocalDate();
    when(pruefungsperiode.getStartdatum()).thenReturn(newAnkertag.plusDays(1));
    when(pruefungsperiode.getEnddatum()).thenReturn(newAnkertag.plusDays(2));
    assertThrows(IllegalTimeSpanException.class, () -> deviceUnderTest.setAnkertag(newAnkertag));
  }

  @Test
  void setAnkertag_atStartOfPruefungsperiode() {
    LocalDate newAnkertag = getRandomTime(1L).toLocalDate();
    when(pruefungsperiode.getStartdatum()).thenReturn(newAnkertag);
    when(pruefungsperiode.getEnddatum()).thenReturn(newAnkertag.plusDays(1));
    assertDoesNotThrow(() -> deviceUnderTest.setAnkertag(newAnkertag));
  }

  @Test
  void setAnkertag_afterEndOfPruefungsperiode() {
    LocalDate newAnkertag = getRandomTime(1L).toLocalDate();
    when(pruefungsperiode.getStartdatum()).thenReturn(newAnkertag.minusDays(2));
    when(pruefungsperiode.getEnddatum()).thenReturn(newAnkertag.minusDays(1));
    assertThrows(IllegalTimeSpanException.class, () -> deviceUnderTest.setAnkertag(newAnkertag));
  }

  @Test
  void setAnkertag_atEndOfPruefungsperiode() {
    LocalDate newAnkertag = getRandomTime(1L).toLocalDate();
    when(pruefungsperiode.getStartdatum()).thenReturn(newAnkertag.minusDays(2));
    when(pruefungsperiode.getEnddatum()).thenReturn(newAnkertag);
    assertDoesNotThrow(() -> deviceUnderTest.setAnkertag(newAnkertag));
  }

  @Test
  void setAnkertag_ankertagIsSetCorrectly()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDate newAnkertag = getRandomTime(1L).toLocalDate();
    when(pruefungsperiode.getStartdatum()).thenReturn(newAnkertag.minusDays(20));
    when(pruefungsperiode.getEnddatum()).thenReturn(newAnkertag.plusDays(20));
    deviceUnderTest.setAnkertag(newAnkertag);
    verify(pruefungsperiode).setAnkertag(newAnkertag);
  }

  @Test
  void getPruefung_pruefungMustNotBeNull() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.getPruefung(null));
  }

  @Test
  void getPruefung_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getPruefung(getRandomUnplannedROPruefung(1L)));
  }

  @Test
  void getPruefung_doesNotExist() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThat(deviceUnderTest.getPruefung(pruefung)).isEmpty();
  }

  @Test
  void getPruefung_exists() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getRandomUnplannedROPruefung(1L);
    when(pruefungsperiode.pruefung(anyString())).thenReturn(getRandomPlannedPruefung(1L));
    assertThat(deviceUnderTest.getPruefung(pruefung)).isPresent();
  }

  @Test
  void getStartOfPeriode_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getStartOfPeriode());
  }

  @Test
  void getEndOfPeriode_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getEndOfPeriode());
  }

  @Test
  void getPeriodenKapazitaet_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getPeriodenKapazitaet());
  }

  @Test
  void getSemester_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getSemester());
  }

  @Test
  void getAllTeilnehmerkreise_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAllTeilnehmerkreise());
  }

  @Test
  void ungeplantePruefungenForTeilnehmerkreis_teilnehmerkreisMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(null));
  }

  @Test
  void ungeplantePruefungenForTeilnehmerkreis_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.ungeplantePruefungenForTeilnehmerkreis(getRandomTeilnehmerkreis(1L)));
  }

  @Test
  void getAllPruefungenBetween_startMustNotBeNull() {
    LocalDateTime time = getRandomTime(2L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAllPruefungenBetween(null, time));
  }

  @Test
  void getAllPruefungenBetween_endMustNotBeNull() {
    LocalDateTime time = getRandomTime(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAllPruefungenBetween(time, null));
  }

  @Test
  void getAllPruefungenBetween_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAllPruefungenBetween(getRandomTime(1L),
            getRandomTime(1L).plusHours(1)));
  }

  @Test
  void getAllPlanungseinheitenBetween_startMustNotBeNull() {
    LocalDateTime time = getRandomTime(2L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAllPlanungseinheitenBetween(null, time));
  }

  @Test
  void getAllPlanungseinheitenBetween_endMustNotBeNull() {
    LocalDateTime time = getRandomTime(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getAllPlanungseinheitenBetween(time, null));
  }

  @Test
  void getAllPlanungseinheitenBetween_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getAllPlanungseinheitenBetween(getRandomTime(1L),
            getRandomTime(1L).plusHours(1)));
  }

  @Test
  void areInSameBlock_noNullParameters() {
    Pruefung pruefung = getRandomUnplannedPruefung(1L);
    assertThrows(NullPointerException.class, () -> deviceUnderTest.areInSameBlock(null, pruefung));
    assertThrows(NullPointerException.class, () -> deviceUnderTest.areInSameBlock(pruefung, null));
  }

  @Test
  void areInSameBlock_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    Pruefung pruefung = getRandomUnplannedPruefung(1L);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.areInSameBlock(pruefung, pruefung));
  }

  @Test
  void setKapazitaetStudents_negativeCapacity() {
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.setKapazitaetStudents(-1));
  }

  @Test
  void setKapazitaetStudents_minimalCapacity() {
    assertDoesNotThrow(() -> deviceUnderTest.setKapazitaetStudents(0));
  }

  @Test
  void setKapazitaetStudents_noPruefungsperiode() {
    deviceUnderTest = new DataAccessService(null);
    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.setKapazitaetStudents(1));
  }

  @Test
  void setKapazitaetStudents_actuallySetNewValue() throws NoPruefungsPeriodeDefinedException {
    deviceUnderTest.setKapazitaetStudents(1);
    verify(pruefungsperiode).setKapazitaet(1);
  }

}
