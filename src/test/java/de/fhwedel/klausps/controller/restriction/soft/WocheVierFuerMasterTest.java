package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TestFactory;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WocheVierFuerMasterTest {

  private Pruefungsperiode mocked_periode;
  private WocheVierFuerMaster deviceUnderTest;
  private DataAccessService accessService;
  private final LocalDate START_PERIODE = LocalDate.of(2021, 1, 1);
  private final LocalDate END_PERIODE = LocalDate.of(2021, 12, 31);


  @BeforeEach
  void setUp() {
    this.mocked_periode = mock(Pruefungsperiode.class);
    accessService = ServiceProvider.getDataAccessService();
    accessService.setPruefungsperiode(this.mocked_periode);
    TestFactory.configureMock_setStartEndOfPeriode(mocked_periode, START_PERIODE, END_PERIODE);
    // no constructor, after configure!
  }

  @Test
  void predicateAnalysisIsOnWeek4Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    assertThat(deviceUnderTest.isWeekFour(modelanalysis)).isTrue();
  }


  @Test
  void predicateAnalysisIsOnWeek5Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 5);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    assertThat(deviceUnderTest.isWeekFour(modelanalysis)).isFalse();
  }

  @Test
  void predicateAnalysisIsOnWeek3Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 3);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    assertThat(deviceUnderTest.isWeekFour(modelanalysis)).isFalse();
  }

  @Test
  void predicateAnalysisIsOnWeek2Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 2);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    assertThat(deviceUnderTest.isWeekFour(modelanalysis)).isFalse();
  }

  @Test
  void predicateAnalysisIsOnWeek1Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    assertThat(deviceUnderTest.isWeekFour(modelanalysis)).isFalse();
  }

  @Test
  void predicateAnalysisIsOnWeek0Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE;
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    assertThat(deviceUnderTest.isWeekFour(modelanalysis)).isFalse();
  }

  @Test
  void evaluateAnalysisIsOnWeek4Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.inf, 20);
    Optional<WeichesKriteriumAnalyse> result = deviceUnderTest.evaluate(modelanalysis);
    assertThat(result).isPresent();
    assertThat(result.get().getCausingPruefungen()).containsOnly(modelanalysis);
    assertThat(result.get().getKriterium()).isEqualTo(WeichesKriterium.WOCHE_VIER_FUER_MASTER);
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(20);
    assertThat(result.get().getAffectedTeilnehmerKreise()).isEqualTo(
        modelanalysis.getTeilnehmerkreise());
  }

  @Test
  void evaluateAnalysisIsOnWeek5Test() {
    deviceUnderTest = new WocheVierFuerMaster(accessService, START_PERIODE);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 5);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.inf, 20);
    Optional<WeichesKriteriumAnalyse> result = deviceUnderTest.evaluate(modelanalysis);
    assertThat(result).isEmpty();
  }
}