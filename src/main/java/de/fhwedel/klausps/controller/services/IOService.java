package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.impl.PruefungsperiodeImpl;
import java.time.LocalDate;

public class IOService {

  private final DataAccessService dataAccessService;

  public IOService(DataAccessService dataAccessService) {
    this.dataAccessService = dataAccessService;
  }

  public void createEmptyPeriode(
      Semester semester, LocalDate start, LocalDate end, int kapazitaet) {
    dataAccessService.setPlanungseinheit(
        new PruefungsperiodeImpl(semester, start, end, kapazitaet));
  }
}
