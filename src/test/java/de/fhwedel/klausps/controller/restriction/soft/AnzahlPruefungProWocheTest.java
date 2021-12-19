package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TestFactory;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnzahlPruefungProWocheTest {

  private Pruefungsperiode mocked_periode;
  private AnzahlPruefungProWoche deviceUnderTest;
  private DataAccessService accessService;
  private final LocalDate START_PERIODE = LocalDate.of(2021, 1, 1);
  private final LocalDate END_PERIODE = LocalDate.of(2021, 12, 31);
  private final int LIMIT_PER_WEEK = 2;

  @BeforeEach
  void setUp() {
    this.mocked_periode = mock(Pruefungsperiode.class);
    accessService = ServiceProvider.getDataAccessService();
    accessService.setPruefungsperiode(this.mocked_periode);
    TestFactory.configureMock_setStartEndOfPeriode(mocked_periode, START_PERIODE, END_PERIODE);
    // no constructor, after configure!
  }

  @Test
  void weekMapOfPruefungTest() {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalDate week_1 = START_PERIODE.plusDays(7);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_1 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_1.atTime(start)));
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_1);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_1));

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);
    Map<Integer, Set<Pruefung>> result = deviceUnderTest.weekMapOfPruefung(
        Set.of(mathe_0, dm_0, haskell_1), START_PERIODE);

    assertThat(result.get(0)).containsOnly(mathe_0, dm_0);
    assertThat(result.get(1)).containsOnly(haskell_1);
    assertThat(result.get(2)).isNull();
  }

  @Test
  void limitIsReached_test() {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalDate week_1 = START_PERIODE.plusDays(7);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0));

    Pruefung haskell_0_isNotPlanned_ShouldBePlanned_At = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode,
        haskell_0_isNotPlanned_ShouldBePlanned_At);

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    assertThat(deviceUnderTest.test(haskell_0_isNotPlanned_ShouldBePlanned_At)).isTrue();
  }

  @Test
  void limit_test() {

    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalDate week_1 = START_PERIODE.plusDays(7);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_1 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_1.atTime(start)));

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_1);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_1));

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    assertThat(deviceUnderTest.test(haskell_1)).isFalse();
    assertThat(deviceUnderTest.test(mathe_0)).isTrue();
    assertThat(deviceUnderTest.test(dm_0)).isTrue();
  }


  @DisplayName("DM und Mathe sind in der selben Woche und das Kriterium wird verletzt.")
  @Test
  void evaluate_test() {

    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalDate week_1 = START_PERIODE.plusDays(7);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_1 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_1.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    haskell_1.addTeilnehmerkreis(TestFactory.bwl, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_1);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_1));

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    KriteriumsAnalyse result = deviceUnderTest.evaluate(dm_0).get();

    ReadOnlyPruefung analysis = new PruefungDTOBuilder(mathe_0).build();
    ReadOnlyPruefung dm = new PruefungDTOBuilder(dm_0).build();
    ReadOnlyPruefung haskell = new PruefungDTOBuilder(haskell_1).build();

    assertThat(result).isNotNull();
    assertThat(result.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result.getAnzahlBetroffenerStudenten()).isNotEqualTo(20); //not 20
    assertThat(result.getAnzahlBetroffenerStudenten()).isEqualTo(10);
    assertThat(result.getBetroffenePruefungen()).containsOnly(analysis, dm);
    assertThat(result.getBetroffenePruefungen()).containsOnly(analysis, dm);
    assertThat(deviceUnderTest.evaluate(haskell_1)).isEmpty();
  }

  @DisplayName("DM und Mathe sind im Block. Haskell wird am selben Tag geplant.")
  @Test
  void evaluate_Block_test() {

    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.bwl, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    Block block = TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    KriteriumsAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    ReadOnlyPruefung analysis = new PruefungDTOBuilder(mathe_0).build();
    ReadOnlyPruefung dm = new PruefungDTOBuilder(dm_0).build();
    ReadOnlyPruefung haskell = new PruefungDTOBuilder(haskell_0).build();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isNotEqualTo(20); //not 20
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isEqualTo(10);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getBetroffenePruefungen()).containsOnly(dm, haskell);
    assertThat(result_dm0.getBetroffenePruefungen()).doesNotContain(analysis);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    KriteriumsAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    assertThat(result_haskell0.getAnzahlBetroffenerStudenten()).isEqualTo(10);
    assertThat(result_haskell0.getBetroffenePruefungen()).containsOnly(dm, analysis, haskell);
  }

  @DisplayName("DM und Mathe sind im Block. Haskell wird am selben Tag geplant.")
  @Test
  void evaluate_Block_test2() {

    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.inf, 20);
    haskell_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.inf, 20);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    Block block = TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    KriteriumsAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    ReadOnlyPruefung analysis = new PruefungDTOBuilder(mathe_0).build();
    ReadOnlyPruefung dm = new PruefungDTOBuilder(dm_0).build();
    ReadOnlyPruefung haskell = new PruefungDTOBuilder(haskell_0).build();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isNotEqualTo(20);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isEqualTo(30);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getBetroffenePruefungen()).containsOnly(dm, haskell);
    assertThat(result_dm0.getBetroffenePruefungen()).doesNotContain(analysis);

    KriteriumsAnalyse result_analysis = deviceUnderTest.evaluate(mathe_0).get();
    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    KriteriumsAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    assertThat(result_haskell0.getAnzahlBetroffenerStudenten()).isEqualTo(30);
    assertThat(result_haskell0.getBetroffenePruefungen()).containsOnly(dm, analysis, haskell);
  }


  @DisplayName("Viele unterschiedliche Teilnehmerkreise")
  @Test
  void evaluate_Block_test3() {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    mathe_0.addTeilnehmerkreis(TestFactory.wing, 10);

    dm_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.inf, 20);

    haskell_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.inf, 20);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    //Mathe und DM sind innerhalb eines Blockes
    Block block = TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    ReadOnlyPruefung analysis = new PruefungDTOBuilder(mathe_0).build();
    ReadOnlyPruefung dm = new PruefungDTOBuilder(dm_0).build();
    ReadOnlyPruefung haskell = new PruefungDTOBuilder(haskell_0).build();

    // Die Teilnehmerkreis von Analysis müssen ignoriert werden, da sie in einem Block mit Analysis sind.
    KriteriumsAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    // wenn es nicht in einem Block wäre, dann wäre die Schätzung 40.
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isNotEqualTo(40);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isEqualTo(30);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getBetroffenePruefungen()).containsOnly(dm, haskell);
    assertThat(result_dm0.getBetroffenePruefungen()).doesNotContain(analysis);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    KriteriumsAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    //die Summe aller TK die betroffen sind 40.
    assertThat(result_haskell0.getAnzahlBetroffenerStudenten()).isEqualTo(40);
    assertThat(result_haskell0.getBetroffenePruefungen()).containsOnly(dm, analysis, haskell);
    assertThat(result_haskell0.getTeilnehmer()).containsOnly(TestFactory.bwl, TestFactory.wing,
        TestFactory.inf);
  }

  @DisplayName("Keine Blöcke viele unterschiedliche Teilnehmerkreise")
  @Test
  void evaluate_Block_test4() {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    mathe_0.addTeilnehmerkreis(TestFactory.wing, 10);

    dm_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.inf, 20);
    dm_0.addTeilnehmerkreis(TestFactory.wing, 10);

    haskell_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.inf, 20);
    haskell_0.addTeilnehmerkreis(TestFactory.wing, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    ReadOnlyPruefung analysis = new PruefungDTOBuilder(mathe_0).build();
    ReadOnlyPruefung dm = new PruefungDTOBuilder(dm_0).build();
    ReadOnlyPruefung haskell = new PruefungDTOBuilder(haskell_0).build();

    // Die Teilnehmerkreis von Analysis müssen ignoriert werden, da sie in einem Block mit Analysis sind.
    KriteriumsAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isEqualTo(40);

    assertThat(result_dm0.getBetroffenePruefungen()).containsOnly(dm, haskell, analysis);

    KriteriumsAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();

    assertThat(result_haskell0.getAnzahlBetroffenerStudenten()).isEqualTo(40);
    assertThat(result_haskell0.getBetroffenePruefungen()).containsOnly(dm, analysis, haskell);

    KriteriumsAnalyse result_analysis = deviceUnderTest.evaluate(mathe_0).get();
    assertThat(result_analysis.getAnzahlBetroffenerStudenten()).isEqualTo(40);
    assertThat(result_analysis.getBetroffenePruefungen()).containsOnly(dm, analysis, haskell);
  }

  @DisplayName("Keine Konflikte")
  @Test
  void evaluate_Block_test5() {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalDate week_1 = START_PERIODE.plusDays(8);
    LocalDate week_2 = START_PERIODE.plusDays(15);

    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_1 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_1.atTime(start)));
    Pruefung haskell_2 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_2.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    mathe_0.addTeilnehmerkreis(TestFactory.wing, 10);

    dm_1.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_1.addTeilnehmerkreis(TestFactory.inf, 20);
    dm_1.addTeilnehmerkreis(TestFactory.wing, 10);

    haskell_2.addTeilnehmerkreis(TestFactory.bwl, 10);
    haskell_2.addTeilnehmerkreis(TestFactory.inf, 20);
    haskell_2.addTeilnehmerkreis(TestFactory.wing, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_1, haskell_2);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_1, haskell_2));

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    assertThat(deviceUnderTest.evaluate(mathe_0)).isEmpty();
    assertThat(deviceUnderTest.evaluate(dm_1)).isEmpty();
    assertThat(deviceUnderTest.evaluate(haskell_2)).isEmpty();
  }


  @DisplayName("DM und Mathe sind im Block. Haskell wird am selben Tag geplant.")
  @Test
  void evaluate_Block_test10() {

    LocalDate week_0 = START_PERIODE.plusDays(6);
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    Pruefung haskell_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));

    mathe_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwl, 10);
    dm_0.addTeilnehmerkreis(TestFactory.inf, 20);

    haskell_0.addTeilnehmerkreis(TestFactory.bwl, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    Block block = TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    KriteriumsAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    ReadOnlyPruefung analysis = new PruefungDTOBuilder(mathe_0).build();
    ReadOnlyPruefung dm = new PruefungDTOBuilder(dm_0).build();
    ReadOnlyPruefung haskell = new PruefungDTOBuilder(haskell_0).build();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isNotEqualTo(20);
    assertThat(result_dm0.getAnzahlBetroffenerStudenten()).isEqualTo(30);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getBetroffenePruefungen()).containsOnly(dm, haskell);
    assertThat(result_dm0.getBetroffenePruefungen()).doesNotContain(analysis);

    KriteriumsAnalyse result_analysis = deviceUnderTest.evaluate(mathe_0).get();
    assertThat(result_analysis.getAnzahlBetroffenerStudenten()).isEqualTo(10);

    //Analysis darf nicht Inf enthalten, da er der TK von BWL ist. Es muss ignoriert werden.
    assertThat(result_analysis.getTeilnehmer()).doesNotContain(TestFactory.inf);
    assertThat(result_analysis.getBetroffenePruefungen()).containsOnly(analysis, haskell);
    assertThat(result_analysis.getTeilnehmer()).containsOnly(TestFactory.bwl);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    KriteriumsAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    assertThat(result_haskell0.getAnzahlBetroffenerStudenten()).isEqualTo(30);
    assertThat(result_haskell0.getBetroffenePruefungen()).containsOnly(dm, analysis, haskell);
    assertThat(result_haskell0.getTeilnehmer()).containsOnly(TestFactory.inf, TestFactory.bwl);
  }


}