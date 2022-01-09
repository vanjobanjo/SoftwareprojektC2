package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.PlanungseinheitUtil.getAllPruefungen;
import static java.util.Objects.nonNull;

import de.fhwedel.klausps.controller.PlanungseinheitUtil;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.helper.Pair;
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
import java.util.HashMap;
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
  private Converter converter; //TODO where does it come from

  public void setPruefungsperiode(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public void setConverter(Converter converter) {
    this.converter = converter;
  }

  public void setKapazitaetPeriode(int kapazitaet) {
    pruefungsperiode.setKapazitaet(kapazitaet);
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr, String refVWS,
      String pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    return createPruefung(name, pruefungsNr, refVWS, Set.of(pruefer), duration, teilnehmerkreise);
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr, String refVWS,
      Set<String> pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {

    if (existsPruefungWith(pruefungsNr)) {
      return null;
    }

    Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, refVWS, duration);
    pruefer.forEach(pruefungModel::addPruefer);
    addTeilnehmerKreisSchaetzungToModelPruefung(pruefungModel, teilnehmerkreise);
    pruefungsperiode.addPlanungseinheit(pruefungModel);
    return converter.convertToReadOnlyPruefung(pruefungModel);

  }

  public boolean existsPruefungWith(String pruefungsNummer) {
    return pruefungsperiode.pruefung(pruefungsNummer) != null;
  }

  private void addTeilnehmerKreisSchaetzungToModelPruefung(Pruefung pruefungModel,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    teilnehmerkreise.forEach(pruefungModel::setSchaetzung);
  }

  public boolean isPruefungsperiodeSet() {
    return nonNull(pruefungsperiode);
  }

  //TODO diese Methode muss in ScheduleService #183?
  //TODO Planungseinheit, wenn Prüfung im Block, dann Block sonst Prüfung als Rückgabe
  public void changeDurationOf(ReadOnlyPruefung pruefung, Duration duration)
      throws HartesKriteriumException, IllegalArgumentException {

    if (duration.isNegative()) {
      throw new IllegalArgumentException("Die Dauer der Pruefung muss positiv sein.");
    }

    Pruefung toChangeDuration = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    // Hartes Kriterium wird in ScheduleService geprüft.
    // Die Änderungen der Pruefungen werden auch im ScheduleService vorgenommen.
    toChangeDuration.setDauer(duration);
  }

  private Pruefung getPruefungFromModelOrException(String pruefungsNr)
      throws IllegalArgumentException {
    if (!existsPruefungWith(pruefungsNr)) {
      throw new IllegalArgumentException(
          "Pruefung mit Pruefungsnummer " + pruefungsNr + " ist in der Datenbank nicht vorhanden.");
    }
    return pruefungsperiode.pruefung(pruefungsNr);
  }

  /**
   * Schedules a pruefung without any consistency checks.
   *
   * @param pruefung    The pruefung to schedule.
   * @param startTermin The time to schedule the pruefung to.
   */
  public Pruefung schedulePruefung(ReadOnlyPruefung pruefung, LocalDateTime startTermin) {
    Pruefung pruefungFromModel = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    if (pruefungsperiode.block(pruefungFromModel) != null) {
      throw new IllegalArgumentException("Prüfung befindet sich innerhalb eines Blockes");
    } else {
      pruefungFromModel.setStartzeitpunkt(startTermin);
      return pruefungFromModel;
    }
  }

  /**
   * Schedules a block without any consistency checks. The passed block is consistent and has
   * pruefungen inside.
   *
   * @param block  The block to schedule
   * @param termin The time to schedule the pruefung to.
   */
  Block scheduleBlock(ReadOnlyBlock block, LocalDateTime termin) {
    Block blockFromModel = getBlockFromModelOrException(block);
    blockFromModel.setStartzeitpunkt(termin);

    return blockFromModel;
  }

  private Block getBlockFromModelOrException(ReadOnlyBlock block) throws IllegalArgumentException {
    if (!exists(block)) {
      throw new IllegalArgumentException(
          "Der angegebene Block ist in der Datenbank nicht vorhanden.");
    }
    return pruefungsperiode.block(block.getBlockId());
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
  }


  private boolean emptyBlockExists(ReadOnlyBlock block) {
    for (Block modelBlock : pruefungsperiode.ungeplanteBloecke()) {
      // todo add all necessary checks for empty blocks
      //TODO pruefungsperiode.block(int number); nutzen?
      if (modelBlock.getId() == block.getBlockId()) {
        return true;
      }
    }
    return false;
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
          && readOnlyBlock.getBlockId() == modelBlock.getId()
          && readOnlyBlock.getROPruefungen().size() == modelBlock.getPruefungen().size()
          && readOnlyBlock.getName().equals(modelBlock.getName()) && (
          (readOnlyBlock.getTermin().isEmpty() && modelBlock.getStartzeitpunkt() == null) || (
              readOnlyTermin.isPresent() && readOnlyTermin.get()
                  .equals(modelBlock.getStartzeitpunkt())));
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

  public Block unscheduleBlock(ReadOnlyBlock block) {
    Block blockModel = getBlockFromModelOrException(block);
    blockModel.setStartzeitpunkt(null);
    return blockModel;
  }

  public ReadOnlyPlanungseinheit changeNameOfPruefung(ReadOnlyPruefung toChange, String name) {
    Pruefung pruefung = getPruefungFromModelOrException(toChange.getPruefungsnummer());
    pruefung.setName(name);

    return getROPlanungseinheitToPruefung(pruefung);
  }

  /*
  Gibt übergeordneten Block oder Pruefung zurück.
   */
  private ReadOnlyPlanungseinheit getROPlanungseinheitToPruefung(Pruefung pruefung) {
    Block block = pruefungsperiode.block(pruefung);
    return block != null ? converter.convertToROBlock(block)
        : converter.convertToReadOnlyPruefung(pruefung);
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen() {
    return new HashSet<>(
        converter.convertToROPruefungCollection(pruefungsperiode.geplantePruefungen()));
  }

  public Set<Pruefung> getGeplanteModelPruefung() {
    return pruefungsperiode.geplantePruefungen();
  }

  public Set<ReadOnlyPruefung> getUngeplantePruefungen() {
    return new HashSet<>(
        converter.convertToROPruefungCollection(pruefungsperiode.ungeplantePruefungen()));
  }

  public Set<ReadOnlyBlock> getGeplanteBloecke() {

    return new HashSet<>(
        converter.convertToROBlockCollection(pruefungsperiode.geplanteBloecke()));
  }

  public Set<ReadOnlyBlock> getUngeplanteBloecke() {
    return new HashSet<>(
        converter.convertToROBlockCollection(pruefungsperiode.ungeplanteBloecke()));
  }

  public Set<ReadOnlyPruefung> ungeplantePruefungenForTeilnehmerkreis(Teilnehmerkreis tk) {
    return new HashSet<>(
        converter.convertToROPruefungCollection(pruefungsperiode.ungeplantePruefungen().stream()
            .filter(pruefung -> pruefung.getTeilnehmerkreise().contains(tk))
            .collect(Collectors.toSet())));
  }

  public Set<ReadOnlyPruefung> geplantePruefungenForTeilnehmerkreis(Teilnehmerkreis tk) {
    return new HashSet<>(
        converter.convertToROPruefungCollection(pruefungsperiode.geplantePruefungen().stream()
            .filter(pruefung -> pruefung.getTeilnehmerkreise().contains(tk))
            .collect(Collectors.toSet())));
  }

  public ReadOnlyPlanungseinheit addPruefer(String pruefungsNummer, String pruefer) {
    Pruefung pruefung = getPruefungFromModelOrException(pruefungsNummer);
    pruefung.addPruefer(pruefer);
    return getROPlanungseinheitToPruefung(pruefung);
  }

  public ReadOnlyPlanungseinheit removePruefer(String pruefungsNummer, String pruefer) {
    Pruefung pruefung = getPruefungFromModelOrException(pruefungsNummer);
    pruefung.removePruefer(pruefer);
    return getROPlanungseinheitToPruefung(pruefung);
  }

  public ReadOnlyPlanungseinheit setPruefungsnummer(ReadOnlyPruefung pruefung,
      String pruefungsnummer) {
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    if (existsPruefungWith(pruefungsnummer)) {
      throw new IllegalArgumentException("Die angegebene Pruefungsnummer ist bereits vergeben.");
    }
    modelPruefung.setPruefungsnummer(pruefungsnummer);
    return getROPlanungseinheitToPruefung(modelPruefung);
  }

  public Block deletePruefung(ReadOnlyPruefung roPruefung) throws IllegalArgumentException {
    Pruefung pruefung = getPruefungFromModelOrException(roPruefung.getPruefungsnummer());
    Block block = pruefungsperiode.block(pruefung);
    pruefungsperiode.removePlanungseinheit(pruefung);
    return block;
  }

  /**
   * Unschedules a pruefung without any consistency checks.
   *
   * @param pruefung The pruefung to schedule.
   */
  public Pruefung unschedulePruefung(ReadOnlyPruefung pruefung) {
    Pruefung pruefungFromModel = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    pruefungFromModel.setStartzeitpunkt(null);
    return pruefungFromModel;
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

  public Pruefung getPruefungWith(String pruefungsNummer) {
    // todo raus, wenn der Converter implementiert ist
    return getPruefungFromModelOrException(pruefungsNummer);
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
    Set<Pruefung> modelPruefungen = new HashSet<>(
        model.getPruefungen()); // very important, when we call
    // de.fhwedel.klausps.model.api.Block.removeAllPruefungen it
    // removes also the set, so we need a deep copy of the set
    model.removeAllPruefungen();
    pruefungsperiode.removePlanungseinheit(model);
    return new LinkedList<>(converter.convertToROPruefungCollection(modelPruefungen));
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

    Block blockModel = new BlockImpl(pruefungsperiode, name,
        Blocktyp.SEQUENTIAL); // TODO bei Erzeugung Sequential?
    Arrays.stream(pruefungen).forEach(pruefung -> blockModel.addPruefung(
        pruefungsperiode.pruefung(pruefung.getPruefungsnummer())));
    if (!pruefungsperiode.addPlanungseinheit(blockModel)) {
      throw new IllegalArgumentException("Irgendwas ist schief gelaufen."
          + " Der Block konnte nicht in die Datenbank übertragen werden.");
    }
    return converter.convertToROBlock(blockModel);
  }

  private boolean isAnyInBlock(Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream()
        .anyMatch(pruefung -> this.pruefungIsInBlock(pruefung.getPruefungsnummer()));
  }

  private boolean contaisDuplicatePruefung(ReadOnlyPruefung[] pruefungen) {
    return pruefungen.length != Arrays.stream(pruefungen).distinct().count();
  }

  private boolean pruefungIsInBlock(String pruefungsNummer) {
    if (existsPruefungWith(pruefungsNummer)) {
      return Optional.ofNullable(pruefungsperiode.block(pruefungsperiode.pruefung(pruefungsNummer)))
          .isPresent();
    }
    throw new IllegalArgumentException("Pruefung existiert nicht.");
  }

  public Pair<Block, Pruefung> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) {
    Block modelBlock = getBlockFromModelOrException(block);
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung.getPruefungsnummer());

    if (!modelBlock.removePruefung(modelPruefung)) {
      throw new IllegalArgumentException("Pruefung konnte nicht aus dem Block entfernt werden.");
    }
    if (modelBlock.getPruefungen().isEmpty()) {
      modelBlock.setStartzeitpunkt(null);
    }
    return new Pair<>(modelBlock, modelPruefung);
  }

  public Pair<Block, Pruefung> addPruefungToBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung) {
    Block modelBlock = getBlockFromModelOrException(block);
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    Optional<Block> potentialOldBlock = Optional.ofNullable(pruefungsperiode.block(modelPruefung));

    if (block.getROPruefungen() != null || !modelBlock.getPruefungen().contains(modelPruefung)) {
      if (potentialOldBlock.isPresent()) {
        Block oldBlock = potentialOldBlock.get();
        Pair<Block, Pruefung> unscheduled = removePruefungFromBlock(
            converter.convertToROBlock(oldBlock), pruefung);
        modelPruefung = unscheduled.right();
      }
      modelBlock.addPruefung(modelPruefung);
    }

    return new Pair<>(modelBlock, modelPruefung);
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

  public List<Planungseinheit> getAllPlanungseinheitenBetween(LocalDateTime start,
      LocalDateTime end)
      throws IllegalTimeSpanException {
    if (start.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }
    return List.copyOf(pruefungsperiode.planungseinheitenBetween(start, end));
  }

  public Set<ReadOnlyPruefung> getAllReadOnlyPruefungenBetween(LocalDateTime start,
      LocalDateTime end)
      throws IllegalTimeSpanException {
    return Set.copyOf(converter.convertToROPruefungCollection(getAllPruefungenBetween(start, end)));
  }

  @NotNull
  public Set<Pruefung> getAllPruefungenBetween(@NotNull LocalDateTime start,
      @NotNull LocalDateTime end)
      throws IllegalTimeSpanException {
    if (start.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }
    Set<Planungseinheit> planungseinheitenBetween = pruefungsperiode.planungseinheitenBetween(start,
        end);
    return getAllPruefungen(planungseinheitenBetween);
  }

  //TODO kann das hier raus? Evtl? Weil hier an dieser Stelle der Converter genutzt wird.
  public Optional<ReadOnlyBlock> getBlockTo(ReadOnlyPruefung pruefung) {
    String nummer = pruefung.getPruefungsnummer();

    if (existsPruefungWith(nummer)) {
      Optional<Block> blockOpt =
          getBlockTo(pruefungsperiode.pruefung(nummer));
      if (blockOpt.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(converter.convertToROBlock(blockOpt.get()));
      }
    } else {
      throw new IllegalArgumentException("Pruefungsnummer nicht im System!");
    }
  }


  public Optional<Block> getBlockTo(Pruefung pruefung) {
    return Optional.ofNullable(pruefungsperiode.block(pruefung));
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


  public boolean removeTeilnehmerkreis(Pruefung roPruefung,
      Teilnehmerkreis teilnehmerkreis) {
    return roPruefung.removeTeilnehmerkreis(teilnehmerkreis);

  }

  public boolean addTeilnehmerkreis(Pruefung pruefung, Teilnehmerkreis teilnehmerkreis,
      int schaetzung) {
    return pruefung.addTeilnehmerkreis(teilnehmerkreis, schaetzung);
  }

  public ReadOnlyBlock setNameOfBlock(ReadOnlyBlock block, String name) {
    Block model = getBlockFromModelOrException(block);
    model.setName(name);
    return converter.convertToROBlock(model);
  }

  public Set<ReadOnlyPruefung> getAllKlausurenFromPruefer(String pruefer) {
    Set<Planungseinheit> planungseinheiten = pruefungsperiode.getPlanungseinheiten();
    Set<ReadOnlyPruefung> result = new HashSet<>();
    Pruefung pruefung;
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (!planungseinheit.isBlock()) {
        pruefung = planungseinheit.asPruefung();
        if (pruefung.getPruefer().contains(pruefer)) {
          result.add(converter.convertToReadOnlyPruefung(pruefung));
        }
      }
    }
    return result;
  }

  public LocalDate getAnkerPeriode() {
    return pruefungsperiode.getAnkertag();
  }

  public int getAnzahlStudentenZeitpunkt(LocalDateTime zeitpunkt) {
    HashMap<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
    int result = 0;
    for (Pruefung pruefung : pruefungsperiode.geplantePruefungen()) {
      LocalDateTime start = pruefung.getStartzeitpunkt();
      LocalDateTime end = pruefung.endzeitpunkt();
      if ((start.equals(zeitpunkt) || start.isBefore(zeitpunkt))
          && (end.equals(zeitpunkt) || end.isAfter(zeitpunkt))) {
        PlanungseinheitUtil.compareAndPutBiggerSchaetzung(schaetzungen,
            pruefung.getSchaetzungen());
      }
    }

    for (Integer schaetzung : schaetzungen.values()) {
      result += schaetzung;
    }
    return result;
  }


  public boolean areInSameBlock(Pruefung pruefung, Pruefung otherPruefung) {
    Optional<Block> pruefungBlock = getBlockTo(pruefung);
    Optional<Block> otherBlock = getBlockTo(otherPruefung);
    if (pruefungBlock.isEmpty()) {
      return false;
    }
    if (otherBlock.isEmpty()) {
      return false;
    }
    return pruefungBlock.equals(otherBlock);
  }

  public Pruefungsperiode getPruefungsperiode() {
    return pruefungsperiode;
  }
}
