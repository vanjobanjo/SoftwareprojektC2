package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScheduleService {

  private final DataAccessService dataAccessService;

  private final RestrictionService restrictionService;

  public ScheduleService(
      DataAccessService dataAccessService, RestrictionService restrictionService) {
    this.dataAccessService = dataAccessService;
    this.restrictionService = restrictionService;
  }

  /**
   * Plant eine uebergebene Pruefung ein! Die uebergebene Pruefung muss Teil des Rueckgabewertes
   * sein.
   *
   * @param pruefung Pruefung die zu planen ist.
   * @param termin Starttermin
   * @return Liste von veränderten Ergebnissen
   */
  public List<ReadOnlyPruefung> schedulePruefung(ReadOnlyPruefung pruefung, LocalDateTime termin)
      throws HartesKriteriumException {
    // todo before any restriction test: unschedule Pruefung
    //  then test restrictions
    //  if hard restriction violated, reschedule at previous position else schedule at new position
    // no check if pruefung exists needed, because DataAccessService already does it
    dataAccessService.schedulePruefung(pruefung, termin);
    return Collections.emptyList(); // TODO return result of test for conflicts
  }

  /**
   * Nimmt eine uebergebene Pruefung aus der Planung. Übergebene Pruefung muss Teil des
   * Rückgabewertes sein.
   *
   * @param pruefung Pruefung zum ausplanen
   * @return Liste von veraenderte Pruefungen
   */
  public List<ReadOnlyPruefung> unschedulePruefung(ReadOnlyPruefung pruefung) {
    // todo before any restriction test: unschedule Pruefung
    //  then test restrictions
    pruefung = dataAccessService.unschedulePruefung(pruefung);
    return List.of(pruefung); // TODO return result of test for conflicts
  }


  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> scheduleBlock(ReadOnlyBlock block,
      LocalDateTime termin) throws HartesKriteriumException {
    if (!dataAccessService.terminIsInPeriod(termin)) {
      throw new IllegalArgumentException(
          "Der angegebene Termin liegt ausserhalb der Pruefungsperiode.");
    }

    if (block.getROPruefungen().isEmpty()) {
      throw new IllegalArgumentException("Leere Bloecke duerfen nicht geplant werden.");
    }
    ReadOnlyBlock roBlock = dataAccessService.scheduleBlock(block, termin);

    return new Pair<>(
        roBlock,
        new LinkedList<>(roBlock.getROPruefungen())); // TODO return result of test for conflicts
  }

  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> unscheduleBlock(ReadOnlyBlock block) {
    ReadOnlyBlock roBlock = dataAccessService.unscheduleBlock(block);
    // TODO bevor wir diese Methode aufrufen, müssen wir den RestriktionsService mitteilen,
    // wegen der Scoring Berechnung
    return new Pair<>(
        roBlock,
        new LinkedList<>(roBlock.getROPruefungen())); // TODO return result of test for conflicts
  }

  /**
   * Ändert die Dauer einer übergebenen Prüfung. Die übergebene Prüfung muss beim erfolgreichen
   * Verändern auch Teil der Rückgabe sein.
   *
   * @param pruefung Pruefung, dessen Dauer geändert werden muss.
   * @param minutes die neue Dauer
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

  public List<ReadOnlyPruefung> deletePruefung(ReadOnlyPruefung pruefung) {
    dataAccessService.deletePruefung(pruefung);
    List<WeichesKriteriumAnalyse> analyses = restrictionService.checkWeicheKriterien();
    // calc new score for all pruefungen
    Map<String, Integer> scoring = getScoringFrom(analyses);
    applyScoring(scoring);
    return analyses.stream()
        // get a stream of all pruefungen
        .flatMap((WeichesKriteriumAnalyse x) -> x.getCausingPruefungen().stream())
        // pass each pruefung only once
        .distinct() // TODO might not work because of missing implementation of .equals()
        // convert to DTO representation
        .map(dataAccessService::fromModelToDTOPruefungWithScoring)
        .toList();
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

    ReadOnlyBlock unscheduledBlock;
    List<ReadOnlyPruefung> changes = new LinkedList<>();

    if (block.geplant()) {
     Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> impact = unscheduleBlock(block); //TODO unscheduleBlock muss das Scoring berechnen.
     unscheduledBlock = impact.left();
     changes = impact.right();
    } else {
      unscheduledBlock = block;
    }

    List<ReadOnlyPruefung> pruefungInBlock = dataAccessService.deleteBlock(unscheduledBlock); //scoring must be 0
    changes.addAll(pruefungInBlock);
    changes = changes.stream().distinct().toList(); //delete double
    return changes;
  }

  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> moveBlock(ReadOnlyBlock block, LocalDateTime termin) {
    if (!dataAccessService.terminIsInPeriod(termin)) {
      throw new IllegalArgumentException(
              "Der angegebene Termin liegt ausserhalb der Pruefungsperiode.");
    }

    if (block.getROPruefungen().isEmpty()) {
      throw new IllegalArgumentException("Leere Bloecke duerfen nicht geplant werden.");
    }
    //TODO update scoring before DataAccessServoce#scheduleBlock
    ReadOnlyBlock result = dataAccessService.scheduleBlock(block, termin);
    return new Pair<>(result, new LinkedList<>(result.getROPruefungen()));
  }
}
