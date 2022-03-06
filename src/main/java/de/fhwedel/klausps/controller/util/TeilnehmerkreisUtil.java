package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Map;

/**
 * This class is used for utility functionality concerning Teilnehmerkreise.
 */
public class TeilnehmerkreisUtil {

  /**
   * A utility class providing only static methods, should not be instantiated
   */
  private TeilnehmerkreisUtil() {

  }

  /**
   * Different Pruefungen can have the same Teilnehmerkreis but with different schaetzungen. When
   * collecting Teilnehmerkreisschaetzungen this needs to be considered and consolidated. This
   * method adds all Teilnehmerkreise from a Map and puts for duplicates the bigger schaetzung in
   * the result map <code>toAdd</code>.
   *
   * @param collectedTeilnehmerkreise the collected Teilnehmerkreisschaetzungen with duplicates
   * @param toAdd                     the consolidated Teilnehmerkreisschaetzungen
   */
  public static void compareAndPutBiggerSchaetzung(
      Map<Teilnehmerkreis, Integer> collectedTeilnehmerkreise,
      Map<Teilnehmerkreis, Integer> toAdd) {
    for (Map.Entry<Teilnehmerkreis, Integer> schaetzung : toAdd.entrySet()) {
      Integer foundSchaetzung = collectedTeilnehmerkreise.getOrDefault(schaetzung.getKey(), null);
      Integer newSchaetzung = schaetzung.getValue();
      // look if Teilnehmerkreis is already in toAdd
      if (foundSchaetzung == null) {
        collectedTeilnehmerkreise.put(schaetzung.getKey(), newSchaetzung);
        // if already in toAdd, exchange if new Teilnehmerkreis has a bigger schaetzung
      } else if (foundSchaetzung < newSchaetzung) {
        collectedTeilnehmerkreise.replace(schaetzung.getKey(), foundSchaetzung, newSchaetzung);
      }
    }
  }
}
