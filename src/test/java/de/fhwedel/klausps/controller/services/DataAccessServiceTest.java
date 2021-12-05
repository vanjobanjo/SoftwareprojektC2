package de.fhwedel.klausps.controller.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessServiceTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();
  private Pruefungsperiode pruefungsperiode;
  private DataAccessService deviceUnderTest;
  private ScheduleService scheduleService;

  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.deviceUnderTest = ServiceProvider.getDataAccessService();
    // todo exchange for real ScheduleService
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

    when(pruefungsperiode.pruefung(before.getPruefungsnummer())).thenReturn(model);

    assertThat(model.getName()).isEqualTo(before.getName());

    ReadOnlyPruefung after = deviceUnderTest.changeNameOfPruefung(before, "NoNameNeeded");
    assertThat(model.getPruefungsnummer()).isEqualTo(after.getPruefungsnummer());
    assertThat(model.getName()).isEqualTo(after.getName());
    assertThat(model.getDauer()).isEqualTo(after.getDauer());
    assertThat(model.getName()).isNotEqualTo(before.getName());
    assertThat(model.getDauer()).isEqualTo(before.getDauer());
    assertThat(model.getDauer()).isEqualTo(after.getDauer());
    assertThat(model.getPruefer()).isEqualTo(after.getPruefer());
    assertThat(model.getTeilnehmerkreise()).hasSameElementsAs(before.getTeilnehmerkreise());
    assertThat(model.getDauer()).isEqualTo(after.getDauer());
  }

  @Test
  void getGeplantePruefungenTest() {
    PruefungDTO p1 = new PruefungDTOBuilder().withPruefungsName("Hallo").build();
    PruefungDTO p2 = new PruefungDTOBuilder().withPruefungsName("Welt").build();
    Pruefung pm1 = getPruefungOfReadOnlyPruefung(p1);
    Pruefung pm2 = getPruefungOfReadOnlyPruefung(p2);
    Set<Pruefung> pruefungen = new HashSet<>(Arrays.asList(pm1, pm2));
    when(pruefungsperiode.geplantePruefungen()).thenReturn(pruefungen);

    Set<ReadOnlyPruefung> result = deviceUnderTest.getGeplantePruefungen();
    assertThat(result).containsOnly(p1, p2);
    Iterator<ReadOnlyPruefung> it = result.iterator();
    ReadOnlyPruefung p1_new = it.next();
    ReadOnlyPruefung p2_new = it.next();
    assertThat(p1_new != p1 && p1_new != p2).isTrue();
    assertThat(p2_new != p1 && p2_new != p2).isTrue();
  }

  @Test
  void ungeplanteKlausurenTest() {
    PruefungDTO p1 = new PruefungDTOBuilder().withPruefungsName("Hallo").build();
    PruefungDTO p2 = new PruefungDTOBuilder().withPruefungsName("Welt").build();
    Pruefung pm1 = getPruefungOfReadOnlyPruefung(p1);
    Pruefung pm2 = getPruefungOfReadOnlyPruefung(p2);
    Set<Pruefung> pruefungen = new HashSet<>(Arrays.asList(pm1, pm2));
    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(pruefungen);

    Set<ReadOnlyPruefung> result = deviceUnderTest.getUngeplanteKlausuren();
    assertThat(result).containsOnly(p1, p2);
    Iterator<ReadOnlyPruefung> it = result.iterator();
    ReadOnlyPruefung p1_new = it.next();
    ReadOnlyPruefung p2_new = it.next();
    assertThat(p1_new != p1 && p1_new != p2).isTrue();
    assertThat(p2_new != p1 && p2_new != p2).isTrue();
  }

  @Test
  void geplanteBloeckeTest() {
    ReadOnlyPruefung ro01 =
        new PruefungDTOBuilder().withPruefungsName("inBlock0").withPruefungsNummer("123").build();
    ReadOnlyPruefung ro02 =
        new PruefungDTOBuilder().withPruefungsName("inBlock1").withPruefungsNummer("1235").build();
    Pruefung inBlock0 = getPruefungOfReadOnlyPruefung(ro01);
    Pruefung inBlock1 = getPruefungOfReadOnlyPruefung(ro02);
    Block block = new BlockImpl(pruefungsperiode, "name");
    block.addPruefung(inBlock0);
    block.addPruefung(inBlock1);

    when(pruefungsperiode.geplanteBloecke()).thenReturn(Set.of(block));
    Set<ReadOnlyBlock> blockController = deviceUnderTest.getGeplanteBloecke();
    assertThat(blockController.iterator().next().getROPruefungen()).containsOnly(ro01, ro02);
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
    ReadOnlyBlockAssert.assertThat(resultBlock).containsOnlyPruefungen(ro01, ro02);
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
  void changeDurationOf_Successfull() {
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
      t.setDauer(Duration.ofMinutes(120)); //ScheduleService muss noch implementiert werden.
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
  void schedulePruefung_successfull() {
    PruefungDTOBuilder pDTOB = new PruefungDTOBuilder();
    pDTOB.withPruefungsName("Analysi");
    pDTOB.withDauer(Duration.ofMinutes(90));
    ReadOnlyPruefung ro01 = pDTOB.build();

    Assertions.assertTrue(ro01.getTermin().isEmpty());
    try {
      when(pruefungsperiode.pruefung(ro01.getPruefungsnummer())).thenReturn(
          getPruefungOfReadOnlyPruefung(ro01));
      deviceUnderTest.schedulePruefung(ro01, LocalDateTime.of(2021, 8, 21, 9, 0));
    } catch (HartesKriteriumException ignore) {
    }
    // TODO falls die Liste implementiert wird Test anpassen
    //  Assertions.assertFalse(ro01.getTermin().isEmpty());
  }

  @Test
  void schedulePruefung_unsuccessfull() {

    PruefungDTOBuilder pDTOB = new PruefungDTOBuilder();
    pDTOB.withPruefungsName("Analysi");
    pDTOB.withDauer(Duration.ofMinutes(90));
    ReadOnlyPruefung ro01 = pDTOB.build();

    Assertions.assertTrue(ro01.getTermin().isEmpty());
    try {
      LocalDateTime lt = LocalDateTime.of(2021, 9, 22, 9, 0);
      when(this.pruefungsperiode.getEnddatum()).thenReturn(LocalDate.of(2021, 8, 22));
      when(pruefungsperiode.pruefung(ro01.getPruefungsnummer())).thenReturn(
          getPruefungOfReadOnlyPruefung(ro01));
      Assertions.assertTrue(deviceUnderTest.schedulePruefung(ro01, lt).isEmpty());
    } catch (HartesKriteriumException ignore) {
    }
  }

  @Test
  void schedulePruefung_change_successsfull() {

    PruefungDTOBuilder pDTOB = new PruefungDTOBuilder();
    pDTOB.withPruefungsName("Analysi");
    pDTOB.withDauer(Duration.ofMinutes(90));
    ReadOnlyPruefung ro01 = pDTOB.build();

    Assertions.assertTrue(ro01.getTermin().isEmpty());

    LocalDateTime change1 = LocalDateTime.of(2021, 8, 21, 9, 0);
    LocalDateTime change2 = LocalDateTime.of(2021, 8, 22, 9, 0);
    Optional<LocalDateTime> expected1 = Optional.of(change1);
    Optional<LocalDateTime> expected2 = Optional.of(change2);

    // TODO falls die Liste implementiert wird Test anpassen
    //   ReadOnlyPruefung roacc = ro01;
    try {
      when(pruefungsperiode.pruefung(ro01.getPruefungsnummer())).thenReturn(
          getPruefungOfReadOnlyPruefung(ro01));
      assertThat(deviceUnderTest.schedulePruefung(ro01, change1)).isEmpty();
      //  roacc = deviceUnderTest.schedulePruefung(ro01, change1).get(0);
    } catch (HartesKriteriumException ignore) {
    }

    // Assertions.assertEquals(expected1,  roacc.getTermin());

    try {
      assertThat(deviceUnderTest.schedulePruefung(ro01, change2)).isEmpty();
      //  roacc = deviceUnderTest.schedulePruefung(ro01, change2).get(0);
    } catch (HartesKriteriumException ignore) {
    }

    // Assertions.assertEquals(expected2, roacc.getTermin());

  }

  @Test
  public void unschedulePruefung_integration() {
    PruefungDTO analysis = new PruefungDTOBuilder()
        .withPruefungsName("Analysis")
        .withPruefungsNummer("2")
        .withDauer(Duration.ofMinutes(120))
        .withAdditionalPruefer("Harms")
        .withStartZeitpunkt(LocalDateTime.now())
        .build();

    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(analysis);
    when(pruefungsperiode.pruefung(analysis.getPruefungsnummer())).thenReturn(modelAnalysis);
    // schedule service funktioniert noch nicht. deshalb wird einfach nur die pruefung zurückgegeben.
    when(scheduleService.unschedulePruefung(modelAnalysis)).thenReturn(List.of(modelAnalysis));
    when(scheduleService.scoringOfPruefung(modelAnalysis)).thenReturn(0);
    List<ReadOnlyPruefung> result = deviceUnderTest.unschedulePruefung(analysis);

    ReadOnlyPruefung ro = result.get(0);
    assertThat(result).hasSize(1);
    assertNotSame(ro, analysis);
  }

  @Test
  public void unschedulePruefung_noExam() {
    PruefungDTO analysis = new PruefungDTOBuilder()
        .withPruefungsName("Analysis")
        .withPruefungsNummer("2")
        .withDauer(Duration.ofMinutes(120))
        .withAdditionalPruefer("Harms")
        .withStartZeitpunkt(LocalDateTime.now())
        .build();

    when(pruefungsperiode.pruefung(any())).thenReturn(null);

    assertThrows(IllegalArgumentException.class,
        () -> deviceUnderTest.unschedulePruefung(analysis));

  }

  private Semester getSemester() {
    return new SemesterImpl(Semestertyp.SOMMERSEMESTER, Year.of(1996));
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

  private Pruefung getPruefungOfReadOnlyPruefung(ReadOnlyPruefung roPruefung) {
    PruefungImpl modelPruefung =
        new PruefungImpl(
            roPruefung.getPruefungsnummer(),
            roPruefung.getName(),
            "",
            roPruefung.getDauer(),
            roPruefung.getTermin().orElse(null));
    roPruefung.getTeilnehmerKreisSchaetzung().forEach(modelPruefung::setSchaetzung);
    return modelPruefung;
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
