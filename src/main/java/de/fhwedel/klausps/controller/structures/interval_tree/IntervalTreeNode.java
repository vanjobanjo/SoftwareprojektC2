package de.fhwedel.klausps.controller.structures.interval_tree;

import static java.util.Collections.emptySet;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * A node to an {@link IntervalTree}.
 */
class IntervalTreeNode {

  private final Interval interval;
  private final Set<Planungseinheit> planungseinheiten = new HashSet<>();
  private LocalDateTime max;
  private IntervalTreeNode left;
  private IntervalTreeNode right;

  IntervalTreeNode(@NotNull Interval interval, @NotNull Set<Planungseinheit> planungseinheiten) {
    this(interval, interval.end(), planungseinheiten, null, null);
  }

  IntervalTreeNode(@NotNull Interval interval, @NotNull LocalDateTime max,
      @NotNull Set<Planungseinheit> planungseinheiten, IntervalTreeNode left,
      IntervalTreeNode right) {
    this.interval = interval;
    this.max = max;
    this.planungseinheiten.addAll(planungseinheiten);
    this.left = left;
    this.right = right;
  }

  IntervalTreeNode(@NotNull Interval interval, @NotNull Planungseinheit... planungseinheiten) {
    this(interval, interval.end(), Set.of(planungseinheiten), null, null);
  }

  static IntervalTreeNode addTo(IntervalTreeNode node, Interval interval,
      Planungseinheit... planungseinheiten) {
    if (node == null) {
      // in case there is no node to add to, create new node
      return new IntervalTreeNode(interval, planungseinheiten);
    }
    if (interval.start().isBefore(node.interval.start())) {
      // in case the interval to add starts before the interval to add to, add as left child
      node.left = addTo(node.left, interval, planungseinheiten);
    } else {
      node.right = addTo(node.right, interval, planungseinheiten);
    }
    updateMaxWithNewIntervalFor(node, interval);
    return node;
  }

  private static void updateMaxWithNewIntervalFor(IntervalTreeNode node, Interval addedInterval) {
    if (addedInterval.end().isAfter(node.max)) {
      node.max = addedInterval.end();
    }
  }

  Interval getInterval() {
    return interval;
  }

  LocalDateTime getMax() {
    return max;
  }

  void setMax(LocalDateTime max) {
    this.max = max;
  }

  Set<Planungseinheit> getPlanungseinheiten() {
    return new HashSet<>(planungseinheiten);
  }

  Optional<IntervalTreeNode> getLeft() {
    return Optional.ofNullable(left);
  }

  void setLeft(IntervalTreeNode left) {
    this.left = left;
  }

  Optional<IntervalTreeNode> getRight() {
    return Optional.ofNullable(right);
  }

  void setRight(IntervalTreeNode right) {
    this.right = right;
  }

  void addPlanungseinheit(@NotNull Planungseinheit planungseinheit) {
    this.planungseinheiten.add(planungseinheit);
  }

  void removePlanungseinheit(@NotNull Planungseinheit planungseinheit) {
    this.planungseinheiten.remove(planungseinheit);
  }

  Set<Planungseinheit> getPlanungseinheitenThatFulfill(
      Predicate<Collection<Planungseinheit>> predicate) {
    Set<Planungseinheit> result = leftPlanungseinheitenThatFulFill(predicate);
    result.addAll(ownPlanungseinheitenThatFulfill(predicate));
    result.addAll(rightPlanungseinheitenThatFulFill(predicate));
    return result;
  }

  private Set<Planungseinheit> ownPlanungseinheitenThatFulfill(
      Predicate<Collection<Planungseinheit>> predicate) {
    if (predicate.test(this.planungseinheiten)) {
      return this.planungseinheiten;
    }
    return emptySet();
  }

  private Set<Planungseinheit> leftPlanungseinheitenThatFulFill(
      Predicate<Collection<Planungseinheit>> predicate) {
    if (getLeft().isPresent()) {
      return left.getPlanungseinheitenThatFulfill(predicate);
    }
    return new HashSet<>();
  }

  private Set<Planungseinheit> rightPlanungseinheitenThatFulFill(
      Predicate<Collection<Planungseinheit>> predicate) {
    if (getRight().isPresent()) {
      return right.getPlanungseinheitenThatFulfill(predicate);
    }
    return emptySet();
  }

}
