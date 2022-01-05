package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public abstract class WeicheRestriktion {

  protected final DataAccessService dataAccessService;
  protected final WeichesKriterium kriterium;

  protected WeicheRestriktion(
      DataAccessService dataAccessService,
      WeichesKriterium kriterium) {
    this.dataAccessService = dataAccessService;
    this.kriterium = kriterium;
  }

  /**
   * Evaluates for a {@link Pruefung} in which way it violates a restriction.
   *
   * @param pruefung The pruefung for which to check for violations of a restriction.
   * @return Either an {@link Optional} containing a {@link KriteriumsAnalyse} for the violated
   * restriction, or an empty Optional in case the Restriction was not violated.
   */
  public abstract Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung);


  protected WeichesKriteriumAnalyse buildAnalysis(Pruefung pruefung,
      Set<Pruefung> affectedPruefungen) {
    int scoring;
    Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise = new HashMap<>();
    for (Pruefung affected : affectedPruefungen) {
      addTeilnehmerkreis(affectedTeilnehmerkreise, getRelevantSchaetzungen(pruefung, affected));
    }
    scoring = addDeltaScoring(affectedPruefungen);

    return new WeichesKriteriumAnalyse(affectedPruefungen, this.kriterium,
        affectedTeilnehmerkreise.keySet(), getAffectedStudents(affectedTeilnehmerkreise), scoring);
  }

  protected void addTeilnehmerkreis(
      Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise,
      Map<Teilnehmerkreis, Integer> teilnehmerkreiseToAdd) {
    for (Map.Entry<Teilnehmerkreis, Integer> schaetzung : teilnehmerkreiseToAdd.entrySet()) {
      Integer foundSchaetzung = affectedTeilnehmerkreise.getOrDefault(schaetzung.getKey(), null);
      Integer newSchaetzung = schaetzung.getValue();
      if (foundSchaetzung == null) {
        affectedTeilnehmerkreise.put(schaetzung.getKey(), newSchaetzung);
      } else if (foundSchaetzung < newSchaetzung) {
        affectedTeilnehmerkreise.replace(schaetzung.getKey(), foundSchaetzung, newSchaetzung);
      }
    }
  }


  protected int getAffectedStudents(Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise) {
    int result = 0;
    for (Integer schaetzung : affectedTeilnehmerkreise.values()) {
      result += schaetzung;
    }
    return result;
  }

  /**
   * default approach
   *
   * @param affectedPruefungen pruefungen that violate restriction
   * @return the scoring
   */
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    return affectedPruefungen.size() * this.kriterium.getWert();
  }


  /**
   * default approach <br>
   * collects all relevant Teilnehmerkreisschätzungen <br>
   * @param pruefung for which the restriction gets tested
   * @param affected an affected pruefung
   * @return the relevant Teilnehmerkreisschätzungen
   */
  protected Map<Teilnehmerkreis, Integer> getRelevantSchaetzungen(Pruefung pruefung,
      Pruefung affected) {
    return affected.getSchaetzungen();
  }

}
