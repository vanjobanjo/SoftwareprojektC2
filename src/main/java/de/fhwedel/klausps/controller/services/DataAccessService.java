package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static de.fhwedel.klausps.controller.util.PlanungseinheitUtil.getAllPruefungen;
import static de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung;
import static java.util.Objects.nonNull;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
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

  public DataAccessService() {
    this.pruefungsperiode = null;
  }

  public DataAccessService(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public void setKapazitaetStudents(int kapazitaet)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    if (kapazitaet < 0) {
      throw new IllegalArgumentException("Tried to set a negative capacity of students.");
    }
    checkForPruefungsperiode();
    pruefungsperiode.setKapazitaet(kapazitaet);
  }

  private void checkForPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    if (pruefungsperiode == null) {
      throw new NoPruefungsPeriodeDefinedException();
    }
  }

  public Pruefung createPruefung(String name, String pruefungsNr, String refVWS,
      String pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise)
      throws NoPruefungsPeriodeDefinedException {
    return createPruefung(name, pruefungsNr, refVWS, Set.of(pruefer), duration, teilnehmerkreise);
  }

  public Pruefung createPruefung(String name, String pruefungsNr, String refVWS,
      Set<String> pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(name, pruefungsNr, pruefer, refVWS);
    checkForPruefungsperiode();
    if (existsPruefungWith(pruefungsNr)) {
      return null;
    }

    Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, refVWS, duration);
    pruefer.forEach(pruefungModel::addPruefer);
    addTeilnehmerKreisSchaetzungToModelPruefung(pruefungModel, teilnehmerkreise);
    pruefungsperiode.addPlanungseinheit(pruefungModel);
    return pruefungModel;

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

  /**
   * Schedules a pruefung without any consistency checks.
   *
   * @param pruefung    The pruefung to schedule.
   * @param startTermin The time to schedule the pruefung to.
   */
  public Pruefung schedulePruefung(ReadOnlyPruefung pruefung, LocalDateTime startTermin)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    Pruefung pruefungFromModel = getPruefungFromModelOrException(pruefung);
    if (pruefungsperiode.block(pruefungFromModel) != null) {
      throw new IllegalArgumentException("Prüfung befindet sich innerhalb eines Blockes");
    } else {
      pruefungFromModel.setStartzeitpunkt(startTermin);
      return pruefungFromModel;
    }
  }

  private Pruefung getPruefungFromModelOrException(ReadOnlyPruefung pruefung)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    Optional<Pruefung> possiblePruefung = getPruefung(pruefung);
    if (possiblePruefung.isEmpty()) {
      throw new IllegalArgumentException(
          "Pruefung mit Pruefungsnummer " + pruefung.getPruefungsnummer()
              + " ist in der Datenbank nicht vorhanden.");
    }
    return possiblePruefung.get();
  }

  public Optional<Pruefung> getPruefung(ReadOnlyPruefung readOnlyPruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(readOnlyPruefung);
    checkForPruefungsperiode();
    return Optional.ofNullable(pruefungsperiode.pruefung(readOnlyPruefung.getPruefungsnummer()));
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
      return blockExists(block);
    } else {
      Optional<Block> modelBlock = searchInModel(block);
      return modelBlock.filter(
              value -> areSameBlocksBySpecs(block, value) && haveSamePruefungen(block, value))
          .isPresent();
    }
  }

  private boolean blockExists(ReadOnlyBlock block) {
    return pruefungsperiode.block(block.getBlockId()) != null;
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

  public Planungseinheit changeNameOfPruefung(ReadOnlyPruefung toChange, String name)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(toChange, name);
    Pruefung pruefung = getPruefungFromModelOrException(toChange);
    pruefung.setName(name);
    return pruefung;
  }

  public Set<Pruefung> getGeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.geplantePruefungen();
  }

  public Set<Planungseinheit> getAllPlanungseinheitenBetween(LocalDateTime start,
      LocalDateTime end) throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    noNullParameters(start, end);
    checkForPruefungsperiode();
    if (start.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }
    return this.getPruefungsperiode().planungseinheitenBetween(start, end);
  }

  public Pruefungsperiode getPruefungsperiode() {
    return pruefungsperiode;
  }

  public void setPruefungsperiode(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  @NotNull
  public Set<Pruefung> getAllPruefungenBetween(LocalDateTime start, LocalDateTime end)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    noNullParameters(start, end);
    checkForPruefungsperiode();
    if (start.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }
    Set<Planungseinheit> planungseinheitenBetween = pruefungsperiode.planungseinheitenBetween(start,
        end);
    return getAllPruefungen(planungseinheitenBetween);
  }

  public Set<Pruefung> getPlannedPruefungen() {
    return pruefungsperiode.geplantePruefungen();
  }

  @NotNull
  public Set<Pruefung> getUngeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.ungeplantePruefungen();
  }

  @NotNull
  public Set<Block> getGeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.geplanteBloecke();
  }

  @NotNull
  public Set<Block> getUngeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.ungeplanteBloecke();
  }

  @NotNull
  public Set<Pruefung> ungeplantePruefungenForTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmerkreis);
    checkForPruefungsperiode();
    return pruefungsperiode.ungeplantePruefungen().stream()
        .filter(pruefung -> pruefung.getTeilnehmerkreise().contains(teilnehmerkreis))
        .collect(Collectors.toSet());
  }

  public Set<Pruefung> geplantePruefungenForTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmerkreis);
    checkForPruefungsperiode();
    return pruefungsperiode.geplantePruefungen().stream()
        .filter(pruefung -> pruefung.getTeilnehmerkreise().contains(teilnehmerkreis))
        .collect(Collectors.toSet());
  }

  public Planungseinheit addPruefer(ReadOnlyPruefung pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungModel = getPruefungFromModelOrException(pruefung);
    pruefungModel.addPruefer(pruefer);
    return pruefungModel;
  }

  public Planungseinheit removePruefer(ReadOnlyPruefung pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung);
    modelPruefung.removePruefer(pruefer);
    return modelPruefung;
  }

  public Planungseinheit setPruefungsnummer(ReadOnlyPruefung pruefung,
      String pruefungsnummer) throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung);
    if (existsPruefungWith(pruefungsnummer)) {
      throw new IllegalArgumentException("Die angegebene Pruefungsnummer ist bereits vergeben.");
    }
    modelPruefung.setPruefungsnummer(pruefungsnummer);
    return modelPruefung;
  }

  public Block deletePruefung(ReadOnlyPruefung roPruefung)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getPruefungFromModelOrException(roPruefung);
    Block block = pruefungsperiode.block(pruefung);
    pruefungsperiode.removePlanungseinheit(pruefung);
    return block;
  }

  /**
   * Unschedules a pruefung without any consistency checks.
   *
   * @param pruefung The pruefung to schedule.
   */
  public Pruefung unschedulePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefungFromModel = getPruefungFromModelOrException(pruefung);
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

  public List<Pruefung> deleteBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    if (block.geplant()) {
      throw new IllegalArgumentException("Nur für ungeplante Blöcke möglich!");
    }
    noNullParameters(block);
    checkForPruefungsperiode();
    Block model = getBlockFromModelOrException(block);
    Set<Pruefung> modelPruefungen = new HashSet<>(
        model.getPruefungen()); // very important, when we call
    // de.fhwedel.klausps.model.api.Block.removeAllPruefungen it
    // removes also the set, so we need a deep copy of the set
    model.removeAllPruefungen();
    pruefungsperiode.removePlanungseinheit(model);
    return new LinkedList<>(modelPruefungen);
  }

  public Block createBlock(String name, ReadOnlyPruefung... pruefungen)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    noNullParameters(name, pruefungen);
    checkForPruefungsperiode();
    if (Arrays.stream(pruefungen).anyMatch(ReadOnlyPlanungseinheit::geplant)) {
      throw new IllegalArgumentException("Einer der übergebenen Prüfungen ist geplant.");
    }

    if (isAnyInBlock(List.of(pruefungen))) {
      throw new IllegalArgumentException("Einer der Prüfungen ist bereits im Block!");
    }

    if (containsDuplicatePruefung(pruefungen)) {
      throw new IllegalArgumentException("Doppelte Prüfungen im Block!");
    }

    Block blockModel = new BlockImpl(pruefungsperiode, name, Blocktyp.PARALLEL);
    Arrays.stream(pruefungen).forEach(pruefung -> blockModel.addPruefung(
        pruefungsperiode.pruefung(pruefung.getPruefungsnummer())));
    if (!pruefungsperiode.addPlanungseinheit(blockModel)) {
      throw new IllegalArgumentException("Irgendwas ist schief gelaufen."
          + " Der Block konnte nicht in die Datenbank übertragen werden.");
    }
    return blockModel;
  }

  private boolean isAnyInBlock(Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream()
        .anyMatch(pruefung -> this.pruefungIsInBlock(pruefung.getPruefungsnummer()));
  }

  private boolean containsDuplicatePruefung(ReadOnlyPruefung[] pruefungen) {
    return pruefungen.length != Arrays.stream(pruefungen).distinct().count();
  }

  private boolean pruefungIsInBlock(String pruefungsNummer) throws IllegalArgumentException {
    if (existsPruefungWith(pruefungsNummer)) {
      return Optional.ofNullable(pruefungsperiode.block(pruefungsperiode.pruefung(pruefungsNummer)))
          .isPresent();
    }
    throw new IllegalArgumentException("Pruefung existiert nicht.");
  }

  public Block removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = getBlockFromModelOrException(block);
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung);

    modelBlock.removePruefung(modelPruefung);
    if (modelBlock.getPruefungen().isEmpty()) {
      modelBlock.setStartzeitpunkt(null);
    }

    return modelBlock;
  }

  public Block addPruefungToBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = getBlockFromModelOrException(block);
    Pruefung modelPruefung = getPruefungFromModelOrException(pruefung);
    modelBlock.addPruefung(modelPruefung);
    return modelBlock;
  }

  @NotNull
  public LocalDate getStartOfPeriode() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.getStartdatum();
  }

  @NotNull
  public LocalDate getEndOfPeriode() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.getEnddatum();
  }

  public int getPeriodenKapazitaet() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.getKapazitaet();
  }

  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.getSemester();
  }

  public Optional<Block> getBlockTo(ReadOnlyPruefung pruefungToGetBlockFor)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    noNullParameters(pruefungToGetBlockFor);
    checkForPruefungsperiode();
    Pruefung pruefung = pruefungsperiode.pruefung(pruefungToGetBlockFor.getPruefungsnummer());
    if (pruefung != null) {
      return getBlockTo(pruefung);
    } else {
      throw new IllegalArgumentException("Asked for block of a pruefung that is unknown.");
    }
  }

  public Optional<Block> getBlockTo(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    checkForPruefungsperiode();
    return Optional.ofNullable(pruefungsperiode.block(pruefung));
  }

  public Set<Teilnehmerkreis> getAllTeilnehmerkreise() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
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

  public Block setNameOfBlock(ReadOnlyBlock block, String name)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, name);
    checkForPruefungsperiode();
    Block model = getBlockFromModelOrException(block);
    model.setName(name);
    return model;
  }

  @NotNull
  public Set<Pruefung> getAllKlausurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefer);
    checkForPruefungsperiode();
    Set<Planungseinheit> planungseinheiten = pruefungsperiode.getPlanungseinheiten();
    Set<Pruefung> result = new HashSet<>();
    Pruefung pruefung;
    for (Planungseinheit planungseinheit : planungseinheiten) {
      if (!planungseinheit.isBlock()) {
        pruefung = planungseinheit.asPruefung();
        if (pruefung.getPruefer().contains(pruefer)) {
          result.add(pruefung);
        }
      }
    }
    return result;
  }

  @NotNull
  public LocalDate getAnkertag() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.getAnkertag();
  }

  public void setAnkertag(LocalDate newAnkerTag)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(newAnkerTag);
    checkForPruefungsperiode();
    ensureNotBeforePruefungsperiode(newAnkerTag);
    ensureNotAfterPruefungsperiode(newAnkerTag);
    pruefungsperiode.setAnkertag(newAnkerTag);
  }

  private void ensureNotBeforePruefungsperiode(LocalDate newAnkerTag)
      throws IllegalTimeSpanException {
    if (newAnkerTag.isBefore(pruefungsperiode.getStartdatum())) {
      throw new IllegalTimeSpanException(
          "An Ankertag must not be before the start of the Pruefungsperiode.");
    }
  }

  private void ensureNotAfterPruefungsperiode(LocalDate newAnkerTag)
      throws IllegalTimeSpanException {
    if (newAnkerTag.isAfter(pruefungsperiode.getEnddatum())) {
      throw new IllegalTimeSpanException(
          "An Ankertag must not be after the end of the Pruefungsperiode.");
    }
  }

  public int getAnzahlStudentenZeitpunkt(LocalDateTime zeitpunkt)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(zeitpunkt);
    checkForPruefungsperiode();
    HashMap<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
    int result = 0;
    for (Pruefung pruefung : pruefungsperiode.geplantePruefungen()) {
      LocalDateTime start = pruefung.getStartzeitpunkt();
      LocalDateTime end = pruefung.endzeitpunkt();
      if ((start.equals(zeitpunkt) || start.isBefore(zeitpunkt))
          && (end.equals(zeitpunkt) || end.isAfter(zeitpunkt))) {
        compareAndPutBiggerSchaetzung(schaetzungen,
            pruefung.getSchaetzungen());
      }
    }

    for (Integer schaetzung : schaetzungen.values()) {
      result += schaetzung;
    }
    return result;
  }

  public boolean areInSameBlock(Pruefung pruefung, Pruefung otherPruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, otherPruefung);
    return getBlockTo(pruefung)
        .filter((Block block) -> block.getPruefungen().contains(otherPruefung)).isPresent();
  }

  public Optional<Block> getBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    checkForPruefungsperiode();
    return Optional.ofNullable(pruefungsperiode.block(block.getBlockId()));
  }

  public boolean existsBlockWith(int blockId) throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.block(blockId) != null;
  }

  @NotNull
  public Set<Planungseinheit> getPlanungseinheitenAt(LocalDateTime time)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(time);
    checkForPruefungsperiode();
    return pruefungsperiode.planungseinheitenAt(time);
  }

  public void adoptPruefungstermine(Pruefungsperiode adoptFrom)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(adoptFrom);
    checkForPruefungsperiode();
    pruefungsperiode.adoptPruefungstermine(adoptFrom);
    unscheduleAdoptedBloeckeOutsideOfPeriode();
    unscheduleAdoptedPruefungenOutsideOfPeriode();
  }

  private void unscheduleAdoptedBloeckeOutsideOfPeriode() {
    for (Block block : pruefungsperiode.geplanteBloecke()) {
      LocalDate plannedDate = block.getStartzeitpunkt().toLocalDate();
      if (isBeforeStartOfPeriode(plannedDate) || isAfterEndOfPeriode(plannedDate)) {
        block.setStartzeitpunkt(null);
      }
    }
  }

  private void unscheduleAdoptedPruefungenOutsideOfPeriode() {
    for (Pruefung pruefung : pruefungsperiode.geplantePruefungen()) {
      LocalDate plannedDate = pruefung.getStartzeitpunkt().toLocalDate();
      if (isBeforeStartOfPeriode(plannedDate) || isAfterEndOfPeriode(plannedDate)) {
        pruefung.setStartzeitpunkt(null);
      }
    }
  }

  private boolean isBeforeStartOfPeriode(LocalDate plannedDate) {
    return plannedDate.isBefore(pruefungsperiode.getStartdatum());
  }

  private boolean isAfterEndOfPeriode(LocalDate plannedDate) {
    return plannedDate.isAfter(pruefungsperiode.getStartdatum());
  }

  public void setDatumPeriode(LocalDate startDatum, LocalDate endDatum) {
    pruefungsperiode.setStartdatum(startDatum);
    pruefungsperiode.setEnddatum(endDatum);
  }
}
