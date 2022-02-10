package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.restriction.Restriktion;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WeicheRestriktion extends Restriktion {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeicheRestriktion.class);

  protected final DataAccessService dataAccessService;
  protected final WeichesKriterium kriterium;

  protected WeicheRestriktion(DataAccessService dataAccessService, WeichesKriterium kriterium) {
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
  protected abstract Optional<WeichesKriteriumAnalyse> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException;


  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.trace("Checking restriction {}.", this.kriterium);
    noNullParameters(pruefung);
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    return evaluateRestriction(pruefung);
  }

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

    compareAndPutBiggerSchaetzung(affectedTeilnehmerkreise,
        teilnehmerkreiseToAdd);
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
   * default approach <br> collects all relevant Teilnehmerkreisschätzungen <br>
   *
   * @param pruefung for which the restriction gets tested
   * @param affected an affected pruefung
   * @return the relevant Teilnehmerkreisschätzungen
   */
  protected Map<Teilnehmerkreis, Integer> getRelevantSchaetzungen(@Nullable Pruefung pruefung,
      Pruefung affected) {
    return affected.getSchaetzungen();
  }


  @Override
  public boolean equals(Object obj) {
    return (obj instanceof WeicheRestriktion weicheRestriktion)
        && weicheRestriktion.kriterium == this.kriterium;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataAccessService, kriterium);
  }
}
