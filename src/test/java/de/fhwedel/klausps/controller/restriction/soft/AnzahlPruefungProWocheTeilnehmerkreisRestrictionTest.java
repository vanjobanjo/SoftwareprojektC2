package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TestFactory;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnzahlPruefungProWocheTeilnehmerkreisRestrictionTest {

  private Pruefungsperiode mocked_periode;
  private AnzahlPruefungProWocheTeilnehmerkreisRestriction deviceUnderTest;
  private DataAccessService accessService;
  private final LocalDate START_PERIODE = LocalDate.of(2021, 1, 1);
  private final LocalDate END_PERIODE = LocalDate.of(2021, 12, 31);
  private final int LIMIT_PER_WEEK = 1;

  @BeforeEach
  void setUp() {
    this.mocked_periode = mock(Pruefungsperiode.class);
    accessService = ServiceProvider.getDataAccessService();
    accessService.setPruefungsperiode(this.mocked_periode);
    TestFactory.configureMock_setStartEndOfPeriode(mocked_periode, START_PERIODE, END_PERIODE);
    // no constructor, after configure!
  }

  @Test
  void violationTest() throws NoPruefungsPeriodeDefinedException {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    mathe_0.addTeilnehmerkreis(bwl, 10);
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    dm_0.addTeilnehmerkreis(bwl, 10);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0);

    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0));

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWocheTeilnehmerkreisRestriction(accessService, LIMIT_PER_WEEK);

    assertThat(deviceUnderTest.evaluateRestriction(mathe_0)).isPresent();
  }

  @Test
  void evaluateTest() throws NoPruefungsPeriodeDefinedException {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    mathe_0.addTeilnehmerkreis(bwl, 10);
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    dm_0.addTeilnehmerkreis(bwl, 10);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0);

    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0));

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWocheTeilnehmerkreisRestriction(accessService, LIMIT_PER_WEEK);
    SoftRestrictionAnalysis ev = deviceUnderTest.evaluateRestriction(mathe_0).get();
    assertThat(ev.getCausingPruefungen()).containsOnly(mathe_0, dm_0);
  }

  @Test
  void evaluateDifferentTkTest() throws NoPruefungsPeriodeDefinedException {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Teilnehmerkreis inf = TestFactory.infBachelor;
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    mathe_0.addTeilnehmerkreis(bwl, 10);
    mathe_0.addTeilnehmerkreis(inf, 10);
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    dm_0.addTeilnehmerkreis(bwl, 10);
    dm_0.addTeilnehmerkreis(inf, 10);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0);

    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0));

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWocheTeilnehmerkreisRestriction(accessService, LIMIT_PER_WEEK);
    SoftRestrictionAnalysis ev = deviceUnderTest.evaluateRestriction(mathe_0).get();
    assertThat(ev.getCausingPruefungen()).containsOnly(mathe_0, dm_0);
    assertThat(ev.getAffectedTeilnehmerKreise()).containsOnly(bwl, inf);
    assertThat(ev.getDeltaScoring().intValue()).isEqualTo(
        WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE.getWert() * 2);
  }

  @Test
  void evaluateDifferentTkInBlockTest() throws NoPruefungsPeriodeDefinedException {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Teilnehmerkreis inf = TestFactory.infBachelor;
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    mathe_0.addTeilnehmerkreis(bwl, 10);
    mathe_0.addTeilnehmerkreis(inf, 10);
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    dm_0.addTeilnehmerkreis(bwl, 10);
    dm_0.addTeilnehmerkreis(inf, 10);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0);
    Block block = new BlockImpl(mocked_periode, "Hallo", Blocktyp.SEQUENTIAL);
    block.addPruefung(mathe_0);
    block.addPruefung(dm_0);

    block.setStartzeitpunkt(week_0.atTime(start));

    when(mocked_periode.block(mathe_0)).thenReturn(block);
    when(mocked_periode.block(dm_0)).thenReturn(block);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0));

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWocheTeilnehmerkreisRestriction(accessService, 2);
    assertThat(deviceUnderTest.evaluateRestriction(mathe_0)).isEmpty();
    assertThat(deviceUnderTest.evaluateRestriction(dm_0)).isEmpty();
  }

  @Test
  void evaluateDifferentTkInBlock2Test() throws NoPruefungsPeriodeDefinedException {
    LocalDate week_0 = START_PERIODE.plusDays(6);
    Teilnehmerkreis bwl = TestFactory.bwlBachelor;
    Teilnehmerkreis inf = TestFactory.infBachelor;
    LocalTime start = LocalTime.of(0, 0);
    Pruefung mathe_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_ANALYSIS_UNPLANNED, week_0.atTime(start)));
    mathe_0.addTeilnehmerkreis(bwl, 10);
    mathe_0.addTeilnehmerkreis(inf, 10);
    Pruefung dm_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_DM_UNPLANNED, week_0.atTime(start)));
    dm_0.addTeilnehmerkreis(bwl, 10);
    dm_0.addTeilnehmerkreis(inf, 10);
    Pruefung haskell_0 = TestFactory.getPruefungOfReadOnlyPruefung(
        TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_0.atTime(start)));
    haskell_0.addTeilnehmerkreis(bwl, 10);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, mathe_0, dm_0, haskell_0);
    Block block = new BlockImpl(mocked_periode, "Hallo", Blocktyp.SEQUENTIAL);
    block.addPruefung(mathe_0);
    block.addPruefung(dm_0);

    block.setStartzeitpunkt(week_0.atTime(start));

    when(mocked_periode.block(mathe_0)).thenReturn(block);
    when(mocked_periode.block(dm_0)).thenReturn(block);
    TestFactory.configureMock_geplantePruefungenFromPeriode(mocked_periode,
        Set.of(mathe_0, dm_0, haskell_0));

    //mock must be configured before constructor call
    this.deviceUnderTest = new AnzahlPruefungProWocheTeilnehmerkreisRestriction(accessService, LIMIT_PER_WEEK);
    assertThat(deviceUnderTest.evaluateRestriction(mathe_0)).isPresent();
    assertThat(deviceUnderTest.evaluateRestriction(mathe_0).get().getCausingPruefungen()).containsOnly(mathe_0,
        haskell_0);
    assertThat(deviceUnderTest.evaluateRestriction(haskell_0).get().getCausingPruefungen()).containsOnly(
        mathe_0, dm_0, haskell_0);

    assertThat(deviceUnderTest.evaluateRestriction(dm_0).get().getCausingPruefungen()).containsOnly(
        haskell_0, dm_0);
  }


}
