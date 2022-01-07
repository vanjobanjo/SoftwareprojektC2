package de.fhwedel.klausps.controller;

import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
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

// Anm.: Wo soll das hin? Ist util, aber nicht unbedingt von Planungseinheiten...
  public static void compareAndPutBiggerSchaetzung(
      Map<Teilnehmerkreis, Integer> collectedTeilnehmerkreise,
      Map<Teilnehmerkreis, Integer> toAdd) {
    for (Map.Entry<Teilnehmerkreis, Integer> schaetzung : toAdd.entrySet()) {
      Integer foundSchaetzung = collectedTeilnehmerkreise.getOrDefault(schaetzung.getKey(), null);
      Integer newSchaetzung = schaetzung.getValue();
      if (foundSchaetzung == null) {
        collectedTeilnehmerkreise.put(schaetzung.getKey(), newSchaetzung);
      } else if (foundSchaetzung < newSchaetzung) {
        collectedTeilnehmerkreise.replace(schaetzung.getKey(), foundSchaetzung, newSchaetzung);
      }
    }
  }

}
