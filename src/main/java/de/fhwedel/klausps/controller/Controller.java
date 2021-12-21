package de.fhwedel.klausps.controller;

import static java.util.Objects.requireNonNull;

import de.fhwedel.klausps.controller.api.InterfaceController;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.export.ExportTyp;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Semestertyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Controller implements InterfaceController {

  private final DataAccessService dataAccessService = ServiceProvider.getDataAccessService();
  private final IOService ioService = ServiceProvider.getIOService();
  private final ScheduleService scheduleService = ServiceProvider.getScheduleService();

  @Override
  public Set<ReadOnlyPruefung> getGeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getGeplantePruefungen();
  }

  @Override
  public Set<ReadOnlyPruefung> getUngeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getUngeplantePruefungen();
  }

  @Override
  public Set<ReadOnlyBlock> getGeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getGeplanteBloecke();
  }

  @Override
  public Set<ReadOnlyBlock> getUngeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getUngeplanteBloecke();
  }

  @Override
  public LocalDate getStartDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getStartOfPeriode();
  }

  @Override
  public LocalDate getEndDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getEndOfPeriode();
  }

  @Override
  public LocalDate getAnkerPeriode() throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");

  }

  @Override
  public int getKapazitaetPeriode() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getPeriodenKapazitaet();
  }

  @Override
  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getSemester();
  }

  @Override
  public Semester createSemester(Semestertyp typ, Year year) {
    throw new IllegalStateException("Not implemented yet!");

  }


  @Override
  public Set<Teilnehmerkreis> getAllTeilnehmerKreise() throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    return dataAccessService.getAllTeilnehmerkreise();
  }

  @Override
  public Set<ReadOnlyPruefung> getAllKlasurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefer);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Set<ReadOnlyPruefung> getAllKlausurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Set<ReadOnlyPruefung> getGeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmer);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Set<ReadOnlyPruefung> getUngeplantePruefungenForTeilnehmer(
      Teilnehmerkreis teilnehmer) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(teilnehmer);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public int getAnzahlStudentenZeitpunkt(LocalDateTime zeitpunkt)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }


  @Override
  public Set<ReadOnlyPruefung> getPruefungenInZeitraum(
      LocalDateTime start, LocalDateTime end) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(start, end);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Set<ReadOnlyPruefung> getGeplantePruefungenWithKonflikt(
      ReadOnlyPlanungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Set<LocalDateTime> getHardConflictedTimes(Set<LocalDateTime> zeitpunkte,
      ReadOnlyPlanungseinheit planungseinheit) throws IllegalArgumentException {
    throw new IllegalStateException("Not implemented yet!");
  }


  @Override
  public Optional<ReadOnlyBlock> getBlockOfPruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    noNullParameters(startDatum, endDatum);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void setAnkerTagPeriode(LocalDate ankertag)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, dauer);
    checkNoPruefungDefined();
    return this.dataAccessService.changeDurationOf(pruefung, dauer);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(kapazitaet);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyPruefung setPruefungsnummer(ReadOnlyPruefung pruefung, String pruefungsnummer)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, pruefungsnummer);
    checkNoPruefungDefined();
    return dataAccessService.setPruefungsnummer(pruefung, pruefungsnummer);
  }

  @Override
  public ReadOnlyPruefung setName(ReadOnlyPruefung pruefung, String name)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, name);
    checkNoPruefungDefined();
    return dataAccessService.changeNameOfPruefung(pruefung, name);
  }

  @Override
  public ReadOnlyBlock setName(ReadOnlyBlock block, String name)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, name);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> setTeilnehmerkreisSchaetzung(
      ReadOnlyPruefung pruefung, Teilnehmerkreis teilnehmerkreis, int schaetzung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis, schaetzung);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyPruefung createPruefung(
      String name,
      String pruefungsNummer,
      String pruefer,
      Duration duration,
      Map<Teilnehmerkreis, Integer> teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {

    noNullParameters(name, pruefungsNummer, pruefer, duration, teilnehmerkreis);
    checkNoPruefungDefined();
    return dataAccessService.createPruefung(
        name, pruefungsNummer, pruefer, duration, teilnehmerkreis);
  }

  @Override
  public Optional<ReadOnlyBlock> deletePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    checkNoPruefungDefined();


   return scheduleService.deletePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> unschedulePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    checkNoPruefungDefined();
    return scheduleService.unschedulePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> schedulePruefung(
      ReadOnlyPruefung pruefung, LocalDateTime startTermin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, startTermin);
    checkNoPruefungDefined();
    return scheduleService.schedulePruefung(pruefung, startTermin);
  }



  @Override
  public ReadOnlyPruefung addPruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, kuerzel);
    checkNoPruefungDefined();
    return dataAccessService.addPruefer(pruefung.getPruefungsnummer(), kuerzel);
  }

  @Override
  public ReadOnlyPruefung removePruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, kuerzel);
    checkNoPruefungDefined();
    return dataAccessService.removePruefer(pruefung.getPruefungsnummer(), kuerzel);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> addTeilnehmerkreis(
      ReadOnlyPruefung pruefung, Teilnehmerkreis teilnehmerkreis, Integer schaetzung)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis, schaetzung);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> removeTeilnehmerkreis(
      ReadOnlyPruefung pruefung, Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung, teilnehmerkreis);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyBlock createBlock(String name, Blocktyp type,  ReadOnlyPruefung... pruefungen)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    noNullParameters((Object[]) pruefungen);
    noNullParameters(name);
    checkNoPruefungDefined();
    return dataAccessService.createBlock(name, pruefungen);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> makeBlockSequential(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> makeBlockParallel(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> scheduleBlock(
      ReadOnlyBlock block, LocalDateTime start)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    noNullParameters(block, start);
    checkNoPruefungDefined();
    return scheduleService.scheduleBlock(block, start);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> deleteBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    checkNoPruefungDefined();
    return scheduleService.deleteBlock(block);
  }

  @Override
  public List<ReadOnlyPlanungseinheit> unscheduleBlock(ReadOnlyBlock block)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block);
    checkNoPruefungDefined();
    return scheduleService.unscheduleBlock(block);
  }


  @Override
  public List<ReadOnlyPlanungseinheit> addPruefungToBlock(ReadOnlyBlock block,
      ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, pruefung);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPlanungseinheit> removePruefungFromBlock(
      ReadOnlyBlock block, ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(block, pruefung);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void importPeriode(Path path) {
    noNullParameters(path);
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void exportPeriode(Path path, ExportTyp typ) throws NoPruefungsPeriodeDefinedException {
    noNullParameters(path, typ);
    checkNoPruefungDefined();
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void createEmptyPeriode(
      Semester semester, LocalDate start, LocalDate end, LocalDate ankertag,
      int kapazitaet) {
    noNullParameters(semester, start, end, kapazitaet);
    ioService.createEmptyPeriode(semester, start, end, kapazitaet);
  }

  @Override
  public void createEmptyPeriodeWithData(
    Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet,
      Path path) {
    noNullParameters(semester, start, end, kapazitaet, path);
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void createEmptyAndAdoptPeriode(
      Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet,
      Path path) {
    noNullParameters(semester, start, end, kapazitaet, path);
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Teilnehmerkreis createTeilnehmerkreis(Ausbildungsgrad grad, String studiengang,
      String ordnung, int semester) {
    throw new IllegalStateException("Not implemented yet!");
  }

  private void checkNoPruefungDefined() throws NoPruefungsPeriodeDefinedException {
    if (!dataAccessService.isPruefungsperiodeSet()) {
      throw new NoPruefungsPeriodeDefinedException();
    }
  }

  /**
   * In case any parameter is null, inmediately throw a nullpointer exception
   *
   * @param objects The parameters to check.
   * @throws NullPointerException In case any of the parameters is null.
   */
  private void noNullParameters(Object... objects) throws NullPointerException {
    for (Object parameter : objects) {
      requireNonNull(parameter);
    }
  }
}
