package de.fhwedel.klausps.controller.structures.interval_tree;

import static de.fhwedel.klausps.controller.structures.interval_tree.IntervalTreeNode.addTo;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IntervalTreeNodeTest {

  @Test
  void add_noChildren_addLeft() {
    IntervalTreeNode deviceUnderTest = new IntervalTreeNode(interval(1L));
    Interval toAdd = new Interval(deviceUnderTest.getInterval().start().minusHours(5),
        deviceUnderTest.getInterval().end().minusHours(5));
    addTo(deviceUnderTest, toAdd);
    assertThat(deviceUnderTest.getLeft()).isPresent();
    assertThat(deviceUnderTest.getRight()).isEmpty();
  }

  private Interval interval(long seed) {
    Random random = new Random(seed);
    LocalDateTime start = LocalDateTime.of(2021, 12, random.nextInt(1, 28), random.nextInt(22), 0);
    LocalDateTime end = start.plusMinutes(random.nextLong(180));

    return new Interval(start, end);
  }

  @Test
  void add_noChildren_addRight() {
    IntervalTreeNode deviceUnderTest = new IntervalTreeNode(interval(1L));
    Interval toAdd = new Interval(deviceUnderTest.getInterval().start().plusHours(5),
        deviceUnderTest.getInterval().end().plusHours(5));
    addTo(deviceUnderTest, toAdd);
    assertThat(deviceUnderTest.getLeft()).isEmpty();
    assertThat(deviceUnderTest.getRight()).isPresent();
  }

  @Test
  void getPlanungseinheitenThat_onlyNodeSelf_getAllPruefungen() {
    Set<Planungseinheit> planungseinheiten = new HashSet<>(getRandomPruefungen(1L, 4));
    IntervalTreeNode deviceUnderTest = new IntervalTreeNode(interval(1L), planungseinheiten);
    assertThat(
        deviceUnderTest.getPlanungseinheitenThatFulfill(x -> true)).containsExactlyInAnyOrderElementsOf(
        planungseinheiten);
  }

  @Test
  void getPlanungseinheitenThat_checkOnLeftChild() {
    IntervalTreeNode leftChild = spy(new IntervalTreeNode(interval(2L)));
    IntervalTreeNode deviceUnderTest = spy(
        new IntervalTreeNode(interval(1L), interval(1L).end(), new HashSet<>(),
            leftChild, null));
    deviceUnderTest.getPlanungseinheitenThatFulfill(x -> true);
    verify(leftChild, times(1)).getPlanungseinheitenThatFulfill(any());
  }

  @Test
  void getPlanungseinheitenThat_checkOnRightChild() {
    IntervalTreeNode deviceUnderTest = spy(new IntervalTreeNode(interval(1L)));
    deviceUnderTest.getPlanungseinheitenThatFulfill(x -> true);
    verify(deviceUnderTest, times(1)).getPlanungseinheitenThatFulfill(any());
  }

}
