package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.export.ExportTyp;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.exporter.ExportException;
import de.fhwedel.klausps.model.api.exporter.Exporter;
import de.fhwedel.klausps.model.api.importer.ImportException;
import de.fhwedel.klausps.model.api.importer.Importer;
import de.fhwedel.klausps.model.impl.PruefungsperiodeImpl;
import de.fhwedel.klausps.model.impl.exporter.CSVExporterImpl;
import de.fhwedel.klausps.model.impl.exporter.KlausPSExporterImpl;
import de.fhwedel.klausps.model.impl.exporter.PDFExporterImpl;
import de.fhwedel.klausps.model.impl.importer.CSVImporterImpl;
import de.fhwedel.klausps.model.impl.importer.KlausPSImporterImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all import and export operations for {@link Pruefungsperiode
 * Pruefungsperioden} as specified in the {@link de.fhwedel.klausps.controller.Controller
 * Controller-Interface}.
 */
public class IOService {

  /**
   * Logger for debugging purposes
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(IOService.class);

  private final DataAccessService dataAccessService;

  public IOService(DataAccessService dataAccessService) {
    this.dataAccessService = dataAccessService;
  }

  /**
   * Export the current {@link Pruefungsperiode} as a File. Possible file-types:
   * <ul>
   *   <li>CSV</li>
   *   <li>PDF</li>
   *   <li>KlausPS (internal format)</li>
   * </ul>
   *
   * @param path Path to export to
   * @param typ  specifies the type of the exported file
   * @throws IOException     when there is problem with a IO
   * @throws ExportException when there is a problem with the export
   */
  public void exportPeriode(Path path, ExportTyp typ) throws IOException, ExportException {
    noNullParameters(path, typ);
    Exporter export;
    Pruefungsperiode periode = dataAccessService.getPruefungsperiode();
    switch (typ) {
      case CSV -> export = new CSVExporterImpl(periode);
      case PDF -> export = new PDFExporterImpl(periode);
      case INTERN -> export = new KlausPSExporterImpl(periode);
      default -> throw new IllegalStateException("Unexpected value: " + typ);
    }
    LOGGER.debug("Calling exporter {} with {}.", export.getClass().getSimpleName(), path);
    export.exportTo(path);
  }

  /**
   * Imports an existing {@link Pruefungsperiode} (from KlausPS-Format). <br> Sets the imported
   * {@link Pruefungsperiode} for the {@link DataAccessService}
   *
   * @param path the path to a KlausPS-File
   * @throws ImportException for syntactical or semantic errors in the file
   * @throws IOException     for import problems
   */
  public void importPeriode(Path path) throws ImportException, IOException {
    noNullParameters(path);
    Importer importer = new KlausPSImporterImpl();
    Pruefungsperiode importedPeriode = importer.importFrom(path);
    dataAccessService.setPruefungsperiode(importedPeriode);
  }

  /**
   * Creates a new {@link Pruefungsperiode} from a CSV-File and optionally an existing KlausPS-File.
   * If a KlausPS-File is specified, Pruefungen from that File will be adopted for the new Periode.
   * The newly created Pruefungsperiode will be set for the {@link DataAccessService}.
   *
   * @param semester    the semester for the Pruefungsperiode
   * @param start       start date of the new Periode
   * @param end         end date of the new periode
   * @param ankertag    date of the ankertag
   * @param kapazitaet  amount of students
   * @param csvPath     Path to the CSV-File
   * @param klausPsPath Path to the KlausPS-File (can be null)
   * @throws ImportException for syntactical or semantic errors in the file
   * @throws IOException     for import problems
   */
  public void createNewPeriodeWithData(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet, Path csvPath, Path klausPsPath)
      throws ImportException, IOException, IllegalTimeSpanException {
    noNullParameters(semester, start, end, ankertag, csvPath);
    checkDatesAndCapacity(start, end, ankertag, kapazitaet);
    Pruefungsperiode pruefungsperiode = new PruefungsperiodeImpl(semester, start, end, ankertag,
        kapazitaet);
    LOGGER.debug("Creating importer with {} and {}", pruefungsperiode, klausPsPath);
    Importer importer = new CSVImporterImpl(pruefungsperiode, klausPsPath);
    LOGGER.debug("Calling {} importer with {}", importer.getClass().getSimpleName(), csvPath);
    pruefungsperiode = importer.importFrom(csvPath);
    dataAccessService.setPruefungsperiode(pruefungsperiode);
  }


  /**
   * Creates an empty {@link Pruefungsperiode} with given Data and sets it for the {@link
   * DataAccessService}
   *
   * @param semester   {@link Semester} of Periode
   * @param start      start date of Pruefungsperiode
   * @param end        start date of Pruefungsperiode
   * @param ankertag   ankertag date of Pruefungsperiode
   * @param kapazitaet amount of students
   * @throws IllegalTimeSpanException when the ankertag is outside start and end or end is after
   *                                  start
   * @throws IllegalArgumentException when Kapazitaet is zero or negative
   */
  public void createEmptyPeriode(
      Semester semester, LocalDate start, LocalDate end, LocalDate ankertag, int kapazitaet)
      throws IllegalTimeSpanException, IllegalArgumentException {
    checkDatesAndCapacity(start, end, ankertag, kapazitaet);
    dataAccessService.setPruefungsperiode(
        new PruefungsperiodeImpl(semester, start, end, ankertag, kapazitaet));
  }

  /**
   * checks if data for a new Pruefungsperiode is valid.<br>
   * <ul>
   *   <li>Start must be before End</li>
   *   <li>Ankertag must be after or at start</li>
   *   <li>Ankertag must be before end </li>
   *   <li>Kapazitaet must be larger than zero </li>
   * </ul>
   * @param start the start date of the {@link Pruefungsperiode}
   * @param end the end date of the Pruefungsperiode
   * @param ankertag the ankertag of the Pruefungsperiode
   * @param kapazitaet the Kapazitaet of the Pruefungsperiode
   * @throws IllegalTimeSpanException if start is before end or ankertag is outside of start and end
   * date of the Pruefungsperiode
   * @throws IllegalArgumentException When the kapazitaet is less than one
   */
  private void checkDatesAndCapacity(LocalDate start, LocalDate end, LocalDate ankertag,
      int kapazitaet) throws IllegalTimeSpanException, IllegalArgumentException {
    if (end.isBefore(start)) {
      throw new IllegalTimeSpanException("Das Enddatum darf nicht vor dem Startdatum liegen.");
    }
    if (ankertag.isBefore(start)) {
      throw new IllegalTimeSpanException("Der Ankertag darf nicht vor dem Startdatum liegen.");
    }
    if (ankertag.isAfter(end)) {
      throw new IllegalTimeSpanException("Der Ankertag darf nicht nach dem Enddatum liegen.");
    }
    if (kapazitaet <= 0) {
      throw new IllegalArgumentException("Die Kapazitaet darf nicht kleiner oder gleich 0 sein");
    }
  }


}
