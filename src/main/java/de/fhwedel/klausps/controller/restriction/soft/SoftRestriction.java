package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.restriction.Restriction;
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

/**
 * This class represents a general soft restriction. It contains some basic functionality necessary
 * for any soft restriction.
 */
public abstract class SoftRestriction extends Restriction {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoftRestriction.class);

  /**
   * the DataAccessService to communicate with the underlying data model
   */
  protected final DataAccessService dataAccessService;

  /**
   * the criteria of the specific restriction, needed for the analysis
   */
  protected final WeichesKriterium kriterium;

  /**
   * Constructs a SoftRestriction
   *
   * @param dataAccessService the {@link DataAccessService} in use
   * @param kriterium         the {@link WeichesKriterium soft criteria} of the specific
   *                          restriction
   */
  protected SoftRestriction(DataAccessService dataAccessService, WeichesKriterium kriterium) {
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
  protected abstract Optional<SoftRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException;


  /**
   * Evaluates for a {@link Pruefung} in which way it violates a restriction.<br> Entry Point for
   * all evaluations performed by the RestrictionService. This method ensures that only planned
   * Pruefungen violate restrictions.
   *
   * @param pruefung The pruefung for which to check for violations of a restriction.
   * @return Either an {@link Optional} containing a {@link KriteriumsAnalyse} for the violated
   * restriction, or an empty Optional in case the Restriction was not violated.
   */
  public Optional<SoftRestrictionAnalysis> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.trace("Checking restriction {}.", this.kriterium);
    noNullParameters(pruefung);
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    return evaluateRestriction(pruefung);
  }

  /**
   * Template method for building a SoftRestrictionAnalysis. The approach for the calculation of
   * scoring and teilnehmerkreisschaetzung may differ by restriction.
   *
   * @param pruefung           the pruefung to build the analysis for
   * @param affectedPruefungen the affected pruefungen
   * @return a SoftRestrictionAnalysis for the specific restriction
   */
  protected SoftRestrictionAnalysis buildAnalysis(Pruefung pruefung,
      Set<Pruefung> affectedPruefungen) {
    int scoring;
    Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise = new HashMap<>();
    for (Pruefung affected : affectedPruefungen) {
      compareAndPutBiggerSchaetzung(affectedTeilnehmerkreise,
          getRelevantSchaetzungen(pruefung, affected));
    }
    scoring = addDeltaScoring(affectedPruefungen);

    return new SoftRestrictionAnalysis(affectedPruefungen, this.kriterium,
        affectedTeilnehmerkreise.keySet(), getAffectedStudents(affectedTeilnehmerkreise), scoring);
  }

  /**
   * calculates the amount of affected students
   *
   * @param affectedTeilnehmerkreise the Teilnehmerkreise involved
   * @return the amount of affected students
   */
  protected int getAffectedStudents(Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise) {
    int result = 0;
    for (Integer schaetzung : affectedTeilnehmerkreise.values()) {
      result += schaetzung;
    }
    return result;
  }

  /**
   * default approach to calculate the scoring for a restriction violation
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
    return (obj instanceof SoftRestriction weicheRestriktion)
        && weicheRestriktion.kriterium == this.kriterium;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataAccessService, kriterium);
  }
}
