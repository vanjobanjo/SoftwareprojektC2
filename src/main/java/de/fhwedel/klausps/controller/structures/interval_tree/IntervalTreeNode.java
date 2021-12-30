package de.fhwedel.klausps.controller.structures.interval_tree;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
}
