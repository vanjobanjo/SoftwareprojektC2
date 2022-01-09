package de.fhwedel.klausps.controller;

import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.IOService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import org.junit.jupiter.api.BeforeEach;

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
    this.deviceUnderTest = new Controller();
  }

}
