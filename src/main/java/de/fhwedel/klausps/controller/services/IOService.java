package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.export.ExportTyp;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.exporter.ExportException;
import de.fhwedel.klausps.model.api.exporter.Exporter;
import de.fhwedel.klausps.model.impl.PruefungsperiodeImpl;
import de.fhwedel.klausps.model.impl.exporter.CSVExporterImpl;
import de.fhwedel.klausps.model.impl.exporter.KlausPSExporterImpl;
import de.fhwedel.klausps.model.impl.exporter.PDFExporterImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

public class IOService {

  private final DataAccessService dataAccessService;

  public IOService(DataAccessService dataAccessService) {
    this.dataAccessService = dataAccessService;
  }

  public void createEmptyPeriode(
      Semester semester, LocalDate start, LocalDate end, int kapazitaet) {
    dataAccessService.setPruefungsperiode(
        new PruefungsperiodeImpl(semester, start, end, kapazitaet));
  }

  public void exportPeriode(Path path, ExportTyp typ) throws IOException, ExportException {
    Exporter export;
    Pruefungsperiode periode = dataAccessService.getPruefungsperiode();
    switch (typ) {
      case CSV -> export = new CSVExporterImpl(periode);
      case PDF -> export = new PDFExporterImpl(periode);
      case INTERN -> export = new KlausPSExporterImpl(periode);
      default -> throw new IllegalStateException("Unexpected value: " + typ);
    }
    export.exportTo(path);
  }
}
