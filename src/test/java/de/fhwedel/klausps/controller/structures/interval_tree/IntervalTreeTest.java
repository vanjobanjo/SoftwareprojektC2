package de.fhwedel.klausps.controller.structures.interval_tree;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IntervalTreeTest {

  @Test
  void emptyAfterCreation() {
    IntervalTree deviceUnderTest = new IntervalTree();
    assertThat(deviceUnderTest.getSize()).isZero();
  }

  @Test
  void addingFirstElement_sizeGrows() {
    IntervalTree deviceUnderTest = new IntervalTree();
    deviceUnderTest.add(interval());
    assertThat(deviceUnderTest.getSize()).isOne();
  }

  private Interval interval() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 13, 0);

    return new Interval(start, end);
  }

  @Test
  void getAllPlanungseinheitenThatOverlapAtLeastWith_find() {
    IntervalTree deviceUnderTest = new IntervalTree();
    Planungseinheit containedPlanungseinheit = getRandomPlannedPruefung(23L);
    deviceUnderTest.add(interval(), containedPlanungseinheit);
    assertThat(deviceUnderTest.getAllPlanungseinheitenThatOverlapAtLeastWith(0)).containsExactly(
        containedPlanungseinheit);
  }

  @Test
  void getAllPlanungseinheitenThatOverlapAtLeastWith_doNotFind() {
    IntervalTree deviceUnderTest = new IntervalTree();
    Planungseinheit containedPlanungseinheit = getRandomPlannedPruefung(23L);
    deviceUnderTest.add(interval(), containedPlanungseinheit);
    assertThat(deviceUnderTest.getAllPlanungseinheitenThatOverlapAtLeastWith(1)).isEmpty();
  }

  @Test
  void getAllPlanungseinheitenThatOverlapAtLeastWith_noEntries() {
    IntervalTree deviceUnderTest = new IntervalTree();
    assertThat(deviceUnderTest.getAllPlanungseinheitenThatOverlapAtLeastWith(0)).isEmpty();
  }

  @Test
  void addTwoElements_sizeGrows() {
    IntervalTree deviceUnderTest = new IntervalTree();
    deviceUnderTest.add(interval(1L), getRandomPlannedPruefung(1L));
    deviceUnderTest.add(interval(2L), getRandomPlannedPruefung(2L));
    assertThat(deviceUnderTest.getSize()).isEqualTo(2);
  }

  private Interval interval(long seed) {
    Random random = new Random(seed);
    LocalDateTime start = LocalDateTime.of(2021, 12, random.nextInt(1, 28), random.nextInt(22), 0);
    LocalDateTime end = start.plusMinutes(random.nextLong(180));

    return new Interval(start, end);
  }

  @Test
  void addTwoElements_canBeRetrieved() {
    IntervalTree deviceUnderTest = new IntervalTree();
    List<Planungseinheit> planungseinheiten = List.of(getRandomPlannedPruefung(1L),
        getRandomPlannedPruefung(2L));
    deviceUnderTest.add(interval(1L), planungseinheiten.get(0));
    deviceUnderTest.add(interval(2L), planungseinheiten.get(1));
    assertThat(deviceUnderTest.getAllPlanungseinheitenThatOverlapAtLeastWith(
        0)).containsExactlyInAnyOrderElementsOf(planungseinheiten);
  }

}
