package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TestFactory;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class KeineKlausurAmSonntagTest {

  private Pruefungsperiode mocked_periode;
  private KeineKlausurAmSonntag deviceUnderTest;
  private final LocalDate START_PERIODE = LocalDate.of(2021, 1, 1);
  private final LocalDate END_PERIODE = LocalDate.of(2021, 12, 31);

  @BeforeEach
  void setUp() {
    this.mocked_periode = mock(Pruefungsperiode.class);
    DataAccessService accessService = ServiceProvider.getDataAccessService();
    accessService.setPruefungsperiode(this.mocked_periode);
    this.deviceUnderTest = new KeineKlausurAmSonntag(accessService);
    TestFactory.configureMock_setStartEndOfPeriode(mocked_periode, START_PERIODE, END_PERIODE);
  }

  @Test
  void keineKlausurenAmSonntag_isTrue() {
    LocalDateTime sonntag = LocalDateTime.of(2021, 12, 19, 0, 0);
    ReadOnlyPruefung roPruefung_sonntag = TestFactory.planRoPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED,
        sonntag);
    Pruefung model_pruefung_so = TestFactory.getPruefungOfReadOnlyPruefung(roPruefung_sonntag);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, model_pruefung_so);

    assertThat(deviceUnderTest.test(model_pruefung_so)).isTrue();
  }

  @Test
  void keineKlausurenAmSonntag_isFalse() {
    LocalDateTime samstag = LocalDateTime.of(2021, 12, 18, 0, 0);
    ReadOnlyPruefung roPruefung_samstag = TestFactory.planRoPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED,
        samstag);
    Pruefung model_pruefung_sa = TestFactory.getPruefungOfReadOnlyPruefung(roPruefung_samstag);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, model_pruefung_sa);

    assertThat(deviceUnderTest.test(model_pruefung_sa)).isFalse();
  }

  @Test
  void keineKlausurenAmTag_outOfPeriode() {
    LocalDateTime samstag = LocalDateTime.of(2024, 12, 18, 0, 0);
    ReadOnlyPruefung roPruefung_samstag = TestFactory.planRoPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED,
        samstag);
    Pruefung model_pruefung_out = TestFactory.getPruefungOfReadOnlyPruefung(
        roPruefung_samstag);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, model_pruefung_out);

    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.test(model_pruefung_out));
  }

  @Test
  void keineKlausurAmSonntagEvaluate_test() {
    LocalDateTime sonntag = LocalDateTime.of(2021, 12, 19, 0, 0);
    ReadOnlyPruefung roPruefung_sonntag = TestFactory.planRoPruefung(
        TestFactory.RO_ANALYSIS_UNPLANNED,
        sonntag);
    Pruefung model_pruefung_so = TestFactory.getPruefungOfReadOnlyPruefung(roPruefung_sonntag);
    model_pruefung_so.addTeilnehmerkreis(TestFactory.wing, 10);
    model_pruefung_so.addTeilnehmerkreis(TestFactory.bwl, 30);
    model_pruefung_so.addTeilnehmerkreis(TestFactory.inf, 50);
    TestFactory.configureMock_getPruefungFromPeriode(mocked_periode, model_pruefung_so);
    KriteriumsAnalyse result = deviceUnderTest.evaluate(model_pruefung_so)
        .orElseThrow(IllegalArgumentException::new
        );
    assertThat(result.getBetroffenePruefungen()).containsOnly(roPruefung_sonntag);
    assertThat(result.getAnzahlBetroffenerStudenten()).isEqualTo(90);
    assertThat(result.getTeilnehmer()).containsOnly(TestFactory.wing, TestFactory.inf, TestFactory.bwl);
  }
}