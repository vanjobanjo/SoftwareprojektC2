package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;

import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.restriction.hard.HardRestriction;
import de.fhwedel.klausps.controller.restriction.soft.SoftRestriction;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service responsible for operations regarding restrictions.
 */
public class RestrictionService {

  /**
   * Logger for logging interactions with this service.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionService.class);

  /**
   * The hard restrictions this service should mind.
   */
  private final Set<HardRestriction> hardRestrictions = new HashSet<>();

  /**
   * The soft restrictions this service should mind.
   */
  private final Set<SoftRestriction> softRestrictions = new HashSet<>();

  /**
   * Create a RestrictionService retrieving its restrictions from a factory.
   */
  RestrictionService() {
    RestrictionFactory factory = new RestrictionFactory();
    factory.createRestrictions(this);
  }

  /**
   * Create a RestrictionService retrieving its restrictions from a defined factory.
   *
   * @param restrictionFactory The factory to get the restrictions from.
   */
  RestrictionService(RestrictionFactory restrictionFactory) {
    restrictionFactory.createRestrictions(this);
  }

  /**
   * Register soft criteria for usage.
   *
   * @param restrictions The restrictions to register.
   */
  void registerSoftCriteria(Set<SoftRestriction> restrictions) {
    softRestrictions.addAll(restrictions);
  }

  /**
   * Register hard criteria for usage.
   *
   * @param restrictions The restrictions to register.
   */
  void registerHardCriteria(Set<HardRestriction> restrictions) {
    hardRestrictions.addAll(restrictions);
  }

  /**
   * Get all the affected pruefungen by the passed block.
   *
   * @param block The block to find affected pruefungen for.
   * @return The pruefungen affected by the block.
   * @throws NoPruefungsPeriodeDefinedException In case no period is defined.
   */
  public Set<Pruefung> getPruefungenAffectedByAnyBlock(Block block)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    if (Objects.equals(block.getTyp(), SEQUENTIAL)) {
      result.addAll(getPruefungenAffectedBySequentialBlock(block));
    } else {
      for (Pruefung pruefung : block.getPruefungen()) {
        result.addAll(getPruefungenAffectedBy(pruefung));
      }
    }
    return result;
  }

  /**
   * Checks which planned Pruefungen a sequential block affects.
   *
   * @param block The block to check for.
   * @return All affected Pruefungen by the block.
   * @throws NoPruefungsPeriodeDefinedException In case no period is defined.
   */
  private Set<Pruefung> getPruefungenAffectedBySequentialBlock(Block block)
      throws NoPruefungsPeriodeDefinedException {
    assert block.getTyp().equals(SEQUENTIAL);
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      Duration prevDuration = pruefung.getDauer();
      pruefung.setDauer(block.getDauer());
      result.addAll(getPruefungenAffectedBy(pruefung));
      pruefung.setDauer(prevDuration);
    }
    return result;
  }

  /**
   * Checks all the soft restrictions of the passed pruefung, and collects all Pruefungen causing
   * these.
   *
   * @param pruefung The pruefung to check.
   * @return Set of affected pruefungen.
   * @throws NoPruefungsPeriodeDefinedException In case period is defined.
   */
  public Set<Pruefung> getPruefungenAffectedBy(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (SoftRestrictionAnalysis analysis : checkWeicheKriterien(pruefung)) {
      result.addAll(analysis.getAffectedPruefungen());
    }
    result.add(pruefung);
    return result;
  }

  /**
   * Evaluates all hard restriction for the passed pruefung.
   *
   * @param pruefungenToCheck The pruefungen to check.
   * @return The analysis resulting of checking all hard criteria.
   * @throws NoPruefungsPeriodeDefinedException In case no period is currently defined.
   */
  public List<HardRestrictionAnalysis> checkHardRestrictions(Set<Pruefung> pruefungenToCheck)
      throws NoPruefungsPeriodeDefinedException {
    List<HardRestrictionAnalysis> result = new LinkedList<>();
    for (Pruefung currentToCheck : pruefungenToCheck) {
      result.addAll(checkHardRestrictions(currentToCheck));
    }
    return result;
  }

  /**
   * Checks all hard restrictions for passed pruefung.
   *
   * @param pruefung The pruefung to check restrictions.
   * @return HartesKriteriumAnalysen The analysis resulting of checking all hard criteria.
   */
  public List<HardRestrictionAnalysis> checkHardRestrictions(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    List<HardRestrictionAnalysis> result = new LinkedList<>();
    for (HardRestriction hard : hardRestrictions) {
      hard.evaluate(pruefung).ifPresent(result::add);
    }
    return result;
  }

  /**
   * Get the scoring of the pruefung, by checking the analysen and accumulate the delta scoring.
   *
   * @param pruefung Pruefung to check the scoring for
   * @return the scoring of the passed pruefung
   * @throws NoPruefungsPeriodeDefinedException In case there is no Periode defined.
   */
  public int getScoringOfPruefung(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    if (!pruefung.isGeplant()) {
      return 0;
    }
    return checkWeicheKriterien(pruefung).stream()
        .reduce(0, (scoring, analyse) -> scoring + analyse.getScoring(), Integer::sum);
  }

  /**
   * Checks all soft restrictions for a pruefung.
   *
   * @param pruefung The pruefung for which to check the criteria.
   * @return WeichesKriteriumAnalysen The analysis resulting of checking all soft criteria.
   */
  public List<SoftRestrictionAnalysis> checkWeicheKriterien(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    List<SoftRestrictionAnalysis> result = new LinkedList<>();
    for (SoftRestriction soft : softRestrictions) {
      soft.evaluate(pruefung).ifPresent(result::add);
    }
    return result;
  }

  /**
   * Get all planned {@link Pruefung Pruefungen} that could potentially violate a hard restriction
   * in conjunction with a handed {@link Planungseinheit} if planned at any time.
   *
   * @param planungseinheit The planungseinheit to get the potential violations for.
   * @return Sll planned pruefungen that could potentially violate a hard restriction in conjunction
   * with a handed planungseinheit if planned at any time.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Periode defined.
   */
  @NotNull
  public Set<Pruefung> getPruefungenPotentiallyInHardConflictWith(Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(planungseinheit);
    Set<Pruefung> potentiallyConflictingPruefungen = new HashSet<>();
    for (HardRestriction hardRestriction : hardRestrictions) {
      potentiallyConflictingPruefungen.addAll(
          hardRestriction.getAllPotentialConflictingPruefungenWith(planungseinheit));
    }
    LOGGER.trace("Found {} to be in conflict with {}.",
        potentiallyConflictingPruefungen, planungseinheit);
    return potentiallyConflictingPruefungen;
  }

  /**
   * Check whether a {@link Planungseinheit} would be in conflict with a hard restriction if it was
   * planned at a certain time.
   *
   * @param startTime       The hypothetical start time to check at.
   * @param planungseinheit The planungseinheit to check for.
   * @return True in case the planungseinheit would be in conflict with a hard restriction if
   * planned at the designated time, otherwise False.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Periode defined.
   */
  public boolean wouldBeHardConflictIfStartedAt(LocalDateTime startTime,
      Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(startTime, planungseinheit);
    boolean isConflicted = false;
    Iterator<HardRestriction> restriktionIterator = hardRestrictions.iterator();
    while (restriktionIterator.hasNext() && !isConflicted) {
      isConflicted = restriktionIterator.next().wouldBeHardConflictAt(startTime, planungseinheit);
    }
    return isConflicted;
  }
}
