package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.convertPruefungenToPlanungseinheiten;
import static de.fhwedel.klausps.controller.util.TestUtils.getPruefungsnummernFromModel;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungenAt;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedPruefung;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.WeicheKriteriumsAnalyseAssert;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnzahlPruefungenGleichzeitigRestriktionTest {

  public AnzahlPruefungenGleichzeitigRestriktion deviceUnderTest;
  public DataAccessService dataAccessService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService);
  }

  /*
   * Grenzfaelle:
   * x Nur die aufgerufene Klausur ist geplant
   * x Aufruf mit ungeplanter Klausur
   * x Keine gleichzeitigen Klausuren
   * p Genau so viele Klausuren gleichzeitig wie erlaubt
   * x Eine Klausur mehr gleichzeitig als erlaubt
   * - Mehr klausuren gleichzeitig als erlaubt, ohne dass die getestete Pruefung involviert ist (nichts soll angezeigt werden)
   * - Mehr klausuren als erlaubt aber alle in einem Block zusammen
   * - Mehr klausuren als erlaubt aber alle einige zusammen in einem Block (so, dass erlaubt)
   * - Mehr klausuren als erlaubt aber in 2 Blöcken, sodass insgesamt erlaubt
   */

  @Test
  void evaluate_onlyCheckedPruefungIsPlanned() {
    Pruefung pruefung = getRandomPlannedPruefung(5L);
    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(pruefung));

    assertThat(deviceUnderTest.evaluate(pruefung)).isEmpty();
  }

  @Test
  void evaluate_callWithUnplannedPruefung() {
    Pruefung pruefung = getRandomUnplannedPruefung(5L);
    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Collections.emptySet());

    assertThat(deviceUnderTest.evaluate(pruefung)).isEmpty();
  }

  @Test
  void evaluate_noSimultaneousPruefungen() {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    LocalDateTime startSecondPruefung = startFirstPruefung.plusMinutes(180);
    LocalDateTime startThirdPruefung = startSecondPruefung.plusMinutes(180);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung, startSecondPruefung,
        startThirdPruefung);

    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Collections.emptySet());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isEmpty();
    assertThat(deviceUnderTest.evaluate(pruefungen.get(1))).isEmpty();
    assertThat(deviceUnderTest.evaluate(pruefungen.get(2))).isEmpty();
  }

  @Test
  void evaluate_onePruefungMoreAtATimeThanAllowed_getCollision() throws IllegalTimeSpanException {
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Planungseinheit> pruefungen = convertPruefungenToPlanungseinheiten(
        getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15),
            startFirstPruefung.plusMinutes(30)));

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(pruefungen);

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0).asPruefung())).isPresent();
  }

  @Test
  void evaluate_onePruefungMoreAtATimeThanAllowed_analysisContainsCorrectPruefungen()
      throws IllegalTimeSpanException {
    // set the max amount of simultaneous pruefungen to 2
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15), startFirstPruefung.plusMinutes(30));

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

    WeicheKriteriumsAnalyseAssert.assertThat(
            (deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()).get()))
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  @Test
  void evaluate_onePruefungMoreAtATimeThanAllowed_equalIntervals() throws IllegalTimeSpanException {
    // set the max amount of simultaneous pruefungen to 1
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenOnSameInterval();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(0)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
    WeicheKriteriumsAnalyseAssert.assertThat(deviceUnderTest.evaluate(pruefungen.get(1)).get())
        .conflictingPruefungenAreExactly(getPruefungsnummernFromModel(pruefungen));
  }

  private List<Pruefung> get2PruefungenOnSameInterval() {
    List<Pruefung> result = new ArrayList<>(2);
    result.add(getRandomPlannedPruefung(10L));
    result.add(
        getRandomPruefung(result.get(0).getStartzeitpunkt(), result.get(0).endzeitpunkt(), 11L));
    return result;
  }

  @Test
  void evaluate_onePruefungMoreAtATimeThanAllowed_overlappingIntervals()
      throws IllegalTimeSpanException {
    // set the max amount of simultaneous pruefungen to 1
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenWithOneOverlappingTheOther();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

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
  void evaluate_onePruefungMoreAtATimeThanAllowed_intervalContainingOtherInterval()
      throws IllegalTimeSpanException {
    // set the max amount of simultaneous pruefungen to 1
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1);
    List<Pruefung> pruefungen = get2PruefungenWithOneContainedByOther();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

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
  void evaluate_onePruefungMoreAtATimeThanAllowed_analysisContainsCorrectTeilnehmerkreise()
      throws IllegalTimeSpanException {
    // set the max amount of simultaneous pruefungen to 2
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15), startFirstPruefung.plusMinutes(30));

    Set<Teilnehmerkreis> expectedTeilnehmerkreise = getAllTeilnehmerKreiseFrom(pruefungen);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

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
  void evaluate_asManyPruefungenAtSameTimeAsAllowed_twoDirectlyOverlapping()
      throws IllegalTimeSpanException {
    // set the max amount of simultaneous pruefungen to 2
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2);
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung,
        startFirstPruefung.plusMinutes(15));

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0).asPruefung()))).isEmpty();
  }

  @Test
  void evaluate_asManyPruefungenAtSameTimeAsAllowed_twoAfterEachOtherOverlappingWithThird()
      throws IllegalTimeSpanException {
    // aaaa bbbb
    //   ccccc
    Duration puffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 2,
        puffer);

    List<Pruefung> pruefungen = get3PruefungenWith2SequentialOverlappingTheThird();
    Pruefung pruefungToCheck = pruefungen.get(pruefungen.size() - 1);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

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
  void evaluate_morePlanungseinheitenThanPermitted_pruefungenCloserThanBufferButNotOverlapping()
      throws IllegalTimeSpanException {
    Duration puffer = Duration.ofMinutes(10);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        puffer);

    List<Pruefung> pruefungen = get2PruefungenCloserToEachOtherThan(puffer);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

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
  void evaluate_morePlanungseinheitenThanPermitted_noBufferPlanungseinheitenAdjacent()
      throws IllegalTimeSpanException {
    Duration puffer = Duration.ZERO;
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        puffer);

    List<Pruefung> pruefungen = get2adjacentPruefungen();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

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
  void evaluate_morePlanungseinheitenThanPermitted_planungseinheitenAdjacent()
      throws IllegalTimeSpanException {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);

    List<Pruefung> pruefungen = get2adjacentPruefungen(buffer);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)))).isNotPresent();
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)))).isNotPresent();
  }

  /**
   * All planungseinheiten have to be pruefungen for this method to be applicable.
   */
  private List<Pruefung> convertPlanungseinheitenToPruefungen(
      Iterable<Planungseinheit> planungseinheiten) {
    List<Pruefung> result = new ArrayList<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      result.add(planungseinheit.asPruefung());
    }
    return result;
  }

  private List<Pruefung> convertPruefungenFromReadonlyToModel(
      Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream().map(this::getPruefungOfReadOnlyPruefung).toList();
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
  void evaluate_scoringForMinimalViolation() throws IllegalTimeSpanException {
    Duration buffer = Duration.ofMinutes(30);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService, 1,
        buffer);

    List<Pruefung> pruefungen = get2PruefungenWithOneOverlappingTheOther();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

    int expectedScoring = WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH.getWert();

    assertThat((deviceUnderTest.evaluate(pruefungen.get(0)).get().getDeltaScoring())).isEqualTo(expectedScoring);
    assertThat((deviceUnderTest.evaluate(pruefungen.get(1)).get().getDeltaScoring())).isEqualTo(expectedScoring);
  }

}
