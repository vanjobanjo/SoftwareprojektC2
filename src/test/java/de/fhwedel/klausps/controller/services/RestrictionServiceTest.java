package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTime;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.hard.HarteRestriktion;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void getPruefungenInHardConflictWith_oneHardRestrictions_noConflicts()
      throws NoPruefungsPeriodeDefinedException {
    deviceUnderTest.registerHardCriteria(Set.of(oneHardRestrictionThatDoesNotTrigger()));
    assertThat(
        deviceUnderTest.getPruefungenInHardConflictWith(getRandomPlannedPruefung(1L))).isEmpty();
  }

  private HarteRestriktion oneHardRestrictionThatDoesNotTrigger()
      throws NoPruefungsPeriodeDefinedException {
    HarteRestriktion restriktion = mock(HarteRestriktion.class);
    when(restriktion.evaluate(any())).thenReturn(Optional.empty());
    when(restriktion.wouldBeHardConflictAt(any(), any())).thenReturn(false);
    return restriktion;
  }

  @Test
  void getPruefungenInHardConflictWith_oneHardRestriction_checkTheOneExistingHardRestriction()
      throws NoPruefungsPeriodeDefinedException {
    HarteRestriktion hardRestriction = oneHardRestrictionThatDoesNotTrigger();
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

  @Test
  void wouldBeHardConflictAt_timeMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.wouldBeHardConflictAt(null, getRandomPlannedPruefung(1L)));
  }

  @Test
  void wouldBeHardConflictAt_planungseinheitMustNotBeNull() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L), null));
  }

  @Test
  void wouldBeHardConflictAt_oneHardRestrictionGetsChecked()
      throws NoPruefungsPeriodeDefinedException {
    HarteRestriktion hardRestriction = oneHardRestrictionThatDoesNotTrigger();
    deviceUnderTest.registerHardCriteria(Set.of(hardRestriction));

    deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L), getRandomPlannedPruefung(1L));
    verify(hardRestriction, times(1)).wouldBeHardConflictAt(any(LocalDateTime.class),
        any(Planungseinheit.class));
  }

  @Test
  void wouldBeHardConflictAt_twoHardRestrictionGetChecked()
      throws NoPruefungsPeriodeDefinedException {
    List<HarteRestriktion> hardRestrictions = List.of(oneHardRestrictionThatDoesNotTrigger(),
        oneHardRestrictionThatDoesNotTrigger());
    deviceUnderTest.registerHardCriteria(new HashSet<>(hardRestrictions));

    deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L), getRandomPlannedPruefung(1L));

    verify(hardRestrictions.get(0), times(1)).wouldBeHardConflictAt(any(LocalDateTime.class),
        any(Planungseinheit.class));
    verify(hardRestrictions.get(1), times(1)).wouldBeHardConflictAt(any(LocalDateTime.class),
        any(Planungseinheit.class));
  }

  @Test
  void wouldBeHardConflictAt_conflictIsIndicated() throws NoPruefungsPeriodeDefinedException {
    List<HarteRestriktion> hardRestrictions = List.of(oneHardRestrictionThatDoesNotTrigger(),
        oneHardRestrictionFailingWouldBeHardConflictAt(), oneHardRestrictionThatDoesNotTrigger());
    deviceUnderTest.registerHardCriteria(new HashSet<>(hardRestrictions));

    assertThat(deviceUnderTest.wouldBeHardConflictAt(getRandomTime(1L),
        getRandomPlannedPruefung(1L))).isTrue();
  }

  private HarteRestriktion oneHardRestrictionFailingWouldBeHardConflictAt()
      throws NoPruefungsPeriodeDefinedException {
    HarteRestriktion restriktion = mock(HarteRestriktion.class);
    when(restriktion.wouldBeHardConflictAt(any(), any())).thenReturn(true);
    return restriktion;
  }

}