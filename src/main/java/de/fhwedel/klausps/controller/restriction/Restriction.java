package de.fhwedel.klausps.controller.restriction;

import java.time.Duration;

public abstract class Restriction {

  protected static final Duration DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN = Duration.ofMinutes(30);

}
