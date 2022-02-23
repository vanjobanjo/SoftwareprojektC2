package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide ever other service.
 * If the Service is not instantiated then the class instantiated and saved.
 */
public class ServiceProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

  private static DataAccessService dataAccessService;

  private static IOService ioService;

  private static ScheduleService scheduleService;

  private static RestrictionService restrictionService;

  private static Converter converter;

  private ServiceProvider() {
  }

  public static IOService getIOService() {
    if (ioService == null) {
      ioService = new IOService(getDataAccessService());
      LOGGER.debug("Created new {}.", ioService.getClass().getSimpleName());
    }
    return ioService;
  }

  public static DataAccessService getDataAccessService() {
    if (dataAccessService == null) {
      dataAccessService = new DataAccessService();
      LOGGER.debug("Created new {}.", dataAccessService.getClass().getSimpleName());
    }
    return dataAccessService;
  }

  public static ScheduleService getScheduleService() {
    if (scheduleService == null) {
      scheduleService = new ScheduleService(getDataAccessService(), getRestrictionService(),
          getConverter());
      LOGGER.debug("Created new {}.", scheduleService.getClass().getSimpleName());
      converter.setScheduleService(scheduleService);
    }
    return scheduleService;
  }

  public static RestrictionService getRestrictionService() {
    if (restrictionService == null) {
      restrictionService = new RestrictionService();
      LOGGER.debug("Created new {}.", restrictionService.getClass().getSimpleName());
    }
    return restrictionService;
  }

  public static Converter getConverter() {
    if (converter == null) {
      converter = new Converter();
      LOGGER.debug("Created new {}.", converter.getClass().getSimpleName());
    }
    if (scheduleService != null) {
      converter.setScheduleService(scheduleService);
    }
    return converter;
  }
}
