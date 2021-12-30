package de.fhwedel.klausps.controller.structures.interval_tree;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

/**
 * A time interval defined by a start and endpoint.
 */
public record Interval(@NotNull LocalDateTime start, @NotNull LocalDateTime end) implements
    Comparable<Interval> {

  /**
   * Instantiate an interval.
   *
   * @param start The start of the interval (inclusive).
   * @param end   The end of the interval (exclusive)
   */
  public Interval(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException();
    }
    if (!start.isBefore(end)) {
      throw new IllegalArgumentException("Only positive time spans permitted.");
    }
    this.start = start;
    this.end = end;
  }

  @Override
  public int compareTo(@NotNull Interval other) {
    int result = this.start.compareTo(other.start);
    if (result == 0) {
      result = this.end.compareTo(other.end);
    }
    return result;
  }
}
