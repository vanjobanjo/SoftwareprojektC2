package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods for {@link Planungseinheit Planungseinheiten}
 */
public class PlanungseinheitUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlanungseinheitUtil.class);

  /**
   * Private constructor, because utility classes with only static methods should not be
   * instantiated
   */
  private PlanungseinheitUtil() {
    // util should not be instantiated
  }

  /**
   * Takes a Collection of {@link Planungseinheit Planungseinheiten} and converts it to a Set of
   * {@link Pruefung Pruefungen}, if the Planungseinheit is a {@link Block} the method discards the
   * block and puts the contained Pruefungen in the result.
   *
   * @param planungseinheiten Planungseinheiten to convert, can contain Bloecke and Pruefungen
   * @return set of Pruefungen
   */
  public static Set<Pruefung> getAllPruefungen(Collection<Planungseinheit> planungseinheiten) {
    LOGGER.trace("Extracting Pruefungen from {}.", planungseinheiten);
    Set<Pruefung> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (planungseinheit.isBlock()) {
        result.addAll(planungseinheit.asBlock().getPruefungen());
      } else {
        result.add(planungseinheit.asPruefung());
      }
    }
    LOGGER.trace("Resulting Pruefungen are: {}.", result);
    return result;
  }

  /**
   * Compares two Sets of {@link PruefungWithScoring} by looking at the scoring and puts those
   * PruefungenWithScoring in the result for a changed scoring is detected.
   *
   * @param before the scoring of pruefungen before the operation
   * @param after  the scoring of Pruefungen after the operation
   * @return Pruefungen which changed scoring
   */
  public static Set<Pruefung> changedScoring(Set<PruefungWithScoring> before,
      Set<PruefungWithScoring> after) {

    return after.stream().filter(afterPruefung -> before.stream()
            // O(n^2)
            .noneMatch(beforePruefung -> beforePruefung.equals(afterPruefung)))
        .map(PruefungWithScoring::pruefung).collect(Collectors.toSet());
  }


}
