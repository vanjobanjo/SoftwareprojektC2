package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.PlanungseinheitUtil.getAllPruefungen;
import static java.util.Objects.nonNull;

import de.fhwedel.klausps.controller.api.BlockDTO;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class DataAccessService {

  private Pruefungsperiode pruefungsperiode;
  private ScheduleService scheduleService; // TODO ScheduleService muss hier noch raus.
  private Converter converter; //TODO where does it come from

  public void setPruefungsperiode(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public void setScheduleService(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  public void setConverter(Converter converter) {
    this.converter = converter;
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr, String refVWS,
      String pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    return createPruefung(name, pruefungsNr, refVWS, Set.of(pruefer), duration, teilnehmerkreise);
  }

  public ReadOnlyPruefung createPruefung(String name, String pruefungsNr, String refVWS,
      Set<String> pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {

    if (!existsPruefungWith(pruefungsNr)) {
      // todo contains static values as it is unclear where to retrieve the data from
      //TODO hier die Duration weg machen
      Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, refVWS, duration);
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

  private void addTeilnehmerKreisSchaetzungToModelPruefung(Pruefung pruefungModel,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    teilnehmerkreise.forEach(pruefungModel::setSchaetzung);
  }

  public boolean isPruefungsperiodeSet() {
    return nonNull(pruefungsperiode);
  }

  public List<ReadOnlyPlanungseinheit> changeDurationOf(ReadOnlyPruefung pruefung, Duration minutes)
      throws HartesKriteriumException, IllegalArgumentException {

    if (minutes.isNegative()) {
      throw new IllegalArgumentException("Die Dauer der Pruefung muss positiv sein.");
    }

    Pruefung toChangeDuration = getPruefungFromModelOrException(pruefung.getPruefungsnummer());
    // Hartes Kriterium wird in ScheduleService geprüft.
    // Die Änderungen der Pruefungen werden auch im ScheduleService vorgenommen.
    List<Pruefung> resultOfChangingDuration = scheduleService.changeDuration(toChangeDuration,
        minutes);
    return new ArrayList<>(createListOfPruefungWithScoring(resultOfChangingDuration));
  }

  private Pruefung getPruefungFromModelOrException(String pruefungsNr)
      throws IllegalArgumentException {
    if (!existsPruefungWith(pruefungsNr)) {
      throw new IllegalArgumentException(
          "Pruefung mit Pruefungsnummer " + pruefungsNr + " ist in der Datenbank nicht vorhanden.");
    }
    return pruefungsperiode.pruefung(pruefungsNr);
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

  public ReadOnlyBlock fromModelToDTOBlock(Block block) {
    // todo auslagern, wenn Converter implementiert ist
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    for (Pruefung pruefung : block.getPruefungen()) {
      pruefungen.add(fromModelToDTOPruefungWithScoring(pruefung));
    }
    return new BlockDTO(block.getName(), block.getStartzeitpunkt(), block.getDauer(),
        pruefungen, block.getId(), block.getTyp());
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

  private ReadOnlyPruefung fromModelToDTOPruefungWithScoring(Pruefung pruefung) {
    // TODO extract into appropriate class
    return new PruefungDTOBuilder(pruefung).withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }

  private boolean emptyBlockExists(ReadOnlyBlock block) {
    for (Block modelBlock : pruefungsperiode.ungeplanteBloecke()) {
      // todo add all necessary checks for empty blocks
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

  public ReadOnlyPruefung changeNameOfPruefung(ReadOnlyPruefung toChange, String name) {
    Pruefung pruefung = getPruefungFromModelOrException(toChange.getPruefungsnummer());
    pruefung.setName(name);
    int scoring = toChange.getScoring();
    return new PruefungDTOBuilder(pruefung).withScoring(scoring).build();
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen() {
    return new HashSet<>(
        converter.convertToROPruefungCollection(pruefungsperiode.geplantePruefungen()));
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

    Block blockModel = new BlockImpl(pruefungsperiode, name,
        Blocktyp.SEQUENTIAL); // TODO bei Erzeugung Sequential?
    Arrays.stream(pruefungen).forEach(pruefung -> blockModel.addPruefung(
        pruefungsperiode.pruefung(pruefung.getPruefungsnummer())));
    if (!pruefungsperiode.addPlanungseinheit(blockModel)) {
      throw new IllegalArgumentException("Irgendwas ist schief gelaufen."
          + " Der Block konnte nicht in die Datenbank übertragen werden.");
    }
    return fromModelToDTOBlock(blockModel);
  }

  private boolean isAnyInBlock(Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream()
        .anyMatch((pruefung) -> this.pruefungIsInBlock(pruefung.getPruefungsnummer()));
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

  /**
   * Gets the amount of pruefungen taking place at a specified time. Multiple pruefungen that are
   * planned as one block only count as one.
   *
   * @param time The time to check for.
   * @return The amount of planned pruefungen.
   */
  @Deprecated
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

  public Optional<ReadOnlyBlock> getBlockTo(ReadOnlyPruefung pruefung) {
    String nummer = pruefung.getPruefungsnummer();

    if (existsPruefungWith(nummer)) {
      Optional<Block> blockOpt =
          getBlockTo(pruefungsperiode.pruefung(nummer));
      if (blockOpt.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(fromModelToDTOBlock(blockOpt.get()));
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

  public boolean addTeilnehmerkreis(Pruefung roPruefung, Teilnehmerkreis teilnehmerkreis) {
    return roPruefung.addTeilnehmerkreis(teilnehmerkreis);
  }

}
