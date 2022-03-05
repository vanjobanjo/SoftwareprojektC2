package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
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

class WocheVierFuerMasterRestrictionTest {

  private Pruefungsperiode mocked_periode;
  private WocheVierFuerMasterRestriction deviceUnderTest;
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
  void analysisIsBachelorAndWeekFourTest() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelanalysis)).isTrue();
  }

  @Test
  void analysisBachelorIsOnWeek5Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 5);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelanalysis)).isFalse();
  }

  @Test
  void analysisBachelorIsOnWeek3Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 3);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelanalysis)).isFalse();
  }

  @Test
  void analysisBachelorIsOnWeek2Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 2);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelanalysis)).isFalse();
  }

  @Test
  void analysisIsOnWeek1Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelanalysis)).isFalse();
  }

  @Test
  void analysisIsOnWeek0Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE;
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelanalysis)).isFalse();
  }

  @Test
  void analysisIsOnWeek4Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelanalysis);
    assertThat(result).isPresent();
    assertThat(result.get().getAffectedPruefungen()).isEmpty();
    assertThat(result.get().getKriterium()).isEqualTo(WeichesKriterium.WOCHE_VIER_FUER_MASTER);
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(20);
    assertThat(result.get().getAffectedTeilnehmerKreise()).isEqualTo(
        modelanalysis.getTeilnehmerkreise());
  }

  @Test
  void analysisIsOnWeek5Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 5);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysis = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysis.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelanalysis);
    assertThat(result).isEmpty();
  }

  @Test
  void masterPruefungOnWeek4Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelanalysisMaster = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelanalysisMaster.addTeilnehmerkreis(TestFactory.infMaster, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelanalysisMaster);
    assertThat(result).isEmpty();
  }

  @Test
  void mixedTkPruefungOnWeek4Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelAnalysisMixedTk = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infMaster, 20);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelAnalysisMixedTk);
    assertThat(result).isPresent();
    assertThat(deviceUnderTest.isWeekFourContainsNotOnlyMaster(modelAnalysisMixedTk)).isTrue();

    //darf nicht Master TK enthalten.
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsOnly(TestFactory.infBachelor);
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(20);
  }

  @Test
  void mixedTkPruefungOnWeek5Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 5);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelAnalysisMixedTk = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infMaster, 20);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelAnalysisMixedTk);
    assertThat(result).isEmpty();
  }

  @Test
  void mixedTkWithPruefungOnWeek4Test() {
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelAnalysisMixedTk = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infMaster, 20);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infBachelor, 20);
    modelAnalysisMixedTk.addTeilnehmerkreis(TestFactory.infPtl, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelAnalysisMixedTk);
    assertThat(result).isPresent();
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(40);
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsOnly(TestFactory.infBachelor,
        TestFactory.infPtl);
    assertThat(result.get().getDeltaScoring()).isEqualTo(2 * WeichesKriterium.WOCHE_VIER_FUER_MASTER.getWert());
    assertThat(result.get().getAffectedTeilnehmerKreise()).doesNotContain(TestFactory.infMaster);
  }

  @Test
  void masterTkOnlyWeek4(){
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelAnalysisMasterTk = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelAnalysisMasterTk.addTeilnehmerkreis(TestFactory.infMaster, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelAnalysisMasterTk);
    assertThat(result).isEmpty();
  }

  @Test
  void ptlTkOnlyWeek4(){
    deviceUnderTest = new WocheVierFuerMasterRestriction(accessService);
    ReadOnlyPruefung analysis = TestFactory.RO_ANALYSIS_UNPLANNED;
    LocalDate week4 = START_PERIODE.plusDays(7 * 4);
    analysis = TestFactory.planRoPruefung(analysis, week4.atTime(LocalTime.MIDNIGHT));
    Pruefung modelAnalysisMasterTk = TestFactory.getPruefungOfReadOnlyPruefung(analysis);
    modelAnalysisMasterTk.addTeilnehmerkreis(TestFactory.infPtl, 20);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(modelAnalysisMasterTk);
    assertThat(result).isPresent();
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsOnly(TestFactory.infPtl);
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(20);
    assertThat(result.get().getDeltaScoring()).isEqualTo(WeichesKriterium.WOCHE_VIER_FUER_MASTER.getWert());
  }
}
