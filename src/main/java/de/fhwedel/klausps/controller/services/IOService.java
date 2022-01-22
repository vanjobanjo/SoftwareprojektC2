package de.fhwedel.klausps.controller.services;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
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

public class IOService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IOService.class);

  private final DataAccessService dataAccessService;

  public IOService(DataAccessService dataAccessService) {
    this.dataAccessService = dataAccessService;
  }

  /**
   * Export the periode
   * @param path Path
   * @param typ Exporttyp
   * @throws IOException when there is problem with a IO
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

  public void importPeriode(Path path) throws ImportException, IOException {
    noNullParameters(path);
    Importer importer = new KlausPSImporterImpl();
    Pruefungsperiode importedPeriode = importer.importFrom(path);
    dataAccessService.setPruefungsperiode(importedPeriode);
  }

  public void createEmptyPeriodeWithData(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet, Path path)
      throws ImportException, IOException, IllegalTimeSpanException {
    noNullParameters(semester, start, end, ankertag, path);
    Pruefungsperiode fallBackPeriode = dataAccessService.getPruefungsperiode();
    Importer importer = new CSVImporterImpl(semester, start, end, kapazitaet);
    Pruefungsperiode pruefungsperiode = importer.importFrom(path);
    dataAccessService.setPruefungsperiode(pruefungsperiode);

    try {
      dataAccessService.setAnkertag(ankertag);
      // kann eigentlich nicht passieren, da unmittelbar davor die Prüfungsperiode gesetzt wird
    } catch (NoPruefungsPeriodeDefinedException e) {
      dataAccessService.setPruefungsperiode(fallBackPeriode);
      throw new ImportException(
          "Prüfungsperiode konnte nicht erstellt werden, alle Änderungen wurden rückgängig gemacht.");
    }
  }

  public void createEmptyAndAdoptPeriode(Semester semester, LocalDate start, LocalDate end,
      LocalDate ankertag, int kapazitaet, Path path)
      throws ImportException, IOException, NoPruefungsPeriodeDefinedException {
    noNullParameters(semester, start, end, ankertag, path);

    Importer importer = new KlausPSImporterImpl();
    Pruefungsperiode adoptFrom = importer.importFrom(path);
    createEmptyPeriode(semester, start, end, ankertag, kapazitaet);
    dataAccessService.adoptPruefungstermine(adoptFrom);
  }

  public void createEmptyPeriode(
      Semester semester, LocalDate start, LocalDate end, LocalDate ankertag, int kapazitaet) {
    dataAccessService.setPruefungsperiode(
        new PruefungsperiodeImpl(semester, start, end, ankertag, kapazitaet));
  }

}
