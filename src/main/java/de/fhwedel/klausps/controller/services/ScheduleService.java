package de.fhwedel.klausps.controller.services;

import static java.util.Collections.emptyList;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;


public class ScheduleService {

  private final DataAccessService dataAccessService;

  private final RestrictionService restrictionService;

  private final Converter converter = new Converter();

  public ScheduleService(DataAccessService dataAccessService,
      RestrictionService restrictionService) {
    this.dataAccessService = dataAccessService;
    this.restrictionService = restrictionService;
    this.converter.setScheduleService(this);
  }

  @NotNull
  private List<ReadOnlyPlanungseinheit> affectedPruefungenSoft(Pruefung pruefungModel) {
    if (!pruefungModel.isGeplant()) {
      return Collections.emptyList();
    }
    Set<Pruefung> changedScoring = restrictionService.getAffectedPruefungen(pruefungModel);
    return new LinkedList<>(
        converter.convertToROPlanungseinheitCollection(getPlanungseinheitenWithBlock(
            changedScoring)));
  }

  @NotNull
  private List<ReadOnlyPlanungseinheit> affectedPruefungenSoft(Block blockModel) {
    if (!blockModel.isGeplant()) {
      return Collections.emptyList();
    }
    Set<Pruefung> changedScoring = new HashSet<>();
    for (Pruefung p : blockModel.getPruefungen()) {
      changedScoring.addAll(restrictionService.getAffectedPruefungen(p));
    }
    return new LinkedList<>(
        converter.convertToROPlanungseinheitCollection(getPlanungseinheitenWithBlock(
            changedScoring)));
  }

  private void checkHardCriteriaUndoScheduling(ReadOnlyPruefung pruefung, Pruefung pruefungModel)
      throws HartesKriteriumException {
    List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterien(
        pruefungModel);

    if (!hard.isEmpty()) {
      // reverse
      dataAccessService.schedulePruefung(pruefung, pruefung.getTermin().get());
      throw converter.convertHardException(hard);
    }
  }

  private Set<Planungseinheit> getPlanungseinheitenWithBlock(Set<Pruefung> pruefungen) {
    Set<Planungseinheit> planungseinheiten = new HashSet<>();
    for (Pruefung p : pruefungen) {
      Optional<Block> blockOpt = dataAccessService.getBlockTo(p);
      blockOpt.ifPresent(planungseinheiten::add);
      planungseinheiten.add(p);
    }
    return planungseinheiten;
  }

