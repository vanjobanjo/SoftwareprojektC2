package de.fhwedel.klausps.controller.structures.interval_tree;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.time.LocalDateTime;
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

}
