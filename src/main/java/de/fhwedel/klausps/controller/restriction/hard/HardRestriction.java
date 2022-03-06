package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.Restriction;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This class represents a general hard restriction. It contains some basic functionality necessary
 * for any hard restriction.
 */
public abstract class HardRestriction extends Restriction {

  /**
   * The criteria describing the specific type of restriction.
   */
  protected final HartesKriterium kriterium;

  /**
   * The service to access the data layer.
   */
  protected DataAccessService dataAccessService;

  /**
   * Create a HardRestriction.
   *
   * @param dataAccessService The service to access the data layer.
   * @param kriterium         The criteria describing the specific type of restriction.
   */
  HardRestriction(DataAccessService dataAccessService, HartesKriterium kriterium) {
    this.kriterium = kriterium;
    this.dataAccessService = dataAccessService;
  }

  /**
   * Evaluate the restriction for a {@link Pruefung}.
   *
   * @param pruefung The pruefung to evaluate for.
   * @return Either an empty optional if the restriction is not violated, otherwise an optional
   * containing a corresponding analysis.
   * @throws NoPruefungsPeriodeDefinedException In case no {@link Pruefungsperiode} is set.
   */
  public Optional<HardRestrictionAnalysis> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    return evaluateRestriction(pruefung);
  }

  /**
   * Evaluates the restriction for a specific {@link Pruefung}.
   *
   * @param pruefung The Pruefung to check the restriction for.
   * @return Either an empty optional if the restriction is not violated, otherwise an optional
   * containing a corresponding analysis.
   */
  protected abstract Optional<HardRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException;

  /**
   * Get all {@link Planungseinheit Planungseinheiten} that are in conflict with a handed {@link
   * Pruefung}.
   *
   * @param planungseinheit The Planungseinheit to check for conflicts for.
   * @return The Pruefungen in conflict with the handed one.
   */
  public abstract Set<Pruefung> getAllPotentialConflictingPruefungenWith(
      Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException;

  /**
   * Test whether a {@link Planungseinheit} would cause a violation of the restriction if planned at
   * a specific time.
   *
   * @param startTime       The time to check the Planungseinehit for.
   * @param planungseinheit The Planungseinheit to check for.
   * @return True in case the Planungseinheit would cause an exception at the specified time,
   * otherwise False.
   * @throws NoPruefungsPeriodeDefinedException In case no {@link Pruefungsperiode} is set.
   */
  public abstract boolean wouldBeHardConflictAt(LocalDateTime startTime,
      Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException;

  @Override
  public int hashCode() {
    return Objects.hash(kriterium, dataAccessService);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof HardRestriction harteRestriktion)
        && harteRestriktion.kriterium == this.kriterium;
  }

}
