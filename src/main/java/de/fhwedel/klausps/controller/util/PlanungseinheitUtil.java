package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PlanungseinheitUtil {

  private PlanungseinheitUtil() {
    // util should not be instantiated
  }

  public static Set<Pruefung> getAllPruefungen(Collection<Planungseinheit> planungseinheiten) {
    Set<Pruefung> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (planungseinheit.isBlock()) {
        result.addAll(planungseinheit.asBlock().getPruefungen());
      } else {
        result.add(planungseinheit.asPruefung());
      }
    }
    return result;
  }


}
