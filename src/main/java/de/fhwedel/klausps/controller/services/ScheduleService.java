package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.hard.TwoKlausurenSameTime;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;


public class ScheduleService {

  private final DataAccessService dataAccessService;

  private final RestrictionService restrictionService;

  public ScheduleService(DataAccessService dataAccessService,
      RestrictionService restrictionService) {
    this.dataAccessService = dataAccessService;
    this.restrictionService = restrictionService;
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
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefung) {
    // todo before any restriction test: unschedule Pruefung
    //  then test restrictions
    pruefung = dataAccessService.unschedulePruefung(pruefung);
    return List.of(pruefung); // TODO return result of test for conflicts
  }


  public List<ReadOnlyPlanungseinheit> scheduleBlock(ReadOnlyBlock block,
      LocalDateTime termin) throws HartesKriteriumException {
    if (!dataAccessService.terminIsInPeriod(termin)) {
      throw new IllegalArgumentException(
          "Der angegebene Termin liegt ausserhalb der Pruefungsperiode.");
    }

    if (block.getROPruefungen().isEmpty()) {
      throw new IllegalArgumentException("Leere Bloecke duerfen nicht geplant werden.");
    }
    ReadOnlyBlock roBlock = dataAccessService.scheduleBlock(block, termin);
    List<ReadOnlyPlanungseinheit> returnList = new ArrayList<>();
    returnList.add(roBlock);
    returnList.addAll(roBlock.getROPruefungen());
    return returnList; // TODO return result of test for conflicts
  }

  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block) {
    ReadOnlyBlock roBlock = dataAccessService.unscheduleBlock(block);
    //TODO bevor wir diese Methode aufrufen, müssen wir den RestriktionsService mitteilen,
    // wegen der Scoring Berechnung
    List<ReadOnlyPlanungseinheit> returnList = new ArrayList<>();
    returnList.add(roBlock);
    returnList.addAll(roBlock.getROPruefungen());
    return returnList;// TODO return result of test for conflicts
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
        Converter.convertToROBlock(dataAccessService.getBlockTo(modelPruefung).get()));

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

  public List<ReadOnlyPlanungseinheit> moveBlock(ReadOnlyBlock block,
      LocalDateTime termin) throws HartesKriteriumException {
    if (block.getTermin().isEmpty()) {
      throw new IllegalArgumentException("Nur geplante Blöcke können verschoben werden!");
    }
    LocalDateTime oldTermin = block.getTermin().get();
    ReadOnlyBlock removedBlock = dataAccessService.unscheduleBlock(block);
    try {
      return scheduleBlock(block, termin);
    } catch (HartesKriteriumException hardRestrictionViolation) {
      dataAccessService.scheduleBlock(removedBlock, oldTermin);
      throw hardRestrictionViolation;
    }
  }


  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) {
    List<ReadOnlyPlanungseinheit> result = new LinkedList<>();
    Pair<Block, Pruefung> separated = dataAccessService.removePruefungFromBlock(block, pruefung);
    if (!block.geplant()) {
      result.addAll(Converter.convertToROPlanungseinheitCollection(separated.left(),
          separated.right()));
    } else {
      // todo update scoring and add changed Planungseinheiten to result
    }
    return result;
  }



  /*
  public List<ReadOnlyPruefung> movePruefung(ReadOnlyPruefung pruefung, LocalDateTime expectedStart)
      throws HartesKriteriumException {
    LocalDateTime currentStart =
        dataAccessService.getStartOfPruefungWith(pruefung.getPruefungsnummer())
            .orElseThrow(
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
        throw new HartesKriteriumException(null,null,null);
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
        throw new HartesKriteriumException(null,null,null);
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
