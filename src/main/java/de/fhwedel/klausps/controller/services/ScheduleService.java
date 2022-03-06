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
   * de.fhwedel.klausps.controller.restriction.Restriction}s
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
      // todo return block and pruefung?
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
      // todo muss hier etwas zurückgegeben werden?
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
   * Schedules a Block. If any Pruefung contained in the Block causes a {@link
   * de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard Restriction} violation, the
   * Block cannot be scheduled and a {@link HartesKriteriumException} is thrown. If no violation
   * occurs the Block gets scheduled and the scoring of all affected Pruefungen will be
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
    Block blockModel = dataAccessService.scheduleBlock(roBlock, termin);
    // rollback and Exception for hard violation
    checkHardCriteriaUndoScheduling(roBlock, blockModel);
    return getAffectedPruefungenBy(blockModel);
  }

  /**
   * Checks if any {@link de.fhwedel.klausps.controller.restriction.hard.HardRestriction hard
   * Restriction} is violated for a scheduled Block. In case of a violation there are two cases to
   * consider.<br> If the Block was already scheduled, it gets rescheduled to its old position
   * otherwise it remains unscheduled. In both cases an {@link HartesKriteriumException Exception}
   * is thrown.<br> If no restriction is violated, the Block remains scheduled at its current
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
   * Planungseinheit Planungseinheiten}
   *
   * @param block
   * @return
   * @throws NoPruefungsPeriodeDefinedException
   * @throws IllegalStateException
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


  public int scoringOfPruefung(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    return restrictionService.getScoringOfPruefung(pruefung);
  }


  public Set<Planungseinheit> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException,
      IllegalArgumentException {
    noNullParameters(pruefung, termin);
    Set<Planungseinheit> old = new HashSet<>();
    if(pruefung.geplant()){
      old.addAll(getAffectedPruefungenBy(dataAccessService.getPruefung(pruefung)));
      old.remove(dataAccessService.getPruefung(pruefung));
    }
    Pruefung pruefungModel = dataAccessService.schedulePruefung(pruefung, termin);
    checkHardCriteriaUndoScheduling(pruefung, pruefungModel);
    old.addAll(getAffectedPruefungenBy(pruefungModel));
    return old;
  }

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


  @NotNull
  private Set<Planungseinheit> getAffectedPruefungenBy(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> result = new HashSet<>();
    if (pruefung.isGeplant()) {
      result.addAll(
          getPlanungseinheitenWithBlock(restrictionService.getPruefungenAffectedBy(pruefung)));
    }
    return result;
  }

  @NotNull
  public List<Planungseinheit> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException, IllegalStateException, IllegalArgumentException {
    noNullParameters(pruefung, dauer);
    if (dauer.isNegative() || dauer.isZero()) {
      throw new IllegalArgumentException("Dauer muss > 0 sein.");
    }
    Pruefung pruefungModel = dataAccessService.getPruefung(pruefung);
    Duration oldDuration = pruefungModel.getDauer();
    pruefungModel.setDauer(dauer);
    List<HardRestrictionAnalysis> hard = restrictionService.checkHardRestrictions(
        pruefungModel);
    if (!hard.isEmpty()) {
      pruefungModel.setDauer(oldDuration);
      throw converter.convertHardException(hard);
    }
    return new ArrayList<>(getAffectedPruefungenBy(pruefungModel));
  }

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

  private void checkValidTeilnehmerkreisSchaetzung(int schaetzung) throws IllegalArgumentException {
    if (schaetzung < 0) {
      throw new IllegalArgumentException("Schätzwert darf nicht negativ sein.");
    }
  }

  @NotNull
  public Set<Planungseinheit> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    dataAccessService.setKapazitaetStudents(kapazitaet);
    Set<Planungseinheit> result = new HashSet<>(); // "Set" avoids duplicate entries
    for (Pruefung pruefung : dataAccessService.getPlannedPruefungen()) {
      result.addAll(getAffectedPruefungenBy(pruefung));
    }
    return result;
  }

  @NotNull
  public Set<Pruefung> getGeplantePruefungenWithKonflikt(
      ReadOnlyPlanungseinheit planungseinheitToCheckFor) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(planungseinheitToCheckFor);
    Planungseinheit planungseinheit = getAsModel(planungseinheitToCheckFor);
    return restrictionService.getPruefungenPotentiallyInHardConflictWith(planungseinheit);
  }

  private Planungseinheit getAsModel(ReadOnlyPlanungseinheit planungseinheitToCheckFor)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    if (planungseinheitToCheckFor.isBlock()) {
      return dataAccessService.getBlock(planungseinheitToCheckFor.asBlock());
    } else {
      return dataAccessService.getPruefung(planungseinheitToCheckFor.asPruefung());
    }
  }

  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(pruefung);
    List<SoftRestrictionAnalysis> analyses = restrictionService.checkWeicheKriterien(
        dataAccessService.getPruefung(pruefung));
    return converter.convertAnalyseList(analyses);
  }

  public Set<LocalDateTime> getHardConflictedTimes(Set<LocalDateTime> timesToCheck,
      ReadOnlyPlanungseinheit planungseinheitToCheck)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters(timesToCheck, planungseinheitToCheck);

    Planungseinheit planungseinheit = getPlanungseinheit(planungseinheitToCheck);
    return calcHardConflictingTimes(timesToCheck, planungseinheit);
  }

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

  public List<ReadOnlyPlanungseinheit> setBlockType(ReadOnlyBlock block, Blocktyp changeTo)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Block modelBlock = dataAccessService.getBlock(block);
    checkExistenceOfPruefungenInBlock(block);
    if (block.getTyp() == changeTo) {
      LOGGER.trace("Not changing block type, same type already set.");
      return emptyList();
    }
    if (block.ungeplant()) {
      LOGGER.debug("Set type of {} to {}.", modelBlock, changeTo);
      modelBlock.setTyp(changeTo);
      return List.of(converter.convertToROBlock(modelBlock));
    }
    Set<Pruefung> affected = restrictionService.getPruefungenAffectedByAnyBlock(modelBlock);
    modelBlock.setTyp(changeTo);
    affected.addAll(restrictionService.getPruefungenAffectedByAnyBlock(modelBlock));
    List<HardRestrictionAnalysis> hard = restrictionService.checkHardRestrictions(
        affected);
    if (!hard.isEmpty()) {
      modelBlock.setTyp(block.getTyp());
      throw converter.convertHardException(hard);
    }
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
   * the whole Block will be unscheduled.
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

  public Set<Planungseinheit> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    checkDatesMaybeException(startDatum, endDatum);

    Set<PruefungWithScoring> before = getPlannedPruefungenWithScoring();

    dataAccessService.setDatumPeriode(startDatum, endDatum);

    Set<PruefungWithScoring> after = getPlannedPruefungenWithScoring();
    return getPlanungseinheitenWithBlock(PlanungseinheitUtil.changedScoring(before, after));
  }

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
