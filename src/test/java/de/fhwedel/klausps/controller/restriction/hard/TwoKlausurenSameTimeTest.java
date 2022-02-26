package de.fhwedel.klausps.controller.restriction.hard;

import static de.fhwedel.klausps.controller.util.TestFactory.RO_ANALYSIS_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_DM_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_HASKELL_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.bwlBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.getPruefungOfReadOnlyPruefung;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungWith;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTime;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedPruefung;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class TwoKlausurenSameTimeTest {

  private DataAccessService dataAccessService;
  private TwoKlausurenSameTime deviceUnderTest;
  private Pruefungsperiode pruefungsperiode;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new TwoKlausurenSameTime(dataAccessService);
    this.pruefungsperiode = mock(Pruefungsperiode.class);
  }

  @Test
  void violationTwoPruefungParallelTest()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime same = LocalDateTime.of(2000, 1, 1, 1, 0);
    Duration minutes90 = Duration.ofMinutes(90);
    Pruefung pruefung1 = new PruefungImpl("1", "Pruefung1", "", minutes90, same);
    Pruefung pruefung2 = new PruefungImpl("2", "Pruefung2", "", minutes90, same);
    Teilnehmerkreis bwl = new TeilnehmerkreisImpl("blw", "1", 1, Ausbildungsgrad.BACHELOR);
    pruefung1.addTeilnehmerkreis(bwl, 20);
    pruefung2.addTeilnehmerkreis(bwl, 30);
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(pruefung1, pruefung2));
    when(dataAccessService.getBlockTo(pruefung1)).thenReturn(Optional.empty());
    TwoKlausurenSameTime deviceUnderTest = new TwoKlausurenSameTime(dataAccessService);
    HartesKriteriumAnalyse result = deviceUnderTest.evaluateRestriction(pruefung1).get();

    assertThat(result.getTeilnehmercount()).containsOnlyKeys(bwl);
    assertThat(result.getTeilnehmercount().values()).containsOnly(30);
    assertThat(result.getCausingPruefungen()).contains(pruefung1);
    assertThat(result.getCausingPruefungen()).contains(pruefung2);
    assertThat(result.getKriterium()).isEqualTo(HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);
  }

  @Test
  void twoKlausurenSameTimeTest_twoSameTime()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    analysis.setStartzeitpunkt(start);
    haskell.setStartzeitpunkt(start);
    Duration duration = Duration.ofMinutes(120);
    analysis.setDauer(duration);
    haskell.setDauer(duration);

    int students = 8;
    analysis.addTeilnehmerkreis(infBachelor, students);
    haskell.addTeilnehmerkreis(infBachelor, students);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(analysis, haskell));

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(haskell);

    assertTrue(analyse.isPresent());
    assertEquals(Set.of(analysis, haskell), analyse.get().getCausingPruefungen());

    assertEquals(Set.of(infBachelor), analyse.get().getTeilnehmercount().keySet());
    assertEquals(students,
        analyse.get().getTeilnehmercount().values().stream().reduce(0, Integer::sum));
  }


  @Test
  void twoKlausurenSameTime_NotSameTime_one_not_planned()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);

    haskell.setStartzeitpunkt(start);
    Duration duration = Duration.ofMinutes(120);
    analysis.setDauer(duration);
    haskell.setDauer(duration);

    int students = 8;
    analysis.addTeilnehmerkreis(infBachelor, students);
    haskell.addTeilnehmerkreis(infBachelor, students);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Collections.emptySet());

    assertThat(deviceUnderTest.evaluateRestriction(haskell)).isEmpty();
  }

  @Test
  void twoKlausurenSameTime_ThreeSameTime()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    int students = 8;
    analysis.addTeilnehmerkreis(infBachelor, students);
    haskell.addTeilnehmerkreis(infBachelor, students);
    dm.addTeilnehmerkreis(infBachelor, students);

    analysis.setStartzeitpunkt(start);
    haskell.setStartzeitpunkt(start);
    dm.setStartzeitpunkt(start);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(analysis, haskell, dm));

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(haskell);

    assertThat(analyse).isPresent();
    assertThat(analyse.get().getCausingPruefungen()).containsOnly(dm, analysis, haskell);
    assertThat(analyse.get().getTeilnehmercount()).containsOnlyKeys(infBachelor);
    assertThat(
        analyse.get().getTeilnehmercount().values().stream().reduce(0, Integer::sum)).isEqualTo(
        students);
  }

  @Test
  void twoKlausurenSameTime_ThreeSameTime_two_DifferentTeilnehmerkreise()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);
    Duration duration = Duration.ofMinutes(120);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    int students = 8;
    analysis.addTeilnehmerkreis(infBachelor, students);
    dm.addTeilnehmerkreis(bwlBachelor, students);
    haskell.addTeilnehmerkreis(infBachelor, students);
    haskell.addTeilnehmerkreis(bwlBachelor, students);

    analysis.setStartzeitpunkt(start);
    haskell.setStartzeitpunkt(start);
    dm.setStartzeitpunkt(start);

    analysis.setDauer(duration);
    haskell.setDauer(duration);
    dm.setDauer(duration);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(analysis, haskell, dm));

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(haskell);
    assertThat(analyse).isPresent();

    assertThat(analyse.get().getCausingPruefungen()).containsOnly(dm, analysis, haskell);
    assertThat(analyse.get().getTeilnehmercount()).containsOnlyKeys(infBachelor, bwlBachelor);
    assertThat(
        analyse.get().getTeilnehmercount().values().stream().reduce(0, Integer::sum))
        .isEqualTo(16);
  }

  @Test
  @DisplayName("HartesKriterium: TwoKlausurenSameTime in Block Parallel Überschneidung mit kürzerer Klausur")
  void test_Blocke2_Parallel_successful()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(59);
    Duration pruefungBDuration = Duration.ofMinutes(180);
    Duration pruefungCDuration = Duration.ofMinutes(59);

    Pruefung aPruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung bPruefung = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung cPruefung = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    aPruefung.setDauer(pruefungADuration);
    bPruefung.setDauer(pruefungBDuration);
    cPruefung.setDauer(pruefungCDuration);

    Block blockA2 = new BlockImpl(pruefungsperiode, "name", PARALLEL);
    blockA2.addPruefung(bPruefung);
    blockA2.addPruefung(aPruefung);

    blockA2.setStartzeitpunkt(startBlock);
    cPruefung.setStartzeitpunkt(startBlock.plusMinutes(90));

    int students = 8;

    aPruefung.addTeilnehmerkreis(infBachelor, students);
    bPruefung.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung.addTeilnehmerkreis(infBachelor, students);

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA2));

    assertThat(deviceUnderTest.evaluateRestriction(cPruefung)).isEmpty();
  }


  @Test
  @DisplayName("HartesKriterium: TwoKlausurenSameTIme in Block Parallel Überschneidung mit kürzere Klausur Block liegt nach Pruefung")
  void test_Blocke2_Parallel_successful_Pruefung_Vor_Block()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 9, 30);
    Duration pruefungADuration = Duration.ofMinutes(59);
    Duration pruefungBDuration = Duration.ofMinutes(180);
    Duration pruefungCDuration = Duration.ofMinutes(59);

    Block blockA = new BlockImpl(pruefungsperiode, "name", PARALLEL);

    Pruefung aPruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung bPruefung = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung cPruefung = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    aPruefung.setDauer(pruefungADuration);
    bPruefung.setDauer(pruefungBDuration);
    cPruefung.setDauer(pruefungCDuration);

    int students = 8;

    aPruefung.addTeilnehmerkreis(infBachelor, students);
    bPruefung.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung.addTeilnehmerkreis(infBachelor, students);

    blockA.addPruefung(aPruefung);
    blockA.addPruefung(bPruefung);
    blockA.setStartzeitpunkt(startBlock);
    cPruefung.setStartzeitpunkt(startBlock.plusMinutes(90));

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA));

    assertThat(deviceUnderTest.evaluateRestriction(cPruefung)).isEmpty();
  }


  @Test
  void test_Blocke2_SEQUENTIAL()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);

    Block blockA = new BlockImpl(pruefungsperiode, "name", SEQUENTIAL);

    Pruefung aPruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung bPruefung = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung cPruefung = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    aPruefung.setDauer(pruefungADuration);
    bPruefung.setDauer(pruefungBDuration);
    cPruefung.setDauer(pruefungCDuration);

    int students = 8;
    aPruefung.addTeilnehmerkreis(infBachelor, students);
    bPruefung.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung.addTeilnehmerkreis(infBachelor, students);

    blockA.addPruefung(aPruefung);
    blockA.addPruefung(bPruefung);

    blockA.setStartzeitpunkt(startBlock);
    cPruefung.setStartzeitpunkt(startBlock.plusMinutes(60));

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA));

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(cPruefung);
    assertThat(analyse).isPresent();
    assertThat(analyse.get().getCausingPruefungen()).containsOnly(cPruefung, aPruefung);
    assertThat(analyse.get().getTeilnehmercount()).containsOnlyKeys(infBachelor);
    assertThat(
        analyse.get().getTeilnehmercount().values().stream().reduce(0, Integer::sum))
        .isEqualTo(students);
  }

  @Test
  void test_Blocke2_Parallel_No_SameTeilnehmerkreise()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);

    Block blockA = new BlockImpl(pruefungsperiode, "name", PARALLEL);

    Pruefung aPruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung bPruefung = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung cPruefung = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    aPruefung.setDauer(pruefungADuration);
    bPruefung.setDauer(pruefungBDuration);
    cPruefung.setDauer(pruefungCDuration);

    int students = 8;
    aPruefung.addTeilnehmerkreis(bwlBachelor, students);
    bPruefung.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung.addTeilnehmerkreis(infBachelor, students);

    blockA.addPruefung(aPruefung);
    blockA.addPruefung(bPruefung);

    blockA.setStartzeitpunkt(startBlock);
    cPruefung.setStartzeitpunkt(startBlock.plusMinutes(60));

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA));

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(cPruefung);
    assertTrue(analyse.isEmpty());

  }


  @Test
  void test_Blocke2_SEQUENTIAL_No_SameTeilnehmerkreise()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);

    Block blockA = new BlockImpl(pruefungsperiode, "name", SEQUENTIAL);

    Pruefung aPruefung = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung bPruefung = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung cPruefung = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    aPruefung.setDauer(pruefungADuration);
    bPruefung.setDauer(pruefungBDuration);
    cPruefung.setDauer(pruefungCDuration);

    int students = 8;
    aPruefung.addTeilnehmerkreis(bwlBachelor, students);
    bPruefung.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung.addTeilnehmerkreis(infBachelor, students);

    blockA.addPruefung(aPruefung);
    blockA.addPruefung(bPruefung);

    blockA.setStartzeitpunkt(startBlock);
    cPruefung.setStartzeitpunkt(startBlock.plusMinutes(60));

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA));

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(cPruefung);
    assertTrue(analyse.isEmpty());
  }


  @Test
  void test_Blocke2_Sequential_LotPruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);
    Duration pruefungDDuration = Duration.ofMinutes(60);
    Duration pruefungEDuration = Duration.ofMinutes(60);

    Block blockA2 = new BlockImpl(pruefungsperiode, "name", SEQUENTIAL);

    Pruefung aPruefung2 = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    aPruefung2.setDauer(pruefungADuration);
    Pruefung bPruefung2 = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    aPruefung2.setDauer(pruefungBDuration);
    Pruefung cPruefung2 = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    aPruefung2.setDauer(pruefungCDuration);
    Pruefung dPruefung2 = new PruefungImpl("4", "aud", "abcde", pruefungDDuration);
    Pruefung ePruefung2 = new PruefungImpl("5", "sp", "abcdefg", pruefungEDuration);

    int students = 8;
    aPruefung2.addTeilnehmerkreis(infBachelor, students);
    bPruefung2.addTeilnehmerkreis(infBachelor, students);
    cPruefung2.addTeilnehmerkreis(infBachelor, students);
    dPruefung2.addTeilnehmerkreis(infBachelor, students);
    ePruefung2.addTeilnehmerkreis(infBachelor, students);

    aPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    bPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    dPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    ePruefung2.addTeilnehmerkreis(bwlBachelor, students);

    blockA2.addPruefung(aPruefung2);
    blockA2.addPruefung(bPruefung2);
    blockA2.addPruefung(dPruefung2);
    blockA2.addPruefung(ePruefung2);

    blockA2.setStartzeitpunkt(startBlock);
    cPruefung2.setStartzeitpunkt(startBlock.plusMinutes(60));

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA2));

    int affectedStudents = 16;

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(cPruefung2);
    assertThat(analyse).isPresent();
    assertThat(analyse.get().getCausingPruefungen()).containsOnly(aPruefung2, bPruefung2,
        cPruefung2, dPruefung2, ePruefung2);
    assertThat(analyse.get().getTeilnehmercount()).containsOnlyKeys(infBachelor, bwlBachelor);
    assertThat(
        analyse.get().getTeilnehmercount().values().stream().reduce(0, Integer::sum))
        .isEqualTo(affectedStudents);
  }


  @Test
  void test_Blocke2_PARALLEL_LotPruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(60);
    Duration pruefungCDuration = Duration.ofMinutes(60);
    Duration pruefungDDuration = Duration.ofMinutes(60);
    Duration pruefungEDuration = Duration.ofMinutes(60);

    Block blockA2 = new BlockImpl(pruefungsperiode, "name", PARALLEL);

    Pruefung aPruefung2 = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    aPruefung2.setDauer(pruefungADuration);
    Pruefung bPruefung2 = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    aPruefung2.setDauer(pruefungBDuration);
    Pruefung cPruefung2 = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    aPruefung2.setDauer(pruefungCDuration);
    Pruefung dPruefung2 = new PruefungImpl("4", "aud", "abcde", pruefungDDuration);
    Pruefung ePruefung2 = new PruefungImpl("5", "sp", "abcdefg", pruefungEDuration);

    int students = 8;
    aPruefung2.addTeilnehmerkreis(infBachelor, students);
    bPruefung2.addTeilnehmerkreis(infBachelor, students);
    cPruefung2.addTeilnehmerkreis(infBachelor, students);
    dPruefung2.addTeilnehmerkreis(infBachelor, students);
    ePruefung2.addTeilnehmerkreis(infBachelor, students);

    aPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    bPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    cPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    dPruefung2.addTeilnehmerkreis(bwlBachelor, students);
    ePruefung2.addTeilnehmerkreis(bwlBachelor, students);

    blockA2.addPruefung(aPruefung2);
    blockA2.addPruefung(bPruefung2);
    blockA2.addPruefung(dPruefung2);
    blockA2.addPruefung(ePruefung2);

    blockA2.setStartzeitpunkt(startBlock);
    cPruefung2.setStartzeitpunkt(startBlock.plusMinutes(60));

    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(blockA2));

    int affectedStudents = 16;

    Optional<HartesKriteriumAnalyse> analyse = deviceUnderTest.evaluateRestriction(cPruefung2);
    assertThat(analyse).isPresent();
    assertThat(analyse.get().getCausingPruefungen()).containsOnly(aPruefung2, bPruefung2,
        cPruefung2, dPruefung2, ePruefung2);
    assertThat(analyse.get().getTeilnehmercount()).containsOnlyKeys(infBachelor, bwlBachelor);
    assertEquals(affectedStudents,
        analyse.get().getTeilnehmercount().values().stream().reduce(0, Integer::sum));
  }

  @Test
  void getAllPotentialConflictingPruefungenWith_Successful()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime timeA = LocalDateTime.of(2022, 8, 11, 8, 0);
    LocalDateTime timeB = LocalDateTime.of(2022, 8, 12, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskel = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, haskel));

    analysis.setStartzeitpunkt(timeA);
    haskel.setStartzeitpunkt(timeB);

    analysis.addTeilnehmerkreis(infBachelor, 8);
    haskel.addTeilnehmerkreis(infBachelor, 8);
    dm.addTeilnehmerkreis(infBachelor, 8);

    assertThat(deviceUnderTest.getAllPotentialConflictingPruefungenWith(dm)).contains(analysis,
        haskel);
  }

  @Test
  void getAllPotentialConflictingPruefungenWith_Successful_NotSameTeilnehmerkreis()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime timeA = LocalDateTime.of(2022, 8, 11, 8, 0);
    LocalDateTime timeB = LocalDateTime.of(2022, 8, 12, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskel = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, haskel));

    analysis.setStartzeitpunkt(timeA);
    haskel.setStartzeitpunkt(timeB);

    analysis.addTeilnehmerkreis(infBachelor, 8);
    haskel.addTeilnehmerkreis(infBachelor, 8);
    dm.addTeilnehmerkreis(bwlBachelor, 8);

    assertThat(deviceUnderTest.getAllPotentialConflictingPruefungenWith(dm)).isEmpty();
  }

  @Test
  void getAllPotentialConflictingPruefungenWith_Successful_someTeilnehmerkreis()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime timeA = LocalDateTime.of(2022, 8, 11, 8, 0);
    LocalDateTime timeB = LocalDateTime.of(2022, 8, 12, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskel = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis, haskel));

    analysis.setStartzeitpunkt(timeA);
    haskel.setStartzeitpunkt(timeB);

    analysis.addTeilnehmerkreis(infBachelor, 8);
    haskel.addTeilnehmerkreis(bwlBachelor, 8);
    dm.addTeilnehmerkreis(bwlBachelor, 8);

    assertThat(deviceUnderTest.getAllPotentialConflictingPruefungenWith(dm)).containsOnly(haskel);
  }


  @Test
  void getAllPotentialConflictingPruefungenWith_Successful_sameTeilnehmerkreisButNotGeplant()
      throws NoPruefungsPeriodeDefinedException {

    LocalDateTime timeA = LocalDateTime.of(2022, 8, 11, 8, 0);

    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung haskel = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);

    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(analysis));
    when(pruefungsperiode.ungeplantePruefungen()).thenReturn(Set.of(haskel));

    analysis.setStartzeitpunkt(timeA);

    analysis.addTeilnehmerkreis(infBachelor, 8);
    haskel.addTeilnehmerkreis(bwlBachelor, 8);
    dm.addTeilnehmerkreis(bwlBachelor, 8);

    assertThat(deviceUnderTest.getAllPotentialConflictingPruefungenWith(dm)).isEmpty();

  }


  @Test
  void wouldBeHardConflictAt_timeMustNotBeNull() {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.wouldBeHardConflictAt(null, pruefung));
  }

  @Test
  void wouldBeHardConflictAt_planungseinheitMustNotBeNull() {
    LocalDateTime time = getRandomTime(1L);
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.wouldBeHardConflictAt(time, null));
  }

  @Test
  void wouldBeHardConflictAt_unPlannedPlanungseinheit_noConflict()
      throws NoPruefungsPeriodeDefinedException {
    assertThat(deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L),
        getRandomUnplannedPruefung(1L))).isFalse();
  }

  @Test
  void wouldBeHardConflictAt_noPlanungseinheitenAtTime() throws NoPruefungsPeriodeDefinedException {
    when(dataAccessService.getPlanungseinheitenAt(any())).thenReturn(emptySet());

    assertThat(deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L),
        getRandomUnplannedPruefung(1L))).isFalse();
  }

  @Test
  void wouldBeHardConflictAt_onePlanungseinheitWithConflictingTeilnehmerkreis()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    Teilnehmerkreis conflictingTeilnehmerkreis = getRandomTeilnehmerkreis(1L);
    Pruefung conflictingPruefung = getRandomPruefungWith(1L, conflictingTeilnehmerkreis);
    Pruefung pruefungToCheckFor = getRandomPruefungWith(2L, conflictingTeilnehmerkreis);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(conflictingPruefung));

    assertThat(
        deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L), pruefungToCheckFor)).isTrue();
  }

  @Test
  void wouldBeHardConflictAt_multiplePlanungseinheit_oneConflicting()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    Teilnehmerkreis conflictingTeilnehmerkreis = getRandomTeilnehmerkreis(1L);
    Pruefung conflictingPruefung = getRandomPruefungWith(1L, conflictingTeilnehmerkreis);
    List<Planungseinheit> planungseinheitenAtTime = List.of(conflictingPruefung,
        getRandomPlannedPruefung(2L), getRandomPlannedPruefung(3L), getRandomPlannedPruefung(4L));
    Pruefung pruefungToCheckFor = getRandomPruefungWith(2L, conflictingTeilnehmerkreis);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(planungseinheitenAtTime));

    assertThat(
        deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L), pruefungToCheckFor)).isTrue();
  }

  @Test
  void wouldBeHardConflictAt_onePlanungseinheit_oneOfManyTeilnehmerkreisConflicting()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    Teilnehmerkreis conflictingTeilnehmerkreis = getRandomTeilnehmerkreis(1L);
    Pruefung conflictingPruefung = getRandomPruefungWith(1L, conflictingTeilnehmerkreis,
        getRandomTeilnehmerkreis(2L), getRandomTeilnehmerkreis(3L));
    Pruefung pruefungToCheckFor = getRandomPruefungWith(2L, conflictingTeilnehmerkreis,
        getRandomTeilnehmerkreis(4L));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(conflictingPruefung));

    assertThat(
        deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L), pruefungToCheckFor)).isTrue();
  }

  @Test
  void wouldBeHardConflictAt_planungseinheitToCheckCanNotConflictWithTime()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    // if the planungseinheit to check is planned, it should not interfere
    Teilnehmerkreis conflictingTeilnehmerkreis = getRandomTeilnehmerkreis(1L);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(getRandomPruefungWith(2L, conflictingTeilnehmerkreis)));

    assertThat(deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L),
        getRandomPruefungWith(2L, conflictingTeilnehmerkreis))).isFalse();
  }

  @Test
  void wouldBeHardConflictAt_checkTimespanOfPruefungBeginningAtSpecifiedTime()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LocalDateTime timeToCheck = getRandomTime(1L);
    Teilnehmerkreis conflictingTeilnehmerkreis = getRandomTeilnehmerkreis(1L);
    Pruefung pruefungToCheck = getRandomPruefungWith(2L, conflictingTeilnehmerkreis);
    Pruefung other = getRandomPruefungWith(3L, conflictingTeilnehmerkreis);
    other.setStartzeitpunkt(pruefungToCheck.getStartzeitpunkt().minusHours(2));
    other.setDauer(Duration.ofHours(2));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(other));
    deviceUnderTest.wouldBeHardConflictAt(timeToCheck, pruefungToCheck);

    verify(dataAccessService).getAllPlanungseinheitenBetween(timeToCheck,
        timeToCheck.plus(pruefungToCheck.getDauer()));
  }

  @Test
  void testIfWeCheckSemster() throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {

    LocalDateTime timeToCheck = getRandomTime(1L);
    Teilnehmerkreis teilnehmerkreisOne = mock(Teilnehmerkreis.class);
    when(teilnehmerkreisOne.getAusbildungsgrad()).thenReturn(Ausbildungsgrad.BACHELOR);
    when(teilnehmerkreisOne.getFachsemester()).thenReturn(1);
    when(teilnehmerkreisOne.getStudiengang()).thenReturn("INF");
    when(teilnehmerkreisOne.getPruefungsordnung()).thenReturn("INF");

    Teilnehmerkreis teilnehmerkreisTwo = mock(Teilnehmerkreis.class);
    when(teilnehmerkreisTwo.getAusbildungsgrad()).thenReturn(Ausbildungsgrad.BACHELOR);
    when(teilnehmerkreisTwo.getFachsemester()).thenReturn(3);
    when(teilnehmerkreisTwo.getStudiengang()).thenReturn("INF");
    when(teilnehmerkreisTwo.getPruefungsordnung()).thenReturn("INF");


    Pruefung pruefungToCheck = getRandomPruefungWith(2L, teilnehmerkreisOne);
    Pruefung other = getRandomPruefungWith(3L, teilnehmerkreisTwo);

    pruefungToCheck.setStartzeitpunkt(timeToCheck);
    other.setStartzeitpunkt(timeToCheck);


    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(other));
    deviceUnderTest.wouldBeHardConflictAt(timeToCheck, pruefungToCheck);


    assertThat(deviceUnderTest.evaluateRestriction(pruefungToCheck)).isEmpty();
  }

}
