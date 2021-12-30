package de.fhwedel.klausps.controller.structures.interval_tree;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntervalTest {

  @DisplayName("End before start")
  @Test
  void startMustBeBeforeEndTest_EndBeforeStart() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 13, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 11, 0);

    Assertions.assertThrows(IllegalArgumentException.class, () -> new Interval(start, end));
  }

  @DisplayName("Start & end at same time")
  @Test
  void startMustBeBeforeEndTest_StartAndEndSimultaneous() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 13, 0);
    LocalDateTime end = LocalDateTime.from(start);

    Assertions.assertThrows(IllegalArgumentException.class, () -> new Interval(start, end));
  }

  @DisplayName("Getting the start corresponds with the set value")
  @Test
  void getStart_returnsSetStart() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 13, 0);
    LocalDateTime expected = LocalDateTime.of(start.getYear(), start.getMonth(),
        start.getDayOfMonth(), start.getHour(), start.getMinute());

    Interval deviceUnderTest = new Interval(start, end);
    assertThat(deviceUnderTest.start()).isEqualTo(expected);
  }

  @DisplayName("Getting the end corresponds with the set value")
  @Test
  void getEnd_returnsSetEnd() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 13, 0);
    LocalDateTime expected = LocalDateTime.of(end.getYear(), end.getMonth(), end.getDayOfMonth(),
        end.getHour(), end.getMinute());

    Interval deviceUnderTest = new Interval(start, end);
    assertThat(deviceUnderTest.end()).isEqualTo(expected);
  }

  @DisplayName("Start must not be null")
  @Test
  void startMustNotBeNull() {
    LocalDateTime start = null;
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 11, 0);

    Assertions.assertThrows(IllegalArgumentException.class, () -> new Interval(start, end));
  }

  @DisplayName("End must not be null")
  @Test
  void endMustNotBeNull() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime end = null;

    Assertions.assertThrows(IllegalArgumentException.class, () -> new Interval(start, end));
  }

  @DisplayName("Comparison by start time")
  @Test
  void compareTo_compareByStart() {
    LocalDateTime startFirst = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime startSecond = LocalDateTime.of(2021, 12, 31, 11, 1);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 13, 0);

    Interval firstInterval = new Interval(startFirst, end);
    Interval secondInterval = new Interval(startSecond, end);

    assertThat(firstInterval.compareTo(secondInterval)).isNegative();
    assertThat(secondInterval.compareTo(firstInterval)).isPositive();
  }

  @DisplayName("Comparison by end time when start is equal")
  @Test
  void compareTo_compareByEnd() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime firstEnd = LocalDateTime.of(2021, 12, 31, 13, 0);
    LocalDateTime secondEnd = firstEnd.plusMinutes(1);

    Interval firstInterval = new Interval(start, firstEnd);
    Interval secondInterval = new Interval(start, secondEnd);

    assertThat(firstInterval.compareTo(secondInterval)).isNegative();
    assertThat(secondInterval.compareTo(firstInterval)).isPositive();
  }

  @DisplayName("Equal intervals are equal")
  @Test
  void compareTo_sameInterval() {
    LocalDateTime start = LocalDateTime.of(2021, 12, 31, 11, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 13, 0);

    Interval firstInterval = new Interval(start, end);

    assertThat(firstInterval.compareTo(firstInterval)).isZero();
  }

}
