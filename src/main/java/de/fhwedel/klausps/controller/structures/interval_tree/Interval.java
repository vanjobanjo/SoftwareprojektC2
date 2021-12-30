package de.fhwedel.klausps.controller.structures.interval_tree;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public record Interval(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {

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

}
