package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.hard.HarteRestriktion;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class RestrictionServiceTest {

  RestrictionFactory restrictionFactory;
  RestrictionService deviceUnderTest;

  @BeforeEach
  void setUp() {
    // set for each test an adequate set of restrictions to use
    // do not force all tests to use the same restrictions by setting them here
    this.restrictionFactory = mock(RestrictionFactory.class);
    this.deviceUnderTest = new RestrictionService(this.restrictionFactory);
  }

  @Test
  void getPruefungenInHardConflictWith_noNullParametersAllowed() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getPruefungenInHardConflictWith(null));
  }

  @Test
  void getPruefungenInHardConflictWith_noHardRestrictions_noConflicts() {
    assertThat(
        deviceUnderTest.getPruefungenInHardConflictWith(getRandomPlannedPruefung(1L))).isEmpty();
  }

  @Test
  void getPruefungenInHardConflictWith_oneHardRestrictions_noConflicts() {
    deviceUnderTest.registerHardCriteria(Set.of(oneHardRestrictionThatDoesNotFail()));
    assertThat(
        deviceUnderTest.getPruefungenInHardConflictWith(getRandomPlannedPruefung(1L))).isEmpty();
  }

  private HarteRestriktion oneHardRestrictionThatDoesNotFail() {
    HarteRestriktion restriktion = mock(HarteRestriktion.class);
    when(restriktion.evaluate(any())).thenReturn(Optional.empty());
    return restriktion;
  }

  @Test
  void getPruefungenInHardConflictWith_oneHardRestriction_checkTheOneExistingHardRestriction() {
    HarteRestriktion hardRestriction = oneHardRestrictionThatDoesNotFail();
    Planungseinheit planungseinheitToCheckFor = getRandomPlannedPruefung(1L);

    deviceUnderTest.registerHardCriteria(Set.of(hardRestriction));
    deviceUnderTest.getPruefungenInHardConflictWith(planungseinheitToCheckFor);

    verify(hardRestriction, times(1)).getAllPotentialConflictingPruefungenWith(
        planungseinheitToCheckFor);
  }

  @Test
  void getPruefungenInHardConflictWith_oneHardRestriction_resultContainsPruefungenFromRestrictionAnalysis() {
    Set<Pruefung> pruefungenToFailWith = new HashSet<>(getRandomPlannedPruefungen(11L, 3));
    HarteRestriktion hardRestriction = oneHardRestrictionThatDoesFailWith(pruefungenToFailWith);
    Planungseinheit planungseinheitToCheckFor = getRandomPlannedPruefung(1L);

    when(hardRestriction.getAllPotentialConflictingPruefungenWith(any())).thenReturn(
        pruefungenToFailWith);

    deviceUnderTest.registerHardCriteria(Set.of(hardRestriction));
    assertThat(deviceUnderTest.getPruefungenInHardConflictWith(
        planungseinheitToCheckFor)).containsExactlyInAnyOrderElementsOf(pruefungenToFailWith);
  }

  private HarteRestriktion oneHardRestrictionThatDoesFailWith(Collection<Pruefung> pruefungen) {
    HarteRestriktion restriktion = mock(HarteRestriktion.class);
    when(restriktion.evaluate(any())).thenReturn(
        Optional.of(hartesKriteriumAnalyseWith(pruefungen)));
    return restriktion;
  }

  private HartesKriteriumAnalyse hartesKriteriumAnalyseWith(Collection<Pruefung> pruefungen) {
    return new HartesKriteriumAnalyse(new HashSet<>(pruefungen), emptySet(), 0,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);
  }

  /*
   * Edge cases for getPruefungenInHardConflictWith:
   * x no hard restriction
   * - one hard restriction TODO check failing check
   * O more than one hard restriction
   */

}
