package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.convertPruefungenToPlanungseinheiten;
import static de.fhwedel.klausps.controller.util.TestUtils.getPruefungsnummernFromModel;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungWith;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungenAt;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.answers.BlockFromPruefungAnswer;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.WeicheKriteriumsAnalyseAssert;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class AnzahlPruefungenGleichzeitigRestriktionTest {

  public AnzahlPruefungenGleichzeitigRestriktion deviceUnderTest;
  public DataAccessService dataAccessService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService);
  }

  @Test
  @DisplayName("A single Pruefung can not violate the restriction")
  void evaluate_onlyCheckedPruefungIsPlanned() throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomPlannedPruefung(5L);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(pruefung));

    assertThat(deviceUnderTest.evaluate(pruefung)).isEmpty();
  }

  @Test
  @DisplayName("More pruefungen at a time than allowed results in presence of result")
  void evaluate_onePruefungMoreAtATimeThanAllowed_getCollision()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen =
        getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15),
            startFirstPruefung.plusMinutes(30));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isPresent();
  }

  @Test
  @DisplayName("Result of multiple overlapping pruefungen contains correct pruefungen")
  void evaluate_onePruefungMoreAtATimeThanAllowed_analysisContainsCorrectPruefungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // set the max amount of simultaneous pruefungen to 2
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15), startFirstPruefung.plusMinutes(30));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(
            (deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()).get()))
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  @Test
  @DisplayName("Violations are detected when pruefungen have identical time slots")
  void evaluate_onePruefungMoreAtATimeThanAllowed_equalIntervals()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // set the max amount of simultaneous pruefungen to 1
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenOnSameInterval();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(0)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(1)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  private List<Pruefung> get2PruefungenOnSameInterval() {
    List<Pruefung> result = new ArrayList<>(2);
    result.add(getRandomPlannedPruefung(12L));
    result.add(
        getRandomPruefung(result.get(0).getStartzeitpunkt(), result.get(0).endzeitpunkt(), 11L));
    return result;
  }

  @Test
  @DisplayName("Violations are detected when pruefungen overlap partly")
  void evaluate_onePruefungMoreAtATimeThanAllowed_overlappingIntervals()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // set the max amount of simultaneous pruefungen to 1
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenWithOneOverlappingTheOther();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(0)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(1)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  private List<Pruefung> get2PruefungenWithOneOverlappingTheOther() {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    return getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15));
  }

  @Test
  @DisplayName("Violations are detected when one pruefungs time-slot contains anotherones")
  void evaluate_onePruefungMoreAtATimeThanAllowed_intervalContainingOtherInterval()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // set the max amount of simultaneous pruefungen to 1
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenWithOneContainedByOther();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(0)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(1)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  private List<Pruefung> get2PruefungenWithOneContainedByOther() {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> result = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15));
    result.get(1).setDauer(result.get(0).getDauer().minusMinutes(30));
    return result;
  }

  @Test
  @DisplayName("Violations with overlapping pruefungen show the correct pruefungen in result")
  void evaluate_onePruefungMoreAtATimeThanAllowed_analysisContainsCorrectTeilnehmerkreise()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // set the max amount of simultaneous pruefungen to 2
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15), startFirstPruefung.plusMinutes(30));

    Set<Teilnehmerkreis> expectedTeilnehmerkreise = getAllTeilnehmerKreiseFrom(pruefungen);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()).get()
        .getAffectedTeilnehmerKreise()).containsExactlyInAnyOrderElementsOf(
        expectedTeilnehmerkreise);
  }

  private Set<Teilnehmerkreis> getAllTeilnehmerKreiseFrom(Collection<Pruefung> pruefungen) {
    return pruefungen.stream().map(Planungseinheit::getTeilnehmerkreise)
        .reduce(new HashSet<>(), (x, y) -> {
          x.addAll(y);
          return x;
        });
  }

  @Test
  @DisplayName("Directly overlapping pruefungen cause no violation when less than limit")
  void evaluate_asManyPruefungenAtSameTimeAsAllowed_twoDirectlyOverlapping()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // set the max amount of simultaneous pruefungen to 2
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()))).isEmpty();
  }

  @Test
  @DisplayName("Two sequential pruefungen both overlapping with third one without exceeding the limit")
  void evaluate_asManyPruefungenAtSameTimeAsAllowed_twoAfterEachOtherOverlappingWithThird()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // aaaa bbbb
    //   ccccc
    Duration puffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        puffer);

    List<Pruefung> pruefungen = get3PruefungenWith2SequentialOverlappingTheThird();
    Pruefung pruefungToCheck = pruefungen.get(pruefungen.size() - 1);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat((deviceUnderTest.evaluate(pruefungToCheck))).isEmpty();
  }

  private List<Pruefung> get3PruefungenWith2SequentialOverlappingTheThird() {
    Duration puffer = Duration.ofMinutes(30);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    Pruefung pruefungA = new PruefungImpl("abcd", "Analysis", "1111", Duration.ofMinutes(45),
        startFirstPruefung);
    Pruefung pruefungB = new PruefungImpl("efgh", "DM", "2222", Duration.ofMinutes(45),
        startFirstPruefung.plus(pruefungA.getDauer()).plus(puffer));
    Pruefung pruefungC = new PruefungImpl("ijkl", "IT", "3333", pruefungA.getDauer().plus(puffer),
        startFirstPruefung.plusMinutes(15));
    return List.of(pruefungA, pruefungB, pruefungC);
  }

  @Test
  @DisplayName("Violations detected when sequential pruefungen do not maintain buffer")
  void evaluate_morePlanungseinheitenThanPermitted_pruefungenCloserThanBufferButNotOverlapping()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Duration puffer = Duration.ofMinutes(10);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        puffer);

    List<Pruefung> pruefungen = get2PruefungenCloserToEachOtherThan(puffer);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)))).isPresent();
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)))).isPresent();
  }

  private List<Pruefung> get2PruefungenCloserToEachOtherThan(Duration duration) {
    Pruefung firstPruefung = getRandomPlannedPruefung(1L);
    firstPruefung.setDauer(Duration.ofHours(1));
    Pruefung secondPruefung = getRandomPlannedPruefung(2L);
    secondPruefung.setStartzeitpunkt(
        firstPruefung.getStartzeitpunkt().plus(firstPruefung.getDauer())
            .plus(duration.minusMinutes(1)));
    return List.of(firstPruefung, secondPruefung);
  }

  @Test
  @DisplayName("Adjacent pruefungen are accepted when buffer is 0")
  void evaluate_morePlanungseinheitenThanPermitted_noBufferPlanungseinheitenAdjacent()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Duration puffer = Duration.ZERO;
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        puffer);

    List<Pruefung> pruefungen = get2adjacentPruefungen();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)))).isNotPresent();
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)))).isNotPresent();
  }

  private List<Pruefung> get2adjacentPruefungen() {
    return get2adjacentPruefungen(Duration.ZERO);
  }

  private List<Pruefung> get2adjacentPruefungen(Duration buffer) {
    List<Pruefung> result = new ArrayList<>(2);
    result.add(getRandomPlannedPruefung(22L));
    result.addAll(getRandomPruefungenAt(23L, result.get(0).endzeitpunkt().plus(buffer)));
    return result;
  }

  @Test
  @DisplayName("Adjacent pruefungen maintaining buffer cause no violation")
  void evaluate_morePlanungseinheitenThanPermitted_planungseinheitenAdjacent()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);

    List<Pruefung> pruefungen = get2adjacentPruefungen(buffer);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)))).isNotPresent();
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)))).isNotPresent();
  }

  private Pruefung getPruefungOfReadOnlyPruefung(ReadOnlyPruefung roPruefung) {
    PruefungImpl modelPruefung = new PruefungImpl(roPruefung.getPruefungsnummer(),
        roPruefung.getName(), "", roPruefung.getDauer(), roPruefung.getTermin().orElse(null));
    for (String pruefer : roPruefung.getPruefer()) {
      modelPruefung.addPruefer(pruefer);
    }
    roPruefung.getTeilnehmerKreisSchaetzung().forEach(modelPruefung::setSchaetzung);
    return modelPruefung;
  }

  @Test
  @DisplayName("The minimal scoring corresponds with the factor of this criteria")
  void evaluate_scoringForMinimalViolation()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);

    List<Pruefung> pruefungen = get2PruefungenWithOneOverlappingTheOther();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  @Test
  void evaluate_scoringForMinimalViolation_otherMaxAmountOfSimultaneous()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        buffer);

    List<Pruefung> pruefungen = get3PruefungenWithOneOverlappingTheOther();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  private List<Pruefung> get3PruefungenWithOneOverlappingTheOther() {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    return getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15),
        startFirstPruefung.plusMinutes(20));
  }

  @Test
  void evaluate_scoringForSecondLowestViolation()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);

    List<Pruefung> pruefungen = get3PruefungenWithOneOverlappingTheOther();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert() * 2;

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(
        expectedScoring);
  }

  @Test
  void calcScoringFor_zero_max1AtATime() {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 1));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isZero();
  }

  @Test
  void calcScoringFor_zero_max2AtATime() {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 2));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isZero();
  }

  @Test
  void calcScoringFor_minimal_max1AtATime() {
    Duration buffer = Duration.ofMinutes(30);
    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert();
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 2));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isEqualTo(
        expectedScoring);
  }

  @Test
  void calcScoringFor_minimal_max2AtATime() {
    Duration buffer = Duration.ofMinutes(30);
    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert();
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 3));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isEqualTo(
        expectedScoring);
  }

  @Test
  void calcScoringFor_secondLowest_max1AtATime() {
    Duration buffer = Duration.ofMinutes(30);
    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert() * 2;
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 3));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isEqualTo(
        expectedScoring);
  }

  @Test
  void calcScoringFor_secondLowest_max2AtATime() {
    Duration buffer = Duration.ofMinutes(30);
    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert() * 2;
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 4));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isEqualTo(
        expectedScoring);
  }

  @Test
  void calcScoringFor_noNegativeScoring() {
    Duration buffer = Duration.ofMinutes(30);
    int expectedScoring = 0;
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        buffer);
    Collection<Planungseinheit> violatingPlanungseinheiten = new HashSet<>(
        getRandomPlannedPruefungen(1L, 1));
    assertThat(deviceUnderTest.calcScoringFor(violatingPlanungseinheiten)).isEqualTo(
        expectedScoring);
  }

  @Test
  @DisplayName("Affected students correspond with sum students per distinct teilnehmerkreis")
  void evaluate_sumOfTeilnehmerkreisschaetzungen()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = get2PruefungenWithDistinctTeilnehmerkreiseWithSchaetzung(5, 12);
    int expectedTeilnehmerAmount = 5 + 12;

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat(
        (deviceUnderTest.evaluate(pruefungen.get(0)).get().getAmountAffectedStudents())).isEqualTo(
        expectedTeilnehmerAmount);
  }

  private List<Pruefung> get2PruefungenWithDistinctTeilnehmerkreiseWithSchaetzung(int s1, int s2) {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> result = List.of(getRandomPruefungWith(1L, getRandomTeilnehmerkreis(1L)),
        getRandomPruefungWith(2L, getRandomTeilnehmerkreis(2L)));

    // set all pruefungen to occupy the same time slot
    for (Pruefung pruefung : result) {
      pruefung.setStartzeitpunkt(result.get(0).getStartzeitpunkt());
      pruefung.setDauer(result.get(0).getDauer());
    }

    result.get(0).setSchaetzung(result.get(0).getTeilnehmerkreise().iterator().next(), s1);
    result.get(1).setSchaetzung(result.get(1).getTeilnehmerkreise().iterator().next(), s2);

    return result;
  }

  @Test
  @DisplayName("Affected students: For each ambiguous teilnehmerkreis the one with the highest amount of students counts")
  void evaluate_sumOfTeilnehmerkreisschaetzungen_onlyCountHighestValueOfTeilnehmerkreis()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenWithSameTeilnehmerkreiseWithSchaetzung(5, 12);
    int expectedTeilnehmerAmount = 12;

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(convertPruefungenToPlanungseinheiten(pruefungen)));

    assertThat(
        (deviceUnderTest.evaluate(pruefungen.get(0)).get().getAmountAffectedStudents())).isEqualTo(
        expectedTeilnehmerAmount);
  }

  private List<Pruefung> get2PruefungenWithSameTeilnehmerkreiseWithSchaetzung(int s1, int s2) {
    List<Pruefung> result = List.of(getRandomPruefungWith(1L, getRandomTeilnehmerkreis(1L)),
        getRandomPruefungWith(2L, getRandomTeilnehmerkreis(1L)));

    // set all pruefungen to occupy the same time slot
    for (Pruefung pruefung : result) {
      pruefung.setStartzeitpunkt(result.get(0).getStartzeitpunkt());
      pruefung.setDauer(result.get(0).getDauer());
    }

    result.get(0).setSchaetzung(result.get(0).getTeilnehmerkreise().iterator().next(), s1);
    result.get(1).setSchaetzung(result.get(1).getTeilnehmerkreise().iterator().next(), s2);

    return result;
  }

  @Test
  @DisplayName("Conflicts do not get caught when the checked pruefung is not involved")
  void evaluate_moreAtSameTimeThatAllowedButRequestedNotInvolvedInConflict()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // max 2 pruefungen at a time and no buffer
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        Duration.ZERO);
    List<Pruefung> pruefungen = get3PruefungenOverlappingAndOneJustOverlappingWithOneOfThem();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(
            convertPruefungenToPlanungseinheiten(List.of(pruefungen.get(0), pruefungen.get(3)))));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)))).isNotPresent();
  }

  /**
   * The one not violating the restriction is the first in the list.
   *
   * @return A list of pruefungen.
   */
  private List<Pruefung> get3PruefungenOverlappingAndOneJustOverlappingWithOneOfThem() {
    List<Pruefung> result = new ArrayList<>(4);
    result.addAll(get2PruefungenOnSameInterval());
    // add the one to check for
    result.add(0,
        getRandomPruefung(result.get(1).endzeitpunkt(), result.get(1).endzeitpunkt().plusHours(1),
            3L));
    // add the one bridging between the one to test and the ones causing the violation
    result.add(getRandomPruefung(result.get(1).endzeitpunkt().minusMinutes(15),
        result.get(0).getStartzeitpunkt().plusMinutes(15), 4L));
    return result;
  }

  @Test
  @DisplayName("Pruefungen exceeding the maximal amount do not violate the restriction when in one block")
  void evaluate_morePruefungenThanAllowedButAllInSameBlock()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        Duration.ZERO);
    Block block = getBlockWith3Pruefungen().asBlock();
    List<Pruefung> pruefungenInBlock = new ArrayList<>(block.getPruefungen());

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(block.getPruefungen());

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));

    assertThat((deviceUnderTest.evaluate(pruefungenInBlock.get(0)))).isNotPresent();
    assertThat((deviceUnderTest.evaluate(pruefungenInBlock.get(1)))).isNotPresent();
    assertThat((deviceUnderTest.evaluate(pruefungenInBlock.get(2)))).isNotPresent();
  }

  private Planungseinheit getBlockWith3Pruefungen() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    Block result = new BlockImpl(pruefungsperiode, "name", Blocktyp.PARALLEL);
    for (Pruefung pruefung : getRandomPlannedPruefungen(2L, 3)) {
      result.addPruefung(pruefung);
    }
    result.setStartzeitpunkt(LocalDateTime.of(1998, 1, 2, 21, 39));
    return result;
  }

  @Test
  @DisplayName("Pruefungen in a block are count as one for check of the limit")
  void evaluate_morePruefungenThanAllowed_validAsSomePruefungenAreTogetherInABlock()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        Duration.ZERO);
    Block block = getBlockWith3Pruefungen().asBlock();
    Pruefung pruefung = getRandomPruefung(block.getStartzeitpunkt(), block.endzeitpunkt(), 22L);
    List<Pruefung> pruefungenInBlock = new ArrayList<>(block.getPruefungen());

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        union(block.getPruefungen(), pruefung));

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));

    assertThat((deviceUnderTest.evaluate(pruefungenInBlock.get(0)))).isNotPresent();
    assertThat((deviceUnderTest.evaluate(pruefung))).isNotPresent();
  }

