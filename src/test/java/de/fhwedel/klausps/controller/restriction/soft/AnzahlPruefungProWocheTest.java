package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.TestFactory;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
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

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, haskell_0_isNotPlanned_ShouldBePlanned_At);

    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

    assertThat(deviceUnderTest.test(haskell_0_isNotPlanned_ShouldBePlanned_At)).isTrue();
  }

  @Test
  void limitIsNotReached_test() {

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

      Pruefung haskell_1_isNotPlanned_ShouldBePlanned_At = TestFactory.getPruefungOfReadOnlyPruefung(
          TestFactory.planRoPruefung(TestFactory.RO_HASKELL_UNPLANNED, week_1.atTime(start)));

    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, haskell_1_isNotPlanned_ShouldBePlanned_At);


    this.deviceUnderTest = new AnzahlPruefungProWoche(accessService, LIMIT_PER_WEEK);

      assertThat(deviceUnderTest.test(haskell_1_isNotPlanned_ShouldBePlanned_At)).isFalse();
  }
}