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
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestrictionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionService.class);

  private final Set<HardRestriction> hardRestrictions;

  private final Set<SoftRestriction> softRestrictions;

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
   *
   * @param restrictions set of restrictions
   */
  void registerSoftCriteria(Set<SoftRestriction> restrictions) {
    softRestrictions.addAll(restrictions);
  }

  /**
   * registers all hard criteria
   *
   * @param restrictions set of hard restrictions
   */
  void registerHardCriteria(Set<HardRestriction> restrictions) {
    hardRestrictions.addAll(restrictions);
  }


  /**
   * Get all the affected pruefungen by the passed block.
   *
   * @param block passed block to check
   * @return Set of affected pruefungen
   * @throws NoPruefungsPeriodeDefinedException In case no period is defined.
   */
  public Set<Pruefung> getPruefungenAffectedByAnyBlock(Block block)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    if (block.getTyp().equals(SEQUENTIAL)) {
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
   * Checks all restriction for passed pruefung.
   *
   * @param pruefung Pruefung to check criteria
   * @return WeichesKriteriumAnalysen
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
   * Evaluates als hard restriction for the passed pruefunge
   *
   * @param pruefungenToCheck Set of pruefungen to check
   * @return List of HartesKriteriumsAnalysen
   * @throws NoPruefungsPeriodeDefinedException when no period is currently defined
   */
  public List<HardRestrictionAnalysis> checkHarteKriterienAll(Set<Pruefung> pruefungenToCheck)
      throws NoPruefungsPeriodeDefinedException {
    List<HardRestrictionAnalysis> result = new LinkedList<>();
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
  public List<HardRestrictionAnalysis> checkHarteKriterien(Pruefung pruefung)
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
    for (HardRestriction hardRestriction : hardRestrictions) {
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
    Iterator<HardRestriction> restriktionIterator = hardRestrictions.iterator();
    while (restriktionIterator.hasNext() && !isConflicted) {
      isConflicted = restriktionIterator.next().wouldBeHardConflictAt(startTime, planungseinheit);
    }
    return isConflicted;
  }
}