/*  @Test
todo test schlägt fehl

  void getAffectedTeilnehmerkreiseFrom_containsAllTeilnehmerkreise() {
    List<Teilnehmerkreis> teilnehmerkreise = getRandomTeilnehmerkreise(1L, 5);
    Set<Planungseinheit> planungseinheiten = union(
        Set.of(getRandomPruefungWith(2L, teilnehmerkreise.subList(0, 3))),
        getRandomPruefungWith(2L, teilnehmerkreise.subList(3, 5)));
    assertThat(deviceUnderTest.getAffectedTeilnehmerkreiseFrom(
        planungseinheiten)).containsExactlyInAnyOrderElementsOf(teilnehmerkreise);
  }*/

  private <T> Set<T> union(Collection<T> fst, T other) {
    Set<T> result = new HashSet<>();
    result.addAll(fst);
    result.addAll(List.of(other));
    return result;
  }

  @Test
  @DisplayName("Multiple overlapping blocks do not violate restriction when their amount does not exceed the limit")
  void evaluate_morePruefungenThanAllowed_twoOverlappingBlocks()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        Duration.ZERO);
    List<Block> planungseinheiten = get2OverlappingBloecke();
    Pruefung pruefungToCheck = new ArrayList<Planungseinheit>(
        planungseinheiten.get(0).asBlock().getPruefungen()).get(0).asPruefung();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        union(planungseinheiten.get(0).asBlock().getPruefungen(),
            planungseinheiten.get(1).asBlock().getPruefungen()));

    // answer with correct block for each pruefung
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenAnswer(
        new BlockFromPruefungAnswer(planungseinheiten));

    assertThat((deviceUnderTest.evaluate(pruefungToCheck))).isNotPresent();
  }

  private List<Block> get2OverlappingBloecke() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    List<Block> result = new ArrayList<>(2);
    for (int i = 0; i < 2; i++) {
      Block block = new BlockImpl(pruefungsperiode, "name", Blocktyp.PARALLEL);
      for (Pruefung pruefung : getRandomPlannedPruefungen(i, 2)) {
        block.addPruefung(pruefung);
      }
      block.setStartzeitpunkt(LocalDateTime.of(1998, 1, 2, 21, 39));
      result.add(block);
    }
    return result;
  }

  private <T> Set<T> union(Collection<T> fst, Collection<T> other) {
    Set<T> result = new HashSet<>();
    result.addAll(fst);
    result.addAll(other);
    return result;
  }

  @Test
  void getAffectedStudentsFrom_noTeilnehmerkreisDuplicated() {
    List<Planungseinheit> planungseinheiten = getPlanungseinheitenWith22DistinctStudents();
    int expectedAmount = 22;
    assertThat(deviceUnderTest.getAffectedStudentsFrom(planungseinheiten)).isEqualTo(
        expectedAmount);
  }

  private List<Planungseinheit> getPlanungseinheitenWith22DistinctStudents() {
    List<Planungseinheit> planungseinheiten = new ArrayList<>();
    planungseinheiten.add(getPlanungseinheitWithDistinctStudents(2L, 12));
    planungseinheiten.add(getPlanungseinheitWithDistinctStudents(1L, 10));
    return planungseinheiten;
  }

  private Planungseinheit getPlanungseinheitWithDistinctStudents(long seed, int amountStudents) {
    Teilnehmerkreis teilnehmerkreis = getRandomTeilnehmerkreis(seed);
    return getPlanungseinheitWithTeilnehmerkreis(seed, teilnehmerkreis, amountStudents);
  }

  private Planungseinheit getPlanungseinheitWithTeilnehmerkreis(long seed,
      Teilnehmerkreis teilnehmerkreis, int amountStudents) {
    Pruefung pruefung = getRandomPruefungWith(seed, teilnehmerkreis);
    pruefung.setSchaetzung(teilnehmerkreis, amountStudents);
    return pruefung;
  }

  @Test
  void getAffectedStudentsFrom_duplicatedTeilnehmerkreis_useHigherAmount() {
    List<Planungseinheit> planungseinheiten = get2PlanungseinheitenWithSameTeilnehmerkreisWithMax12Teilnehmer();
    int expectedAmount = 12;
    assertThat(deviceUnderTest.getAffectedStudentsFrom(planungseinheiten)).isEqualTo(
        expectedAmount);
  }

  private List<Planungseinheit> get2PlanungseinheitenWithSameTeilnehmerkreisWithMax12Teilnehmer() {
    List<Planungseinheit> planungseinheiten = new ArrayList<>();
    Teilnehmerkreis teilnehmerkreis = getRandomTeilnehmerkreis(1L);
    planungseinheiten.add(getPlanungseinheitWithTeilnehmerkreis(1L, teilnehmerkreis, 10));
    planungseinheiten.add(getPlanungseinheitWithTeilnehmerkreis(1L, teilnehmerkreis, 11));
    planungseinheiten.add(getPlanungseinheitWithTeilnehmerkreis(2L, teilnehmerkreis, 12));
    return planungseinheiten;
  }

}
