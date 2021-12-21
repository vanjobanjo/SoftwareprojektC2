package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <ul>
 * <li>Pruefung &rarr; ReadOnlyPruefung</li>
 * <li>Block &rarr; ReadOnlyBlock</li>
 * <li>Collection&lt;Pruefung&gt; &rarr; Collection&lt;ReadOnlyPruefung&gt;</li>
 * <li>Collection&lt;Block&gt; &rarr; Collection&lt;ReadOnlyBlock&gt;</li>
 * </ul>
 */
public class Converter {

  private Converter() {
  }
  // todo add scoring to pruefung

  public static ReadOnlyBlock convertToROBlock(Block block) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>(
        convertToROPruefungCollection(block.getPruefungen()));

    return new BlockDTO(block.getName(),
        block.getStartzeitpunkt(),
        block.getDauer(),
        pruefungen,
        block.getId(),
        block.getTyp());
  }

  public static ReadOnlyPruefung convertToReadOnlyPruefung(Pruefung pruefung) {
    return new PruefungDTOBuilder(pruefung).build();
  }

  public static ReadOnlyPlanungseinheit convertToReadOnlyPlanungseinheit(
      Planungseinheit planungseinheit) {
    if (planungseinheit.isBlock()) {
      return convertToROBlock(planungseinheit.asBlock());
    } else {
      return convertToReadOnlyPruefung(planungseinheit.asPruefung());
    }
  }


  public static Collection<ReadOnlyPruefung> convertToROPruefungCollection(
      Collection<Pruefung> collection) {
    Collection<ReadOnlyPruefung> result = new HashSet<>();
    for (Pruefung pruefung : collection) {
      result.add(convertToReadOnlyPruefung(pruefung));
    }
    return result;
  }

  public static Collection<ReadOnlyBlock> convertToROBlockCollection(
      Collection<Block> collection) {
    Collection<ReadOnlyBlock> result = new HashSet<>();
    for (Block block : collection) {
      result.add(convertToROBlock(block));
    }
    return result;
  }

  public static Collection<ReadOnlyPlanungseinheit> convertToROPlanungseinheitCollection(
      Collection<Planungseinheit> collection) {
    Collection<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : collection) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }
}
