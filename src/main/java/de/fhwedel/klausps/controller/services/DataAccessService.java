package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noEmptyStrings;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static de.fhwedel.klausps.controller.util.PlanungseinheitUtil.getAllPruefungen;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the only one who change the data in the model. And provide the Data from the
 * model.
 */
public class DataAccessService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessService.class);

  private Pruefungsperiode pruefungsperiode;

  public DataAccessService() {
    this.pruefungsperiode = null;
  }

  public DataAccessService(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  /**
   * Sets the kapazität of the periode with the passed kapazitaet
   *
   * @param kapazitaet to set
   * @throws IllegalArgumentException           when the passed number is negative
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public void setKapazitaetStudents(int kapazitaet)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    if (kapazitaet <= 0) {
      throw new IllegalArgumentException("Tried to set a negative capacity of students.");
    }
    checkForPruefungsperiode();
    LOGGER.debug("Changing the student capacity in Model from {} to {}.",
        pruefungsperiode.getKapazitaet(), kapazitaet);
    pruefungsperiode.setKapazitaet(kapazitaet);
  }

  /**
   * Creates a pruefung inside the periode which is set
   *
   * @param name             name of the pruefung
   * @param pruefungsNr      number of the pruefung
   * @param refVWS           reference of the pruefung
   * @param pruefer          set of pruefer
   * @param duration         duration of the pruefung
   * @param teilnehmerkreise teilnehmerkreise with schaetzung as a map
   * @return the created pruefung is ungeplant
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalArgumentException           when duplicated pruefungsnummern
   */
  public Pruefung createPruefung(String name, String pruefungsNr, String refVWS,
      Set<String> pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    noNullParameters(name, pruefungsNr, pruefer, refVWS);
    noEmptyStrings(name, pruefungsNr, refVWS);
    noEmptyStrings(pruefer.toArray(new String[0]));
    checkForPruefungsperiode();
    if (existsPruefungWith(pruefungsNr)) {
      LOGGER.trace("Found Pruefung with Pruefungsnummer {} in Model", pruefungsNr);
      throw new IllegalArgumentException("Es existiert bereits eine Prüfung mit dieser Nummer");
    }
    if (duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException("Die Dauer einer Prüfung muss positiv sein.");
    }

    for (Integer schaetzung : teilnehmerkreise.values()) {
      if (schaetzung < 0) {
        throw new IllegalArgumentException("Schätzwerte müssen positiv sein");
      }
    }
    Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, refVWS, duration);
    pruefer.forEach(pruefungModel::addPruefer);
    addTeilnehmerKreisSchaetzungToModelPruefung(pruefungModel, teilnehmerkreise);
    LOGGER.debug("Created {} and saved it to Model", pruefungModel);
    pruefungsperiode.addPlanungseinheit(pruefungModel);
    return pruefungModel;

  }

  /**
   * Creates a pruefung with only one pruefer
   *
   * @param name             name of the pruefung
   * @param pruefungsNr      number of the pruefung
   * @param refVWS           reference of the pruefung
   * @param pruefer          name of the pruefer
   * @param duration         duration of the pruefung
   * @param teilnehmerkreise teilnehmerkreise with schaetzungen
   * @return the created model pruefung
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Pruefung createPruefung(String name, String pruefungsNr, String refVWS,
      String pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreise)
      throws NoPruefungsPeriodeDefinedException {
    return createPruefung(name, pruefungsNr, refVWS, Set.of(pruefer), duration, teilnehmerkreise);
  }

  /**
   * Checks if periode is set
   *
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  private void checkForPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    LOGGER.trace("Check if pruefungsperiode is set.");
    if (pruefungsperiode == null) {
      throw new NoPruefungsPeriodeDefinedException();
    }
  }

  /**
   * Checks if a pruefung with the passed number is set
   *
   * @param pruefungsNummer passed number
   * @return true when a pruefung with the passed number is set
   */
  public boolean existsPruefungWith(String pruefungsNummer) {
    return pruefungsperiode.pruefung(pruefungsNummer) != null;
  }

  /**
   * Adds the passed teilnehmerkreisschaetzung to the passed pruefung
   *
   * @param pruefung         to add the teilnehmerkreis to
   * @param teilnehmerkreise passed teilnehmerkreisschaetzung to be added to the passed pruefung
   */
  private void addTeilnehmerKreisSchaetzungToModelPruefung(Pruefung pruefung,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    for (Entry<Teilnehmerkreis, Integer> teilnehmerkreis : teilnehmerkreise.entrySet()) {
      pruefung.addTeilnehmerkreis(teilnehmerkreis.getKey(), teilnehmerkreis.getValue());
    }
  }

  /**
   * Checks if a periode is set
   *
   * @return true when it is set otherwise false
   */
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
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException, IllegalStateException {
    Pruefung pruefungFromModel = getPruefung(pruefung);
    ensurePlanungseinheitIsInPeriode(startTermin, pruefung.getDauer());
    if (pruefungsperiode.block(pruefungFromModel) != null) {
      throw new IllegalArgumentException("Prüfung befindet sich innerhalb eines Blockes");
    } else {
      LOGGER.debug("Scheduling {} from previously {} to {}.", pruefungFromModel,
          pruefungFromModel.getStartzeitpunkt(), startTermin);
      pruefungFromModel.setStartzeitpunkt(startTermin);
      return pruefungFromModel;
    }
  }

  /**
   * Ensures that the passed termin with the passed duration is in the periode of the periode which
   * is set
   *
   * @param startTermin start termin of the pruefung to ensure
   * @param duration    duration of the pruefung to ensure
   */
  private void ensurePlanungseinheitIsInPeriode(LocalDateTime startTermin, Duration duration) {
    if (startTermin.isBefore(pruefungsperiode.getStartdatum().atStartOfDay())) {
      throw new IllegalArgumentException(
          "Prüfungstermin muss nach dem Start der Prüfungsperiode liegen");
    }
    if (!startTermin.plusMinutes(duration.toMinutes())
        .isBefore(pruefungsperiode.getEnddatum().plusDays(1).atStartOfDay())) {
      throw new IllegalArgumentException(
          "Pruefung darf nicht außerhalb der Pruefungsperiode enden");
    }
  }

  /**
   * Gets the model pruefung to the passed DTO pruefung
   *
   * @param readOnlyPruefung DTOPruefung to get the model pruefung
   * @return model pruefung
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalStateException              an exception from the model
   */
  public Pruefung getPruefung(ReadOnlyPruefung readOnlyPruefung)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(readOnlyPruefung);
    checkForPruefungsperiode();
    LOGGER.debug("Requesting Pruefung {} from Model results in {}.", readOnlyPruefung,
        pruefungsperiode.pruefung(readOnlyPruefung.getPruefungsnummer()));
    Pruefung pruefung = pruefungsperiode.pruefung(readOnlyPruefung.getPruefungsnummer());
    if (pruefung == null) {
      throw new IllegalStateException("Pruefung existiert nicht.");
    }
    return pruefung;
  }

  /**
   * Schedules a block without any consistency checks. The passed block is consistent and has
   * pruefungen inside.
   *
   * @param block  The block to schedule
   * @param termin The time to schedule the pruefung to.
   */
  Block scheduleBlock(ReadOnlyBlock block, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    Block blockFromModel = getBlock(block);
    if (blockFromModel.getPruefungen().isEmpty()) {
      throw new IllegalArgumentException("Leere Blöcke dürfen nicht geplant werden.");
    }
    ensurePlanungseinheitIsInPeriode(termin, block.getDauer());
    LOGGER.debug("Scheduled {} from {} to {}.", blockFromModel, blockFromModel.getStartzeitpunkt(),
        termin);
    blockFromModel.setStartzeitpunkt(termin);

    return blockFromModel;
  }


  /**
   * Checks the consistency of a ReadOnlyBlock
   *
   * @param block Block to check with the model data
   */
  boolean exists(ReadOnlyBlock block) {
    return pruefungsperiode.block(block.getBlockId()) != null;
  }

  public Block unscheduleBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = getBlock(block);
    LOGGER.debug("Unscheduling {} from Model.", modelBlock);
    modelBlock.setStartzeitpunkt(null);
    return modelBlock;
  }

  /**
   * Changes the name of the passed pruefung to passed name
   *
   * @param toChange DTOPruefung to change the name
   * @param name     the new name
   * @return the block when the pruefung was inside a block, or a pruefung
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalArgumentException           when the pruefung doesn't exist
   */
  public Planungseinheit changeNameOf(ReadOnlyPruefung toChange, String name)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(toChange, name);
    noEmptyStrings(name);

    Pruefung pruefung = getPruefung(toChange);
    LOGGER.debug("Change name for {} in Model from {} to {}.", pruefung, pruefung.getName(), name);
    Optional<Block> blockToPruefung = getBlockTo(pruefung);
    pruefung.setName(name);
    if (blockToPruefung.isEmpty()) {
      return pruefung;
    } else {
      return blockToPruefung.get();
    }
  }

  /**
   * Gets a set of Planungseinheiten, which are scheduled between the passed start and end. Uses the
   * model implementation. Check the documentation of the model for further details.
   *
   * @param start start
   * @param end   end
   * @return set of planungseinheiten without duplicates
   * @throws IllegalTimeSpanException           when start is after end or inversly
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Set<Planungseinheit> getAllPlanungseinheitenBetween(LocalDateTime start,
      LocalDateTime end) throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    noNullParameters(start, end);
    checkForPruefungsperiode();
    if (start.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }
    LOGGER.debug("Request Planungseinheiten between {} and {} from Model: {}.", start, end,
        pruefungsperiode.planungseinheitenBetween(start, end));
    return new HashSet<>(this.getPruefungsperiode().planungseinheitenBetween(start, end));
  }

  public Pruefungsperiode getPruefungsperiode() {
    return pruefungsperiode;
  }

  public void setPruefungsperiode(Pruefungsperiode pruefungsperiode) {
    LOGGER.debug("Setting the pruefungsperiode in Model to: {}.", pruefungsperiode);
    this.pruefungsperiode = pruefungsperiode;
  }

  /**
   * Gets a set of Pruefungen, which are scheduled between the passed start and end. Uses the model
   * implementation. Check the documentation of the model for further details.
   *
   * @param start start
   * @param end   end
   * @return set of pruefungen, also the one which are inside a block
   * @throws IllegalTimeSpanException           when start is after end or inversly
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Pruefung> getAllPruefungenBetween(LocalDateTime start, LocalDateTime end)
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    noNullParameters(start, end);
    checkForPruefungsperiode();
    if (start.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Start liegt nach dem Ende des Zeitslots");
    }
    LOGGER.debug("Getting all Planungseinheiten between {} and {} from Model: {}.", start, end,
        pruefungsperiode.planungseinheitenBetween(start, end));
    Set<Planungseinheit> planungseinheitenBetween = pruefungsperiode.planungseinheitenBetween(start,
        end);
    return getAllPruefungen(planungseinheitenBetween);
  }

  /**
   * Gets all planned pruefungen of the periode
   *
   * @return set of pruefungen which are planned, also the ones which are inside a block
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Set<Pruefung> getPlannedPruefungen() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Request all planned Pruefungen from Model: {}.",
        pruefungsperiode.geplantePruefungen());
    return pruefungsperiode.geplantePruefungen();
  }

  /**
   * Gets all unplanned pruefungen of the periode
   *
   * @return set of pruefungen which are unplanned, also the ones which are inside a block
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Pruefung> getUngeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get all unplanned Pruefungen from Model: {}.",
        pruefungsperiode.ungeplantePruefungen());
    return pruefungsperiode.ungeplantePruefungen();
  }

  /**
   * Gets all planned blocks of the periode
   *
   * @return set of blocks which are planned
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Block> getGeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get all planned Bloecke from Model: {}.", pruefungsperiode.geplanteBloecke());
    return pruefungsperiode.geplanteBloecke();
  }

  /**
   * Gets all unplanned blocks of the periode
   *
   * @return set of blocks which are unplanned
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Block> getUngeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get all unplanned Bloecke from Model: {}.", pruefungsperiode.ungeplanteBloecke());
    return pruefungsperiode.ungeplanteBloecke();
  }

  /**
   * Gets unplannend pruefungen filtered by the passed teilnehmerkreis
   *
   * @param teilnehmerkreis pruefungen with teilnehmerkreis to consider
   * @return set of unplanned pruefungen which contain the passed teilnehmerkreis
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Pruefung> ungeplantePruefungenForTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmerkreis);
    checkForPruefungsperiode();
    Set<Pruefung> result = filterPruefungenWith(pruefungsperiode.ungeplantePruefungen(),
        teilnehmerkreis);
    LOGGER.debug("All unplanned Pruefungen for {} are: {}", teilnehmerkreis, result);
    return result;
  }

  /**
   * Filters the passed collection of pruefung with the passed teilnehmerkreis
   *
   * @param pruefungen      collection of pruefungen to filter with the passed teilnehmerkreis
   * @param teilnehmerkreis to consider
   * @return set of pruefungen which contain the passed teilnehmerkreis
   */
  private Set<Pruefung> filterPruefungenWith(Collection<Pruefung> pruefungen,
      Teilnehmerkreis teilnehmerkreis) {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung pruefung : pruefungen) {
      if (pruefung.getTeilnehmerkreise().contains(teilnehmerkreis)) {
        result.add(pruefung);
      }
    }
    return result;
  }

  /**
   * Gets plannend pruefungen filtered by the passed teilnehmerkreis
   *
   * @param teilnehmerkreis pruefungen with teilnehmerkreis to consider
   * @return set of planned pruefungen which contain the passed teilnehmerkreis
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Set<Pruefung> geplantePruefungenForTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmerkreis);
    checkForPruefungsperiode();
    Set<Pruefung> result = filterPruefungenWith(pruefungsperiode.geplantePruefungen(),
        teilnehmerkreis);
    LOGGER.debug("All planned Pruefungen for {} are: {}", teilnehmerkreis, result);
    return result;
  }

  /**
   * Adds a pruefer to passed pruefung
   *
   * @param pruefung DTOPruefung
   * @param pruefer  pruefer which should be added
   * @return a block when the pruefung was inside a block
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalStateException              an exception from the model
   * @throws IllegalArgumentException           when the passed pruefung doesn't exist
   */
  public Planungseinheit addPruefer(ReadOnlyPruefung pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException, IllegalArgumentException {
    checkForPruefungsperiode();
    noEmptyStrings(pruefer);
    Pruefung modelPruefung = getPruefung(pruefung);
    LOGGER.debug("Adding Pruefer {} to {} in Model.", pruefer, modelPruefung);
    modelPruefung.addPruefer(pruefer);
    Optional<Block> blockOfPruefung = getBlockTo(modelPruefung);
    if (blockOfPruefung.isPresent()) {
      return blockOfPruefung.get();
    } else {
      return modelPruefung;
    }
  }

  /**
   * Gets the block to pruefung when the pruefung is inside one
   *
   * @param pruefung get the block to the passed pruefung
   * @return empty when pruefung was not in a block, or the block
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Optional<Block> getBlockTo(Pruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    checkForPruefungsperiode();
    LOGGER.debug("Requesting block containing {} from Model: {}.", pruefung,
        pruefungsperiode.block(pruefung));
    return Optional.ofNullable(pruefungsperiode.block(pruefung));
  }

  /**
   * Removes the pruefer from the passed pruefung
   *
   * @param pruefung pruefung to remove the pruefer from
   * @param pruefer  to remove from the pruefung
   * @return a block when the passed pruefung was inside one or a pruefung
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalStateException              an exception from the block
   * @throws IllegalArgumentException           when the passsed pruefer is is an empty string
   */
  public Planungseinheit removePruefer(ReadOnlyPruefung pruefung, String pruefer)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException, IllegalArgumentException {
    Pruefung modelPruefung = getPruefung(pruefung);
    noEmptyStrings(pruefer);
    LOGGER.debug("Removing Pruefer {} from {} in Model.", pruefer, modelPruefung);
    modelPruefung.removePruefer(pruefer);
    return modelPruefung;
  }

  /**
   * Sets the pruefungsnummer of the pruefung.
   *
   * @param pruefung        pruefung
   * @param pruefungsnummer nummer
   * @return modelpruefung a pruefung or the block, when the pruefung was inside one
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalStateException              an exception from the model
   */
  public Planungseinheit setPruefungsnummer(ReadOnlyPruefung pruefung,
      String pruefungsnummer)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException, IllegalArgumentException {
    noNullParameters(pruefung, pruefungsnummer);
    noEmptyStrings(pruefungsnummer);
    Pruefung modelPruefung = getPruefung(pruefung);
    Optional<Block> modelBlock = getBlockTo(pruefung);

    if (modelPruefung.getPruefungsnummer().equals(pruefungsnummer)) {
      return modelBlock.isPresent() ? modelBlock.get() : modelPruefung;
    }

    if (existsPruefungWith(pruefungsnummer)) {
      throw new IllegalArgumentException("Die angegebene Pruefungsnummer ist bereits vergeben.");
    }
    LOGGER.debug("Changing Pruefungsnummer for {} in Model from {} to {}.", modelPruefung,
        modelPruefung.getPruefungsnummer(), pruefungsnummer);
    modelPruefung.setPruefungsnummer(pruefungsnummer);
    return modelBlock.isPresent() ? modelBlock.get() : modelPruefung;
  }

  /**
   * Deletes the passed pruefung and returns maybe a block, when pruefung was inside a block
   *
   * @param roPruefung pruefung to delete
   * @return a maybe block when the pruefung was inside one
   * @throws IllegalStateException              exception from the model
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalArgumentException           when the passed pruefung is planned
   */
  public Optional<Block> deletePruefung(ReadOnlyPruefung roPruefung)
      throws IllegalStateException, NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    Pruefung pruefung = getPruefung(roPruefung);
    if (pruefung.isGeplant()) {
      throw new IllegalArgumentException("Geplante Pruefungen dürfen nicht gelöscht werden.");
    }
    Optional<Block> block = getBlockTo(pruefung);
    pruefungsperiode.removePlanungseinheit(pruefung);
    if (block.isPresent()) {
      LOGGER.debug("Deleting {} from Model.", pruefung);
    }
    return block;
  }

  /**
   * Unschedules a pruefung without any consistency checks.
   *
   * @param pruefung The pruefung to schedule.
   */
  public void unschedulePruefung(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException, IllegalArgumentException {

    if (pruefungsperiode.pruefung(pruefung.getPruefungsnummer()) == null) {
      throw new IllegalStateException("Pruefung existiert nicht.");
    }
    if (getBlockTo(pruefung).isPresent()) {
      throw new IllegalArgumentException("Prüfungen in Blöcken dürfen nicht ausgeplant werden.");
    }
    LOGGER.debug("Unscheduling {} from previously {}.", pruefung,
        pruefung.getStartzeitpunkt());
    pruefung.setStartzeitpunkt(null);
  }

  /**
   * Checks the passed termin with endtermin and starttermin of the periode
   *
   * @param termin passed termin to check
   * @return true when the passed termin is in the period
   */
  public boolean terminIsInPeriod(LocalDateTime termin) {
    return terminIsSameDayOrAfterPeriodStart(termin) && terminIsSameDayOrBeforePeriodEnd(termin);
  }

  /**
   * Checks the passed termin with startermin of the periode
   *
   * @param termin passed termin to check
   * @return true when the passed termin is te same day or after the periodendtermin
   */
  private boolean terminIsSameDayOrAfterPeriodStart(LocalDateTime termin) {
    LocalDate start = pruefungsperiode.getStartdatum();
    return start.isBefore(termin.toLocalDate()) || start.isEqual(termin.toLocalDate());
  }

  /**
   * Checks the passed termin with endtermin of periode
   *
   * @param termin passed termin to check
   * @return true when the passed termin is te same day or before the periodendtermin
   */
  private boolean terminIsSameDayOrBeforePeriodEnd(LocalDateTime termin) {
    LocalDate end = pruefungsperiode.getEnddatum();
    return end.isAfter(termin.toLocalDate()) || end.isEqual(termin.toLocalDate());
  }

  /**
   * Deletes the passed block inside the model
   *
   * @param block block to delete
   * @return exams that are inside the passed block but model pruefung
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalArgumentException           when the passed block is planned
   * @throws IllegalStateException              is an exception from the model
   */
  public List<Pruefung> deleteBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException, IllegalStateException {
    noNullParameters(block);
    checkForPruefungsperiode();
    Block modelBlock = getBlock(block);
    if (modelBlock.isGeplant()) {
      throw new IllegalArgumentException("Nur für ungeplante Blöcke möglich!");
    }

    LOGGER.debug("Deleting {} in Model.", modelBlock);
    pruefungsperiode.removePlanungseinheit(modelBlock);
    return new LinkedList<>(getPruefungenOf(block));
  }

  /**
   * Get the pruefungen associated with the model representation of a block.
   *
   * @param block The blk to get the pruefungen for.
   * @return The pruefungen associated with the model representation of a block.
   * @throws NoPruefungsPeriodeDefinedException In case the method is called without an existing
   *                                            Pruefungsperiode.
   */
  private List<Pruefung> getPruefungenOf(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    List<Pruefung> pruefungen = new ArrayList<>(block.getROPruefungen().size());
      for (ReadOnlyPruefung pruefung : block.getROPruefungen()) {
        pruefungen.add(getPruefung(pruefung));
      }
    return pruefungen;
  }

  /**
   * Creates a block in the pruefungsperiode
   *
   * @param name       name of the block
   * @param type       type of the block
   * @param pruefungen the pruefungen to put into a block
   * @return model block with the pruefung (is unplanned)
   * @throws NoPruefungsPeriodeDefinedException when there is no periode set
   * @throws IllegalArgumentException           when a pruefung is another block, or is planned or
   *                                            already in the block
   */
  public Block createBlock(String name, Blocktyp type, ReadOnlyPruefung... pruefungen)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException {
    noNullParameters(name, type, pruefungen);
    checkForPruefungsperiode();
    noEmptyStrings(name);

    if (Arrays.stream(pruefungen).anyMatch(ReadOnlyPlanungseinheit::geplant)) {
      throw new IllegalArgumentException("Eine der übergebenen Prüfungen ist geplant.");
    }

    if (isAnyInBlock(List.of(pruefungen))) {
      throw new IllegalArgumentException("Eine der Prüfungen ist bereits im Block!");
    }

    if (containsDuplicatePruefung(pruefungen)) {
      throw new IllegalArgumentException("Doppelte Prüfungen im Block!");
    }

    Block blockModel = new BlockImpl(pruefungsperiode, name, type);
    Arrays.stream(pruefungen).forEach(pruefung -> blockModel.addPruefung(
        pruefungsperiode.pruefung(pruefung.getPruefungsnummer())));
    LOGGER.debug("Adding {} to Model.", blockModel);
    if (!pruefungsperiode.addPlanungseinheit(blockModel)) {
      throw new IllegalArgumentException("""
          Irgendwas ist schiefgelaufen.
          Der Block konnte nicht in die Datenbank übertragen werden.
          """);
    }
    return blockModel;
  }

  /**
   * Is on the passed pruefungen in a block?
   *
   * @param pruefungen collection of pruefungen to check if one is a block
   * @return true when any is in a block
   */
  private boolean isAnyInBlock(Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream()
        .anyMatch(pruefung -> this.pruefungIsInBlock(pruefung.getPruefungsnummer()));
  }

  /**
   * Checks for duplicates in the passed array
   *
   * @param pruefungen array of pruefungen
   * @return when there are duplicated inside the array
   */
  private boolean containsDuplicatePruefung(ReadOnlyPruefung[] pruefungen) {
    return pruefungen.length != Arrays.stream(pruefungen).distinct().count();
  }

  /**
   * Checks if the passed number of a pruefung is inside a block
   *
   * @param pruefungsNummer number of the pruefung to check
   * @return true when the pruefung is inside a block otherwise false
   * @throws IllegalArgumentException when the pruefung doesn't exist
   */
  private boolean pruefungIsInBlock(String pruefungsNummer) throws IllegalArgumentException {
    if (existsPruefungWith(pruefungsNummer)) {
      return Optional.ofNullable(pruefungsperiode.block(pruefungsperiode.pruefung(pruefungsNummer)))
          .isPresent();
    }
    throw new IllegalArgumentException("Pruefung existiert nicht.");
  }

  /**
   * Removes the passed pruefung from the passed block. No consistency checks.
   *
   * @param block    block to remove the passed pruefung from
   * @param pruefung to be removed from the passed block
   * @return the block with the removed pruefung from, pruefung is not inside the block anymore
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Block removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = getBlock(block);
    Pruefung modelPruefung = getPruefung(pruefung);

    LOGGER.debug("Removing {} from {} in Model.", modelPruefung, modelBlock);
    modelBlock.removePruefung(modelPruefung);
    if (modelBlock.getPruefungen().isEmpty()) {
      modelBlock.setStartzeitpunkt(null);
    }

    return modelBlock;
  }

  /**
   * Adds the passed pruefung to the passed block. No consistency checks.
   *
   * @param block    block to add the passed pruefung
   * @param pruefung to be added to the passed block
   * @return block with the added pruefung
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Block addPruefungToBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Block modelBlock = getBlock(block);
    Pruefung modelPruefung = getPruefung(pruefung);
    LOGGER.debug("Adding {} to {} in Model.", modelPruefung, modelBlock);
    modelBlock.addPruefung(modelPruefung);
    return modelBlock;
  }

  /**
   * Gets the start date of the current periode
   *
   * @return start date
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   */
  @NotNull
  public LocalDate getStartOfPeriode() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get start of Pruefungsperiode from Model.");
    return pruefungsperiode.getStartdatum();
  }

  /**
   * Gets the end date of the current periode
   *
   * @return end date
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   */
  @NotNull
  public LocalDate getEndOfPeriode() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get end of Pruefungsperiode from Model.");
    return pruefungsperiode.getEnddatum();
  }

  /**
   * Gets the capacity of the current periode
   *
   * @return capacity
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   */
  public int getPeriodenKapazitaet() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get capacity of the Pruefungsperiode from Model.");
    return pruefungsperiode.getKapazitaet();
  }

  /**
   * Gets the semester of the current period
   *
   * @return semester
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   */
  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get the Semester from Model.");
    return pruefungsperiode.getSemester();
  }

  /**
   * Gets the block to passed DTOPruefung
   *
   * @param pruefungToGetBlockFor get the block of
   * @return empty when the passed pruefung is not inside a block, otherwise the block
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   * @throws IllegalStateException              an exception from the model
   */
  public Optional<Block> getBlockTo(ReadOnlyPruefung pruefungToGetBlockFor)
      throws NoPruefungsPeriodeDefinedException, IllegalStateException {
    noNullParameters(pruefungToGetBlockFor);
    checkForPruefungsperiode();
    Pruefung pruefung = getPruefung(pruefungToGetBlockFor);
    return getBlockTo(pruefung);

  }

  /**
   * Get all Teilnehmerkreise, by getting planned and unplanned Pruefungen, extract the
   * Teilnehmerkreise
   *
   * @return all Teilnehmerkreise
   * @throws NoPruefungsPeriodeDefinedException when no period is defined
   */
  public Set<Teilnehmerkreis> getAllTeilnehmerkreise() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get all planned Pruefungen from Model: {}.",
        pruefungsperiode.geplantePruefungen());
    Set<Pruefung> allPruefungen = new HashSet<>(pruefungsperiode.geplantePruefungen());
    LOGGER.debug("Get all unplanned Pruefungen from Model: {}.",
        pruefungsperiode.ungeplantePruefungen());
    allPruefungen.addAll(pruefungsperiode.ungeplantePruefungen());
    Set<Teilnehmerkreis> allTeilnehmerkreise = new HashSet<>();
    for (Pruefung pruefung : allPruefungen) {
      allTeilnehmerkreise.addAll(pruefung.getTeilnehmerkreise());
    }
    LOGGER.debug("Found Teilnehmerkreise: {}.", allTeilnehmerkreise);
    return allTeilnehmerkreise;
  }

  /**
   * Removes the passed teilnehmerkreis from the passed pruefung
   *
   * @param pruefung        to remove the teilnehmerkreis from
   * @param teilnehmerkreis to be removed from the passed pruefung
   * @return true when it's succeeded otherwise false
   */
  public boolean removeTeilnehmerkreis(Pruefung pruefung,
      Teilnehmerkreis teilnehmerkreis) {
    LOGGER.debug("Removing {} from {} in Model.", teilnehmerkreis, pruefung);
    return pruefung.removeTeilnehmerkreis(teilnehmerkreis);
  }

  /**
   * Sets the passed teilnehmerkreis with the passed the estimated number of students to the passed
   * pruefung
   *
   * @param pruefung        pruefung from model
   * @param teilnehmerkreis to set the new estimated number for
   * @param schaetzung      estimated participants
   * @return true when it was successful
   * @throws IllegalArgumentException when the estimated number of participants is negative
   */
  public boolean setTeilnehmerkreis(Pruefung pruefung, Teilnehmerkreis teilnehmerkreis,
      int schaetzung) throws IllegalArgumentException {
    if (schaetzung < 0) {
      throw new IllegalArgumentException("Schätzwert darf nicht negativ sein.");
    }
    LOGGER.debug("Adding {} with {} Students to {} in Model.", teilnehmerkreis, schaetzung,
        pruefung);
    if (!pruefung.addTeilnehmerkreis(teilnehmerkreis, schaetzung)) {
      pruefung.removeTeilnehmerkreis(teilnehmerkreis);
      return pruefung.addTeilnehmerkreis(teilnehmerkreis, schaetzung);
    }
    return true;
  }

  /**
   * Sets the new name of the passed block
   *
   * @param block block to change the name
   * @param name  to be set
   * @return model block with the new name
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   * @throws IllegalArgumentException           when the passed string is empty
   * @throws IllegalStateException              a model exception
   */
  public Block setNameOf(ReadOnlyBlock block, String name)
      throws NoPruefungsPeriodeDefinedException, IllegalArgumentException, IllegalStateException {
    noNullParameters(block, name);
    noEmptyStrings(name);
    checkForPruefungsperiode();
    Block modelBlock = getBlock(block);
    LOGGER.debug("Change name of {} in Model from {} to {}.", modelBlock, modelBlock.getName(),
        name);
    modelBlock.setName(name);
    return modelBlock;
  }

  /**
   * Gets all pruefungen from a specific pruefer
   *
   * @param pruefer pruefer
   * @return pruefungen from a specific pruefer
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Pruefung> getAllKlausurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefer);
    checkForPruefungsperiode();
    LOGGER.debug("Get all Planungseinheiten from Model: {}.",
        pruefungsperiode.getPlanungseinheiten());
    Set<Planungseinheit> planungseinheiten = pruefungsperiode.getPlanungseinheiten();
    Set<Pruefung> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      addPruefungToResultIfItHasPruefer(result, pruefer, planungseinheit);
    }
    LOGGER.debug("All Pruefungen from {} are: {}", pruefer, result);
    return result;
  }

  /**
   * Adds Pruefungen with the specified pruefer to the result.<br> If planungseinheit is a Pruefung,
   * the Planungseinheit may be added as a Pruefung.<br> If planungseinheit is a Block, all
   * contained Pruefungen with the specified Pruefer are added to the result.
   *
   * @param result          the set of pruefungen with this pruefer
   * @param pruefer         the pruefer to check for
   * @param planungseinheit the Planungseinheit to check
   */
  private void addPruefungToResultIfItHasPruefer(Set<Pruefung> result, String pruefer,
      Planungseinheit planungseinheit) {
    if (planungseinheit.isBlock()) {
      for (Pruefung bPruefung : planungseinheit.asBlock().getPruefungen()) {
        if (bPruefung.getPruefer().contains(pruefer)) {
          result.add(bPruefung);
        }
      }
    } else {
      Pruefung pruefung = planungseinheit.asPruefung();
      if (pruefung.getPruefer().contains(pruefer)) {
        result.add(pruefung);
      }
    }
  }

  /**
   * Gets the anker tag of the periode
   *
   * @return anker tag of the periode which is set
   * @throws NoPruefungsPeriodeDefinedException when no period is set
   */
  @NotNull
  public LocalDate getAnkertag() throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    LOGGER.debug("Get Ankertag from Model: {}.", pruefungsperiode.getAnkertag());
    return pruefungsperiode.getAnkertag();
  }

  /**
   * Sets the ankertag to the periode which is set
   *
   * @param newAnkerTag the new ankertag to set
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   * @throws IllegalTimeSpanException           when the ankertag is not in the period of time
   */
  public void setAnkertag(LocalDate newAnkerTag)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(newAnkerTag);
    checkForPruefungsperiode();
    ensureNotBeforePruefungsperiode(newAnkerTag);
    ensureNotAfterPruefungsperiode(newAnkerTag);
    LOGGER.debug("Set Ankertag in Model from {} to{}.", pruefungsperiode.getAnkertag(),
        newAnkerTag);
    pruefungsperiode.setAnkertag(newAnkerTag);
  }

  /**
   * Ensures if the ankertag is not after the end
   *
   * @param newAnkerTag ankertag to check
   * @throws IllegalTimeSpanException when the passed ankertag is after the end
   */
  private void ensureNotBeforePruefungsperiode(LocalDate newAnkerTag)
      throws IllegalTimeSpanException {
    if (newAnkerTag.isBefore(pruefungsperiode.getStartdatum())) {
      throw new IllegalTimeSpanException(
          "An Ankertag must not be before the start of the Pruefungsperiode.");
    }
  }

  /**
   * Ensures if the ankertag is not before the start
   *
   * @param newAnkerTag ankertag to check
   * @throws IllegalTimeSpanException when the passed ankertag is before start
   */
  private void ensureNotAfterPruefungsperiode(LocalDate newAnkerTag)
      throws IllegalTimeSpanException {
    if (newAnkerTag.isAfter(pruefungsperiode.getEnddatum())) {
      throw new IllegalTimeSpanException(
          "An Ankertag must not be after the end of the Pruefungsperiode.");
    }
  }

  /**
   * Get the number of students which are writing an exam at the passed time
   *
   * @param zeitpunkt passed time
   * @return number of students
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public int getAnzahlStudentenZeitpunkt(LocalDateTime zeitpunkt)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(zeitpunkt);
    checkForPruefungsperiode();
    int result = 0;
    Set<Pruefung> pruefungen = getPruefungenAt(zeitpunkt);
    HashMap<Teilnehmerkreis, Integer> schaetzungen = getMaxTeilnehmerPerTeilnehmerkreis(pruefungen);
    for (Integer schaetzung : schaetzungen.values()) {
      result += schaetzung;
    }
    LOGGER.debug("Found {} students at {}", result, zeitpunkt);
    return result;
  }

  /**
   * Gets the planned pruefungen its time period (start - end) cross the passed time. Also, the
   * pruefungen inside a block.
   *
   * @param moment passed time
   * @return set of pruefungen
   */
  private Set<Pruefung> getPruefungenAt(LocalDateTime moment) {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung pruefung : pruefungsperiode.geplantePruefungen()) {
      LocalDateTime start = pruefung.getStartzeitpunkt();
      LocalDateTime end = pruefung.endzeitpunkt();
      if ((start.equals(moment) || start.isBefore(moment))
          && (end.equals(moment) || end.isAfter(moment))) {
        result.add(pruefung);
      }
    }
    return result;
  }

  /**
   * Gets the max. of teilnehmerkreisschätzung of the passed pruefungen
   *
   * @param pruefungen pruefungen to get the merged teilnehmerkreisschaetzung (merge: max)
   * @return teilnehmerkreisschätzung with max of schaetzung
   */
  @NotNull
  private HashMap<Teilnehmerkreis, Integer> getMaxTeilnehmerPerTeilnehmerkreis(
      Set<Pruefung> pruefungen) {
    HashMap<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
    for (Pruefung pruefung : pruefungen) {
      for (Map.Entry<Teilnehmerkreis, Integer> entry : pruefung.getSchaetzungen().entrySet()) {
        if (schaetzungen.containsKey(entry.getKey())) {
          schaetzungen.merge(entry.getKey(), entry.getValue(), Integer::max);
        } else {
          schaetzungen.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return schaetzungen;
  }

  /**
   * Checks if the passed pruefungen are in the same block.
   *
   * @param pruefung      pruefung to check
   * @param otherPruefung pruefung to check
   * @return true when the passed pruefung are in the same block otherwise false
   * @throws NoPruefungsPeriodeDefinedException when periode is set
   */
  public boolean areInSameBlock(Pruefung pruefung, Pruefung otherPruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, otherPruefung);
    return getBlockTo(pruefung)
        .filter((Block block) -> block.getPruefungen().contains(otherPruefung)).isPresent();
  }

  /**
   * Gets the model block to the passed dtoblock
   *
   * @param block dtoblock to get the modelblock of
   * @return model block
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  public Block getBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    checkForPruefungsperiode();
    Block retrievedBlock = pruefungsperiode.block(block.getBlockId());
    if (retrievedBlock == null) {
      throw new IllegalStateException("Übergebener Block existiert nicht");
    }
    return retrievedBlock;
  }

  /**
   * Checks if a block with the passed id exists
   *
   * @param blockId passed id
   * @return true when there is a block with the passed id, otherwise false
   * @throws NoPruefungsPeriodeDefinedException when o periode is set
   */
  public boolean existsBlockWith(int blockId) throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    return pruefungsperiode.block(blockId) != null;
  }

  /**
   * Gets the planned planungseinheiten its time period (start - end) cross the passed time
   *
   * @param time passed time
   * @return set of planungsheiten
   * @throws NoPruefungsPeriodeDefinedException when no periode is set
   */
  @NotNull
  public Set<Planungseinheit> getPlanungseinheitenAt(LocalDateTime time)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(time);
    checkForPruefungsperiode();
    LOGGER.debug("Requesting all Planungseinheiten at {} from Model: {}.", time,
        pruefungsperiode.planungseinheitenAt(time));
    return pruefungsperiode.planungseinheitenAt(time);
  }

  /**
   * Unschedules the planungseinheiten after adoption, when they are not in the period.
   */
  public void unschedulePlanungseinheitenOutsideOfPeriode()
      throws NoPruefungsPeriodeDefinedException {
    checkForPruefungsperiode();
    unscheduleAdoptedBloeckeOutsideOfPeriode();
    unscheduleAdoptedPruefungenOutsideOfPeriode();
  }

  /**
   * Unschedules the blocks after adoption, when they are not in the period.
   */
  private void unscheduleAdoptedBloeckeOutsideOfPeriode() {
    for (Block block : pruefungsperiode.geplanteBloecke()) {
      LocalDate plannedDate = block.getStartzeitpunkt().toLocalDate();
      if (isBeforeStartOfPeriode(plannedDate) || isAfterEndOfPeriode(plannedDate)) {
        block.setStartzeitpunkt(null);
      }
    }
  }

  /**
   * Unschedules the pruefungen after adoption, when they are not in the period.
   */
  private void unscheduleAdoptedPruefungenOutsideOfPeriode() {
    for (Pruefung pruefung : pruefungsperiode.geplantePruefungen()) {
      LocalDate plannedDate = pruefung.getStartzeitpunkt().toLocalDate();
      if (isBeforeStartOfPeriode(plannedDate) || isAfterEndOfPeriode(plannedDate)) {
        pruefung.setStartzeitpunkt(null);
      }
    }
  }

  /**
   * @param plannedDate passed date to check
   * @return true when the passed date is before the start of the period otherwise false
   */
  private boolean isBeforeStartOfPeriode(LocalDate plannedDate) {
    return plannedDate.isBefore(pruefungsperiode.getStartdatum());
  }

  /**
   * @param plannedDate passed date to check
   * @return true when the passed date is after the end of the set period otherwise false
   */
  private boolean isAfterEndOfPeriode(LocalDate plannedDate) {
    return plannedDate.isAfter(pruefungsperiode.getEnddatum());
  }

  /**
   * Sets the passed dates to the periode which is set
   *
   * @param startDatum start
   * @param endDatum   end
   */
  public void setDatumPeriode(LocalDate startDatum, LocalDate endDatum) {
    LOGGER.debug("Changing start date of Pruefungsperiode in Model from {} to {}.",
        pruefungsperiode.getStartdatum(), startDatum);
    pruefungsperiode.setStartdatum(startDatum);
    LOGGER.debug("Changing end date of Pruefungsperiode in Model from {} to {}.",
        pruefungsperiode.getEnddatum(), endDatum);
    pruefungsperiode.setEnddatum(endDatum);
  }
}
