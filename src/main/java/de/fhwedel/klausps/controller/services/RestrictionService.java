package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.restriction.hard.HarteRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.WeicheRestriktion;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestrictionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionService.class);

  private final Set<HarteRestriktion> hardRestrictions;

  private final Set<WeicheRestriktion> softRestrictions;

  RestrictionService() {
    softRestrictions = new HashSet<>();
    hardRestrictions = new HashSet<>();
    RestrictionFactory factory = new RestrictionFactory();
    factory.createRestrictions(this);
  }

  RestrictionService(RestrictionFactory restrictionFactory) {
    softRestrictions = new HashSet<>();
    hardRestrictions = new HashSet<>();
    restrictionFactory.createRestrictions(this);
  }

  /**
   * Registers all soft criteria
   * @param restrictions set of restrictions
   */
  void registerSoftCriteria(Set<WeicheRestriktion> restrictions) {
    softRestrictions.addAll(restrictions);
  }

  /**
   * registers all hard criteria
   * @param restrictions set of hard restrictions
   */
  void registerHardCriteria(Set<HarteRestriktion> restrictions) {
    hardRestrictions.addAll(restrictions);
  }

  /**
   * Get all the affected pruefungen by the passed block
   * @param block passed block to check
   * @return Set of affected pruefungen
   * @throws NoPruefungsPeriodeDefinedException when no period is defiined
   */
  public Set<Pruefung> getPruefungenAffectedBy(Block block)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      result.addAll(getPruefungenAffectedBy(pruefung));
    }
    return result;
  }

  /**
   * Checks all the soft restrictions of the passed pruefung, and extracts the caused pruefungen
   * @param pruefung pruefung to check
   * @return Set of affected pruefungen
   * @throws NoPruefungsPeriodeDefinedException when no period is defined
   */
  public Set<Pruefung> getPruefungenAffectedBy(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (WeichesKriteriumAnalyse w : checkWeicheKriterien(pruefung)) {
      result.addAll(w.getCausingPruefungen());
    }
    result.remove(pruefung);
    return result;
  }

  /**
   * Checks all restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return WeichesKriteriumAnalysen
   */
  public List<WeichesKriteriumAnalyse> checkWeicheKriterien(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    List<WeichesKriteriumAnalyse> result = new LinkedList<>();
    for (WeicheRestriktion soft : softRestrictions) {
      soft.evaluate(pruefung).ifPresent(result::add);
    }
    return result;
  }

  /**
   * Evaluates als hard restriction for the passed pruefunge
   * @param pruefungenToCheck Set of pruefungen to check
   * @return List of HartesKriteriumsAnalysen
   * @throws NoPruefungsPeriodeDefinedException when no period is currently defined
   */
  public List<HartesKriteriumAnalyse> checkHarteKriterienAll(Set<Pruefung> pruefungenToCheck)
      throws NoPruefungsPeriodeDefinedException {
    List<HartesKriteriumAnalyse> result = new LinkedList<>();
    for (Pruefung currentToCheck : pruefungenToCheck) {
      result.addAll(checkHarteKriterien(currentToCheck));
    }
    return result;
  }

  /**
   * Checks all hard restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return HartesKriteriumAnalysen
   */
  public List<HartesKriteriumAnalyse> checkHarteKriterien(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    List<HartesKriteriumAnalyse> result = new LinkedList<>();
    for (HarteRestriktion hard : hardRestrictions) {
      hard.evaluate(pruefung).ifPresent(result::add);
    }
    return result;
  }

  /**
   * Get the scoring of the pruefung, by checking the analysen and accumulate the delta scoring.
   * @param pruefung Pruefung to check the scoring for
   * @return the scoring of the passed pruefung
   * @throws NoPruefungsPeriodeDefinedException when there is no Periode defined
   */
  public int getScoringOfPruefung(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    if (!pruefung.isGeplant()) {
      return 0;
    }
    return checkWeicheKriterien(pruefung).stream()
        .reduce(0, (scoring, analyse) -> scoring + analyse.getDeltaScoring(), Integer::sum);
  }

  @NotNull
  public Set<Pruefung> getPruefungenInHardConflictWith(Planungseinheit planungseinheitToCheckFor)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(planungseinheitToCheckFor);
    Set<Pruefung> potentiallyConflictingPruefungen = new HashSet<>();
    for (HarteRestriktion hardRestriction : hardRestrictions) {
      potentiallyConflictingPruefungen.addAll(
          hardRestriction.getAllPotentialConflictingPruefungenWith(planungseinheitToCheckFor));
    }
    LOGGER.trace("Found {} to be in conflict with {}.",
        potentiallyConflictingPruefungen, planungseinheitToCheckFor);
    return potentiallyConflictingPruefungen;
  }

  public boolean wouldBeHardConflictIfStartedAt(LocalDateTime startTime,
      Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(startTime, planungseinheit);
    boolean isConflicted = false;
    Iterator<HarteRestriktion> restriktionIterator = hardRestrictions.iterator();
    while (restriktionIterator.hasNext() && !isConflicted) {
      isConflicted = restriktionIterator.next().wouldBeHardConflictAt(startTime, planungseinheit);
    }
    return isConflicted;
  }
}
