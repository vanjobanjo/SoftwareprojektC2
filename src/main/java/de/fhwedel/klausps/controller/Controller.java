package de.fhwedel.klausps.controller;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noEmptyStrings;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;

import de.fhwedel.klausps.controller.api.InterfaceController;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.export.ExportTyp;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.Converter;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.api.exporter.ExportException;
import de.fhwedel.klausps.model.api.importer.ImportException;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller implements InterfaceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

  private final DataAccessService dataAccessService;
  private final IOService ioService;
  private final ScheduleService scheduleService;
  private final Converter converter;

  public Controller() {
    this(ServiceProvider.getDataAccessService(), ServiceProvider.getIOService(),
        ServiceProvider.getScheduleService(), ServiceProvider.getConverter());
  }

  public Controller(DataAccessService dataAccessService, IOService ioService,
      ScheduleService scheduleService, Converter converter) {
    this.dataAccessService = dataAccessService;
    this.ioService = ioService;
    this.scheduleService = scheduleService;
    this.converter = converter;
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getGeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getGeplantePruefungen().");
    return converter.convertToROPruefungSet(dataAccessService.getGeplantePruefungen());
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getUngeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getUngeplantePruefungen().");
    return converter.convertToROPruefungSet(dataAccessService.getUngeplantePruefungen());
  }

  @Override
  @NotNull
  public Set<ReadOnlyBlock> getGeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getGeplanteBloecke().");
    return converter.convertToROBlockSet(dataAccessService.getGeplanteBloecke());
  }

  @Override
  @NotNull
  public Set<ReadOnlyBlock> getUngeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getUngeplanteBloecke().");
    return converter.convertToROBlockSet(dataAccessService.getUngeplanteBloecke());
  }

  @Override
  @NotNull
  public LocalDate getStartDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getStartDatumPeriode().");
    return dataAccessService.getStartOfPeriode();
  }

  @Override
  @NotNull
  public LocalDate getEndDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getEndDatumPeriode().");
    return dataAccessService.getEndOfPeriode();
  }

  @Override
  @NotNull
  public LocalDate getAnkerPeriode() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getAnkerPeriode().");
    return dataAccessService.getAnkertag();
  }

  @Override
  public int getKapazitaetPeriode() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getKapazitaetPeriode().");
    return dataAccessService.getPeriodenKapazitaet();
  }

  @Override
  @NotNull
  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getSemester().");
    return dataAccessService.getSemester();
  }

  @Override
  @NotNull
  public Semester createSemester(Semestertyp typ, Year year) {
    LOGGER.debug("Call to createSemester({}, {}).", typ, year);
    noNullParameters(typ, year);
    return new SemesterImpl(typ, year);
  }

  @Override
  @NotNull
  public Set<Teilnehmerkreis> getAllTeilnehmerKreise() throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getAllTeilnehmerKreise().");
    return dataAccessService.getAllTeilnehmerkreise();
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getAllKlausurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getAllKlausurenFromPruefer({}).", pruefer);
    noNullParameters(pruefer);
    return converter.convertToROPruefungSet(dataAccessService.getAllKlausurenFromPruefer(pruefer));
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getGeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getGeplantePruefungenForTeilnehmer({}).", teilnehmer);
    noNullParameters(teilnehmer);
    return converter.convertToROPruefungSet(
        dataAccessService.geplantePruefungenForTeilnehmerkreis(teilnehmer));
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getUngeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getUngeplantePruefungenForTeilnehmer({}).", teilnehmer);
    noNullParameters(teilnehmer);
    return converter.convertToROPruefungSet(
        dataAccessService.ungeplantePruefungenForTeilnehmerkreis(teilnehmer));
  }

  @Override
  public int getAnzahlStudentenZeitpunkt(LocalDateTime zeitpunkt)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getAnzahlStudentenZeitpunkt({}).", zeitpunkt);
    noNullParameters(zeitpunkt);
    return dataAccessService.getAnzahlStudentenZeitpunkt(zeitpunkt);
  }

  @Override
  public Set<ReadOnlyPruefung> getPruefungenInZeitraum(LocalDateTime start, LocalDateTime end)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LOGGER.debug("Call to getPruefungenInZeitraum({}, {}).", start, end);
    noNullParameters(start, end);
    return converter.convertToROPruefungSet(dataAccessService.getAllPruefungenBetween(start, end));
  }

  @Override
  public Set<ReadOnlyPlanungseinheit> getPlanungseinheitenInZeitraum(LocalDateTime start,
      LocalDateTime end) throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LOGGER.debug("Call to getPlanungseinheitenInZeitraum({}, {}).", start, end);
    noNullParameters(start, end);
    return converter.convertToROPlanungseinheitSet(
        dataAccessService.getAllPlanungseinheitenBetween(start, end));
  }

  @Override
  public Set<ReadOnlyPruefung> getGeplantePruefungenWithKonflikt(
      ReadOnlyPlanungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getGeplantePruefungenWithKonflikt({}).", planungseinheit);
    noNullParameters(planungseinheit);
    return converter.convertToROPruefungSet(
        scheduleService.getGeplantePruefungenWithKonflikt(planungseinheit));
  }

  @Override
  public Set<LocalDateTime> getHardConflictedTimes(Set<LocalDateTime> zeitpunkte,
      ReadOnlyPlanungseinheit planungseinheit)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getHardConflictedTimes({}).", planungseinheit);
    noNullParameters(zeitpunkte, planungseinheit);
    return scheduleService.getHardConflictedTimes(zeitpunkte, planungseinheit);
  }

  @Override
  public Optional<ReadOnlyBlock> getBlockOfPruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to getBlockOfPruefung({}).", pruefung);
    noNullParameters(pruefung);
    Optional<Block> block = dataAccessService.getBlockTo(pruefung);
    if (block.isPresent()) {
      return Optional.of(converter.convertToROBlock(block.get()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LOGGER.debug("Call to setDatumPeriode({}, {}).", startDatum, endDatum);
    noNullParameters(startDatum, endDatum);
    return scheduleService.setDatumPeriode(startDatum, endDatum);
  }

  @Override
  public void setAnkerTagPeriode(LocalDate ankertag)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    LOGGER.debug("Call to setAnkerTagPeriode({}).", ankertag);
    noNullParameters(ankertag);
    dataAccessService.setAnkertag(ankertag);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to setDauer({}, {}).", pruefung, dauer);
    noNullParameters(pruefung, dauer);
    return converter.convertToROPlanungseinheitList(scheduleService.setDauer(pruefung, dauer));
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to setKapazitaetPeriode({}).", kapazitaet);
    noNullParameters(kapazitaet);
    return converter.convertToROPlanungseinheitList(
        scheduleService.setKapazitaetPeriode(kapazitaet));
  }

  @Override
  public ReadOnlyPlanungseinheit setPruefungsnummer(ReadOnlyPruefung pruefung,
      String pruefungsnummer) throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to setPruefungsnummer({}, {}).", pruefung, pruefungsnummer);
    noNullParameters(pruefung, pruefungsnummer);
    return converter.convertToReadOnlyPlanungseinheit(
        dataAccessService.setPruefungsnummer(pruefung, pruefungsnummer));
  }

  @Override
  public ReadOnlyPlanungseinheit setName(ReadOnlyPruefung pruefung, String name)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to setName({}, {}).", pruefung, name);
    noNullParameters(pruefung, name);
    return converter.convertToReadOnlyPlanungseinheit(
        dataAccessService.changeNameOf(pruefung, name));
  }

  @Override
  public ReadOnlyBlock setName(ReadOnlyBlock block, String name)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to setName({}, {}).", block, name);
    noNullParameters(block, name);
    return converter.convertToROBlock(dataAccessService.setNameOf(block, name));
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setTeilnehmerkreisSchaetzung(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to setTeilnehmerkreisSchaetzung({}, {}, {}).", pruefung, teilnehmerkreis,
        schaetzung);
    noNullParameters(pruefung, teilnehmerkreis, schaetzung);
    ensureAvailabilityOfPruefungsperiode();
    return converter.convertToROPlanungseinheitList(
        scheduleService.setTeilnehmerkreisSchaetzung(pruefung, teilnehmerkreis, schaetzung));
  }

  @Override
  public ReadOnlyPruefung createPruefung(String ref, String name, String pruefungsNummer,
      Set<String> pruefer, Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to createPruefung({}, {}, {}, {}, {}, {}).", ref, name, pruefungsNummer,
        pruefer, duration, teilnehmerkreis);
    noNullParameters(name, pruefungsNummer, pruefer, duration, teilnehmerkreis);

    return converter.convertToReadOnlyPruefung(
        dataAccessService.createPruefung(name, pruefungsNummer, ref, pruefer, duration,
            teilnehmerkreis));
  }

  @Override
  public Optional<ReadOnlyBlock> deletePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to deletePruefung({}).", pruefung);
    noNullParameters(pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.deletePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to unschedulePruefung({}).", pruefung);
    noNullParameters(pruefung);
    return scheduleService.unschedulePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime startTermin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to schedulePruefung({}, {}).", pruefung, startTermin);
    noNullParameters(pruefung, startTermin);
    ensureAvailabilityOfPruefungsperiode();
    return converter.convertToROPlanungseinheitList(
        scheduleService.schedulePruefung(pruefung, startTermin));
  }

  @Override
  public ReadOnlyPlanungseinheit addPruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to addPruefer({}, {}).", pruefung, kuerzel);
    noNullParameters(pruefung, kuerzel);
    return converter.convertToReadOnlyPlanungseinheit(
        dataAccessService.addPruefer(pruefung, kuerzel));
  }

  @Override
  public ReadOnlyPlanungseinheit removePruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to removePruefer({}, {}).", pruefung, kuerzel);
    noNullParameters(pruefung, kuerzel);
    ensureAvailabilityOfPruefungsperiode();
    return converter.convertToReadOnlyPlanungseinheit(
        dataAccessService.removePruefer(pruefung, kuerzel));
  }

  @Override
  public List<ReadOnlyPlanungseinheit> addTeilnehmerkreis(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, Integer schaetzung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to addTeilnehmerkreis({}, {}, {}).", pruefung, teilnehmerkreis, schaetzung);
    noNullParameters(pruefung, teilnehmerkreis, schaetzung);
    ensureAvailabilityOfPruefungsperiode();
    return converter.convertToROPlanungseinheitList(
        scheduleService.addTeilnehmerkreis(pruefung, teilnehmerkreis, schaetzung));
  }

  @Override
  public List<ReadOnlyPlanungseinheit> removeTeilnehmerkreis(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis) throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to removeTeilnehmerkreis({}, {}).", pruefung, teilnehmerkreis);
    noNullParameters(pruefung, teilnehmerkreis);
    ensureAvailabilityOfPruefungsperiode();
    return converter.convertToROPlanungseinheitList(
        scheduleService.removeTeilnehmerKreis(pruefung, teilnehmerkreis));
  }

  @Override
  public ReadOnlyBlock createBlock(String name, Blocktyp type, ReadOnlyPruefung... pruefungen)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to createBlock({}, {}, {}).", name, type, pruefungen);
    noNullParameters((Object[]) pruefungen);
    noNullParameters(name, type);
    return converter.convertToROBlock(dataAccessService.createBlock(name, type, pruefungen));
  }

  @Override
  public List<ReadOnlyPlanungseinheit> makeBlockSequential(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    LOGGER.debug("Call to makeBlockSequential({}).", block);
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.setBlockType(block, PARALLEL);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> makeBlockParallel(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    LOGGER.debug("Call to makeBlockParallel({}).", block);
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.setBlockType(block, SEQUENTIAL);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> scheduleBlock(ReadOnlyBlock block, LocalDateTime start)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to scheduleBlock({}, {}).", block, start);
    noNullParameters(block, start);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.scheduleBlock(block, start);
  }

  @Override
  public List<ReadOnlyPruefung> deleteBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to deleteBlock({}).", block);
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return converter.convertToROPruefungList(dataAccessService.deleteBlock(block));
  }

  @Override
  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to unscheduleBlock({}).", block);
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.unscheduleBlock(block);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    LOGGER.debug("Call to addPruefungToBlock({}, {}).", block, pruefung);
    noNullParameters(block, pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.addPruefungToBlock(block, pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to removePruefungFromBlock({}, {}).", block, pruefung);
    noNullParameters(block, pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.removePruefungFromBlock(block, pruefung);
  }

  @Override
  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LOGGER.debug("Call to analyseScoring({}).", pruefung);
    noNullParameters(pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.analyseScoring(pruefung);
  }

  @Override
  public void importPeriode(Path path) throws ImportException, IOException {
    LOGGER.debug("Call to importPeriode({}).", path);
    noNullParameters(path);
    ioService.importPeriode(path);
  }

  @Override
  public void exportPeriode(Path path, ExportTyp typ) throws NoPruefungsPeriodeDefinedException,
      ExportException, IOException {
    LOGGER.debug("Call to exportPeriode({}, {}).", path, typ);
    noNullParameters(path, typ);
    ensureAvailabilityOfPruefungsperiode();
    ioService.exportPeriode(path, typ);
  }

  @Override
  public void createEmptyPeriode(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet) {
    LOGGER.debug("Call to createEmptyPeriode({}, {}, {}, {}, {}).", semester, start, end, ankertag,
        kapazitaet);
    noNullParameters(semester, start, end, kapazitaet);
    ioService.createEmptyPeriode(semester, start, end, ankertag, kapazitaet);
  }

  @Override
  public void createEmptyPeriodeWithData(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet, Path path) throws ImportException, IOException {
    LOGGER.debug("Call to createEmptyPeriodeWithData({}, {}, {}, {}, {}, {}).", semester, start,
        end, ankertag, kapazitaet, path);
    noNullParameters(semester, start, end, ankertag, kapazitaet, path);
    // todo sollte auch eine IllegalTimeSpanException werfen falls Start, Ende
    //  und/oder Ankertag falsch sind
    try {
      ioService.createEmptyPeriodeWithData(semester, start, end, ankertag, kapazitaet, path);
    } catch (IllegalTimeSpanException e) {
      throw new IllegalArgumentException("Die Daten der Prüfungsperiode passen nicht");
    }
  }

  @Override
  public void createEmptyAndAdoptPeriode(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet, Path path) throws ImportException, IOException {
    LOGGER.debug("Call to createEmptyAndAdoptPeriode({}, {}, {}, {}, {}, {}).", semester, start,
        end, ankertag, kapazitaet, path);
    noNullParameters(semester, start, end, ankertag, kapazitaet, path);
    scheduleService.createEmptyAndAdoptPeriode(ioService, semester, start, end, ankertag,
        kapazitaet, path);
  }

  @Override
  public Teilnehmerkreis createTeilnehmerkreis(Ausbildungsgrad grad, String studiengang,
      String ordnung, int semester) {
    LOGGER.debug("Call to createTeilnehmerkreis({}, {}, {}, {}).", grad, studiengang, ordnung,
        semester);
    noNullParameters(grad, studiengang, ordnung);
    noEmptyStrings(studiengang, ordnung);
    return new TeilnehmerkreisImpl(studiengang, ordnung, semester, grad);
  }

  private void ensureAvailabilityOfPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    if (!dataAccessService.isPruefungsperiodeSet()) {
      throw new NoPruefungsPeriodeDefinedException();
    }
  }

}
