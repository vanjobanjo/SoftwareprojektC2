package de.fhwedel.klausps.controller;


import de.fhwedel.klausps.controller.api.InterfaceController;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.export.ExportTyp;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Controller implements InterfaceController {

  @Override
  public Set<ReadOnlyPruefung> getGeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Set<ReadOnlyPruefung> getUngeplantePruefungen() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Set<ReadOnlyBlock> getGeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Set<ReadOnlyBlock> getUngeplanteBloecke() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public LocalDate getStartDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public LocalDate getEndDatumPeriode() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public int getKapazitaetPeriode() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Semester getSemester() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void setSemester(Semester semester) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Set<Teilnehmerkreis> getAllTeilnehmerKreise() throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Collection<ReadOnlyPruefung> getAllKlasurenFromPruefer(String pruefer)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Collection<ReadOnlyPruefung> getGeplantePruefungenForTeilnehmer(Teilnehmerkreis teilnehmer)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Collection<ReadOnlyPruefung> getUngeplantePruefungenForTeilnehmer(
      Teilnehmerkreis teilnehmer) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public int getAnzahlStudentenInZeitraum(LocalDateTime start, LocalDateTime end)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Collection<ReadOnlyPruefung> getPruefungenInZeitraum(LocalDateTime start,
      LocalDateTime end) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> getGeplantePruefungenWithKonflikt(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<SimpleEntry<LocalDateTime, Duration>> getOptimaleZeitslots(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public Optional<ReadOnlyBlock> getBlockOfPruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> setDatumPeriode(LocalDate startDatum, LocalDate endDatum)
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> setDauer(ReadOnlyPruefung pruefung, Duration dauer)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> setKapazitaetPeriode(int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void setPruefungsnummer(ReadOnlyPruefung pruefung, String pruefungsnummer)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void setName(ReadOnlyPruefung pruefung, String name)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void setName(ReadOnlyBlock block, String name) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> setTeilnehmerkreisSchaetzung(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis, int schaetzung) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public ReadOnlyPruefung createPruefung(String name, String pruefungsNummer, String pruefer,
      Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreis)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void deletePruefung(ReadOnlyPruefung pruefung) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void unschedulePruefung(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime startTermin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> movePruefung(ReadOnlyPruefung pruefung, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void addPruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void removePruefer(ReadOnlyPruefung pruefung, String kuerzel)
      throws NoPruefungsPeriodeDefinedException {

  }

  @Override
  public List<ReadOnlyPruefung> addTeilnehmerkreis(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> removeTeilnehmerkreis(ReadOnlyPruefung pruefung,
      Teilnehmerkreis teilnehmerkreis) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public ReadOnlyBlock createBlock(ReadOnlyPruefung... pruefungen)
      throws IllegalArgumentException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> scheduleBlock(ReadOnlyBlock block, LocalDateTime start)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void deleteBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void unscheduleBlock(ReadOnlyBlock block) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public List<ReadOnlyPruefung> moveBlock(ReadOnlyBlock block, LocalDateTime termin)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public ReadOnlyBlock addPruefungToBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void removePruefungFromBlock(ReadOnlyBlock block, ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {

  }

  @Override
  public List<KriteriumsAnalyse> analyseScoring(ReadOnlyPruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void importPeriode(Path path) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void exportPeriode(Path path, ExportTyp typ) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void createEmptyPeriode(Semester semester, LocalDate start, LocalDate end, int kapazitaet)
      throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }

  @Override
  public void createEmptyPeriodeWithData(Semester semester, LocalDate start, LocalDate end,
      int kapazitaet, Path path) throws NoPruefungsPeriodeDefinedException {

  }

  @Override
  public void createEmptyAndAdoptPeriode(Semester semester, LocalDate start, LocalDate end,
      int kapazitaet, Path path) throws NoPruefungsPeriodeDefinedException {
    throw new UnsupportedOperationException("nicht implementiert");
  }
}