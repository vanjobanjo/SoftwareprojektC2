package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Map;
import java.util.Set;

/**
 * An analysis describing the result of checking hard restrictions ant meant for internal data
 * transfer.
 */
public class HardRestrictionAnalysis {

  /**
   * The pruefungen in conflict causing the hard restriction to be violated.
   */
  Set<Pruefung> conflictingPruefungen;

  /**
   * The criterion this analysis was constructed by.
   */
  HartesKriterium kriterium;

  /**
   * The participants affected by the restriction violation described by this analysis.
   */
  Map<Teilnehmerkreis, Integer> participants;

  /**
   * Create a new analysis.
   *
   * @param conflictingPruefungen The {@link Pruefung Pruefungen} being in conflict.
   * @param kriterium             The criterion by which the alanysis was created.
   * @param participants          The participants affected by the restriction violation.
   */
  public HardRestrictionAnalysis(
      Set<Pruefung> conflictingPruefungen,
      HartesKriterium kriterium, Map<Teilnehmerkreis, Integer> participants) {
    this.conflictingPruefungen = conflictingPruefungen;
    this.kriterium = kriterium;
    this.participants = participants;
  }

  /**
   * Get the {@link Pruefung Pruefungen} being in conflict.
   *
   * @return The Pruefungen being in conflict.
   */
  public Set<Pruefung> getConflictingPruefungen() {
    return this.conflictingPruefungen;
  }

  /**
   * Get the criterion this analysis was made for.
   *
   * @return The Pruefungen being in conflict.
   */
  public HartesKriterium getKriterium() {
    return this.kriterium;
  }

  /**
   * Get the affected participants.
   *
   * @return The affected participants.
   */
  public Map<Teilnehmerkreis, Integer> getParticipants() {
    return participants;
  }
}
