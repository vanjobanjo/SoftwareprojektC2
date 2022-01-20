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

  public static Set<Pruefung> getAllPruefungen(Collection<Planungseinheit> planungseinheiten) {
    LOGGER.debug("Converting {} to Pruefungen only.", planungseinheiten);
    Set<Pruefung> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (planungseinheit.isBlock()) {
        result.addAll(planungseinheit.asBlock().getPruefungen());
      } else {
        result.add(planungseinheit.asPruefung());
      }
    }
    LOGGER.debug("Resulting Planungseinheiten are: {}.", result);
    return result;
  }

  /**
   * Determines the changed scoring of two different sets
   * @param before the scoring of pruefungen before the operation
   * @param after the scoring of pruefunge after the operation
   * @return Prueufungen which scoring has changed
   */
  public static Set<Pruefung> changedScoring(Set<PruefungScoringWrapper> before,
      Set<PruefungScoringWrapper> after) {

    return after.stream().filter(afterPruefung -> before.stream()
            .noneMatch(beforePruefung -> beforePruefung.equals(afterPruefung)))
        .map(PruefungScoringWrapper::getPruefung).collect(Collectors.toSet());
  }


}
