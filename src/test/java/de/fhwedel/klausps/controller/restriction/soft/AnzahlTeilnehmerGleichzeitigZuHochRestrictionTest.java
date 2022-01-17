package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.convertPruefungenToPlanungseinheiten;
import static de.fhwedel.klausps.controller.util.TestUtils.getPruefungsnummernFromModel;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungenAt;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTime;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.assertions.WeicheKriteriumsAnalyseAssert;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.BlockImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
@SuppressWarnings("OptionalGetWithoutIsPresent")
class AnzahlTeilnehmerGleichzeitigZuHochRestrictionTest {

  public AnzahlTeilnehmerGleichzeitigZuHochRestriction deviceUnderTest;
  public DataAccessService dataAccessService;

  // TODO implement usage of start & end time of sequential blocks

  @BeforeEach
  public void setUp() {
    int maxTeilnehmerAtSameTime = 200;
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(this.dataAccessService,
        Duration.ZERO, maxTeilnehmerAtSameTime);
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_onePruefung_oneTeilnehmerkreis()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 200);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(Set.of(pruefung));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isNotPresent();
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_onePruefung_multipleTeilnehmerkreise()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 100);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 51);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 49);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(Set.of(pruefung));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isNotPresent();
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_multiplePruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal200Students();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isNotPresent();
  }

  private List<Pruefung> get3PruefungenWithTotal200Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 111);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 43);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 46);
    return pruefungen;
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_multiplePruefungenInOneBlock()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal200Students();
    Block block = getBlockWith(pruefungen);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isNotPresent();
  }

  private Block getBlockWith(Iterable<Pruefung> pruefungen) {
    Block block = new BlockImpl(mock(Pruefungsperiode.class), "name", PARALLEL);
    for (Pruefung pruefung : pruefungen) {
      block.addPruefung(pruefung);
    }
    return block;
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_onePruefung_oneTeilnehmerkreis()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Planungseinheit planungseinheit = getRandomPlannedPruefung(1);
    planungseinheit.asPruefung().addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 201);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(planungseinheit));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(planungseinheit.asPruefung())).isPresent();
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_onePruefung_multipleTeilnehmerkreise()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 101);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 51);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 49);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(List.of(pruefung))));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isPresent();
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_multiplePruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal201Students();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isPresent();
  }

  private List<Pruefung> get3PruefungenWithTotal201Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 111);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 43);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 47);
    return pruefungen;
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_multiplePruefungenInOneBlock()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal201Students();
    Block block = getBlockWith(pruefungen);
    block.setStartzeitpunkt(LocalDateTime.of(1999, 11, 12, 13, 14));

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isPresent();
  }

  @Test
  void oneTeilnehmerMoreAtATimeThanAllowed_analysisContainsCorrectPruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3OverlappingPruefungenWith201Teilnehmer();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(
            (deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()).get()))
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  private List<Pruefung> get3OverlappingPruefungenWith201Teilnehmer() {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15), startFirstPruefung.plusMinutes(30));
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 150);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 50);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 1);
    return pruefungen;
  }

  @Test
  void oneTeilnehmerMoreAtATimeThanAllowed_analysisContainsCorrectPruefungen_fromBlock()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Block block = getBlockWithPlanungseinheitenWithMoreThan200Teilnehmer();
    Pruefung pruefungToTest = block.getPruefungen().iterator().next();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(new ArrayList<>(block.getPruefungen()))));

    WeicheKriteriumsAnalyseAssert.assertThat((deviceUnderTest.evaluate(pruefungToTest).get()))
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(block.getPruefungen()));
  }

  private Block getBlockWithPlanungseinheitenWithMoreThan200Teilnehmer() {
    List<Pruefung> pruefungen = get3OverlappingPruefungenWith201Teilnehmer();
    Block block = new BlockImpl(mock(Pruefungsperiode.class), "name", PARALLEL);
    for (Pruefung pruefung : pruefungen) {
      block.addPruefung(pruefung);
    }
    block.setStartzeitpunkt(getRandomTime(1L));
    return block;
  }

  @Test
  void correctAmountOfStudents_oneMoreThanAllowed()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3OverlappingPruefungenWith201Teilnehmer();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(
            (deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()).get()))
        .affectsExactlyAsManyStudentsAs(201);
  }

  @Test
  void correctAmountOfStudents_wayMoreThanAllowed()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3OverlappingPruefungenWith999Teilnehmer();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(
            (deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()).get()))
        .affectsExactlyAsManyStudentsAs(999);
  }

  private List<Pruefung> get3OverlappingPruefungenWith999Teilnehmer() {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15), startFirstPruefung.plusMinutes(30));
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 765);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 123);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 111);
    return pruefungen;
  }

  @Test
  void evaluate_scoringForMinimalViolation()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal201Students();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    int expectedScoring = WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  @Test
  void evaluate_scoringForMinimalViolation_nonDefaultScoringSteps()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(this.dataAccessService,
        Duration.ZERO, 50, 3);
    List<Pruefung> pruefungen = get3PruefungenWithTotal51Students();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    int expectedScoring = WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  private List<Pruefung> get3PruefungenWithTotal51Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 11);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 22);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 18);
    return pruefungen;
  }

  @Test
  void evaluate_scoringForMinimalViolation_nonDefaultScoringSteps_highScoring()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(this.dataAccessService,
        Duration.ZERO, 10, 3);
    List<Pruefung> pruefungen = get3PruefungenWithTotal51Students();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    int expectedScoring = 14 * WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  @Test
  void evaluate_secondLowestScoring()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(this.dataAccessService,
        Duration.ZERO, 200, 10);
    List<Pruefung> pruefungen = get3PruefungenWithTotal211Students();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf((convertPruefungenToPlanungseinheiten(pruefungen))));

    int expectedScoring = 2 * WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  private List<Pruefung> get3PruefungenWithTotal211Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 111);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 43);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 57);
    return pruefungen;
  }

  @Test
  void nonPositiveStepSizeNotAllowed_zero() {
    assertThrows(IllegalArgumentException.class,
        () -> this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(
            this.dataAccessService, Duration.ZERO, 200, 0));
  }

  @Test
  void nonPositiveStepSizeNotAllowed_negative() {
    assertThrows(IllegalArgumentException.class,
        () -> this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(
            this.dataAccessService, Duration.ZERO, 200, -1));
  }

}
