package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Service provides a single point of entry for the creation of all necessary services. Due to
 * dependency structures, some services have to be registered with each other. The ServiceProvider
 * therefore ensures that all services are registered correctly and available when needed. If the
 * ServiceProvider is used for handling all services, it also ensures that only one instance of each
 * service will exist at runtime.
 */
public class ServiceProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

  /**
   * The {@link DataAccessService} used to communicate with the {@link
   * de.fhwedel.klausps.model.api.Pruefungsperiode}
   */
  private static DataAccessService dataAccessService;

  /**
   * The {@link IOService Service} used to import and export {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   * Pruefungsperioden}
   */
  private static IOService ioService;

  /**
   * The {@link ScheduleService Service} used to (un)schedule {@link de.fhwedel.klausps.model.api.Planungseinheit
   * Planungseinheiten}
   */
  private static ScheduleService scheduleService;

  /**
   * The {@link RestrictionService Service} used to evaluate {@link de.fhwedel.klausps.controller.restriction.Restriction
   * Restrictions}
   */
  private static RestrictionService restrictionService;

  /**
   * The {@link Converter} used to convert internally used
   */
  private static Converter converter;

  /**
   * Changes visibility of the default constructor to private because utility classes containing
   * only static methods should not be instantiated.
   */
  private ServiceProvider() {
    // I don't need to be instantiated
  }

  /**
   * Provides the IOService. If none exists already, a new one is created, otherwise this method
   * returns the existing one.<br> The IOService requires a DataAccessService, therefore calling
   * this method might lead to the instantiation of the DataAccessService.
   *
   * @return the IOService
   */
  public static IOService getIOService() {
    if (ioService == null) {
      ioService = new IOService(getDataAccessService());
      LOGGER.debug("Created new {}.", ioService.getClass().getSimpleName());
    }
    return ioService;
  }

  /**
   * Provides the DataAccessService. If none exists already, a new one is created, otherwise this
   * method returns the existing one.
   *
   * @return the DataAccessService
   */
  public static DataAccessService getDataAccessService() {
    if (dataAccessService == null) {
      dataAccessService = new DataAccessService();
      LOGGER.debug("Created new {}.", dataAccessService.getClass().getSimpleName());
    }
    return dataAccessService;
  }

  /**
   * Provides the ScheduleService. If none exists already, a new one is created, otherwise this
   * method returns the existing one.<br> Because the ScheduleService requires a {@link
   * DataAccessService}, {@link RestrictionService} and a {@link Converter}, calling this method
   * ensures the existence of those. <br> It also registers the newly created ScheduleService in the
   * Converter.
   *
   * @return the ScheduleService
   */
  public static ScheduleService getScheduleService() {
    if (scheduleService == null) {
      scheduleService = new ScheduleService(getDataAccessService(), getRestrictionService(),
          getConverter());
      LOGGER.debug("Created new {}.", scheduleService.getClass().getSimpleName());
      converter.setScheduleService(scheduleService);
    }
    return scheduleService;
  }

  /**
   * Provides the RestrictionService. If none exists already, a new one is created, otherwise this
   * method returns the existing one.
   *
   * @return the RestrictionService
   */
  public static RestrictionService getRestrictionService() {
    if (restrictionService == null) {
      restrictionService = new RestrictionService();
      LOGGER.debug("Created new {}.", restrictionService.getClass().getSimpleName());
    }
    return restrictionService;
  }

  /**
   * Provides the Converter. If none exists already, a new one is created, otherwise this method
   * returns the existing one. If the ScheduleService already exists, it gets set in the Converter.
   *
   * @return the Converter
   */
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
