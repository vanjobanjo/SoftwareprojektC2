package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Blocktyp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Data tranfer Object representing {@link ReadOnlyBlock}.
 */
public class BlockDTO extends ReadOnlyBlock {

  /**
   * Create a new BlockDTO.
   *
   * @param name               The name of the Block.
   * @param termin             The time the Block is scheduled at.
   * @param dauer              The duration of the Block.
   * @param readOnlyPruefungen The pruefungen included in the block.
   * @param id                 The blocks id.
   * @param type               The {@link Blocktyp} of this block.
   */
  public BlockDTO(
      String name,
      LocalDateTime termin,
      Duration dauer,
      Set<ReadOnlyPruefung> readOnlyPruefungen,
      int id,
      Blocktyp type) {
    super(name, termin, dauer, readOnlyPruefungen, id, type);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "[", "]")
        .add(this.getName())
        .add(Integer.toString(this.getBlockId()))
        .toString();
  }
  
}
