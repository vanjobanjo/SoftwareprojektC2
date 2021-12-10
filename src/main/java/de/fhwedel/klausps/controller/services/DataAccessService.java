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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class DataAccessService {

  private static final String INVALID_ARGUMENT = "Passed unknown pruefung!";

  private Pruefungsperiode pruefungsperiode;
  private ScheduleService scheduleService; // TODO Scheduleservice muss hier noch raus.

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

  /**
   * Checks the consistency of a ReadOnlyBlock
   *
   * @param block Block to check with the model data
   */
  boolean exists(ReadOnlyBlock block) {
    if (!block.getROPruefungen().isEmpty()) {
      Optional<Block> modelBlock = searchInModel(block);
      return modelBlock
          .filter(value -> areSameBlocksBySpecs(block, value) && haveSamePruefungen(block, value))
          .isPresent();
    }
    return true;
    // TODO wie bekommen wir den Model Block wenn der Block leer ist? Um z.B. den Namen
    //  und der Termin zu überprüfen.
  }

  private boolean haveSamePruefungen(ReadOnlyBlock readOnlyBlock, Block modelBlock) {
    Set<Pruefung> modelPruefungen = modelBlock.getPruefungen();
    if (modelPruefungen.size() != readOnlyBlock.getROPruefungen().size()) {
      return false;
    }
    for (ReadOnlyPruefung pruefung : readOnlyBlock.getROPruefungen()) {
      Pruefung pruefungFromModel = pruefungsperiode.pruefung(pruefung.getPruefungsnummer());
      if (!existsPruefungWith(pruefung.getPruefungsnummer())
          || modelPruefungen.stream()
              .noneMatch(
                  (Pruefung p) -> hasPruefungsnummer(p, pruefungFromModel.getPruefungsnummer()))) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPruefungsnummer(Pruefung pruefung, String pruefungsnummer) {
    return pruefung.getPruefungsnummer().equals(pruefungsnummer);
  }

  private Optional<Block> searchInModel(ReadOnlyBlock block) {
    // TODO a block is expected to get a unique identifier, this should be used for search
    Iterator<ReadOnlyPruefung> blockIterator = block.getROPruefungen().iterator();
    if (blockIterator.hasNext()) {
      ReadOnlyPruefung pruefung = blockIterator.next();
      Pruefung modelPruefung = pruefungsperiode.pruefung(pruefung.getPruefungsnummer());
      if (modelPruefung != null) {
        Block modelBlock = pruefungsperiode.block(modelPruefung);
        if (modelBlock != null) {
          return Optional.of(modelBlock);
        }
      }
    }
    return Optional.empty();
  }

  private boolean areSameBlocksBySpecs(ReadOnlyBlock readOnlyBlock, Block modelBlock) {
    if (readOnlyBlock != null) {
      Optional<LocalDateTime> readOnlyTermin = readOnlyBlock.getTermin();
      return modelBlock != null
          && readOnlyBlock.getROPruefungen().size() == modelBlock.getPruefungen().size()
          && readOnlyBlock.getName().equals(modelBlock.getName())
          && ((readOnlyBlock.getTermin().isEmpty() && modelBlock.getStartzeitpunkt() == null)
              || (readOnlyTermin.isPresent()
                  && readOnlyTermin.get().equals(modelBlock.getStartzeitpunkt())));
    }
    return false;
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

  /**
   * Schedules a block without any consistency checks. The passed block is consistent and has
   * pruefungen inside.
   *
   * @param block The block to schedule
   * @param termin The time to schedule the pruefung to.
   */
  ReadOnlyBlock scheduleBlock(ReadOnlyBlock block, LocalDateTime termin) {
    String number = new LinkedList<>(block.getROPruefungen()).get(0).getPruefungsnummer();
    Block blockFromModel = pruefungsperiode.block(pruefungsperiode.pruefung(number));
    blockFromModel.setStartzeitpunkt(termin);

    return fromModelToDTOBlock(blockFromModel);
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
    return new PruefungDTOBuilder(pruefung)
        .withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }

  public boolean deletePruefung(ReadOnlyPruefung roPruefung) throws IllegalArgumentException {
    // TODO auf referenzVerwaltungssystem-Nummer ändern, wenn das Model das anpasst!
    Pruefung pruefung = this.pruefungsperiode.pruefung(roPruefung.getPruefungsnummer());
    if (pruefung == null) {
      throw new IllegalArgumentException(
          "Die Pruefungsnummer ist in der Datenbank nicht vorhanden!");
    }
    this.unschedulePruefung(roPruefung);
    return this.pruefungsperiode.removePlanungseinheit(pruefung);
  }

  boolean terminIsInPeriod(LocalDateTime termin) {
    return terminIsSameDayOrAfterPeriodStart(termin) && terminIsSameDayOrBeforePeriodEnd(termin);
  }

  private boolean terminIsSameDayOrAfterPeriodStart(LocalDateTime termin) {
    LocalDate start = pruefungsperiode.getStartdatum();
    return start.isBefore(termin.toLocalDate()) || start.isEqual(termin.toLocalDate());
  }

  private boolean terminIsSameDayOrBeforePeriodEnd(LocalDateTime termin) {
    LocalDate end = pruefungsperiode.getEnddatum();
    return end.isAfter(termin.toLocalDate()) || end.isEqual(termin.toLocalDate());
  }
}
