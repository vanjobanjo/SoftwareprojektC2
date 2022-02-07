package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
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


public class ScheduleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

  private final DataAccessService dataAccessService;

  private final RestrictionService restrictionService;

  private final Converter converter;

  public ScheduleService(DataAccessService dataAccessService, RestrictionService restrictionService,
      Converter converter) {
    this.dataAccessService = dataAccessService;
    this.restrictionService = restrictionService;
    this.converter = converter;
    converter.setScheduleService(this);
  }

  /**
   * Nimmt eine uebergebene Pruefung aus der Planung. Übergebene Pruefung muss Teil des
   * Rückgabewertes sein.
   *
   * @param pruefungToUnschedule Pruefung zum ausplanen
   * @return Liste von veraenderte Pruefungen
   */
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefungToUnschedule)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefungToUnschedule);
    Pruefung pruefung = getPruefungIfExistent(pruefungToUnschedule);
    Set<Pruefung> affectedPruefungen = restrictionService.getPruefungenAffectedBy(pruefung);
    dataAccessService.unschedulePruefung(pruefungToUnschedule);
    List<ReadOnlyPlanungseinheit> result = calculateScoringForCachedAffected(affectedPruefungen);
    result.add(converter.convertToReadOnlyPruefung(pruefung));
    return result;
  }

  @NotNull
  private Pruefung getPruefungIfExistent(ReadOnlyPruefung pruefungToGet)
      throws NoPruefungsPeriodeDefinedException {
    Optional<Pruefung> pruefung = dataAccessService.getPruefung(pruefungToGet);
    if (pruefung.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Pruefung %s does not exists.", pruefungToGet));
    }
    return pruefung.get();
  }

  private List<ReadOnlyPlanungseinheit> calculateScoringForCachedAffected(Set<Pruefung> affected)
      throws NoPruefungsPeriodeDefinedException {
    return new ArrayList<>(
        converter.convertToROPlanungseinheitSet(getPlanungseinheitenWithBlock(affected)));
  }

  private Set<Planungseinheit> getPlanungseinheitenWithBlock(Set<Pruefung> pruefungen)
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> planungseinheiten = new HashSet<>();
    for (Pruefung p : pruefungen) {
      Optional<Block> blockOpt = dataAccessService.getBlockTo(p);
      blockOpt.ifPresent(planungseinheiten::add);
      planungseinheiten.add(p);
    }
    return planungseinheiten;
  }

  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(block, pruefung);
    if (block.getROPruefungen().contains(pruefung)) {
      return emptyList();
    }
    if (pruefung.geplant()) {
      throw new IllegalArgumentException("Planned Pruefungen can not be added to a Block.");
    }
    Optional<Block> oldBlock = dataAccessService.getBlockTo(pruefung);
    if (oldBlock.isPresent() && (oldBlock.get().getId() != block.getBlockId())) {
      throw new IllegalArgumentException(
          "Pruefungen contained in a block can not be added to another block.");
    }
    if (!block.geplant()) {
      return emptyList();
    }
    Block newBlock = dataAccessService.addPruefungToBlock(block, pruefung);
    checkHardCriteriaUndoAddPruefungToBlock(pruefung, block);
    return getAffectedPruefungenBy(newBlock);
  }

  private void checkHardCriteriaUndoAddPruefungToBlock(ReadOnlyPruefung pruefung,
      ReadOnlyBlock block) throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Pruefung modelPruefung = getPruefungIfExistent(pruefung);
    List<HartesKriteriumAnalyse> hardAnalyses = restrictionService.checkHarteKriterien(
        modelPruefung);
    if (!hardAnalyses.isEmpty()) {
      removePruefungFromBlock(block, pruefung);
      throw converter.convertHardException(hardAnalyses);
    }
  }

  @NotNull
  private List<ReadOnlyPlanungseinheit> getAffectedPruefungenBy(Block blockModel)
      throws NoPruefungsPeriodeDefinedException {
    if (!blockModel.isGeplant()) {
      return emptyList();
    }
    Set<Pruefung> changedScoring = new HashSet<>();
    for (Pruefung p : blockModel.getPruefungen()) {
      changedScoring.addAll(restrictionService.getPruefungenAffectedBy(p));
    }
    return new LinkedList<>(converter.convertToROPlanungseinheitSet(
        getPlanungseinheitenWithBlock(changedScoring)));
  }

  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, pruefung);
    Pruefung toRemove = getPruefungIfExistent(pruefung);
    Optional<Block> toRemoveFrom = dataAccessService.getBlockTo(toRemove);
    if (toRemoveFrom.isEmpty()) {
      return emptyList();
    }
    if (!block.geplant()) {
      Block resultingBlock = dataAccessService.removePruefungFromBlock(block, pruefung);
      Pruefung unscheduledPruefung = getPruefungIfExistent(pruefung);
      return new ArrayList<>(
          converter.convertToROPlanungseinheitSet(resultingBlock, unscheduledPruefung));
    }

    Set<Pruefung> affectedPruefungen = restrictionService.getPruefungenAffectedBy(toRemove);
    Block blockWithoutPruefung = dataAccessService.removePruefungFromBlock(block, pruefung);
    List<ReadOnlyPlanungseinheit> result = calculateScoringForCachedAffected(affectedPruefungen);
    result.add(converter.convertToReadOnlyPlanungseinheit(blockWithoutPruefung));
    return result;

  }

  public List<ReadOnlyPlanungseinheit> scheduleBlock(ReadOnlyBlock roBlock, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(termin);
    if (!dataAccessService.terminIsInPeriod(termin)) {
      throw new IllegalArgumentException(
          "Der angegebene Termin liegt ausserhalb der Pruefungsperiode.");
    }

    if (roBlock.getROPruefungen().isEmpty()) {
      throw new IllegalArgumentException("Leere Bloecke duerfen nicht geplant werden.");
    }
    Block blockModel = dataAccessService.scheduleBlock(roBlock, termin);
    checkHardCriteriaUndoScheduling(roBlock, blockModel);
    return getAffectedPruefungenBy(blockModel);
  }

  private void checkHardCriteriaUndoScheduling(ReadOnlyBlock roBlock, Block modelBlock)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    List<HartesKriteriumAnalyse> list = new LinkedList<>();
    for (Pruefung p : modelBlock.getPruefungen()) {
      list.addAll(restrictionService.checkHarteKriterien(p));
    }
    Optional<LocalDateTime> termin = roBlock.getTermin();
    if (!list.isEmpty() && termin.isPresent()) {
      dataAccessService.scheduleBlock(roBlock, termin.get());
      throw converter.convertHardException(list);
    }
  }

  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    Optional<Block> blockModel = dataAccessService.getBlock(block);
    if (blockModel.isEmpty()) {
      throw new IllegalArgumentException("Block does not exist");
    }
    Set<Pruefung> affected = restrictionService.getPruefungenAffectedBy(blockModel.get());
    Block unscheduledBlock = dataAccessService.unscheduleBlock(block);
    List<ReadOnlyPlanungseinheit> result = new ArrayList<>();
    result.add(converter.convertToROBlock(unscheduledBlock));
    result.addAll(calculateScoringForCachedAffected(affected));
    return result;
  }


  /**
   * Gibt das Scoring zu einer übergebenen Pruefung zurück. Wenn Klausur ungeplant, dann 0.
   *
   * @param pruefung Pruefung, dessen Scoring bestimmt werden soll
   * @return Scoring ungeplant ? 0 : scoring
   */
  public int scoringOfPruefung(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    return restrictionService.getScoringOfPruefung(pruefung);
  }

  public Optional<ReadOnlyBlock> deletePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    Block block = dataAccessService.deletePruefung(pruefung);
    return block == null ? Optional.empty() : Optional.of(converter.convertToROBlock(block));
  }

  /**
   * Plant eine uebergebene Pruefung ein! Die uebergebene Pruefung muss Teil des Rueckgabewertes
   * sein.
   *
   * @param pruefung Pruefung die zu planen ist.
   * @param termin   Starttermin
   * @return Liste von veränderten Ergebnissen
   */
  public Set<Planungseinheit> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime termin) throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, termin);
    Pruefung pruefungModel = dataAccessService.schedulePruefung(pruefung, termin);
    checkHardCriteriaUndoScheduling(pruefung, pruefungModel);
    return getAffectedPruefungenBy(pruefungModel);
  }

  private void checkHardCriteriaUndoScheduling(ReadOnlyPruefung pruefung, Pruefung pruefungModel)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterien(pruefungModel);

    Optional<LocalDateTime> termin = pruefung.getTermin();
    if (!hard.isEmpty() && termin.isPresent()) {
      // reverse
      dataAccessService.schedulePruefung(pruefung, termin.get());
      throw converter.convertHardException(hard);
    }
  }

  @NotNull
  private Set<Planungseinheit> getAffectedPruefungenBy(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Set<Planungseinheit> result = new HashSet<>();
    if (pruefung.isGeplant()) {
      result.addAll(restrictionService.getPruefungenAffectedBy(pruefung));
    }
    return result;
  }

  @NotNull
  public List<Planungseinheit> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, dauer);
    Optional<Pruefung> pruefungModel = dataAccessService.getPruefung(pruefung);
    if (pruefungModel.isPresent()) {
      Duration oldDuration = pruefungModel.get().getDauer();
      pruefungModel.get().setDauer(dauer);
      List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterien(
          pruefungModel.get());
      if (!hard.isEmpty()) {
        pruefungModel.get().setDauer(oldDuration);
        throw converter.convertHardException(hard);
      }
    } else {
      throw new IllegalArgumentException("Unknown Pruefung");
    }
    return new ArrayList<>(getAffectedPruefungenBy(pruefungModel.get()));
  }

  public List<Planungseinheit> removeTeilnehmerKreis(ReadOnlyPruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmerkreis);
    if (!roPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return new ArrayList<>();
    }

    Pruefung pruefungModel = getPruefungIfExistent(roPruefung);

    //Damit man eine Liste hat, wo sich das Scoring ändert
    Set<Planungseinheit> listOfRead = new HashSet<>(getAffectedPruefungenBy(pruefungModel));

    //Hier muss ja nicht auf HarteKriteren gecheckt werden.
    this.dataAccessService.removeTeilnehmerkreis(pruefungModel, teilnehmerkreis);

    return new ArrayList<>(listOfRead);
  }

  public Set<Planungseinheit> addTeilnehmerkreis(ReadOnlyPruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(roPruefung, teilnehmerkreis);
    Set<Planungseinheit> listOfRead = new HashSet<>();

    if (roPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return listOfRead;
    }
    Pruefung pruefungModel = getPruefungIfExistent(roPruefung);

    if (dataAccessService.addTeilnehmerkreis(pruefungModel, teilnehmerkreis, schaetzung)) {
      List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterien(pruefungModel);
      if (!hard.isEmpty()) {
        dataAccessService.removeTeilnehmerkreis(pruefungModel, teilnehmerkreis);
        throw converter.convertHardException(hard);
      }
    }
    listOfRead = getAffectedPruefungenBy(pruefungModel);

    return listOfRead;
  }

  public Set<Planungseinheit> setTeilnehmerkreisSchaetzung(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis);
    Pruefung modelPruefung = getPruefungIfExistent(pruefung);
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
    return restrictionService.getPruefungenInHardConflictWith(planungseinheit);
  }

  private Planungseinheit getAsModel(ReadOnlyPlanungseinheit planungseinheitToCheckFor)
      throws NoPruefungsPeriodeDefinedException {
    if (planungseinheitToCheckFor.isBlock()) {
      Optional<Block> optionalBlock = dataAccessService.getBlock(
          planungseinheitToCheckFor.asBlock());
      if (optionalBlock.isEmpty()) {
        throw new IllegalArgumentException("Block does not exist");
      }
      return optionalBlock.get();
    } else {
      return getPruefungIfExistent(planungseinheitToCheckFor.asPruefung());
    }
  }

  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    List<WeichesKriteriumAnalyse> analyses = restrictionService.checkWeicheKriterien(
        getPruefungIfExistent(pruefung));
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
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    Planungseinheit planungseinheit;
    if (planungseinheitToCheck.isBlock()) {
      planungseinheit = getBlock(planungseinheitToCheck.asBlock());
    } else {
      planungseinheit = getPruefungIfExistent(planungseinheitToCheck.asPruefung());
    }
    return planungseinheit;
  }

  @NotNull
  private Set<LocalDateTime> calcHardConflictingTimes(@NotNull Set<LocalDateTime> timesToCheck,
      @NotNull Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    Set<LocalDateTime> result = new HashSet<>();
    for (LocalDateTime startTimeToCheck : timesToCheck) {
      if (restrictionService.wouldBeHardConflictIfStartedAt(startTimeToCheck, planungseinheit)) {
        result.add(startTimeToCheck);
      }
    }
    return result;
  }

  @NotNull
  private Block getBlock(@NotNull ReadOnlyBlock blockToGet)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Requesting block {} from Model.", blockToGet);
    Optional<Block> block = dataAccessService.getBlock(blockToGet.asBlock());
    if (block.isEmpty()) {
      throw new IllegalArgumentException("The handed Planungseinheit is not known.");
    }
    return block.get();
  }

  public List<ReadOnlyPlanungseinheit> setBlockType(ReadOnlyBlock block, Blocktyp changeTo)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Optional<Block> optionalBlock = dataAccessService.getBlock(block);
    if (optionalBlock.isEmpty()) {
      throw new IllegalArgumentException("Block does not exist.");
    }
    if (block.getTyp() == changeTo) {
      LOGGER.trace("Not changing block type, same type already set.");
      return emptyList();
    }
    Block modelBlock = optionalBlock.get();
    if (block.ungeplant()) {
      LOGGER.debug("Set type of {} to {}.", modelBlock, changeTo);
      modelBlock.setTyp(changeTo);
      return List.of(converter.convertToROBlock(modelBlock));
    }
    Set<Pruefung> affected = restrictionService.getPruefungenAffectedBy(modelBlock);
    modelBlock.setTyp(changeTo);

    List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterienAll(
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


  public void createEmptyAndAdoptPeriode(IOService ioService, Semester semester, LocalDate start,
      LocalDate end, LocalDate ankertag, int kapazitaet, Path path)
      throws ImportException, IOException {

    Pruefungsperiode fallbackPeriode = dataAccessService.getPruefungsperiode();
    try {
      ioService.createEmptyAndAdoptPeriode(semester, start, end, ankertag, kapazitaet, path);
      unscheduleHardConflictingFromAdoptedPeriode();
    } catch (NoPruefungsPeriodeDefinedException e) {
      dataAccessService.setPruefungsperiode(fallbackPeriode);
      throw new ImportException(
          "Prüfungsperiode konnte nicht adaptiert werden, alter Zustand wurde wieder hergestellt.");
    }

  }


  private void unscheduleHardConflictingFromAdoptedPeriode()
      throws NoPruefungsPeriodeDefinedException, ImportException {
    boolean foundConflicts = unscheduleConflictingBlocksFromAdoptedPeriode();
    foundConflicts |= unscheduleConflictingPruefungFromAdoptedPeriode();

    if (foundConflicts) {
      throw new ImportException(
          "Harte Konflikte in Prüfungsperiode gefunden, Planungseinheiten wurden ausgeplant");
    }
  }

  private boolean unscheduleConflictingBlocksFromAdoptedPeriode()
      throws NoPruefungsPeriodeDefinedException {
    boolean foundConflicts = false;
    Set<Planungseinheit> plannedPruefungen = sortPlanungseinheitenByStartzeitpunkt(
        dataAccessService.getGeplanteBloecke());
    for (Planungseinheit block : plannedPruefungen) {
      if (!restrictionService.checkHarteKriterienAll(block.asBlock().getPruefungen()).isEmpty()) {
        foundConflicts = true;
        block.setStartzeitpunkt(null);
      }
    }
    return foundConflicts;
  }

  private boolean unscheduleConflictingPruefungFromAdoptedPeriode()
      throws NoPruefungsPeriodeDefinedException {
    boolean foundConflicts = false;
    Set<Planungseinheit> plannedPruefungen = sortPlanungseinheitenByStartzeitpunkt(
        dataAccessService.getGeplantePruefungen());
    for (Planungseinheit pruefung : plannedPruefungen) {
      if (!restrictionService.checkHarteKriterien(pruefung.asPruefung()).isEmpty()) {
        foundConflicts = true;
        pruefung.setStartzeitpunkt(null);
      }
    }
    return foundConflicts;
  }

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

  public List<ReadOnlyPlanungseinheit> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    checkDatesMaybeException(startDatum, endDatum);

    Set<PruefungWithScoring> before = new HashSet<>();
    for (Pruefung pruefung : dataAccessService.getGeplantePruefungen()) {
      before.add(
          new PruefungWithScoring(pruefung, restrictionService.getScoringOfPruefung(pruefung)));
    }

    dataAccessService.setDatumPeriode(startDatum, endDatum);

    Set<PruefungWithScoring> after = new HashSet<>();
    for (Pruefung pruefung : dataAccessService.getGeplantePruefungen()) {
      before.add(
          new PruefungWithScoring(pruefung, restrictionService.getScoringOfPruefung(pruefung)));
    }

    return new LinkedList<>(converter.convertToROPlanungseinheitSet(
        getPlanungseinheitenWithBlock(PlanungseinheitUtil.changedScoring(before, after))));
  }

  private void checkDatesMaybeException(LocalDate startDatum, LocalDate endDatum)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    if (startDatum.isAfter(dataAccessService.getAnkertag())) {
      throw new IllegalTimeSpanException("Startdatum ist nach Ankertag");
    }
    if (endDatum.isBefore(dataAccessService.getAnkertag())) {
      throw new IllegalTimeSpanException("Ankertag ist nach Enddatum");
    }
    if (anyIsBefore(dataAccessService.getGeplantePruefungen(), startDatum)) {
      throw new IllegalArgumentException(
          "Startdatum ist nach geplanter Prüfungen, bitte Prüfungen entplanen");
    }
    if (anyIsAfter(dataAccessService.getGeplantePruefungen(), endDatum)) {
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

}
