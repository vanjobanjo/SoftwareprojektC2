package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class DataAccessService {

  private static final String INVALID_ARGUMENT = "Passed unknown pruefung!";

  private Pruefungsperiode pruefungsperiode;
  private ScheduleService scheduleService;

  public void setPruefungsperiode(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public void setScheduleService(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  public ReadOnlyPruefung createPruefung(
      String name,
      String pruefungsNr,
      Set<String> pruefer,
      Duration duration,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {

    if (!existsPruefungWith(pruefungsNr)) {
      // todo contains static values as it is unclear where to retrieve the data from
      Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, "", duration, null);
      pruefer.forEach(pruefungModel::addPruefer);
      addTeilnehmerKreisSchaetzungToModelPruefung(pruefungModel, teilnehmerkreise);
      pruefungsperiode.addPlanungseinheit(pruefungModel);
      return new PruefungDTOBuilder(pruefungModel)
          .build(); // Scoring ist 0, da Pruefung beim Erstellen ungeplant.
    }
    return null;
  }

  public boolean existsPruefungWith(String pruefungsNummer) {
    return pruefungsperiode.pruefung(pruefungsNummer) != null;
  }

  private void addTeilnehmerKreisSchaetzungToModelPruefung(
      Pruefung pruefungModel, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    teilnehmerkreise.forEach(pruefungModel::setSchaetzung);
  }

  public ReadOnlyPruefung createPruefung(
      String name,
      String pruefungsNr,
      String pruefer,
      Duration duration,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    return createPruefung(name, pruefungsNr, Set.of(pruefer), duration, teilnehmerkreise);
  }

  public boolean isPruefungsperiodeSet() {
    return nonNull(pruefungsperiode);
  }

  public List<ReadOnlyPruefung> changeDurationOf(ReadOnlyPruefung pruefung, Duration minutes)
      throws HartesKriteriumException, IllegalArgumentException {

    if (minutes.isNegative()) {
      throw new IllegalArgumentException("The duration was negative!");
    }

    String pruefungsNummer = pruefung.getPruefungsnummer();

    if (!existsPruefungWith(pruefungsNummer)) {
      throw new IllegalArgumentException("Exam doesn't exist");
    }

    Pruefung toChangeDuration = pruefungsperiode.pruefung(pruefungsNummer);
    // Hartes Kriterium wird in ScheduleService geprüft.
    // Die Änderungen der Pruefungen werden auch im ScheduleService vorgenommen.
    List<Pruefung> resultOfChangingDuration =
        scheduleService.changeDuration(toChangeDuration, minutes);
    return createListOfPruefungWithScoring(resultOfChangingDuration);
  }

  private List<ReadOnlyPruefung> createListOfPruefungWithScoring(List<Pruefung> pruefungen) {
    List<ReadOnlyPruefung> result = new ArrayList<>();
    for (Pruefung pruefung : pruefungen) {
      PruefungDTO build =
          new PruefungDTOBuilder(pruefung)
              .withScoring(scheduleService.scoringOfPruefung(pruefung))
              .build();
      result.add(build);
    }
    return result;
  }

  /**
   * Schedules a pruefung without any consistency checks.
   *
   * @param pruefung The pruefung to schedule.
   * @param startTermin The time to schedule the pruefung to.
   */
  public ReadOnlyPruefung schedulePruefung(ReadOnlyPruefung pruefung, LocalDateTime startTermin) {
    Pruefung pruefungFromModel = pruefungsperiode.pruefung(pruefung.getPruefungsnummer());
    if (pruefungFromModel != null) {
      pruefungFromModel.setStartzeitpunkt(startTermin);
      return new PruefungDTOBuilder(pruefungFromModel).build();
    }
    throw new IllegalArgumentException("Unknown pruefung.");
  }

  /**
   * Unschedules a pruefung without any consistency checks.
   *
   * @param pruefung The pruefung to schedule.
   */
  public ReadOnlyPruefung unschedulePruefung(ReadOnlyPruefung pruefung) {
    Pruefung pruefungFromModel = pruefungsperiode.pruefung(pruefung.getPruefungsnummer());
    if (pruefungFromModel != null) {
      pruefungFromModel.setStartzeitpunkt(null);
      return new PruefungDTOBuilder(pruefungFromModel).build();
    }
    throw new IllegalArgumentException("Unknown pruefung.");
  }

  public ReadOnlyPruefung changeNameOfPruefung(ReadOnlyPruefung toChange, String name) {
    Pruefung pruefung = pruefungsperiode.pruefung(toChange.getPruefungsnummer());
    pruefung.setName(name);
    int scoring = toChange.getScoring();
    return new PruefungDTOBuilder(pruefung).withScoring(scoring).build();
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen() {
    return pruefungsperiode.geplantePruefungen().stream()
        .map(this::fromModelToDTOPruefungWithScoring)
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyPruefung> getUngeplantePruefungen() {
    return pruefungsperiode.ungeplantePruefungen().stream()
        .map(this::fromModelToDTOPruefungWithScoring)
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyBlock> getGeplanteBloecke() {
    Set<ReadOnlyBlock> result = new HashSet<>();
    for (Block block : pruefungsperiode.geplanteBloecke()) {
      result.add(fromModelToDTOBlock(block));
    }
    return result;
  }

  public Set<ReadOnlyBlock> getUngeplanteBloecke() {
    Set<ReadOnlyBlock> result = new HashSet<>();
    for (Block block : pruefungsperiode.ungeplanteBloecke()) {
      result.add(fromModelToDTOBlock(block));
    }
    return result;
  }

  public ReadOnlyPruefung addPruefer(String pruefungsNummer, String pruefer) {
    if (existsPruefungWith(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.addPruefer(pruefer);
      return fromModelToDTOPruefungWithScoring(pruefung);
    }
    throw new IllegalArgumentException(INVALID_ARGUMENT);
  }

  public ReadOnlyPruefung removePruefer(String pruefungsNummer, String pruefer) {
    if (existsPruefungWith(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.removePruefer(pruefer);
      return fromModelToDTOPruefungWithScoring(pruefung);
    }
    throw new IllegalArgumentException(INVALID_ARGUMENT);
  }

  public ReadOnlyPruefung setPruefungsnummer(ReadOnlyPruefung pruefung, String pruefungsnummer) {
    String oldNo = pruefung.getPruefungsnummer();

    if (!existsPruefungWith(oldNo)) {
      throw new IllegalArgumentException(INVALID_ARGUMENT);
    }

    if (existsPruefungWith(pruefungsnummer)) {
      throw new IllegalArgumentException("Passed number already exists");
    }

    Pruefung modelPruefung = pruefungsperiode.pruefung(oldNo);
    modelPruefung.setPruefungsnummer(pruefungsnummer);

    return fromModelToDTOPruefungWithScoring(modelPruefung);
  }

  private ReadOnlyBlock fromModelToDTOBlock(Block block) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      pruefungen.add(fromModelToDTOPruefungWithScoring(pruefung));
    }
    return new BlockDTO(
        block.getName(),
        block.getStartzeitpunkt(),
        block.getDauer(),
        block.isGeplant(),
        pruefungen);
  }

  private ReadOnlyPruefung fromModelToDTOPruefungWithScoring(Pruefung pruefung) {
    return new PruefungDTOBuilder()
        .withPruefungsName(pruefung.getName())
        .withPruefungsNummer(pruefung.getPruefungsnummer())
        .withDauer(pruefung.getDauer())
        .withStartZeitpunkt(pruefung.getStartzeitpunkt())
        .withPruefer(pruefung.getPruefer())
        .withTeilnehmerKreisSchaetzung(pruefung.getSchaetzungen())
        .withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }

  public boolean deletePruefung(ReadOnlyPruefung roPruefung) {
    //TODO auf referenzVerwaltungssystem-Nummer ändern, wenn das Model das anpasst!
    Pruefung pruefung = this.pruefungsperiode.pruefung(roPruefung.getPruefungsnummer());
    return this.pruefungsperiode.removePlanungseinheit(pruefung);
  }
}
