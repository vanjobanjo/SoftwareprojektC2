package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    checkHardCriteriaUndo(pruefung, pruefungModel);
    return checkSoftCriteria(pruefungModel);
  }

  @NotNull
  private List<ReadOnlyPlanungseinheit> checkSoftCriteria(Pruefung pruefungModel) {
    Set<Pruefung> changedScoring = restrictionService.getAffectedPruefungen(pruefungModel);
    return new LinkedList<>(
        converter.convertToROPlanungseinheitCollection(getPlanungseinheitenWithBlock(
            changedScoring)));
  }

  @NotNull
  private List<ReadOnlyPlanungseinheit> checkSoftCriteria(Block blockModel) {
    Set<Pruefung> changedScoring = new HashSet<>();
    for (Pruefung p : blockModel.getPruefungen()) {
      changedScoring.addAll(restrictionService.getAffectedPruefungen(p));
    }
    return new LinkedList<>(
        converter.convertToROPlanungseinheitCollection(getPlanungseinheitenWithBlock(
            changedScoring)));
  }

  private void checkHardCriteriaUndo(ReadOnlyPruefung pruefung, Pruefung pruefungModel)
      throws HartesKriteriumException {
    List<HartesKriteriumAnalyse> hard = restrictionService.checkHarteKriterien(
        pruefungModel);

    if (!hard.isEmpty()) {
      // reverse
      dataAccessService.schedulePruefung(pruefung, pruefung.getTermin().get());
      HartesKriteriumException exHard = converter.convertHardException(hard); //TODO #172
      throw exHard;
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
    Pruefung pruefungModel = dataAccessService.unschedulePruefung(pruefung);
    return checkSoftCriteria(pruefungModel);
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

    checkHardCriteriaUndo(roBlock, blockModel);

    return checkSoftCriteria(blockModel);
  }

  private void checkHardCriteriaUndo(ReadOnlyBlock roBlock, Block modelBlock)
      throws HartesKriteriumException {
    List<HartesKriteriumAnalyse> list = new LinkedList<>();
    for (Pruefung p : modelBlock.getPruefungen()) {
      list.addAll(restrictionService.checkHarteKriterien(p));
    }
    if (!list.isEmpty()) {
      dataAccessService.scheduleBlock(roBlock, roBlock.getTermin().get());
      HartesKriteriumException exHard = converter.convertHardException(list);
      throw exHard;
    }
  }

  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block) {
    Block blockModel = dataAccessService.unscheduleBlock(block);
    return checkSoftCriteria(blockModel);// TODO return result of test for conflicts
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
    // TODO get scoring from some kind of cache
    return 0; // TODO implement
  }

  public Optional<ReadOnlyBlock> deletePruefung(ReadOnlyPruefung pruefung) {
    dataAccessService.deletePruefung(pruefung);
    Pruefung modelPruefung = dataAccessService.getPruefungWith(pruefung.getPruefungsnummer());
    List<WeichesKriteriumAnalyse> analyses = restrictionService.checkWeicheKriterien(modelPruefung);
    // calc new score for all pruefungen
    //TODO keine geplante Klausuren löschen
    Map<String, Integer> scoring = getScoringFrom(analyses);
    applyScoring(scoring);

    return Optional.of(
        converter.convertToROBlock(dataAccessService.getBlockTo(modelPruefung).get()));

  }

  private Map<String, Integer> getScoringFrom(List<WeichesKriteriumAnalyse> analyses) {
    // TODO extract into adequate class
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  private void applyScoring(Map<String, Integer> scoring) {
    // TODO extract into adequate class
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  public List<ReadOnlyPruefung> deleteBlock(ReadOnlyBlock block) {
    if (!dataAccessService.exists(block)) {
      throw new IllegalArgumentException("Block existiert nicht!");
    }
    if (block.geplant()) {
      throw new IllegalArgumentException("Block ist geplant!");
    }

    return dataAccessService.deleteBlock(block); //scoring must be 0
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


  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws HartesKriteriumException {
    List<ReadOnlyPlanungseinheit> result = new LinkedList<>();
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
    return result;
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

  /*
  public List<ReadOnlyPruefung> movePruefung(ReadOnlyPruefung pruefung, LocalDateTime expectedStart)
      throws HartesKriteriumException {
    LocalDateTime currentStart = dataAccessService.getStartOfPruefungWith(
        pruefung.getPruefungsnummer()).orElseThrow(
        () -> new IllegalArgumentException("Nur geplante Pruefungen können verschoben werden!"));
    dataAccessService.schedulePruefung(pruefung, expectedStart);
    List<HartesKriteriumAnalyse> hardRestrictionFailures = restrictionService.checkHarteKriterien();
    if (!hardRestrictionFailures.isEmpty()) {
      dataAccessService.schedulePruefung(pruefung, currentStart);
      signalHartesKriteriumFailure(hardRestrictionFailures);
    }
    return new ArrayList<>((getPruefungenInvolvedIn(restrictionService.checkWeicheKriterien())));
  }*/


  public List<ReadOnlyPruefung> addTeilnehmerKreis(Pruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis) throws HartesKriteriumException {

    if (roPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return new ArrayList<>();
    }
    List<ReadOnlyPruefung> listOfRead = new ArrayList<>();

    if (this.dataAccessService.addTeilnehmerkreis(roPruefung, teilnehmerkreis)) {
      try {
        //TODO hier auf HarteRestirktionen testen dann noch auf Weiche und dann Liste zurückgeben
        //   listOfRead = testHartKriterium(roPruefung);
        //   listOfRead.addAll()
        throw new HartesKriteriumException(null, null, null);
      } catch (HartesKriteriumException e) {
        this.dataAccessService.removeTeilnehmerkreis(roPruefung, teilnehmerkreis);
        throw e;
      }
    }
    //TODO weiche KriteriumsAnalysen machen und hinzufügen
    return listOfRead;

  }

  public List<ReadOnlyPruefung> remmoveTeilnehmerKreis(Pruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis) throws HartesKriteriumException {

    if (!roPruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
      return new ArrayList<>();
    }
    List<ReadOnlyPruefung> listOfRead = new ArrayList<>();

    if (this.dataAccessService.removeTeilnehmerkreis(roPruefung, teilnehmerkreis)) {
      try {
        //TODO hier auf HarteRestirktionen testen dann noch auf Weiche und dann Liste zurückgeben
        //listOfRead = signalHartesKriteriumFailure(null);
        throw new HartesKriteriumException(null, null, null);
      } catch (HartesKriteriumException e) {
        this.dataAccessService.addTeilnehmerkreis(roPruefung, teilnehmerkreis);
        throw e;
      }
    }
    //TODO weiche KriteriumsAnalysen machen und hinzufügen
    return listOfRead;
  }

  private List<ReadOnlyPruefung> testHartKriterium(ReadOnlyPruefung roPruefung)
      throws HartesKriteriumException {

    throw new IllegalStateException("Not implemented yet!");
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
      result.addAll(hartesKriteriumAnalyse.getCausingPruefungen());
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

}
