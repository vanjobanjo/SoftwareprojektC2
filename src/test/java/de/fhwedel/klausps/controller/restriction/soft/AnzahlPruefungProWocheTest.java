package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TestFactory;
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

    assertThat(deviceUnderTest.isAboveTheWeekLimit(haskell_0_isNotPlanned_ShouldBePlanned_At,
        Set.of(haskell_0_isNotPlanned_ShouldBePlanned_At, dm_0, mathe_0))).isTrue();
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

    assertThat(deviceUnderTest.isAboveTheWeekLimit(haskell_1, Set.of(haskell_1))).isFalse();
    assertThat(deviceUnderTest.isAboveTheWeekLimit(mathe_0, Set.of(mathe_0, dm_0))).isTrue();
    assertThat(deviceUnderTest.isAboveTheWeekLimit(dm_0, Set.of(mathe_0, dm_0))).isTrue();
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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    haskell_1.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_1);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_1));

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    WeichesKriteriumAnalyse result = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result).isNotNull();
    assertThat(result.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result.getAmountAffectedStudents()).isEqualTo(10);
    assertThat(result.getCausingPruefungen()).containsOnly(mathe_0, dm_0);
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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    WeichesKriteriumAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAmountAffectedStudents()).isEqualTo(10);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getCausingPruefungen()).containsOnly(dm_0, haskell_0);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in derselben woche stattfinden.
    WeichesKriteriumAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    assertThat(result_haskell0.getAmountAffectedStudents()).isEqualTo(10);
    assertThat(result_haskell0.getCausingPruefungen()).containsOnly(dm_0, mathe_0, haskell_0);
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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    haskell_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    WeichesKriteriumAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAmountAffectedStudents()).isEqualTo(30);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getCausingPruefungen()).containsOnly(dm_0, haskell_0);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    WeichesKriteriumAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    assertThat(result_haskell0.getAmountAffectedStudents()).isEqualTo(30);
    assertThat(result_haskell0.getCausingPruefungen()).containsOnly(dm_0, mathe_0, haskell_0);
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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    mathe_0.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

    dm_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);

    haskell_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    //Mathe und DM sind innerhalb eines Blockes
    TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    // Die Teilnehmerkreis von Analysis müssen ignoriert werden, da sie in einem Block mit Analysis sind.
    WeichesKriteriumAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    // wenn es nicht in einem Block wäre, dann wäre die Schätzung 40.
    assertThat(result_dm0.getAmountAffectedStudents()).isEqualTo(30);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getCausingPruefungen()).containsOnly(dm_0, haskell_0);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    WeichesKriteriumAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    //die Summe aller TK die betroffen sind 40.
    assertThat(result_haskell0.getAmountAffectedStudents()).isEqualTo(40);
    assertThat(result_haskell0.getCausingPruefungen()).containsOnly(dm_0, mathe_0, haskell_0);
    assertThat(result_haskell0.getAffectedTeilnehmerKreise()).containsOnly(TestFactory.bwlBachelor,
        TestFactory.wingBachelor,
        TestFactory.infBachelor);
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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    mathe_0.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

    dm_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    dm_0.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

    haskell_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    haskell_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    haskell_0.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    // Die Teilnehmerkreis von Analysis müssen ignoriert werden, da sie in einem Block mit Analysis sind.
    WeichesKriteriumAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAmountAffectedStudents()).isEqualTo(40);

    assertThat(result_dm0.getCausingPruefungen()).containsOnly(dm_0, haskell_0, mathe_0);

    WeichesKriteriumAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();

    assertThat(result_haskell0.getAmountAffectedStudents()).isEqualTo(40);
    assertThat(result_haskell0.getCausingPruefungen()).containsOnly(dm_0, mathe_0, haskell_0);

    WeichesKriteriumAnalyse result_analysis = deviceUnderTest.evaluate(mathe_0).get();
    assertThat(result_analysis.getAmountAffectedStudents()).isEqualTo(40);
    assertThat(result_analysis.getCausingPruefungen()).containsOnly(dm_0, mathe_0, haskell_0);
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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    mathe_0.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

    dm_1.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_1.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    dm_1.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

    haskell_2.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    haskell_2.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    haskell_2.addTeilnehmerkreis(TestFactory.wingBachelor, 10);

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

    mathe_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);
    dm_0.addTeilnehmerkreis(TestFactory.infBachelor, 20);

    haskell_0.addTeilnehmerkreis(TestFactory.bwlBachelor, 10);

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    TestFactory.configureMock_addPruefungToBlockModel(mocked_periode, "Block",
        dm_0.getStartzeitpunkt(), dm_0, mathe_0);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    WeichesKriteriumAnalyse result_dm0 = deviceUnderTest.evaluate(dm_0).get();

    assertThat(result_dm0).isNotNull();
    assertThat(result_dm0.getKriterium()).isEqualTo(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    assertThat(result_dm0.getAmountAffectedStudents()).isEqualTo(30);
    // weil Analysis und DM im Block sind, darf Analysis nicht mehr davon betroffen sein.
    assertThat(result_dm0.getCausingPruefungen()).containsOnly(dm_0, haskell_0);

    WeichesKriteriumAnalyse result_analysis = deviceUnderTest.evaluate(mathe_0).get();
    assertThat(result_analysis.getAmountAffectedStudents()).isEqualTo(10);

    //Analysis darf nicht Inf enthalten, da er der TK von BWL ist. Es muss ignoriert werden.
    assertThat(result_analysis.getCausingPruefungen()).containsOnly(mathe_0, haskell_0);
    assertThat(result_analysis.getAffectedTeilnehmerKreise()).containsOnly(TestFactory.bwlBachelor);

    //für haskell müssen alle 3 unter betroffen sein, weil alle in der selben woche stattfinden.
    WeichesKriteriumAnalyse result_haskell0 = deviceUnderTest.evaluate(haskell_0).get();
    assertThat(result_haskell0.getAmountAffectedStudents()).isEqualTo(30);
    assertThat(result_haskell0.getCausingPruefungen()).containsOnly(dm_0, mathe_0, haskell_0);
    assertThat(result_haskell0.getAffectedTeilnehmerKreise()).containsOnly(TestFactory.infBachelor,
        TestFactory.bwlBachelor);
  }
}
