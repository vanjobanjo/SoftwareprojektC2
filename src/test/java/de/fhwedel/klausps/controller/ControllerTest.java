package de.fhwedel.klausps.controller;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedROPruefung;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControllerTest {

  Controller deviceUnderTest;
  DataAccessService dataAccessService;
  IOService ioService;
  ScheduleService scheduleService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.ioService = mock(IOService.class);
    this.scheduleService = mock(ScheduleService.class);
    this.deviceUnderTest = new Controller(dataAccessService, ioService, scheduleService);
  }

  @Test
  void getGeplantePruefungenWithKonflikt_noNullParametersAllowed() {
    assertThrows(NullPointerException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(null));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_missingPruefungsperiodeIsDetected() {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);

    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(false);

    assertThrows(NoPruefungsPeriodeDefinedException.class,
        () -> deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor));
  }

  @Test
  void getGeplantePruefungenWithKonflikt_useDataAccessServiceOnlyForCheckForPruefungsperiode()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);

    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);

    deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor);
    verify(dataAccessService, only()).isPruefungsperiodeSet();
  }

  @Test
  void getGeplantePruefungenWithKonflikt_delegateToScheduleService()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit toCheckFor = getRandomPlannedROPruefung(1L);

    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);

    deviceUnderTest.getGeplantePruefungenWithKonflikt(toCheckFor);
    verify(scheduleService, times(1)).getGeplantePruefungenWithKonflikt(any());
  }

}
