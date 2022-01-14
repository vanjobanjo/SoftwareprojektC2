package de.fhwedel.klausps.controller;

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

public class Controller implements InterfaceController {

  private final DataAccessService dataAccessService;
  private final IOService ioService;
  private final ScheduleService scheduleService;
  private final Converter converter;

  public Controller() {
    this(ServiceProvider.getDataAccessService(), ServiceProvider.getIOService(),
        ServiceProvider.getScheduleService(), new Converter());
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
    return converter.convertToROPruefungSet(dataAccessService.getGeplantePruefungen());
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getUngeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    return converter.convertToROPruefungSet(dataAccessService.getUngeplantePruefungen());
  }

  @Override
  @NotNull
  public Set<ReadOnlyBlock> getGeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    return converter.convertToROBlockSet(dataAccessService.getGeplanteBloecke());
  }

  @Override
  @NotNull
  public Set<ReadOnlyBlock> getUngeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    return converter.convertToROBlockSet(dataAccessService.getUngeplanteBloecke());
  }

  @Override
  @NotNull
  public LocalDate getStartDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    return dataAccessService.getStartOfPeriode();
  }

  @Override
  @NotNull
  public LocalDate getEndDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    return dataAccessService.getEndOfPeriode();
  }

  @Override
  @NotNull
  public LocalDate getAnkerPeriode() throws NoPruefungsPeriodeDefinedException {
    return dataAccessService.getAnkertag();
  }

  @Override
  public int getKapazitaetPeriode() throws NoPruefungsPeriodeDefinedException {
    return dataAccessService.getPeriodenKapazitaet();
  }

  @Override
  @NotNull
  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    return dataAccessService.getSemester();
  }

  @Override
  @NotNull
  public Semester createSemester(Semestertyp typ, Year year) {
    noNullParameters(typ, year);
    return new SemesterImpl(typ, year);
  }

  @Override
  @NotNull
  public Set<Teilnehmerkreis> getAllTeilnehmerKreise() throws NoPruefungsPeriodeDefinedException {
    return dataAccessService.getAllTeilnehmerkreise();
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getAllKlausurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefer);
    return dataAccessService.getAllKlausurenFromPruefer(pruefer);
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getGeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmer);
    return converter.convertToROPruefungSet(
        dataAccessService.geplantePruefungenForTeilnehmerkreis(teilnehmer));
  }

  @Override
  @NotNull
  public Set<ReadOnlyPruefung> getUngeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmer);
    return converter.convertToROPruefungSet(
        dataAccessService.ungeplantePruefungenForTeilnehmerkreis(teilnehmer));
  }

  @Override
  public int getAnzahlStudentenZeitpunkt(LocalDateTime zeitpunkt)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(zeitpunkt);
    return dataAccessService.getAnzahlStudentenZeitpunkt(zeitpunkt);
  }

  @Override
  public Set<ReadOnlyPruefung> getPruefungenInZeitraum(LocalDateTime start, LocalDateTime end)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(start, end);
    return converter.convertToROPruefungSet(dataAccessService.getAllPruefungenBetween(start, end));
  }

  @Override
  public Set<ReadOnlyPlanungseinheit> getPlanungseinheitenInZeitraum(LocalDateTime start,
      LocalDateTime end) throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(start, end);
    return converter.convertToROPlanungseinheitSet(
        dataAccessService.getAllPlanungseinheitenBetween(start, end));
  }

  @Override
  public Set<ReadOnlyPruefung> getGeplantePruefungenWithKonflikt(
      ReadOnlyPlanungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(planungseinheit);
    return converter.convertToROPruefungSet(
        scheduleService.getGeplantePruefungenWithKonflikt(planungseinheit));
  }

  @Override
  public Set<LocalDateTime> getHardConflictedTimes(Set<LocalDateTime> zeitpunkte,
      ReadOnlyPlanungseinheit planungseinheit)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters(zeitpunkte, planungseinheit);
    return scheduleService.getHardConflictedTimes(zeitpunkte, planungseinheit);
  }

  @Override
  public Optional<ReadOnlyBlock> getBlockOfPruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.getBlockTo(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(startDatum, endDatum);
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void setAnkerTagPeriode(LocalDate ankertag)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(ankertag);
    dataAccessService.setAnkertag(ankertag);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, dauer);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.setDauer(pruefung, dauer);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(kapazitaet);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.setKapazitaetPeriode(kapazitaet);
  }

  @Override
  public ReadOnlyPlanungseinheit setPruefungsnummer(ReadOnlyPruefung pruefung,
      String pruefungsnummer) throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, pruefungsnummer);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.setPruefungsnummer(pruefung, pruefungsnummer);
  }

  @Override
  public ReadOnlyPlanungseinheit setName(ReadOnlyPruefung pruefung, String name)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, name);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.changeNameOfPruefung(pruefung, name);
  }

  @Override
  public ReadOnlyBlock setName(ReadOnlyBlock block, String name)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, name);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.setNameOfBlock(block, name);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setTeilnehmerkreisSchaetzung(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis, schaetzung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.setTeilnehmerkreisSchaetzung(pruefung, teilnehmerkreis, schaetzung);
  }


  @Override
  public ReadOnlyPruefung createPruefung(String ref, String name, String pruefungsNummer,
      Set<String> pruefer, Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {

    noNullParameters(name, pruefungsNummer, pruefer, duration, teilnehmerkreis);
    ensureAvailabilityOfPruefungsperiode();

    return dataAccessService.createPruefung(name, pruefungsNummer, ref, pruefer, duration,
        teilnehmerkreis);
  }

  @Override
  public Optional<ReadOnlyBlock> deletePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    ensureAvailabilityOfPruefungsperiode();

    return scheduleService.deletePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    return scheduleService.unschedulePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime startTermin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, startTermin);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.schedulePruefung(pruefung, startTermin);
  }

  @Override
  public ReadOnlyPlanungseinheit addPruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, kuerzel);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.addPruefer(pruefung.getPruefungsnummer(), kuerzel);
  }

  @Override
  public ReadOnlyPlanungseinheit removePruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, kuerzel);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.removePruefer(pruefung.getPruefungsnummer(), kuerzel);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> addTeilnehmerkreis(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, Integer schaetzung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis, schaetzung);
    ensureAvailabilityOfPruefungsperiode();
    return this.scheduleService.addTeilnehmerkreis(pruefung, teilnehmerkreis, schaetzung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> removeTeilnehmerkreis(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis);
    ensureAvailabilityOfPruefungsperiode();
    return this.scheduleService.removeTeilnehmerKreis(pruefung, teilnehmerkreis);
  }

  @Override
  public ReadOnlyBlock createBlock(String name, Blocktyp type, ReadOnlyPruefung... pruefungen)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters((Object[]) pruefungen);
    noNullParameters(name);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.createBlock(name, pruefungen);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> makeBlockSequential(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.toggleBlockType(block, PARALLEL);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> makeBlockParallel(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.toggleBlockType(block, SEQUENTIAL);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> scheduleBlock(ReadOnlyBlock block, LocalDateTime start)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(block, start);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.scheduleBlock(block, start);
  }

  @Override
  public List<ReadOnlyPruefung> deleteBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return dataAccessService.deleteBlock(block);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.unscheduleBlock(block);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    noNullParameters(block, pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.addPruefungToBlock(block, pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.removePruefungFromBlock(block, pruefung);
  }

  @Override
  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    ensureAvailabilityOfPruefungsperiode();
    return scheduleService.analyseScoring(pruefung);
  }

  @Override
  public void importPeriode(Path path) throws ImportException, IOException {
    noNullParameters(path);
    ioService.importPeriode(path);
  }

  @Override
  public void exportPeriode(Path path, ExportTyp typ) throws NoPruefungsPeriodeDefinedException,
      ExportException, IOException {
    noNullParameters(path, typ);
    ensureAvailabilityOfPruefungsperiode();
    ioService.exportPeriode(path, typ);
  }

  @Override
  public void createEmptyPeriode(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet) {
    noNullParameters(semester, start, end, kapazitaet);
    ioService.createEmptyPeriode(semester, start, end, ankertag, kapazitaet);
  }

  @Override
  public void createEmptyPeriodeWithData(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet, Path path) throws ImportException, IOException {
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
    noNullParameters(semester, start, end, ankertag, kapazitaet, path);
    scheduleService.createEmptyAndAdoptPeriode(ioService, semester, start, end, ankertag,
        kapazitaet, path);

  }


  @Override
  public Teilnehmerkreis createTeilnehmerkreis(Ausbildungsgrad grad, String studiengang,
      String ordnung, int semester) {
    noNullParameters(grad, studiengang, ordnung);
    return new TeilnehmerkreisImpl(studiengang, ordnung, semester, grad);
  }

  private void ensureAvailabilityOfPruefungsperiode() throws NoPruefungsPeriodeDefinedException {
    if (!dataAccessService.isPruefungsperiodeSet()) {
      throw new NoPruefungsPeriodeDefinedException();
    }
  }

}
