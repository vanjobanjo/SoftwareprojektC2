package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public class BlockDTO extends ReadOnlyBlock {

  public BlockDTO(
      String name,
      LocalDateTime termin,
      Duration dauer,
      boolean geplant,
      Set<ReadOnlyPruefung> readOnlyPruefungen) {
    super(name, termin, dauer, geplant, readOnlyPruefungen);
  }

  public BlockDTO(String name, Set<ReadOnlyPruefung> pruefungen) {
    super(name, null, Duration.ZERO, false, pruefungen);
  }
}
