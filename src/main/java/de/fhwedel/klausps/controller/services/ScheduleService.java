package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.util.PlanungseinheitUtil;
import de.fhwedel.klausps.controller.util.PruefungWithScoring;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.api.importer.ImportException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ScheduleService is responsible for any scheduling task, where Restrictions have to be
 * checked. It handles all scheduling logic using a RestrictionService for checking hard and soft
 * restrictions as well as a DataAccessService for communicating with the Pruefungsperiode.
 */
public class ScheduleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

  /**
   * DataAccessService used for communication with the {@link Pruefungsperiode}
   */
  private final DataAccessService dataAccessService;

  /**
   * The RestrictionService used for checking all hard and soft {@link
   * de.fhwedel.klausps.controller.restriction.Restriction Restrictions}
   */
  private final RestrictionService restrictionService;

  /**
   * Converts {@link Planungseinheit Planungseinheiten} from internally used Planungseinheiten to
   * externally used {@link ReadOnlyPlanungseinheit Planungseinheiten}
   */
  private final Converter converter;

  /**
   * Construct a ScheduleService with a {@link DataAccessService}, {@link RestrictionService} and a
   * {@link Converter}. The ScheduleService is also set in the Converter to provide Scoring
   * information.
   *
   * @param dataAccessService  service needed to communicate with the {@link Pruefungsperiode}
   * @param restrictionService service needed to check {@link de.fhwedel.klausps.controller.restriction.Restriction
   *                           Restrictions}
   * @param converter          service needed to convert {@link Planungseinheit Planungseinheiten}
   *                           to {@link ReadOnlyPlanungseinheit ReadOnlyPlanungseinheiten}
   */
  public ScheduleService(DataAccessService dataAccessService, RestrictionService restrictionService,
      Converter converter) {
    this.dataAccessService = dataAccessService;
    this.restrictionService = restrictionService;
    this.converter = converter;
    converter.setScheduleService(this);
  }

  /**
   * Unschedules a {@link ReadOnlyPruefung Pruefung} from the {@link Pruefungsperiode} and
   * calculates the scoring for all affected Pruefungen. Because affected Pruefungen can be part of
   * a {@link ReadOnlyBlock Block}, Blocks might also be part of the resulting List of {@link
   * Planungseinheit Planungseinheiten}
   *
   * @param pruefungToUnschedule pruefung to be unscheduled
   * @return DTOPlanungseinheiten, which are changed after the operation with duplicates (parents
   * and children)
   */
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefungToUnschedule)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefungToUnschedule);
    Pruefung pruefung = dataAccessService.getPruefung(pruefungToUnschedule);
    Set<Pruefung> affectedPruefungen = restrictionService.getPruefungenAffectedBy(pruefung);
    dataAccessService.unschedulePruefung(pruefung);
    return calculateScoringForCachedAffected(affectedPruefungen);
  }

  /**
   * Calculates the scoring of the passed Pruefungen and converts them to {@link ReadOnlyPruefung
   * read only Pruefungen}
   *
   * @param affected pruefungen that are affected
   * @return List of DTOPlanungseinheiten, it can contain the parent (block) and also the children
   * (pruefung of a block)
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   */
  @NotNull
  private List<ReadOnlyPlanungseinheit> calculateScoringForCachedAffected(Set<Pruefung> affected)
      throws NoPruefungsPeriodeDefinedException {
    return new ArrayList<>(
        converter.convertToROPlanungseinheitSet(getPlanungseinheitenWithBlock(affected)));
  }

  /**
   * Combines a Set of {@link Pruefung Pruefungen} with their enclosing {@link Block Blocks} to a
   * Set containing both
   *
   * @param pruefungen passed pruefungen
   * @return Set of Planungseinheiten, it can contain the parent (block) and also the children
   * (pruefung of a block)
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  private Set<Planungseinheit> getPlanungseinheitenWithBlock(Set<Pruefung> pruefungen)
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> planungseinheiten = new HashSet<>();
    for (Pruefung p : pruefungen) {
      Optional<Block> blockOpt = dataAccessService.getBlockTo(p);
      // if present, add the block as well
      blockOpt.ifPresent(planungseinheiten::add);
      planungseinheiten.add(p);
    }
    return planungseinheiten;
  }

  /**
   * Adds a pruefung to a block and calculates the scoring for all affected Pruefungen.
   *
   * @param readOnlyBlock    to add the passed pruefung to
   * @param readOnlyPruefung to be added to the passed block
   * @return List of changed DTOPlanungseinheiten
   * @throws HartesKriteriumException           when a hard constraint is violated
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   * @throws IllegalStateException              when the Pruefung or the Block do not exist in the
   *                                            {@link Pruefungsperiode}
   * @throws IllegalArgumentException           When the {@link ReadOnlyPruefung Pruefung} is
   *                                            already planned or already part of another {@link
   *                                            ReadOnlyBlock Block}
   */
  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(ReadOnlyBlock readOnlyBlock,
      ReadOnlyPruefung readOnlyPruefung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException,
      IllegalArgumentException {
    noNullParameters(readOnlyBlock, readOnlyPruefung);
    // check if corresponding Pruefung and Block exist in Pruefungsperiode, if yes get them
    Pruefung pruefung = dataAccessService.getPruefung(readOnlyPruefung);
    Block block = dataAccessService.getBlock(readOnlyBlock);
    checkExistenceOfPruefungenInBlock(readOnlyBlock);

    // nothing needs to be done if the Pruefung is already in the right block
    if (block.getPruefungen().contains(pruefung)) {
      return emptyList();
    }
    // check if the Pruefung may be added to a Block
    if (pruefung.isGeplant()) {
      throw new IllegalArgumentException("Planned Pruefungen can not be added to a Block.");
    }
    Optional<Block> oldBlock = dataAccessService.getBlockTo(pruefung);
    if (oldBlock.isPresent() && (!oldBlock.get().equals(block))) {
      throw new IllegalArgumentException(
          "Pruefungen contained in a block can not be added to another block.");
    }

    // actually add the Pruefung to the Block
    Block modelBlock = dataAccessService.addPruefungToBlock(readOnlyBlock, readOnlyPruefung);
    checkHardCriteriaUndoAddPruefungToBlock(readOnlyPruefung, readOnlyBlock);

    // evaluate Restrictions only if the block is planned
    if (!modelBlock.isGeplant()) {
      List<ReadOnlyPlanungseinheit> result = new ArrayList<>();
      result.add(converter.convertToROBlock(modelBlock));
      return result;
    }
    List<ReadOnlyPlanungseinheit> result = getAffectedPruefungenBy(modelBlock);
    result.add(converter.convertToROBlock(modelBlock));
    return result;
  }

  /**
   * Checks if Pruefung added to a Block violates any {@link  de.fhwedel.klausps.controller.restriction.hard.HardRestriction
   * hard Restrictions}. If a hard Restriction is violated, the Pruefung gets removed from the Block
   * and the Analysis of the Restriction violation is converted to a {@link HartesKriteriumException
   * exception} and thrown.
   *
   * @param pruefung the Pruefung to check for
   * @param block    the Block with the added Pruefung
   * @throws HartesKriteriumException           when a hard Restriction is violated
   * @throws NoPruefungsPeriodeDefinedException when no {@link Pruefungsperiode} is set
   */
  private void checkHardCriteriaUndoAddPruefungToBlock(ReadOnlyPruefung pruefung,
      ReadOnlyBlock block) throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung modelPruefung = dataAccessService.getPruefung(pruefung);
    // collect all hard Restriction analyses
    List<HardRestrictionAnalysis> hardAnalyses = restrictionService.checkHardRestrictions(
        modelPruefung);
    // undo everything if a hard restriction was violated
    if (!hardAnalyses.isEmpty()) {
      removePruefungFromBlock(block, pruefung);
      throw converter.convertHardException(hardAnalyses);
    }
  }

  /**
   * Collects all {@link ReadOnlyPlanungseinheit ReadOnlyPlanungseinheiten} affected by a {@link
   * Block}.
   *
   * @param block the block to check for
   * @return A List of all affected Planungseinheiten.
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode is present
   */
  @NotNull
  private List<ReadOnlyPlanungseinheit> getAffectedPruefungenBy(Block block)
      throws NoPruefungsPeriodeDefinedException {
    if (!block.isGeplant()) {
      return emptyList();
    }
    Set<Pruefung> changedScoring = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      changedScoring.addAll(restrictionService.getPruefungenAffectedBy(pruefung));
    }
    return new LinkedList<>(converter.convertToROPlanungseinheitSet(
        getPlanungseinheitenWithBlock(changedScoring)));
  }

  /**
   * Removes a {@link ReadOnlyPruefung} from a {@link ReadOnlyBlock} and changes the scoring for all
   * from the removal affected Pruefungen if the Block is scheduled.<br> Because a removed Pruefung
   * is always marked as unscheduled afterwards and no hard conflicting Planungseinheit can be
   * schedule, the only change in affected Pruefungen will be a scoring improvement. For this
   * reason, the method does not evaluate any {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction
   * hard Restrictions}.
   *
   * @param block    the Block to remove a Pruefung from
   * @param pruefung the Pruefung to remove
   * @return A List of all affected Pruefungen as well as the by the removal changed Block
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} exists
   * @throws IllegalStateException              if the Block to remove from does not exist
   */
  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(block, pruefung);
    Pruefung toRemove = dataAccessService.getPruefung(pruefung);
    // only to check if block exists
    dataAccessService.getBlock(block);
    checkExistenceOfPruefungenInBlock(block);

    // try to get the enclosing Block
    Optional<Block> oldBlock = dataAccessService.getBlockTo(toRemove);
    // if the Pruefung was not in a Block, do nothing
    if (oldBlock.isEmpty()) {
      return emptyList();
    }
    // if the Block is not scheduled, no Restrictions need to be checked
    if (!block.geplant()) {
      Block resultingBlock = dataAccessService.removePruefungFromBlock(block, pruefung);
      Pruefung unscheduledPruefung = dataAccessService.getPruefung(pruefung);
      return new ArrayList<>(
          converter.convertToROPlanungseinheitSet(resultingBlock, unscheduledPruefung));
    }

    // collect the affected Pruefungen and recalculate the scoring after removal
    Set<Pruefung> affectedPruefungen = restrictionService.getPruefungenAffectedBy(toRemove);
    Block blockWithoutPruefung = dataAccessService.removePruefungFromBlock(block, pruefung);
    List<ReadOnlyPlanungseinheit> result = calculateScoringForCachedAffected(affectedPruefungen);
    result.add(converter.convertToReadOnlyPlanungseinheit(blockWithoutPruefung));
    return result;

  }

  /**
   * Schedules a Block at a passed date and time. If any Pruefung contained in the Block causes a
   * {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction}
   * violation, the Block cannot be scheduled and a {@link HartesKriteriumException} is thrown. If
   * no violation occurs the Block gets scheduled and the scoring of all affected Pruefungen will be
   * recalculated.
   *
   * @param roBlock the block to schedule.
   * @param termin  the date and time to schedule
   * @return a List of all affected Planungseinheiten for any Pruefung in the result, the scoring is
   * updated
   * @throws HartesKriteriumException           if a hard Restriction is violated
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} exists
   * @throws IllegalStateException              if the Block or any of its Pruefungen does not
   *                                            exist.
   */
  public List<ReadOnlyPlanungseinheit> scheduleBlock(ReadOnlyBlock roBlock, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(termin);
    // existence check
    dataAccessService.getBlock(roBlock);
    checkExistenceOfPruefungenInBlock(roBlock);

    List<ReadOnlyPlanungseinheit> previouslyAffected = new ArrayList<>();
    if (roBlock.geplant()) {
      previouslyAffected.addAll(getAffectedPruefungenBy(dataAccessService.getBlock(roBlock)));
      for (ReadOnlyPruefung ro : roBlock.getROPruefungen()) {
        previouslyAffected.remove(ro);
      }
    }
    Block blockModel = dataAccessService.scheduleBlock(roBlock, termin);
    // rollback and Exception for hard violation
    checkHardCriteriaUndoScheduling(roBlock, blockModel);
    Set<Pruefung> newAffectedPruefungen = new HashSet<>();
    Set<ReadOnlyPlanungseinheit> updatedAffectedPlanungseinheiten = new HashSet<>();
    // get Pruefungen to recalculate scoring
    for (ReadOnlyPlanungseinheit s : previouslyAffected) {
      if (!s.isBlock()) {
        newAffectedPruefungen.add(dataAccessService.getPruefung(s.asPruefung()));
      }
    }
    // get read only Planungseinheiten of affected Pruefungen and their Bloecke
    updatedAffectedPlanungseinheiten.addAll(converter.convertToROPlanungseinheitSet(
        getPlanungseinheitenWithBlock(newAffectedPruefungen)));
    updatedAffectedPlanungseinheiten.addAll(getAffectedPruefungenBy(blockModel));
    return new ArrayList<>(updatedAffectedPlanungseinheiten);
  }

  /**
   * Checks if any {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard
   * Restriction} is violated for a scheduled Block. In case of a violation there are two cases to
   * consider.<br> If the Block was already scheduled, it gets rescheduled to its old position
   * otherwise it remains unscheduled. In both cases an {@link HartesKriteriumException Exception}
   * is thrown.<br> If no restriction is violated, the Block will be scheduled at the passed
   * position.
   *
   * @param roBlock    the Block with the old scheduling information
   * @param modelBlock the newly scheduled Block to validate
   * @throws HartesKriteriumException           if the new position of the Block triggers a hard
   *                                            Restriction
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} exists
   */
  private void checkHardCriteriaUndoScheduling(ReadOnlyBlock roBlock, Block modelBlock)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    List<HardRestrictionAnalysis> list = new LinkedList<>();

    Block blockOfPruefung = dataAccessService.getBlock(roBlock);
    Optional<LocalDateTime> termin = roBlock.getTermin();
    // check the restrictions
    for (Pruefung p : modelBlock.getPruefungen()) {
      list.addAll(restrictionService.checkHardRestrictions(p));
    }
    // if the positioning violated a hard restriction, block needs to be unscheduled
    if (!list.isEmpty() && blockOfPruefung.isGeplant()) {
      // if it was previously scheduled, the Block needs to be rescheduled at its old position
      if (termin.isPresent()) {
        dataAccessService.scheduleBlock(roBlock, termin.get());
      } else {
        dataAccessService.unscheduleBlock(roBlock);
      }
      throw converter.convertHardException(list);
    }
  }

  /**
   * Unschedules a Block from the {@link Pruefungsperiode}. Afterwards all contained {@link
   * ReadOnlyPruefung} will also be unscheduled.<br> Because unscheduling can never violate a {@link
   * de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction} only {@link
   * de.fhwedel.klausps.controller.restriction.soft.SoftRestriction soft Restrictions} are
   * tested.<br> Unscheduling a {@link Planungseinheit} can only improve the scoring of other {@link
   * Planungseinheit Planungseinheiten}. Because the Block itself is changed by this action it will
   * be added to the return value.
   *
   * @param block the Block to unschedule
   * @return a List of affected Planungseinheiten and the unscheduled Block
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              if the Block or any of its Pruefungen does not
   *                                            exist
   */
  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(block);
    Block blockModel = dataAccessService.getBlock(block);
    checkExistenceOfPruefungenInBlock(block);
    Set<Pruefung> affected = restrictionService.getPruefungenAffectedByAnyBlock(blockModel);
    dataAccessService.unscheduleBlock(block);
    return new ArrayList<>(calculateScoringForCachedAffected(affected));
  }


  /**
   * Calculates the scoring for a {@link Pruefung}.
   *
   * @param pruefung the Pruefung to calculate the scoring for
   * @return the Pruefungs scoring
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  public int scoringOfPruefung(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    return restrictionService.getScoringOfPruefung(pruefung);
  }


  /**
   * Schedules a Pruefung at a passed date and time.  In case of a {@link
   * de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction} violation
   * there are two cases to consider.<br> If the Pruefung was already scheduled, it gets rescheduled
   * to its old position otherwise it remains unscheduled. In both cases an {@link
   * HartesKriteriumException Exception} is thrown.<br> If no restriction is violated, the Pruefung
   * will be scheduled at the passed position.<br> Scheduling Pruefungen might affect the scoring of
   * other Pruefungen and therefore also their Blocks.
   *
   * @param pruefung the pruefung to schedule
   * @param termin   the date and time the pruefung is supposed to be scheduled at
   * @return All affected Planungseinheiten.
   * @throws HartesKriteriumException           when the positioning of the Pruefung violates a hard
   *                                            Restriction
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode exists
   * @throws IllegalStateException              when the Pruefung does not exist.
   * @throws IllegalArgumentException           when the Pruefung is in a Block
   */
  public Set<Planungseinheit> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException,
      IllegalArgumentException {
    noNullParameters(pruefung, termin);
    Set<Planungseinheit> previouslyAffected = new HashSet<>();
    if (pruefung.geplant()) {
      previouslyAffected.addAll(getAffectedPruefungenBy(dataAccessService.getPruefung(pruefung)));
      previouslyAffected.remove(dataAccessService.getPruefung(pruefung));
    }
    // schedule and check criteria
    Pruefung pruefungModel = dataAccessService.schedulePruefung(pruefung, termin);
    checkHardCriteriaUndoScheduling(pruefung, pruefungModel);
    previouslyAffected.addAll(getAffectedPruefungenBy(pruefungModel));
    return previouslyAffected;
  }

  /**
   * Checks if a positioning of a Pruefung violates a {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction
   * hard Restriction}. If a violation occurs, an Exception gets thrown immediately. If the Pruefung
   * was already scheduled, it is set to its old position, otherwise it will remain unscheduled.
   *
   * @param pruefung      the Pruefung to check, containing the old date and time
   * @param pruefungModel the newly scheduled pruefung, which might be located at an invalid
   *                      position
   * @throws HartesKriteriumException           when the Pruefung is scheduled at an invalid
   *                                            position
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode ist set
   */
  private void checkHardCriteriaUndoScheduling(ReadOnlyPruefung pruefung, Pruefung pruefungModel)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    List<HardRestrictionAnalysis> hard = restrictionService.checkHardRestrictions(pruefungModel);

    Optional<LocalDateTime> termin = pruefung.getTermin();
    if (!hard.isEmpty()) {
      // reverse
      if (termin.isPresent()) {
        dataAccessService.schedulePruefung(pruefung, termin.get());
      } else {
        dataAccessService.unschedulePruefung(pruefungModel);
      }
      throw converter.convertHardException(hard);
    }
  }


  /**
   * Collect all Pruefungen that are affected by the positioning of a passed Pruefung.
   *
   * @param pruefung the Pruefung to check for
   * @return all affected Pruefungen affected by the
   * @throws NoPruefungsPeriodeDefinedException when no {@link Pruefungsperiode} is set
   */
  @NotNull
  private Set<Planungseinheit> getAffectedPruefungenBy(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    Set<Planungseinheit> result = new HashSet<>();
    if (pruefung.isGeplant()) {
      result.addAll(
          getPlanungseinheitenWithBlock(restrictionService.getPruefungenAffectedBy(pruefung)));
    }
    return result;
  }


  /**
   * Sets the duration of a Pruefung. Changing the duration might produce a Restriction violation.
   * If a {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction} is
   * violated the duration won't be changed and an Exception will bet triggered.<br> For any {@link
   * de.fhwedel.klausps.controller.restriction.soft.SoftRestriction soft Restriction} violation the
   * Duration will be set. All affected Planungseinheiten will get an updated scoring and will be
   * returned.
   *
   * @param pruefung the Pruefung to set the duration for
   * @param dauer    the new duration
   * @return all affected Pruefungen and Blocks
   * @throws HartesKriteriumException           when a hard Restriction is violated
   * @throws NoPruefungsPeriodeDefinedException when no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              when the Pruefung does not exist
   * @throws IllegalArgumentException           when the duration is negative or zero
   */
  public List<Planungseinheit> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException,
      IllegalArgumentException {
    noNullParameters(pruefung, dauer);
    if (dauer.isNegative() || dauer.isZero()) {
      throw new IllegalArgumentException("Dauer muss > 0 sein.");
    }
    Pruefung pruefungModel = dataAccessService.getPruefung(pruefung);
    Duration oldDuration = pruefungModel.getDauer();
    pruefungModel.setDauer(dauer);
    List<HardRestrictionAnalysis> hard = restrictionService.checkHardRestrictions(
        pruefungModel);
    // rollback if hard Restrictions were violated
    if (!hard.isEmpty()) {
      pruefungModel.setDauer(oldDuration);
      throw converter.convertHardException(hard);
    }
    return new ArrayList<>(getAffectedPruefungenBy(pruefungModel));
  }

  /**
   * Removes a {@link Teilnehmerkreis} from a Pruefung. Because such a removal can never cause a
   * {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction}
   * violation, this method does not test such a case.<br> For any currently affected {@link
   * ReadOnlyPruefung} the scoring might improve.
   *
   * @param roPruefung      the pruefung where a Teilnehmerkreis is removed
   * @param teilnehmerkreis the Teilnehmerkreis to remove
   * @return all affected Pruefungen and their block
   * @throws NoPruefungsPeriodeDefinedException when no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              when the Pruefung doesn't exist
   */
  public List<Planungseinheit> removeTeilnehmerKreis(ReadOnlyPruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(teilnehmerkreis);
    Pruefung pruefungModel = dataAccessService.getPruefung(roPruefung);
    if (!pruefungModel.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return new ArrayList<>();
    }
    //Damit man eine Liste hat, wo sich das Scoring ändert
    Set<Planungseinheit> listOfRead = new HashSet<>(getAffectedPruefungenBy(pruefungModel));

    //Hier muss ja nicht auf HarteKriterien gecheckt werden.
    this.dataAccessService.removeTeilnehmerkreis(pruefungModel, teilnehmerkreis);

    return new ArrayList<>(listOfRead);
  }

  /**
   * Adds a {@link Teilnehmerkreis} to a Pruefung. This might cause a {@link
   * de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction} violation.
   * When such a violation occurs, the Teilnehmerkreis won't be added and an Exception thrown.<br>
   * For any {@link de.fhwedel.klausps.controller.restriction.soft.SoftRestriction soft Restriction}
   * violation the affected Planungseinheiten are added to the result.
   *
   * @param roPruefung      the Pruefung to add a Teilnehmerkreis to
   * @param teilnehmerkreis the Teilnehmerkreis to add
   * @param schaetzung      the Teilnehmerkreisschaetzung of the new Teilnehmerkreis
   * @return all affected Pruefungen and their Blocks
   * @throws HartesKriteriumException           if a hard Restriction is violated
   * @throws NoPruefungsPeriodeDefinedException when no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              when the Pruefung does not exist.
   */
  public Set<Planungseinheit> addTeilnehmerkreis(ReadOnlyPruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(roPruefung, teilnehmerkreis);

    Pruefung pruefungModel = dataAccessService.getPruefung(roPruefung);
    if (dataAccessService.setTeilnehmerkreis(pruefungModel, teilnehmerkreis, schaetzung)) {
      List<HardRestrictionAnalysis> hard = restrictionService.checkHardRestrictions(pruefungModel);
      if (!hard.isEmpty()) {
        dataAccessService.removeTeilnehmerkreis(pruefungModel, teilnehmerkreis);
        throw converter.convertHardException(hard);
      }
    }
    Set<Planungseinheit> affected = getAffectedPruefungenBy(pruefungModel);
    affected.add(pruefungModel);
    dataAccessService.getBlockTo(pruefungModel).ifPresent(affected::add);
    return affected;
  }

  /**
   * Sets a Teilnehmerkreisschaetzung for a {@link Teilnehmerkreis} of a Pruefung. This might cause
   * a {@link de.fhwedel.klausps.controller.restriction.soft.SoftRestriction soft Restriction}
   * violation, therefore the scoring of other pruefungen might change.
   *
   * @param pruefung        the Pruefung to set the Schaetzung for
   * @param teilnehmerkreis the Teilnehmerkreis to set a Schaetzung for
   * @param schaetzung      the new Teilnehmerkreisschaetzung
   * @return all affected Pruefungen and their Blocks
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              if the Pruefung does not exist
   * @throws IllegalArgumentException           if the Pruefung doesn't have the Teilnehmerkreis or
   *                                            the new Schaetzung is negative
   */
  public Set<Planungseinheit> setTeilnehmerkreisSchaetzung(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) throws NoPruefungsPeriodeDefinedException,
      IllegalStateException, IllegalArgumentException {
    noNullParameters(pruefung, teilnehmerkreis);
    Pruefung modelPruefung = dataAccessService.getPruefung(pruefung);
    checkValidTeilnehmerkreisSchaetzung(schaetzung);
    if (!modelPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      throw new IllegalArgumentException("Pruefung hat keinen Teilnehmerkreis mit diesem Namen.");
    }
    LOGGER.debug("Changing Schaetzung of {} at {} from {} to {}.", teilnehmerkreis, modelPruefung,
        modelPruefung.getSchaetzungen().get(teilnehmerkreis), schaetzung);
    modelPruefung.setSchaetzung(teilnehmerkreis, schaetzung);
    if (modelPruefung.isGeplant()) {
      return getAffectedPruefungenBy(modelPruefung);
    }
    return emptySet();
  }

  /**
   * Check if a Schaetzwert for a {@link Teilnehmerkreis} is valid, meaning non-negative. Throws an
   * Exception, if the value is not valid.
   *
   * @param schaetzung the Schaetzwert to test
   * @throws IllegalArgumentException when the Schaetzwert is negative
   */
  private void checkValidTeilnehmerkreisSchaetzung(int schaetzung) throws IllegalArgumentException {
    if (schaetzung < 0) {
      throw new IllegalArgumentException("Schätzwert darf nicht negativ sein.");
    }
  }

  /**
   * Set the capacity of the {@link Pruefungsperiode}. This might affect the scoring of a Pruefung.
   *
   * @param kapazitaet the new capacity
   * @return all affected Pruefungen and their Blocks
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalArgumentException           if the capacity is negative
   */
  @NotNull
  public Set<Planungseinheit> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    dataAccessService.setKapazitaetStudents(kapazitaet);
    Set<Planungseinheit> result = new HashSet<>();
    for (Pruefung pruefung : dataAccessService.getPlannedPruefungen()) {
      result.addAll(getAffectedPruefungenBy(pruefung));
    }
    return result;
  }

  /**
   * Gets all planned Pruefung which are in conflict with a passed Planungseinheit.
   *
   * @param planungseinheitToCheckFor the Planungseinheit to check
   * @return the Pruefungen in conflict with the passed Planungseinheit
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  @NotNull
  public Set<Pruefung> getGeplantePruefungenWithKonflikt(
      ReadOnlyPlanungseinheit planungseinheitToCheckFor) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(planungseinheitToCheckFor);
    Planungseinheit planungseinheit = getAsModel(planungseinheitToCheckFor);
    return restrictionService.getPruefungenPotentiallyInHardConflictWith(planungseinheit);
  }

  /**
   * Gets the equivalent {@link Planungseinheit} of a {@link ReadOnlyPlanungseinheit}.
   *
   * @param planungseinheitToCheckFor the Planungseinheit to get
   * @return the Planungseinheit for the ReadOnlyPruefung
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              if the {@link Planungseinheit} does not exist
   */
  @NotNull
  private Planungseinheit getAsModel(ReadOnlyPlanungseinheit planungseinheitToCheckFor)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    if (planungseinheitToCheckFor.isBlock()) {
      return dataAccessService.getBlock(planungseinheitToCheckFor.asBlock());
    } else {
      return dataAccessService.getPruefung(planungseinheitToCheckFor.asPruefung());
    }
  }

  /**
   * Collects all {@link SoftRestrictionAnalysis soft restriction analyses} for a Pruefung and
   * converts them to a List of {@link KriteriumsAnalyse}.
   *
   * @param pruefung the Pruefung to check
   * @return the collected analyses
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              if the {@link Pruefung} does not exist
   */
  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(pruefung);
    List<SoftRestrictionAnalysis> analyses = restrictionService.checkWeicheKriterien(
        dataAccessService.getPruefung(pruefung));
    return converter.convertAnalyseList(analyses);
  }

  /**
   * Picks all points in time from a passed Set, where a {@link ReadOnlyPlanungseinheit read only
   * Planungseinheit} may not be scheduled.
   *
   * @param timesToCheck           the times to check for the {@link ReadOnlyPlanungseinheit}
   * @param planungseinheitToCheck the Planungseinheit to check for
   * @return all points in time, where the Planungseinheit may not be scheduled
   * @throws IllegalArgumentException           when at least one of the points in time is not in
   *                                            the {@link Pruefungsperiode}
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  public Set<LocalDateTime> getHardConflictedTimes(Set<LocalDateTime> timesToCheck,
      ReadOnlyPlanungseinheit planungseinheitToCheck)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters(timesToCheck, planungseinheitToCheck);

    Planungseinheit planungseinheit = getPlanungseinheit(planungseinheitToCheck);
    return calcHardConflictingTimes(timesToCheck, planungseinheit);
  }

  /**
   * Gets the corresponding {@link Planungseinheit} for a {@link ReadOnlyPlanungseinheit} from the
   * {@link Pruefungsperiode}.
   *
   * @param planungseinheitToCheck the Planungseinheit to check
   * @return the corresponding Planungseinheit
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              if the {@link Planungseinheit} does not exist
   */
  @NotNull
  private Planungseinheit getPlanungseinheit(
      @NotNull ReadOnlyPlanungseinheit planungseinheitToCheck)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    Planungseinheit planungseinheit;
    if (planungseinheitToCheck.isBlock()) {
      planungseinheit = dataAccessService.getBlock(planungseinheitToCheck.asBlock());
    } else {
      planungseinheit = dataAccessService.getPruefung(planungseinheitToCheck.asPruefung());
    }
    return planungseinheit;
  }

  /**
   * Picks all points in time from a passed Set, where a {@link Planungseinheit Planungseinheit} may
   * not be scheduled.
   *
   * @param timesToCheck    the times to check for the {@link Planungseinheit}
   * @param planungseinheit the Planungseinheit to check for
   * @return all points in time, where the Planungseinheit may not be scheduled
   * @throws IllegalArgumentException           when at least one of the points in time is not in
   *                                            the {@link Pruefungsperiode}
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  @NotNull
  private Set<LocalDateTime> calcHardConflictingTimes(@NotNull Set<LocalDateTime> timesToCheck,
      @NotNull Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    assertTimestampsInPeriod(timesToCheck);
    Set<LocalDateTime> result = new HashSet<>();
    for (LocalDateTime startTimeToCheck : timesToCheck) {
      if (restrictionService.wouldBeHardConflictIfStartedAt(startTimeToCheck, planungseinheit)) {
        result.add(startTimeToCheck);
      }
    }
    return result;
  }

  /**
   * Checks that all timestamps are inside the Pruefungsperiode.
   *
   * @param timestamps The timestamps to check.
   * @throws IllegalArgumentException In case a timestamp is outside the Pruefungsperiode.
   */
  private void assertTimestampsInPeriod(Iterable<LocalDateTime> timestamps)
      throws NoPruefungsPeriodeDefinedException {
    for (LocalDateTime timestamp : timestamps) {
      if (timestamp.toLocalDate().isBefore(dataAccessService.getStartOfPeriode())
          || timestamp.toLocalDate().isAfter(dataAccessService.getEndOfPeriode())) {
        String message = String.format(
            "%s was illegally called with %s outside of Pruefungsperiode.",
            Thread.currentThread().getStackTrace()[1].getMethodName(), timestamp);
        throw new IllegalArgumentException(message);
      }
    }
  }

  /**
   * Changes the {@link Blocktyp type} of a passed {@link ReadOnlyBlock}. This might cause a {@link
   * de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction} violation.<br>
   * If a violation occurs, the BLocktype won't be changed. Otherwise,  the type is changed and the
   * scoring is updated for the affected Pruefungen.
   *
   * @param block    the Block to change
   * @param changeTo the new Type
   * @return all affected Pruefungen and their Blocks
   * @throws HartesKriteriumException           if a hard Restriction is violated
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  public List<ReadOnlyPlanungseinheit> setBlockType(ReadOnlyBlock block, Blocktyp changeTo)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Block modelBlock = dataAccessService.getBlock(block);
    checkExistenceOfPruefungenInBlock(block);
    // nothing to be done, if the blocktype stays the same
    if (block.getTyp() == changeTo) {
      LOGGER.trace("Not changing block type, same type already set.");
      return emptyList();
    }
    // no scoring needed for unscheduled block
    if (block.ungeplant()) {
      LOGGER.debug("Set type of {} to {}.", modelBlock, changeTo);
      modelBlock.setTyp(changeTo);
      return List.of(converter.convertToROBlock(modelBlock));
    }
    // save the affected Pruefungen to update later
    Set<Pruefung> affected = restrictionService.getPruefungenAffectedByAnyBlock(modelBlock);
    modelBlock.setTyp(changeTo);
    // collect the newly affected after change of type
    affected.addAll(restrictionService.getPruefungenAffectedByAnyBlock(modelBlock));
    // check if hard Restriction violation occurred
    List<HardRestrictionAnalysis> hard = restrictionService.checkHardRestrictions(
        affected);
    // rollback if violation occurred
    if (!hard.isEmpty()) {
      modelBlock.setTyp(block.getTyp());
      throw converter.convertHardException(hard);
    }
    // otherwise, update scoring information for all affected Pruefungen
    List<ReadOnlyPlanungseinheit> result = new ArrayList<>();
    if (affected.isEmpty()) {
      result.add(converter.convertToROBlock(modelBlock));
    }
    result.addAll(calculateScoringForCachedAffected(affected));
    return result;
  }

  /**
   * Checks for a {@link ReadOnlyBlock} if the contained {@link ReadOnlyPruefung ReadOnlyPruefungen}
   * exist as {@link Pruefung Pruefungen} in the current {@link Pruefungsperiode}, if not an
   * Exception is thrown immediately.
   *
   * @param block the block to check
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   * @throws IllegalStateException              when a {@link ReadOnlyPruefung} has no equivalent
   *                                            {@link Pruefung} in the {@link Pruefungsperiode}
   */
  private void checkExistenceOfPruefungenInBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    for (ReadOnlyPruefung pruefung : block.getROPruefungen()) {
      dataAccessService.getPruefung(pruefung);
    }
  }


  /**
   * Creates a new {@link Pruefungsperiode} from a CSV File and an optional KlausPS-File. All {@link
   * Pruefung Pruefungen} from the CSV-File will be added as unscheduled to the new Periode.<br> If
   * a path to a KlausPS-File is present, all Pruefungen that are scheduled in the old Periode will
   * be adapted and scheduled in the new Pruefungsperiode.<br> If a hard conflict is detected in the
   * adapted Pruefungen and {@link  Block Blocks}, as many Pruefungen/Blocks will be unscheduled
   * (sorted by date and time) to remove the conflict.<br> For any conflicting Pruefung in a Block,
   * the whole Block will be unscheduled.<br>Any scheduled {@link Block} or {@link Pruefung} outside
   * the Pruefungsperiode will also be unscheduled afterwards.
   *
   * @param ioService    performs the import and export operations
   * @param semester     the semester of the new Pruefungsperiode
   * @param start        the start date of the new Pruefungsperiode
   * @param end          the end date of the new Pruefungsperiode
   * @param ankertag     the ankertag of the new Pruefungsperiode
   * @param kapazitaet   the kapazität of the new Pruefungsperiode
   * @param pathCSV      path to the CSV-File
   * @param adoptKlausPS path to the KlausPS-File (it may be null, if no adaptation wanted)
   * @throws ImportException          for syntactic or semantic errors
   * @throws IOException              for technical errors when reading the files
   * @throws IllegalTimeSpanException for invalid dates: begin is after end, ankertag is before
   *                                  begin or after end
   */
  public void createNewPeriodeWithData(IOService ioService, Semester semester, LocalDate start,
      LocalDate end, LocalDate ankertag, int kapazitaet, Path pathCSV, Path adoptKlausPS)
      throws ImportException, IOException, IllegalTimeSpanException, IllegalArgumentException {
    Pruefungsperiode fallbackPeriode = dataAccessService.getPruefungsperiode();
    try {
      ioService.createNewPeriodeWithData(semester, start, end, ankertag, kapazitaet, pathCSV,
          adoptKlausPS);
      dataAccessService.unschedulePlanungseinheitenOutsideOfPeriode();
      unscheduleHardConflictingFromAdoptedPeriode();
    } catch (ImportException | IOException | IllegalTimeSpanException | IllegalArgumentException e) {
      dataAccessService.setPruefungsperiode(fallbackPeriode);
      throw e;
    } catch (NoPruefungsPeriodeDefinedException f) {
      dataAccessService.setPruefungsperiode(fallbackPeriode);
      throw new ImportException("Pruefungsperiode konnte nicht erstellt werden.");
    }
  }

  /**
   * Imports an existing {@link Pruefungsperiode} from a model-internal JSON-file.
   * <br> If a hard conflict is detected in the imported Pruefungsperiode,
   * as many Pruefungen/Blocks will be unscheduled (sorted by date and time) to remove the
   * conflict.<br> For any conflicting Pruefung in a Block, the whole Block will be unscheduled.<br>
   * Any {@link Block} or {@link Pruefung} outside the Pruefungsperiode will also be unscheduled
   * afterwards.
   *
   * @param ioService performs the import and export operations
   * @param path      path to the KlausPS-File
   * @throws ImportException when syntactic or semantic errors are detected in the file
   * @throws IOException     for technical errors when reading the file
   */
  public void importPeriode(IOService ioService, Path path) throws ImportException, IOException {
    Pruefungsperiode fallbackPeriode = dataAccessService.getPruefungsperiode();
    try {
      ioService.importPeriode(path);
      dataAccessService.unschedulePlanungseinheitenOutsideOfPeriode();
      unscheduleHardConflictingFromAdoptedPeriode();
    } catch (ImportException | IOException | IllegalArgumentException e) {
      dataAccessService.setPruefungsperiode(fallbackPeriode);
      throw e;
    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new ImportException("Pruefungsperiode konnte nicht erstellt werden");
    }
  }


  /**
   * Checks for hard conflicts of all scheduled {@link Pruefung Pruefungen} and {@link  Block
   * Blocks} from the new {@link Pruefungsperiode} and unschedules them to remove inconsistencies.
   *
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode is defined
   */
  private void unscheduleHardConflictingFromAdoptedPeriode()
      throws NoPruefungsPeriodeDefinedException {
    unscheduleConflictingBlocksFromAdoptedPeriode();
    unscheduleConflictingPruefungFromAdoptedPeriode();

  }

  /**
   * Checks for hard conflicts of all scheduled {@link  Block Blocks} from the new {@link
   * Pruefungsperiode} and unschedules them and all their contained {@link Pruefung Pruefungen} to
   * remove inconsistencies.
   *
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode is defined
   */
  private void unscheduleConflictingBlocksFromAdoptedPeriode()
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> plannedPruefungen = sortPlanungseinheitenByStartzeitpunkt(
        dataAccessService.getGeplanteBloecke());
    for (Planungseinheit block : plannedPruefungen) {
      if (!restrictionService.checkHardRestrictions(block.asBlock().getPruefungen()).isEmpty()) {
        block.setStartzeitpunkt(null);
      }
    }

  }

  /**
   * Checks for hard conflicts of all scheduled {@link  Pruefung Pruefungen} from the new {@link
   * Pruefungsperiode} and unschedules them to remove inconsistencies.
   *
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode is defined
   */
  private void unscheduleConflictingPruefungFromAdoptedPeriode()
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> plannedPruefungen = sortPlanungseinheitenByStartzeitpunkt(
        dataAccessService.getPlannedPruefungen());
    for (Planungseinheit pruefung : plannedPruefungen) {
      if (!restrictionService.checkHardRestrictions(pruefung.asPruefung()).isEmpty()) {
        pruefung.setStartzeitpunkt(null);
      }
    }
  }

  /**
   * Sorts a given Set of Planungseinheiten by their Startzeitpunkt.
   *
   * @param planungseinheiten the Planungseinheiten to sort
   * @return a Set of sorted Planungseinheiten
   */
  private Set<Planungseinheit> sortPlanungseinheitenByStartzeitpunkt(
      Set<? extends Planungseinheit> planungseinheiten) {
    Set<Planungseinheit> sortedByStartzeit = new TreeSet<>(
        (planungseinheit1, planungseinheit2) -> {
          if (planungseinheit1.getStartzeitpunkt().equals(planungseinheit2.getStartzeitpunkt())) {
            return Integer.compare(planungseinheit1.schaetzung(), planungseinheit2.schaetzung());
          }
          return planungseinheit1.getStartzeitpunkt().isBefore(planungseinheit2.getStartzeitpunkt())
              ? -1 : 1;
        });

    sortedByStartzeit.addAll(planungseinheiten);
    return sortedByStartzeit;
  }

  /**
   * Sets the start and end dates of the current {@link Pruefungsperiode} to a passed start and
   * end.<br> The changed dates might lead to scheduled Pruefungen outside the Pruefungsperiode. If
   * this occurs, an Exception is thrown and the dates won't be changed.<br> If the new dates are
   * valid, the scoring of all Pruefungen is updated and returned.
   *
   * @param startDatum the new start date
   * @param endDatum   the new end date
   * @return all affected Pruefungen and their Blocks
   * @throws IllegalTimeSpanException           if start is after the end or the ankertag is outside
   *                                            the {@link Pruefungsperiode}
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  public Set<Planungseinheit> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    noNullParameters(startDatum, endDatum);
    checkDatesMaybeException(startDatum, endDatum);

    Set<PruefungWithScoring> before = getPlannedPruefungenWithScoring();

    dataAccessService.setDatumPeriode(startDatum, endDatum);

    Set<PruefungWithScoring> after = getPlannedPruefungenWithScoring();
    return getPlanungseinheitenWithBlock(PlanungseinheitUtil.changedScoring(before, after));
  }

  /**
   * Checks if a start and end date are valid by checking if the start is after the end and if the
   * current ankertag would be inside the new dates.<br> If the dates are valid all Pruefungen are
   * checked if they are inside the bounds of start and end. For any outlier an Exception is
   * thrown.
   *
   * @param startDatum the new start date
   * @param endDatum   the new end date
   * @throws IllegalTimeSpanException           if the start is after the end or the ankertag is
   *                                            before the start or after the end
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  private void checkDatesMaybeException(LocalDate startDatum, LocalDate endDatum)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDate ankerTag = dataAccessService.getAnkertag();
    Collection<Pruefung> plannedPruefungen = dataAccessService.getPlannedPruefungen();

    checkTimeSpanPeriode(startDatum, endDatum, ankerTag);

    if (anyIsBefore(plannedPruefungen, startDatum)) {
      throw new IllegalArgumentException(
          "Startdatum ist nach geplanter Prüfungen, bitte Prüfungen entplanen");
    }

    if (anyIsAfter(plannedPruefungen, endDatum)) {
      throw new IllegalArgumentException(
          "Enddatum ist vor geplanter Prüfungen, bitte Prüfungen entplanen");
    }
  }

  /**
   * Checks for an Iterable of {@link Pruefung Pruefungen} if at least one is before a passed date.
   *
   * @param pruefungen the Pruefungen to check
   * @param date       the date to check
   * @return true if at least one Pruefung is before the passed date
   */
  private boolean anyIsBefore(Iterable<Pruefung> pruefungen, LocalDate date) {
    LocalDateTime time = date.atStartOfDay();
    Iterator<Pruefung> iterator = pruefungen.iterator();
    boolean anyIsBefore = false;
    while (iterator.hasNext() && !anyIsBefore) {
      Planungseinheit planungseinheit = iterator.next();
      anyIsBefore = planungseinheit.getStartzeitpunkt().isBefore(time);
    }
    return anyIsBefore;
  }

  /**
   * Checks for an Iterable of {@link Pruefung Pruefungen} if at least one of them is after a passed
   * date.
   *
   * @param pruefungen the Pruefungen to check
   * @param date       the dates to check
   * @return true if at least one Pruefung is after the passed date
   */
  private boolean anyIsAfter(Iterable<Pruefung> pruefungen, LocalDate date) {
    LocalDateTime time = date.plusDays(1).atStartOfDay();
    Iterator<Pruefung> iterator = pruefungen.iterator();
    boolean anyIsAfter = false;
    while (iterator.hasNext() && !anyIsAfter) {
      Planungseinheit planungseinheit = iterator.next();
      anyIsAfter = !planungseinheit.endzeitpunkt().isBefore(time);
    }
    return anyIsAfter;
  }

  /**
   * checks if the given beginning, end and Ankertag are valid.
   *
   * @param begin    the beginning of the Pruefungsperiode
   * @param end      the end of the Pruefungsperiode
   * @param ankertag the Ankertag of the Pruefungsperiode
   * @throws IllegalTimeSpanException when the beginning is after the end or the Ankertag is outside
   *                                  the beginning and end of the Pruefungsperiode
   */
  private void checkTimeSpanPeriode(LocalDate begin, LocalDate end, LocalDate ankertag)
      throws IllegalTimeSpanException {
    if (begin.isAfter(end)) {
      throw new IllegalTimeSpanException("Startdatum ist nach Enddatum");
    }
    if (begin.isAfter(ankertag)) {
      throw new IllegalTimeSpanException("Startdatum ist nach Ankertag");
    }
    if (end.isBefore(ankertag)) {
      throw new IllegalTimeSpanException("Ankertag ist nach Enddatum");
    }
  }

  /**
   * Sets the Ankertag for the current {@link Pruefungsperiode}.
   *
   * @param ankertag the new Date for the Ankertag
   * @return all Pruefungen (and their blocks) that are affected by the change
   * @throws IllegalTimeSpanException           when the Ankertag date is before the start or after
   *                                            the end of the Pruefungsperiode
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode is defined
   */
  public Set<Planungseinheit> setAnkertag(LocalDate ankertag)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    Set<PruefungWithScoring> before = getPlannedPruefungenWithScoring();
    dataAccessService.setAnkertag(ankertag);
    Set<PruefungWithScoring> after = getPlannedPruefungenWithScoring();

    return getPlanungseinheitenWithBlock(PlanungseinheitUtil.changedScoring(before, after));
  }


  /**
   * Gets all planned Pruefungen and adds a scoring.
   *
   * @return a Set of all planned Pruefungen with their Scoring
   * @throws NoPruefungsPeriodeDefinedException when no {@link Pruefungsperiode} is defined
   */
  private Set<PruefungWithScoring> getPlannedPruefungenWithScoring()
      throws NoPruefungsPeriodeDefinedException {
    Set<PruefungWithScoring> planned = new HashSet<>();
    for (Pruefung pruefung : dataAccessService.getPlannedPruefungen()) {
      planned.add(
          new PruefungWithScoring(pruefung, restrictionService.getScoringOfPruefung(pruefung)));
    }
    return planned;
  }

}
