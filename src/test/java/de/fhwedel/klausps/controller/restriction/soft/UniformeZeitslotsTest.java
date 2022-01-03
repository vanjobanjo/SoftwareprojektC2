package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_ANALYSIS_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_DM_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_HASKELL_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.getPruefungOfReadOnlyPruefung;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class UniformeZeitslotsTest {

  public UniformeZeitslots deviceUnderTest;
  public DataAccessService dataAccessService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new UniformeZeitslots(dataAccessService);
  }


  @Test
  void pruefung_not_planned() {
    LocalDateTime termin = LocalDateTime.of(2022, 1, 3, 10, 30);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.setStartzeitpunkt(termin);

    assertThat(deviceUnderTest.evaluate(haskell)).isEmpty();
  }

  @Test
  void no_overlap_pruefungen_before() {
    LocalDateTime date = LocalDateTime.of(2022, 1, 3, 15, 0);
    LocalDateTime before = LocalDateTime.of(2022, 1, 3, 8, 0);

    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.setStartzeitpunkt(date);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.setStartzeitpunkt(before);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(dm, haskell));

    assertThat(deviceUnderTest.evaluate(dm)).isEmpty();
  }

  @Test
  void no_overlap_pruefungen_after() {
    LocalDateTime after = LocalDateTime.of(2022, 1, 3, 15, 0);
    LocalDateTime before = LocalDateTime.of(2022, 1, 3, 8, 0);

    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.setStartzeitpunkt(before);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.setStartzeitpunkt(after);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(dm, haskell));

    assertThat(deviceUnderTest.evaluate(dm)).isEmpty();
  }

  @Test
  void no_overlap_pruefungen_directly_bordering_before() {
    LocalDateTime after = LocalDateTime.of(2022, 1, 3, 10, 1);
    LocalDateTime before = LocalDateTime.of(2022, 1, 3, 8, 0);

    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.setStartzeitpunkt(after);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.setStartzeitpunkt(before);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(dm, haskell));

    assertThat(deviceUnderTest.evaluate(dm)).isEmpty();
  }

  @Test
  void no_overlap_pruefungen_directly_bordering_after() {
    LocalDateTime after = LocalDateTime.of(2022, 1, 3, 10, 1);
    LocalDateTime before = LocalDateTime.of(2022, 1, 3, 8, 0);

    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.setStartzeitpunkt(before);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.setStartzeitpunkt(after);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(dm, haskell));

    assertThat(deviceUnderTest.evaluate(dm)).isEmpty();
  }

  @Test
  void no_overlap_pruefungen_exactly_same_time_same_duration() {
    LocalDateTime time = LocalDateTime.of(2022, 1, 3, 8, 0);

    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.setStartzeitpunkt(time);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.setStartzeitpunkt(time);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(dm, haskell));

    assertThat(deviceUnderTest.evaluate(dm)).isEmpty();
  }

  @Test
  void no_overlap_pruefungen_same_start_shorter_duration() {
    LocalDateTime time = LocalDateTime.of(2022, 1, 3, 8, 0);

    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.setStartzeitpunkt(time);
    dm.addTeilnehmerkreis(infBachelor);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.setDauer(Duration.ofMinutes(60));
    haskell.setStartzeitpunkt(time);
    haskell.addTeilnehmerkreis(infBachelor);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(dm, haskell));

    testKriterium(haskell, dm, infBachelor);
  }


  private void testKriterium(Pruefung toEvaluate, Pruefung causingPruefungen,
      Teilnehmerkreis causingTeilnehmerkreise) {
    testKriterium(toEvaluate, Set.of(causingPruefungen), Set.of(causingTeilnehmerkreise));
  }


  private void testKriterium(Pruefung toEvaluate, Set<Pruefung> causingPruefungen,
      Set<Teilnehmerkreis> causingTeilnehmerkreise) {

    Optional<WeichesKriteriumAnalyse> result = deviceUnderTest.evaluate(toEvaluate);
    assertThat(result).isPresent();
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsExactlyInAnyOrderElementsOf(
        causingTeilnehmerkreise);
    assertThat(result.get().getCausingPruefungen()).containsExactlyInAnyOrderElementsOf(
        causingPruefungen);
    // todo test amount of affected students right
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(toEvaluate.schaetzung());
    assertThat(result.get().getDeltaScoring()).isEqualTo(
        calculateExpectedScoring(causingPruefungen.size()));
  }


  private int calculateExpectedScoring(int numberOfAffectedPruefungen) {
    return numberOfAffectedPruefungen * UNIFORME_ZEITSLOTS.getWert();
  }
}