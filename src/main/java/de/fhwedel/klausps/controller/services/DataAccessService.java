package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.BlockDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class DataAccessService {

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

    if (!existsPruefung(pruefungsNr)) {
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

  private boolean existsPruefung(String pruefungsNummer) {
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

    if (!existsPruefung(pruefungsNummer)) {
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
    return pruefungen.stream()
        .map(
            pruefung ->
                new PruefungDTOBuilder(pruefung)
                    .withScoring(scheduleService.scoringOfPruefung(pruefung))
                    .build())
        .collect(Collectors.toList());
  }

  public List<ReadOnlyPruefung> schedulePruefung(
      ReadOnlyPruefung pruefung, LocalDateTime startTermin) throws HartesKriteriumException {

    String pruefungsNummer = pruefung.getPruefungsnummer();

    if (!existsPruefung(pruefungsNummer)) {
      throw new IllegalArgumentException("Exam doesn't exist");
    }

    Pruefung model = pruefungsperiode.pruefung(pruefungsNummer);

    if (model.isGeplant()) {
      throw new IllegalArgumentException("Exam already planned.");
    }

    List<Pruefung> resultOfSchedulePruefung = scheduleService.schedulePruefung(model, startTermin);
    return createListOfPruefungWithScoring(resultOfSchedulePruefung);
  }

  public List<ReadOnlyPruefung> unschedulePruefung(ReadOnlyPruefung pruefung) {
    String pruefungsNummer = pruefung.getPruefungsnummer();

    if (!existsPruefung(pruefungsNummer)) {
      throw new IllegalArgumentException("Exam doesn't exist");
    }

    Pruefung model = pruefungsperiode.pruefung(pruefungsNummer);

    if (!model.isGeplant()) {
      throw new IllegalArgumentException("Exam already unplanned.");
    }

    List<Pruefung> resultOfUnschedulePruefung = scheduleService.unschedulePruefung(model);

    return createListOfPruefungWithScoring(resultOfUnschedulePruefung);
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

  public Set<ReadOnlyPruefung> getUngeplanteKlausuren() {
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
    if (existsPruefung(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.addPruefer(pruefer);
      return fromModelToDTOPruefungWithScoring(pruefung);
    }
    throw new IllegalArgumentException("Passed unknown pruefung!");
  }

  public ReadOnlyPruefung removePruefer(String pruefungsNummer, String pruefer) {
    if (existsPruefung(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.removePruefer(pruefer);
      return fromModelToDTOPruefungWithScoring(pruefung);
    }
    throw new IllegalArgumentException("Passed unknown pruefung!");
  }

  public ReadOnlyPruefung setPruefungsnummer(ReadOnlyPruefung pruefung, String pruefungsnummer) {
    String oldNo = pruefung.getPruefungsnummer();

    if (!existsPruefung(oldNo)) {
      throw new IllegalArgumentException("Passed unknown pruefung!");
    }

    if (existsPruefung(pruefungsnummer)) {
      throw new IllegalArgumentException("Passed number already exists");
    }

    Pruefung model = pruefungsperiode.pruefung(oldNo);
    model.setPruefungsnummer(pruefungsnummer);

    return fromModelToDTOPruefungWithScoring(model);
  }

  public void setPlanungseinheit(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  private ReadOnlyBlock fromModelToDTOBlock(Block model) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    for (Pruefung pruefung : model.getPruefungen()) {
      pruefungen.add(fromModelToDTOPruefungWithScoring(pruefung));
    }
    return new BlockDTO(
        model.getName(),
        model.getStartzeitpunkt(),
        model.getDauer(),
        model.isGeplant(),
        pruefungen);
  }

  private ReadOnlyPruefung fromModelToDTOPruefungWithScoring(Pruefung model) {
    return new PruefungDTOBuilder()
        .withPruefungsName(model.getName())
        .withPruefungsNummer(model.getPruefungsnummer())
        .withDauer(model.getDauer())
        .withStartZeitpunkt(model.getStartzeitpunkt())
        .withPruefer(model.getPruefer())
        .withTeilnehmerKreisSchaetzung(model.getSchaetzungen())
        .withScoring(scheduleService.scoringOfPruefung(model))
        .build();
  }
}