  /**
   * Nimmt eine uebergebene Pruefung aus der Planung. Übergebene Pruefung muss Teil des
   * Rückgabewertes sein.
   *
   * @param pruefung Pruefung zum ausplanen
   * @return Liste von veraenderte Pruefungen
   */
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefung) {
    Pruefung pruefungModel = dataAccessService.getPruefungWith(pruefung.getPruefungsnummer());
    List<ReadOnlyPlanungseinheit> list = affectedPruefungenSoft(pruefungModel);
    pruefungModel = dataAccessService.unschedulePruefung(pruefung);
    list.add(converter.convertToReadOnlyPruefung(pruefungModel));
    return list;
  }


  public List<ReadOnlyPlanungseinheit> scheduleBlock(ReadOnlyBlock roBlock,
      LocalDateTime termin) throws HartesKriteriumException {
    if (!dataAccessService.terminIsInPeriod(termin)) {
      throw new IllegalArgumentException(
          "Der angegebene Termin liegt ausserhalb der Pruefungsperiode.");
    }

    if (roBlock.getROPruefungen().isEmpty()) {
      throw new IllegalArgumentException("Leere Bloecke duerfen nicht geplant werden.");
    }
    Block blockModel = dataAccessService.scheduleBlock(roBlock, termin);

    checkHardCriteriaUndoScheduling(roBlock, blockModel);

    return affectedPruefungenSoft(blockModel);
  }

  private void checkHardCriteriaUndoScheduling(ReadOnlyBlock roBlock, Block modelBlock)
      throws HartesKriteriumException {
    List<HartesKriteriumAnalyse> list = new LinkedList<>();
    for (Pruefung p : modelBlock.getPruefungen()) {
      list.addAll(restrictionService.checkHarteKriterien(p));
    }
    if (!list.isEmpty()) {
      dataAccessService.scheduleBlock(roBlock, roBlock.getTermin().get());
      throw converter.convertHardException(list);
    }
  }

  //TODO siehe unschedulePrufung, so noch nicht richtig.
  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block) {
    Block blockModel = dataAccessService.unscheduleBlock(block);
    return affectedPruefungenSoft(blockModel);
  }

  /**
   * Ändert die Dauer einer übergebenen Prüfung. Die übergebene Prüfung muss beim erfolgreichen
   * Verändern auch Teil der Rückgabe sein.
   *
   * @param pruefung Pruefung, dessen Dauer geändert werden muss.
   * @param minutes  die neue Dauer
   * @return Liste von Pruefung, jene die sich durch die Operation geändert haben.
   */
  public List<Pruefung> changeDuration(Pruefung pruefung, Duration minutes)
      throws HartesKriteriumException {
    // todo please implement
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * Gibt das Scoring zu einer übergebenen Pruefung zurück. Wenn Klausur ungeplant, dann 0.
   *
   * @param pruefung Pruefung, dessen Scoring bestimmt werden soll
   * @return Scoring ungeplant ? 0 : scoring
   */
  public int scoringOfPruefung(Pruefung pruefung) {
    return restrictionService.getScoringOfPruefung(pruefung);
  }

  public Optional<ReadOnlyBlock> deletePruefung(ReadOnlyPruefung pruefung) {
    Block block = dataAccessService.deletePruefung(pruefung);
    return block == null ? Optional.empty() : Optional.of(converter.convertToROBlock(block));
  }

  private void checkHartesKriteriumAddPruefungToBlock(ReadOnlyPruefung pruefung,
      ReadOnlyBlock block,
      Optional<ReadOnlyBlock> oldBlock, Pruefung addedPruefung)
      throws HartesKriteriumException {
    List<HartesKriteriumAnalyse> hardAnalyses = restrictionService.checkHarteKriterien(
        addedPruefung);
    if (!hardAnalyses.isEmpty()) {
      removePruefungFromBlock(block, pruefung);
      if (oldBlock.isPresent()) {
        addPruefungToBlock(oldBlock.get(), pruefung);
      } else if (pruefung.geplant()) {
        schedulePruefung(pruefung, pruefung.getTermin().get());
      }
      signalHartesKriteriumFailure(hardAnalyses);
    }
  }

  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) {
    List<ReadOnlyPlanungseinheit> result = new LinkedList<>();
    // todo vorher Analyse
    Pair<Block, Pruefung> separated = dataAccessService.removePruefungFromBlock(block, pruefung);
    if (!block.geplant()) {
      result.addAll(converter.convertToROPlanungseinheitCollection(separated.left(),
          separated.right()));
    } else {
      // todo update scoring and add changed Planungseinheiten to result
      // scoring für prüfung wird 0, weil sie ungeplant wird
      // für alle betroffenen Klausuren aus alter Analyse Scoring neu berechnen und in
      // Liste hinzufügen
    }
    return result;
  }

  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(@NotNull ReadOnlyBlock block,
      @NotNull ReadOnlyPruefung pruefung) throws HartesKriteriumException {
    Objects.requireNonNull(block);
    Objects.requireNonNull(pruefung);
    if (pruefung.geplant()) {
      throw new IllegalArgumentException("Planned Pruefungen can not be added to a Block.");
    }
    Optional<ReadOnlyBlock> oldBlock = dataAccessService.getBlockTo(pruefung);
    if (oldBlock.isPresent() && !oldBlock.get().equals(block)) {
      throw new IllegalArgumentException(
          "Pruefungen contained in a block can not be added to another block.");
    }
    return null;
    /*return new ArrayList<>(new BlockDTO(block.getName(), block.getTermin().orElse(null), block.getDauer(),
        Sets.union(block.getROPruefungen(), Set.of(pruefung)), block.getBlockId(), block.))*/
    /*List<ReadOnlyPlanungseinheit> result = new LinkedList<>();
    // todo get analyse before applying any changes
    Optional<ReadOnlyBlock> oldBlock = dataAccessService.getBlockTo(pruefung);
    Pair<Block, Pruefung> added = dataAccessService.addPruefungToBlock(block, pruefung);

    if (!block.geplant()) {
      result.addAll(converter.convertToROPlanungseinheitCollection(added.left(), added.right()));
      if (pruefung.geplant()) {
        // todo analyse scoring for affected Pruefungen
        //  block is not planned therefore new scorings can only get better (no hard check needed)
      }
      return result;
    }
    checkHartesKriteriumAddPruefungToBlock(pruefung, block, oldBlock, added.right());
    // todo check soft criteria
    return result;*/
    /*return null;*/
  }

  /**
   * Plant eine uebergebene Pruefung ein! Die uebergebene Pruefung muss Teil des Rueckgabewertes
   * sein.
   *
   * @param pruefung Pruefung die zu planen ist.
   * @param termin   Starttermin
   * @return Liste von veränderten Ergebnissen
   */
  public List<ReadOnlyPlanungseinheit> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime termin)
      throws HartesKriteriumException {

    Pruefung pruefungModel = dataAccessService.schedulePruefung(pruefung, termin);
    checkHardCriteriaUndoScheduling(pruefung, pruefungModel);
    return affectedPruefungenSoft(pruefungModel);
  }

  private Set<Pruefung> getPruefungenInvolvedIn(
      List<WeichesKriteriumAnalyse> weicheKriterien) {
    Set<Pruefung> result = new HashSet<>();
    for (WeichesKriteriumAnalyse weichesKriteriumAnalyse : weicheKriterien) {
      result.addAll(weichesKriteriumAnalyse.getCausingPruefungen());
    }
    return result;
  }

  private void signalHartesKriteriumFailure(List<HartesKriteriumAnalyse> hardRestrictionFailures)
      throws HartesKriteriumException {
    Set<ReadOnlyPruefung> causingPruefungen = getPruefungenInvolvedIn(hardRestrictionFailures);
    throw new HartesKriteriumException(getPruefungenInvolvedIn(hardRestrictionFailures),
        getAllTeilnehmerkreiseFrom(hardRestrictionFailures), 0);
    // TODO number of affected students can not be calculated correctly when multiple analyses
    //  affect the same teilnehmerkreise, therefore currently set to 0
  }

  private Set<ReadOnlyPruefung> getPruefungenInvolvedIn(
      Iterable<HartesKriteriumAnalyse> hartesKriteriumAnalysen) {
    Set<ReadOnlyPruefung> result = new HashSet<>();
    for (HartesKriteriumAnalyse hartesKriteriumAnalyse : hartesKriteriumAnalysen) {
      result.addAll(
          converter.convertToROPruefungCollection(hartesKriteriumAnalyse.getCausingPruefungen()));
    }
    return result;
  }

  private Set<Teilnehmerkreis> getAllTeilnehmerkreiseFrom(
      Iterable<HartesKriteriumAnalyse> hartesKriteriumAnalysen) {
    Set<Teilnehmerkreis> result = new HashSet<>();
    for (HartesKriteriumAnalyse hartesKriteriumAnalyse : hartesKriteriumAnalysen) {
      result.addAll(hartesKriteriumAnalyse.getAffectedTeilnehmerkreise());
    }
    return result;
  }


  public List<ReadOnlyPlanungseinheit> removeTeilnehmerKreis(ReadOnlyPruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis) {

    List<ReadOnlyPlanungseinheit> listOfRead = new ArrayList<>();
    if (!roPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return listOfRead;
    }

    Pruefung pruefungModel = this.dataAccessService.getPruefungWith(
        roPruefung.getPruefungsnummer());

    //Damit man eine Liste hat, wo sich das Scoring ändert
    listOfRead = affectedPruefungenSoft(pruefungModel);

    //Hier muss ja nicht auf HarteKriteren gecheckt werden.
    this.dataAccessService.removeTeilnehmerkreis(pruefungModel, teilnehmerkreis);

    return listOfRead;
  }

  public List<ReadOnlyPlanungseinheit> addTeilnehmerkreis(ReadOnlyPruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) throws HartesKriteriumException {

    List<ReadOnlyPlanungseinheit> listOfRead = new ArrayList<>();

    if (roPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return listOfRead;
    }
    Pruefung pruefungModel = this.dataAccessService.getPruefungWith(
        roPruefung.getPruefungsnummer());

    if (this.dataAccessService.addTeilnehmerkreis(pruefungModel, teilnehmerkreis, schaetzung)) {
      List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterien(
          pruefungModel);
      if (!hard.isEmpty()) {
        this.dataAccessService.removeTeilnehmerkreis(pruefungModel, teilnehmerkreis);
        throw converter.convertHardException(hard);
      }
    }
    listOfRead = affectedPruefungenSoft(pruefungModel);

    return listOfRead;
  }

  public List<ReadOnlyPlanungseinheit> setTeilnehmerkreisSchaetzung(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) {
    Pruefung modelPruefung = dataAccessService.getPruefungWith(pruefung.getPruefungsnummer());
    if (!modelPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      throw new IllegalArgumentException("Pruefung hat keinen Teilnehmerkreis mit diesem Namen.");
    }
    modelPruefung.setSchaetzung(teilnehmerkreis, schaetzung);
    if (modelPruefung.isGeplant()) {
      return affectedPruefungenSoft(modelPruefung);
    }
    return emptyList();
  }

  public List<ReadOnlyPlanungseinheit> setKapazitaetPeriode(int kapazitaet) {
    dataAccessService.setKapazitaetPeriode(kapazitaet);
    // collect in set => same Pruefung will not be added twice
    Set<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Pruefung pruefung : dataAccessService.getGeplanteModelPruefung()) {
      result.addAll(affectedPruefungenSoft(pruefung));
    }
    return new LinkedList<>(result);
  }
}
