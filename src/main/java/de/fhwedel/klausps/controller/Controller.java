package de.fhwedel.klausps.controller;

import de.fhwedel.klausps.controller.api.InterfaceController;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.export.ExportTyp;
import de.fhwedel.klausps.controller.helper.Pair;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Controller implements InterfaceController{

  private final DataAccessService dataAccessService;

  public Controller(DataAccessService dataAccessService) {
    this.dataAccessService = dataAccessService;

  }

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
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public LocalDate getEndDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public int getKapazitaetPeriode() throws NoPruefungsPeriodeDefinedException {
    return 0;
  }

  @Override
  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void setSemester(Semester semester) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Set<Teilnehmerkreis> getAllTeilnehmerKreise() throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Collection<ReadOnlyPruefung> getAllKlasurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Collection<ReadOnlyPruefung> getGeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Collection<ReadOnlyPruefung> getUngeplantePruefungenForTeilnehmer(
      Teilnehmerkreis teilnehmer) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public int getAnzahlStudentenInZeitraum(LocalDateTime start, LocalDateTime end)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Collection<ReadOnlyPruefung> getPruefungenInZeitraum(
      LocalDateTime start, LocalDateTime end) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> getGeplantePruefungenWithKonflikt(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<SimpleEntry<LocalDateTime, Duration>> getOptimaleZeitslots(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Optional<ReadOnlyBlock> getBlockOfPruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyPruefung setPruefungsnummer(ReadOnlyPruefung pruefung, String pruefungsnummer)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyPruefung setName(ReadOnlyPruefung pruefung, String name)
      throws NoPruefungsPeriodeDefinedException {

    noNullParameters(pruefung, name);
    checkNoPruefungDefined();
    return dataAccessService.changeNameOfPruefung(pruefung, name);
  }

  @Override
  public ReadOnlyBlock setName(ReadOnlyBlock block, String name) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> setTeilnehmerkreisSchaetzung(
      ReadOnlyPruefung pruefung, Teilnehmerkreis teilnehmerkreis, int schaetzung)
      throws NoPruefungsPeriodeDefinedException {
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

  private void checkNoPruefungDefined() throws NoPruefungsPeriodeDefinedException {
    if (!dataAccessService.isPruefungsperiodeSet()) {
      throw new NoPruefungsPeriodeDefinedException();
    }

  }

  @Override
  public void deletePruefung(ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> unschedulePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    checkNoPruefungDefined();
    noNullParameters(pruefung);
    return dataAccessService.unschedulePruefung(pruefung);
  }

  @Override
  public List<ReadOnlyPruefung> schedulePruefung(
      ReadOnlyPruefung pruefung, LocalDateTime startTermin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> movePruefung(ReadOnlyPruefung pruefung, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyPruefung addPruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyPruefung removePruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> addTeilnehmerkreis(ReadOnlyPruefung readOnlyPruefung, Teilnehmerkreis teilnehmerkreis, Integer integer) throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<ReadOnlyPruefung> removeTeilnehmerkreis(
      ReadOnlyPruefung pruefung, Teilnehmerkreis teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyBlock createBlock(ReadOnlyPruefung... pruefungen)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> scheduleBlock(ReadOnlyBlock block, LocalDateTime start)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyBlock deleteBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyBlock unscheduleBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Pair<ReadOnlyBlock, List<ReadOnlyPruefung>> moveBlock(ReadOnlyBlock block, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public ReadOnlyBlock addPruefungToBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public Pair<ReadOnlyBlock, ReadOnlyPruefung> removePruefungFromBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void importPeriode(Path path) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void exportPeriode(Path path, ExportTyp typ) throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void createEmptyPeriode(Semester semester, LocalDate start, LocalDate end, int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void createEmptyPeriodeWithData(
      Semester semester, LocalDate start, LocalDate end, int kapazitaet, Path path)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public void createEmptyAndAdoptPeriode(
      Semester semester, LocalDate start, LocalDate end, int kapazitaet, Path path)
      throws NoPruefungsPeriodeDefinedException {
    throw new IllegalStateException("Not implemented yet!");
  }

  /**
   * In case any parameter is null, inmediately throw a nullpointer exception
   *
   * @param objects The parameters to check.
   * @throws NullPointerException In case any of the parameters is null.
   */
  private void noNullParameters(Object... objects) throws NullPointerException {
    if (Arrays.stream(objects).anyMatch(Objects::isNull)) {
      throw new NullPointerException();
    }
  }
}
