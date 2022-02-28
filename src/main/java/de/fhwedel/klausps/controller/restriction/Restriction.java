package de.fhwedel.klausps.controller.restriction;

import java.time.Duration;

/**
 * This class represents a Restriction. A restriction may be a hard restriction, which blocks the
 * action tried by the user or a soft restriction which only affects the scoring of a Pruefung.
 */
public abstract class Restriction {
  /**
   * the default buffer time in between Planungseinheiten.
   */
  protected static final Duration DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN = Duration.ofMinutes(30);

}
