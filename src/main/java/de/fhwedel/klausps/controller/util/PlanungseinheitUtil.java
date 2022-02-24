package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanungseinheitUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlanungseinheitUtil.class);

  private PlanungseinheitUtil() {
    // util should not be instantiated
  }

  /**
   * Extracts the pruefungen of a possible block.
   * @param planungseinheiten bock or pruefung
   * @return set of pruefungen
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
   * Determines the changed scoring of two different sets
   *
   * @param before the scoring of pruefungen before the operation
   * @param after  the scoring of Pruefungen after the operation
   * @return Pruefungen which scoring has changed
   */
  public static Set<Pruefung> changedScoring(Set<PruefungWithScoring> before,
      Set<PruefungWithScoring> after) {

    return after.stream().filter(afterPruefung -> before.stream()
            .noneMatch(beforePruefung -> beforePruefung.equals(afterPruefung)))
        .map(PruefungWithScoring::pruefung).collect(Collectors.toSet());
  }


}
