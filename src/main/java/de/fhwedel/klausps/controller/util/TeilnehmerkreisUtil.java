package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Map;


public class TeilnehmerkreisUtil {

  private TeilnehmerkreisUtil() {

  }

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
