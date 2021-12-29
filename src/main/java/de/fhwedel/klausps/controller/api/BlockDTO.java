package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Blocktyp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public class BlockDTO extends ReadOnlyBlock {

  public BlockDTO(
      String name,
      LocalDateTime termin,
      Duration dauer,
      Set<ReadOnlyPruefung> readOnlyPruefungen,
      int id,
      Blocktyp type) {
    super(name, termin, dauer, readOnlyPruefungen, id, type);
  }

}
