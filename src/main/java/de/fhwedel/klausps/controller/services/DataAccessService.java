package de.fhwedel.klausps.controller.services;

import static java.util.Objects.nonNull;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    List<ReadOnlyPruefung> result = new ArrayList<>();
    for (Pruefung pruefung : pruefungen) {
      PruefungDTO build = new PruefungDTOBuilder(pruefung)
          .withScoring(scheduleService.scoringOfPruefung(pruefung))
          .build();
      result.add(build);
    }
    return result;
  }

  public List<ReadOnlyPruefung> schedulePruefung(
      ReadOnlyPruefung pruefung, LocalDateTime startTermin) throws HartesKriteriumException {

    String pruefungsNummer = pruefung.getPruefungsnummer();

    if (!existsPruefung(pruefungsNummer)) {
      throw new IllegalArgumentException("Exam doesn't exist");
    }

    Pruefung modelPruefung = pruefungsperiode.pruefung(pruefungsNummer);

    if (modelPruefung.isGeplant()) {
      throw new IllegalArgumentException("Exam already planned.");

    }

    List<Pruefung> resultOfSchedulePruefung = scheduleService.schedulePruefung(modelPruefung,
        startTermin);
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
        .map(
            pruefung ->
                new PruefungDTOBuilder(pruefung) // TODO pruefung.getScoring(); Scoring berechnen
                    .build())
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyPruefung> getUngeplanteKlausuren() {
    return pruefungsperiode.ungeplantePruefungen().stream()
        .map(
            pruefung ->
                new PruefungDTOBuilder(pruefung) // TODO use scoring
                    .build())
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
      return new PruefungDTOBuilder(pruefung).build();
      // TODO add scoring to result
    }
    throw new IllegalArgumentException("Passed unknown pruefung!");
  }

  public ReadOnlyPruefung removePruefer(String pruefungsNummer, String pruefer) {
    if (existsPruefung(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.removePruefer(pruefer);
      return new PruefungDTOBuilder(pruefung).build();
      // TODO add scoring to result
    }
    throw new IllegalArgumentException("Passed unknown pruefung!");
  }


  private ReadOnlyBlock fromModelToDTOBlock(Block modelBlock) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    for (Pruefung pruefung : modelBlock.getPruefungen()) {
      pruefungen.add(fromModelToDTOPruefung(pruefung));
    }
    return new BlockDTO(
        modelBlock.getName(),
        modelBlock.getStartzeitpunkt(),
        modelBlock.getDauer(),
        modelBlock.isGeplant(),
        pruefungen);
  }

  private ReadOnlyPruefung fromModelToDTOPruefung(Pruefung modelPruefung) {
    return new PruefungDTOBuilder()
        .withPruefungsName(modelPruefung.getName())
        .withPruefungsNummer(modelPruefung.getPruefungsnummer())
        .withDauer(modelPruefung.getDauer())
        .withStartZeitpunkt(modelPruefung.getStartzeitpunkt())
        .withPruefer(modelPruefung.getPruefer())
        .withTeilnehmerKreisSchaetzung(modelPruefung.getSchaetzungen())
        .build();
  }
}
