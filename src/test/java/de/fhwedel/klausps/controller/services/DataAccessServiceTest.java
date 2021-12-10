package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyBlockAssert;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomDuration;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

class DataAccessServiceTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();

  final ReadOnlyPruefung RO_ANALYSIS =
          new PruefungDTOBuilder()
                  .withPruefungsName("Analysis")
                  .withPruefungsNummer("1")
                  .withDauer(Duration.ofMinutes(120))
                  .build();
  final ReadOnlyPruefung RO_DM =
          new PruefungDTOBuilder()
                  .withPruefungsName("DM")
                  .withPruefungsNummer("2")
                  .withDauer(Duration.ofMinutes(120))
                  .build();

  final ReadOnlyPruefung RO_HASKELL =
          new PruefungDTOBuilder()
                  .withPruefungsName("HASKELL")
                  .withPruefungsNummer("3")
                  .withDauer(Duration.ofMinutes(120))
                  .build();

  private Pruefungsperiode pruefungsperiode;
  private DataAccessService deviceUnderTest;
  private ScheduleService scheduleService;

  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.deviceUnderTest = ServiceProvider.getDataAccessService();
    // todo make sure the mocked class is not tested
    this.scheduleService = mock(ScheduleService.class);
    deviceUnderTest.setPruefungsperiode(pruefungsperiode);
    deviceUnderTest.setScheduleService(this.scheduleService);
  }

  @Test
  @DisplayName("A Pruefung gets created by calling the respective method of the pruefungsPeriode")
  void createPruefungSuccessTest() {
    ReadOnlyPruefung pruefung =
        deviceUnderTest.createPruefung(
            "Analysis", "b123", "pruefer 1", Duration.ofMinutes(90), new HashMap<>());
    assertThat(pruefung).isNotNull();
    verify(pruefungsperiode, times(1)).addPlanungseinheit(any());
  }

  @Test
  @DisplayName("A created pruefung has the expected attributes")
  void createPruefungSuccessRightAttributesTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    ReadOnlyPruefung actual =
        deviceUnderTest.createPruefung(
            expected.getName(),
            expected.getPruefungsnummer(),
            expected.getPruefer(),
            expected.getDauer(),
            expected.getTeilnehmerKreisSchaetzung());

    ReadOnlyPruefungAssert.assertThat(actual).isTheSameAs(getReadOnlyPruefung());
  }

  @Test
  @DisplayName("A created pruefung is persisted in the data model")
  void createPruefungSaveInModelTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    ReadOnlyPruefung actual =
        deviceUnderTest.createPruefung(
            expected.getName(),
            expected.getPruefungsnummer(),
            expected.getPruefer(),
            expected.getDauer(),
            expected.getTeilnehmerKreisSchaetzung());
    verify(pruefungsperiode, times(1)).addPlanungseinheit(notNull());
    assertThat(actual).isNotNull();
  }

  @Test
  @DisplayName("A pruefung can not be created when one with the same pruefungsnummer")
  void createPruefung_existsAlreadyTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    Pruefung test =
        new PruefungImpl(
            expected.getPruefungsnummer(), expected.getName(), "ABCDEF", expected.getDauer());

    when(pruefungsperiode.pruefung(expected.getPruefungsnummer())).thenReturn(test);
    assertThat(
            deviceUnderTest.createPruefung(
                expected.getName(),
                expected.getPruefungsnummer(),
                expected.getPruefer(),
                expected.getDauer(),
                expected.getTeilnehmerKreisSchaetzung()))
        .isNull();
  }

  @Test
  @DisplayName("Change name of a Pruefung")
  void setName_successfullyTest() {
    ReadOnlyPruefung before = getReadOnlyPruefung();
    Pruefung model = getPruefungOfReadOnlyPruefung(before);
    String newName = "NoNameNeeded";

    when(pruefungsperiode.pruefung(before.getPruefungsnummer())).thenReturn(model);
    ReadOnlyPruefung after = deviceUnderTest.changeNameOfPruefung(before, newName);
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
    List<ReadOnlyPruefung> pruefungen = getRandomPruefungen(1234, 2);
    List<Pruefung> pruefungenFromModel = convertPruefungenFromReadonlyToModel(pruefungen);
    Block initialBlock = new BlockImpl(pruefungsperiode, "name");
    pruefungenFromModel.forEach(initialBlock::addPruefung);

    when(pruefungsperiode.geplanteBloecke()).thenReturn(Set.of(initialBlock));

    Set<ReadOnlyBlock> blockController = deviceUnderTest.getGeplanteBloecke();
    ReadOnlyBlock resultingBlock = blockController.stream().toList().get(0);
    ReadOnlyBlockAssert.assertThat(resultingBlock)
        .containsOnlyPruefungen(pruefungen.toArray(new ReadOnlyPruefung[0]));
  }

  @Test
  void ungeplanteBloeckeTest() {
    ReadOnlyPruefung ro01 =
        new PruefungDTOBuilder().withPruefungsName("inBlock0").withPruefungsNummer("123").build();
    ReadOnlyPruefung ro02 =
        new PruefungDTOBuilder().withPruefungsName("inBlock1").withPruefungsNummer("1235").build();
    Pruefung inBlock0 = getPruefungOfReadOnlyPruefung(ro01);
    Pruefung inBlock1 = getPruefungOfReadOnlyPruefung(ro02);
    Block block = new BlockImpl(pruefungsperiode, "name");
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
    when(pruefungsperiode.pruefung(anyString())).thenReturn(getPruefungWithPruefer("Cohen"));
    ReadOnlyPruefungAssert.assertThat(deviceUnderTest.addPruefer("b321", "Cohen"))
        .hasPruefer("Cohen");
  }

  @Test
  void addPruefer_unknownPruefungTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.addPruefer("b110", "Gödel"));
  }

  @Test
  void removePruefer_successTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(getPruefungWithPruefer("Cohen"));
    ReadOnlyPruefungAssert.assertThat(deviceUnderTest.removePruefer("b321", "Cohen"))
        .hasNotPruefer("Cohen");
  }

  @Test
  void removePruefer_unknownPruefungTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    assertThrows(
        IllegalArgumentException.class, () -> deviceUnderTest.removePruefer("b110", "Gödel"));
  }

  @Test
  void removePruefer_otherPrueferStayTest() {
    when(pruefungsperiode.pruefung(anyString()))
        .thenReturn(getPruefungWithPruefer("Hilbert", "Einstein"));
    ReadOnlyPruefungAssert.assertThat(deviceUnderTest.removePruefer("b321", "Hilbert"))
        .hasPruefer("Einstein")
        .hasNotPruefer("Cohen");
  }

  @Test
  void changeDurationOf_Successful() {
    PruefungDTOBuilder pDTOB = new PruefungDTOBuilder();
    pDTOB.withPruefungsName("Analysi");
    pDTOB.withDauer(Duration.ofMinutes(90));
    ReadOnlyPruefung ro01 = pDTOB.build();
    PruefungImpl t =
        new PruefungImpl(
            ro01.getPruefungsnummer(), ro01.getName(), "Keine Ahnnung", ro01.getDauer());
    pruefungsperiode.addPlanungseinheit(t);

    assertEquals(ro01.getDauer(), Duration.ofMinutes(90));
    ReadOnlyPruefung roAcc = ro01;
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
    assertThrows(
        IllegalArgumentException.class, () -> deviceUnderTest.changeDurationOf(ro01, minusMinus));
  }

  @Test
  void unschedulePruefung_integration() {
    LocalDateTime initialSchedule = LocalDateTime.of(2022, 1, 1, 10, 30);
    when(pruefungsperiode.pruefung(anyString()))
        .thenReturn(
            new PruefungImpl(
                "Pruefungsnummer", "name", "nbr", Duration.ofMinutes(90), initialSchedule));
    ReadOnlyPruefungAssert.assertThat(deviceUnderTest.unschedulePruefung(getReadOnlyPruefung()))
        .isNotScheduled();
  }

  @Test
  void unschedulePruefung_noExam() {
    when(pruefungsperiode.pruefung(any())).thenReturn(null);
    ReadOnlyPruefung somePruefung = getReadOnlyPruefung();
    assertThrows(
        IllegalArgumentException.class, () -> deviceUnderTest.unschedulePruefung(somePruefung));
  }

  @Test
  void scheduleBlock_integration() {
    ReadOnlyBlock blockToSchedule =
        new BlockDTO(
            "Namme",
            null,
            Duration.ZERO,
            false,
            new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));

    LocalDateTime termin = LocalDateTime.of(2000, 1,1, 0,0);
    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);

    ReadOnlyBlock result = deviceUnderTest.scheduleBlock(blockToSchedule, termin);
    ReadOnlyBlockAssert.assertThat(result).containsOnlyPruefungen(RO_ANALYSIS, RO_HASKELL, RO_DM);
    assertThat(result.getTermin()).hasValue(termin);
    for(ReadOnlyPruefung p : result.getROPruefungen()){
      ReadOnlyPruefungAssert.assertThat(p).isScheduledAt(termin);
    }
    assertThat(modelBlock.getStartzeitpunkt()).isEqualTo(termin);
  }

  @Test
  void SetPruefungsnummerTest() {

    String oldNumber = "2";
    String newNumber = "1";

    PruefungDTO analysis =
        new PruefungDTOBuilder()
            .withPruefungsName("Analysis")
            .withPruefungsNummer(oldNumber)
            .withDauer(Duration.ofMinutes(120))
            .withAdditionalPruefer("Harms")
            .withStartZeitpunkt(LocalDateTime.now())
            .build();

    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(analysis);
    when(pruefungsperiode.pruefung(oldNumber)).thenReturn(modelAnalysis);
    ReadOnlyPruefung analysisNewNumber = deviceUnderTest.setPruefungsnummer(analysis, newNumber);
    assertThat(analysisNewNumber.getPruefungsnummer()).isEqualTo(newNumber);
    assertThat(analysisNewNumber).isNotEqualTo(analysis); // Equal arbeitet prüft die Nummern
    assertThat(analysisNewNumber.getDauer()).isEqualTo(analysis.getDauer());
  }

  @Test
  void schedulePruefungTest() {
    LocalDateTime expectedSchedule = LocalDateTime.of(2022, 1, 1, 10, 30);
    when(pruefungsperiode.pruefung(anyString()))
        .thenReturn(new PruefungImpl("Pruefungsnummer", "name", "nbr", Duration.ofMinutes(90)));
    ReadOnlyPruefungAssert.assertThat(
            deviceUnderTest.schedulePruefung(getReadOnlyPruefung(), expectedSchedule))
        .isScheduledAt(expectedSchedule);
  }

  @Test
  void schedulePruefung_noExamTest() {
    when(pruefungsperiode.pruefung(anyString())).thenReturn(null);
    ReadOnlyPruefung somePruefung = getReadOnlyPruefung();
    LocalDateTime someSchedule = getRandomTime();
    assertThrows(
        IllegalArgumentException.class,
        () -> deviceUnderTest.schedulePruefung(somePruefung, someSchedule));
  }

  @Test
  void deletePruefung_successful() {
    ReadOnlyPruefung roP = getRandomPruefungen(8415, 1).get(0);
    Pruefung pruefungModel = this.getPruefungOfReadOnlyPruefung(roP);
    when(this.pruefungsperiode.pruefung(roP.getPruefungsnummer())).thenReturn(pruefungModel);
    when(this.pruefungsperiode.removePlanungseinheit(pruefungModel)).thenReturn(true);
    this.deviceUnderTest.deletePruefung(roP);
    verify(this.pruefungsperiode, times(1)).removePlanungseinheit(pruefungModel);
  }

  @Test
  void deletePruefung_throw() {
    ReadOnlyPruefung roP = getRandomPruefungen(8415, 1).get(0);
    when(this.pruefungsperiode.pruefung(any())).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> this.deviceUnderTest.deletePruefung(roP));
  }

  @Test
  void existsBlockSuccessful(){
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    null,
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));
    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isTrue();
  }

  @Test
  void existsBlockDifferentBlockNames(){
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    null,
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));

    Block modelBlock = new BlockImpl(pruefungsperiode, "OtherName", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();

  }

  @Test
  void existsBlockDifferentDates(){
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    LocalDateTime.now(),
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));

    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();

  }

  @Test
  void existsBlockDifferentDates2(){
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    LocalDateTime.now(),
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));
    LocalDateTime termin = LocalDateTime.of(2000, 1,1, 0,0);
    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", termin);

    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }


  @Test
  void existsBlockDifferentDates3(){
    LocalDateTime termin = LocalDateTime.of(2000, 1,1, 0,0);
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    termin,
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));
    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existsBlockROBlockHasLessPruefungen(){
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    null,
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL)));

    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM, RO_HASKELL);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  @Test
  void existsBlockROBlockHasMorePruefungen(){
    ReadOnlyBlock blockToSchedule =
            new BlockDTO(
                    "Name",
                    null,
                    Duration.ZERO,
                    false,
                    new HashSet<>(List.of(RO_ANALYSIS, RO_HASKELL, RO_DM)));

    Block modelBlock = new BlockImpl(pruefungsperiode, "Name", null);
    configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(modelBlock, RO_ANALYSIS, RO_DM);
    assertThat(deviceUnderTest.exists(blockToSchedule)).isFalse();
  }

  private void configureMock_buildModelBlockAndGetBlockToPruefungAndPruefungToNumber(Block modelBlock, ReadOnlyPruefung... pruefungen) {
    for(ReadOnlyPruefung p : pruefungen){
      Pruefung temp = getPruefungOfReadOnlyPruefung(p);
      modelBlock.addPruefung(temp);
      when(pruefungsperiode.pruefung(p.getPruefungsnummer())).thenReturn(temp);
      when(pruefungsperiode.block(temp)).thenReturn(modelBlock);
    }
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

  /**
   * Gibt eine vorgegeben ReadOnlyPruefung zurueck
   *
   * @return gibt eine vorgebene ReadOnlyPruefung zurueck
   */
  private ReadOnlyPruefung getReadOnlyPruefung() {
    return new PruefungDTOBuilder()
        .withPruefungsName("Analysis")
        .withPruefungsNummer("b001")
        .withAdditionalPruefer("Harms")
        .withDauer(Duration.ofMinutes(90))
        .build();
  }

  private LocalDateTime getRandomTime() {
    return LocalDateTime.of(2022, 7, 22, 12, 0);
  }

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
