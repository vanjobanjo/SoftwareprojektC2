package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;

import de.fhwedel.klausps.controller.helper.Pair;

import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;

import de.fhwedel.klausps.model.api.Pruefung;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ScheduleService {

  private final DataAccessService dataAccessService;

  private final RestrictionService restrictionService;

  public ScheduleService(DataAccessService dataAccessService, RestrictionService restrictionService) {
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
    String pruefungsNummer = pruefung.getPruefungsnummer();

    if (!dataAccessService.existsPruefungWith(pruefungsNummer)) {
      throw new IllegalArgumentException("Exam doesn't exist");
    }
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
    String pruefungsNummer = pruefung.getPruefungsnummer();

    if (!dataAccessService.existsPruefungWith(pruefungsNummer)) {
      throw new IllegalArgumentException("Exam doesn't exist");
    }

    pruefung = dataAccessService.unschedulePruefung(pruefung);
    return List.of(pruefung); // TODO return result of test for conflicts
  }

  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> scheduleBlock(ReadOnlyBlock block,
                                                                LocalDateTime termin) throws HartesKriteriumException {
    if(!dataAccessService.terminIsInPeriod(termin)){
      throw new IllegalArgumentException("Termin isn't in period");
    }

    if(block.getROPruefungen().isEmpty()){
      throw new IllegalArgumentException("Empty blocks aren't allow to be scheduled");
    }

    consistentBlockOrThrowException(block);

    ReadOnlyBlock roBlock = dataAccessService.scheduleBlock(block, termin);

    return new Pair<>(roBlock, new LinkedList<>(roBlock.getROPruefungen())); // TODO return result of test for conflicts
  }

  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> unscheduleBlock(ReadOnlyBlock block) {

    consistentBlockOrThrowException(block);

    ReadOnlyBlock roBlock = dataAccessService.unscheduleBlock(block); //TODO bevor wir diese Methode aufrufen, müssen wir den RestriktionsService mitteilen, wegen der Scoring Berechnung


    return new Pair<>(roBlock, new LinkedList<>(roBlock.getROPruefungen())); // TODO return result of test for conflicts
  }

  private void consistentBlockOrThrowException(ReadOnlyBlock block) {
    for(ReadOnlyPruefung p : block.getROPruefungen()){
      if(!dataAccessService.existsPruefungWith(p.getPruefungsnummer())){
        throw new IllegalArgumentException("Exam with " + p.getPruefungsnummer() + " doesn't exist");
      }
    }

    if(!dataAccessService.exists(block)){
      throw new IllegalArgumentException("The block doesn't exist");
    }
  }


  /**
   * Ändert die Dauer einer übergebenen Prüfung. Die übergebene Prüfung muss beim erfolgreichen
   * Verändern auch Teil der Rückgabe sein.
   *
   * @param pruefung Pruefung, dessen Dauer geändert werden muss.
   * @param minutes die naue Dauer
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
    return 0; //TODO implement
  }

  public void deletePruefung(ReadOnlyPruefung pruefung) {
    List<KriteriumsAnalyse> pre = restrictionService.checkWeicheKriterien();
    dataAccessService.deletePruefung(pruefung);
    List<KriteriumsAnalyse> after = restrictionService.checkWeicheKriterien();
  }

}
