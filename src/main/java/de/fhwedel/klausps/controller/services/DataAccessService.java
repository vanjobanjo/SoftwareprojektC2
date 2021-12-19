package de.fhwedel.klausps.controller.services;

import static java.util.Objects.nonNull;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class DataAccessService {

  private Pruefungsperiode pruefungsperiode;
  private ScheduleService scheduleService; // TODO ScheduleService muss hier noch raus.

  public void setPruefungsperiode(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public void setScheduleService(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr, Set<String> pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {

    if (!existsPruefungWith(pruefungsNr)) {
      // todo contains static values as it is unclear where to retrieve the data from
      //TODO hier die Duration weg machen
      Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, "", duration);
      pruefer.forEach(pruefungModel::addPruefer);
      addTeilnehmerKreisSchaetzungToModelPruefung(pruefungModel, teilnehmerkreise);
      pruefungsperiode.addPlanungseinheit(pruefungModel);
      return new PruefungDTOBuilder(
          pruefungModel).build(); // Scoring ist 0, da Pruefung beim Erstellen ungeplant.
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
    if (block.getROPruefungen().isEmpty()) {
      return emptyBlockExists(block);
    } else {
      Optional<Block> modelBlock = searchInModel(block);
      return modelBlock.filter(
              value -> areSameBlocksBySpecs(block, value) && haveSamePruefungen(block, value))
          .isPresent();
    }

    // TODO wie bekommen wir den Model Block wenn der Block leer ist? Um z.B. den Namen
    //  und der Termin zu überprüfen.
  }

  private boolean emptyBlockExists(ReadOnlyBlock block) {
    for (Block modelBlock : pruefungsperiode.ungeplanteBloecke()) {
      // todo add all necessary checks for empty blocks
      if (modelBlock.getName().equals(block.getName())) {
        return true;
      }
    }
    return false;
  }

  private boolean haveSamePruefungen(ReadOnlyBlock readOnlyBlock, Block modelBlock) {
    Set<Pruefung> modelPruefungen = modelBlock.getPruefungen();
    if (modelPruefungen.size() != readOnlyBlock.getROPruefungen().size()) {
      return false;
    }
    for (ReadOnlyPruefung pruefung : readOnlyBlock.getROPruefungen()) {
      Pruefung pruefungFromModel = pruefungsperiode.pruefung(pruefung.getPruefungsnummer());
      if (!existsPruefungWith(pruefung.getPruefungsnummer()) || modelPruefungen.stream().noneMatch(
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
          && readOnlyBlock.getName().equals(modelBlock.getName()) && (
          (readOnlyBlock.getTermin().isEmpty() && modelBlock.getStartzeitpunkt() == null) || (
              readOnlyTermin.isPresent() && readOnlyTermin.get()
                  .equals(modelBlock.getStartzeitpunkt())));
    }
    return false;
  }

  private void addTeilnehmerKreisSchaetzungToModelPruefung(Pruefung pruefungModel,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    teilnehmerkreise.forEach(pruefungModel::setSchaetzung);
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr, String pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    return createPruefung(name, pruefungsNr, Set.of(pruefer), duration, teilnehmerkreise);
  }

  public boolean isPruefungsperiodeSet() {
    return nonNull(pruefungsperiode);
  }

  public List<ReadOnlyPruefung> changeDurationOf(ReadOnlyPruefung pruefung, Duration minutes)
      throws HartesKriteriumException, IllegalArgumentException {

    if (minutes.isNegative()) {
      throw new IllegalArgumentException("Die Dauer der Pruefung muss positiv sein.");
    }

    Pruefung toChangeDuration = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    // Hartes Kriterium wird in ScheduleService geprüft.
    // Die Änderungen der Pruefungen werden auch im ScheduleService vorgenommen.
    List<Pruefung> resultOfChangingDuration = scheduleService.changeDuration(toChangeDuration,
        minutes);
    return createListOfPruefungWithScoring(resultOfChangingDuration);
  }


  private List<ReadOnlyPruefung> createListOfPruefungWithScoring(List<Pruefung> pruefungen) {
    List<ReadOnlyPruefung> result = new ArrayList<>();
    for (Pruefung pruefung : pruefungen) {
      PruefungDTO build = new PruefungDTOBuilder(pruefung).withScoring(
          scheduleService.scoringOfPruefung(pruefung)).build();
      result.add(build);
    }
    return result;
  }

  /**
   * Schedules a pruefung without any consistency checks.
   *
   * @param pruefung    The pruefung to schedule.
   * @param startTermin The time to schedule the pruefung to.
   */
  public ReadOnlyPruefung schedulePruefung(ReadOnlyPruefung pruefung, LocalDateTime startTermin) {
    Pruefung pruefungFromModel = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    if (pruefungsperiode.block(pruefungFromModel) != null) {
      throw new IllegalArgumentException("Prüfung befindet sich innerhalb eines Blockes");
    } else {
      pruefungFromModel.setStartzeitpunkt(startTermin);
      return new PruefungDTOBuilder(pruefungFromModel).build();
    }
  }

  /**
   * Unschedules a pruefung without any consistency checks.
   *
   * @param pruefung The pruefung to schedule.
   */
  public ReadOnlyPruefung unschedulePruefung(ReadOnlyPruefung pruefung) {
    Pruefung pruefungFromModel = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    pruefungFromModel.setStartzeitpunkt(null);
    return new PruefungDTOBuilder(pruefungFromModel).build();
  }

  /**
   * Schedules a block without any consistency checks. The passed block is consistent and has
   * pruefungen inside.
   *
   * @param block  The block to schedule
   * @param termin The time to schedule the pruefung to.
   */
  ReadOnlyBlock scheduleBlock(ReadOnlyBlock block, LocalDateTime termin) {
    // todo look for model block with same block id, instead of comparing pruefungen
    Block blockFromModel = getBlockFromModelOrException(block);
    blockFromModel.setStartzeitpunkt(termin);

    return fromModelToDTOBlock(blockFromModel);
  }

  public ReadOnlyBlock unscheduleBlock(ReadOnlyBlock block) {
    Block blockModel = getBlockFromModelOrException(block);
    blockModel.setStartzeitpunkt(null);
    return fromModelToDTOBlock(blockModel);
  }

  public ReadOnlyPruefung changeNameOfPruefung(ReadOnlyPruefung toChange, String name) {
    Pruefung pruefung = getPruefungFromModelOrException(toChange.getPruefungsnummer());
    pruefung.setName(name);
    int scoring = toChange.getScoring();
    return new PruefungDTOBuilder(pruefung).withScoring(scoring).build();
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen() {
    return pruefungsperiode.geplantePruefungen().stream()
        .map(this::fromModelToDTOPruefungWithScoring).collect(Collectors.toSet());
  }


  public Set<Pruefung> getGeplanteModelPruefung() {
    return pruefungsperiode.geplantePruefungen();
  }

  public Set<ReadOnlyPruefung> getUngeplantePruefungen() {
    return pruefungsperiode.ungeplantePruefungen().stream()
        .map(this::fromModelToDTOPruefungWithScoring).collect(Collectors.toSet());
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
    Pruefung pruefung = getPruefungFromModelOrException(pruefungsNummer);
    pruefung.addPruefer(pruefer);
    return fromModelToDTOPruefungWithScoring(pruefung);
  }

  public ReadOnlyPruefung removePruefer(String pruefungsNummer, String pruefer) {
    Pruefung pruefung = getPruefungFromModelOrException(pruefungsNummer);
    pruefung.removePruefer(pruefer);
    return fromModelToDTOPruefungWithScoring(pruefung);
  }

  public ReadOnlyPruefung setPruefungsnummer(ReadOnlyPruefung pruefung, String pruefungsnummer) {
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    if (existsPruefungWith(pruefungsnummer)) {
      throw new IllegalArgumentException("Die angegebene Pruefungsnummer ist bereits vergeben.");
    }
    modelPruefung.setPruefungsnummer(pruefungsnummer);
    return fromModelToDTOPruefungWithScoring(modelPruefung);
  }

  private ReadOnlyBlock fromModelToDTOBlock(Block block) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      pruefungen.add(fromModelToDTOPruefungWithScoring(pruefung));
    }
    return new BlockDTO(block.getName(), block.getStartzeitpunkt(), block.getDauer(),
        block.isGeplant(), pruefungen);
  }

  private ReadOnlyPruefung fromModelToDTOPruefungWithScoring(Pruefung pruefung) {
    // TODO extract into appropriate class
    return new PruefungDTOBuilder(pruefung).withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }

  private ReadOnlyPruefung fromModelToDTOPruefungWithoutScoring(Pruefung pruefung) {
    // TODO extract into appropriate class
    return new PruefungDTOBuilder(pruefung)
        .build();
  }


  public boolean deletePruefung(ReadOnlyPruefung roPruefung) throws IllegalArgumentException {
    Pruefung pruefung = getPruefungFromModelOrException(roPruefung.getPruefungsnummer());
    this.unschedulePruefung(roPruefung);
    return this.pruefungsperiode.removePlanungseinheit(pruefung);
  }

  private Pruefung getPruefungFromModelOrException(String pruefungsNr)
      throws IllegalArgumentException {
    if (!existsPruefungWith(pruefungsNr)) {
      throw new IllegalArgumentException(
          "Pruefung mit Pruefungsnummer " + pruefungsNr + " ist in der Datenbank nicht vorhanden.");
    }
    return pruefungsperiode.pruefung(pruefungsNr);
  }

  private Block getBlockFromModelOrException(ReadOnlyBlock block) throws IllegalArgumentException {
    if (!exists(block)) {
      throw new IllegalArgumentException(
          "Der angegebene Block ist in der Datenbank nicht vorhanden.");
    }
    // todo look for model block with same block id, instead of comparing pruefungen
    return pruefungsperiode.block(pruefungsperiode.pruefung(
        new LinkedList<>(block.getROPruefungen()).get(0).getPruefungsnummer()));
  }


  public boolean terminIsInPeriod(LocalDateTime termin) {
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

  public ReadOnlyPruefung getPruefungWith(String pruefungsNummer) {
    return fromModelToDTOPruefungWithScoring(getPruefungFromModelOrException(pruefungsNummer));
  }

  public Optional<LocalDateTime> getStartOfPruefungWith(String pruefungsNummer) {
    LocalDateTime start = getPruefungFromModelOrException(pruefungsNummer).getStartzeitpunkt();
    if (start == null) {
      return Optional.empty();
    } else {
      return Optional.of(start);
    }
  }

  // nur fuer ungeplante bloecke aufrufen, wegen SCORING!!!!!
  public List<ReadOnlyPruefung> deleteBlock(ReadOnlyBlock block) {
    if (block.geplant()) {
      throw new IllegalArgumentException("Nur für ungeplante Blöcke möglich!");
    }
    Block model = getBlockFromModelOrException(block);
    Set<Pruefung> modelPruefung = new HashSet<>(
        model.getPruefungen()); // very important, when we call
    // de.fhwedel.klausps.model.api.Block.removeAllPruefungen it
    // removes also the set, so we need a deep copy of the set
    model.removeAllPruefungen();
    pruefungsperiode.removePlanungseinheit(model);
    return modelPruefung.stream().map(this::fromModelToDTOPruefungWithScoring).toList();
  }

  public ReadOnlyBlock createBlock(String name, ReadOnlyPruefung... pruefungen) {
    if (Arrays.stream(pruefungen).anyMatch(ReadOnlyPlanungseinheit::geplant)) {
      throw new IllegalArgumentException("Einer der übergebenen Prüfungen ist geplant.");
    }

    if (isAnyInBlock(List.of(pruefungen))) {
      throw new IllegalArgumentException("Einer der Prüfungen ist bereits im Block!");
    }

    if (contaisDuplicatePruefung(pruefungen)) {
      throw new IllegalArgumentException("Doppelte Prüfungen im Block!");
    }

    Block block_model = new BlockImpl(pruefungsperiode, name,
        Blocktyp.SEQUENTIAL); // TODO bei Erzeugung Sequential?
    Arrays.stream(pruefungen).forEach(pruefung -> block_model.addPruefung(
        pruefungsperiode.pruefung(pruefung.getPruefungsnummer())));
    if (!pruefungsperiode.addPlanungseinheit(block_model)) {
      throw new IllegalArgumentException("Irgendwas ist schief gelaufen."
          + " Der Block konnte nicht in die Datenbank übertragen werden.");
    }
    return fromModelToDTOBlock(block_model);
  }

  /**
   * Gets the amount of pruefungen taking place at a specified time. Multiple pruefungen that are
   * planned as one block only count as one.
   *
   * @param time The time to check for.
   * @return The amount of planned pruefungen.
   */
  public Integer getAmountOfPruefungenAt(LocalDateTime time) {
    Set<Planungseinheit> planungseinheiten = pruefungsperiode.planungseinheitenAt(time);
    Set<String> pruefungsNummernInBloecken = new HashSet<>();
    int amountPruefungen = 0;
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (planungseinheit.isBlock()) {
        planungseinheit.asBlock().getPruefungen()
            .forEach(x -> pruefungsNummernInBloecken.add(x.getPruefungsnummer()));
        amountPruefungen++;
      } else {
        String pruefungsNummer = planungseinheit.asPruefung().getPruefungsnummer();
        if (!pruefungsNummernInBloecken.contains(pruefungsNummer)) {
          amountPruefungen++;
        }
      }
    }
    return amountPruefungen;
  }

  private boolean isAnyInBlock(Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream()
        .anyMatch((pruefung) -> this.pruefungIsInBlock(pruefung.getPruefungsnummer()));
  }

  private boolean pruefungIsInBlock(String pruefungsNummer) {
    if (existsPruefungWith(pruefungsNummer)) {
      return Optional.ofNullable(pruefungsperiode.block(pruefungsperiode.pruefung(pruefungsNummer)))
          .isPresent();
    }
    throw new IllegalArgumentException("Pruefung existiert nicht.");
  }

  private boolean contaisDuplicatePruefung(ReadOnlyPruefung[] pruefungen) {
    return pruefungen.length != Arrays.stream(pruefungen).distinct().count();
  }

  public LocalDate getStartOfPeriode() {
    return pruefungsperiode.getStartdatum();
  }

  public LocalDate getEndOfPeriode() {
    return pruefungsperiode.getEnddatum();
  }

  public int getPeriodenKapazitaet() {
    return pruefungsperiode.getKapazitaet();
  }

  public Semester getSemester() {
    return pruefungsperiode.getSemester();
  }

  public void setSemester(@NotNull Semester semester) {
    // TODO model does not support setting the semester
    throw new UnsupportedOperationException("Not implemented yet!");
  }


  public List<Planungseinheit> getAllPruefungenBetween(LocalDateTime start, LocalDateTime end)
      throws IllegalTimeSpanException {

    List<Planungseinheit> listOfAllPruefungenBetween = new ArrayList<>();

    if(start.isAfter(end)){
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }

    for (Planungseinheit einheit : this.pruefungsperiode.planungseinheitenBetween(start, end)) {
      if (einheit.isBlock()) {
        for (Pruefung pruefung : einheit.asBlock().getPruefungen()) {
          listOfAllPruefungenBetween.add(pruefung);
        }
      } else {
        listOfAllPruefungenBetween.add(einheit.asPruefung());
      }
    }
    return listOfAllPruefungenBetween;
  }

  public Set<Teilnehmerkreis> getAllTeilnehmerkreise() {
    Set<Pruefung> allPruefungen = new HashSet<>();
    allPruefungen.addAll(pruefungsperiode.geplantePruefungen());
    allPruefungen.addAll(pruefungsperiode.ungeplantePruefungen());
    Set<Teilnehmerkreis> allTeilnehmerkreise = new HashSet<>();
    for (Pruefung pruefung : allPruefungen) {
      allTeilnehmerkreise.addAll(pruefung.getTeilnehmerkreise());
    }
    return allTeilnehmerkreise;
  }
}
