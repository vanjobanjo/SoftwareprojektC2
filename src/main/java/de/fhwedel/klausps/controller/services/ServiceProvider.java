package de.fhwedel.klausps.controller.services;

public class ServiceProvider {

  private static DataAccessService dataAccessService;

  private static IOService ioService;

  private static ScheduleService scheduleService;

  private static RestrictionService restrictionService;

  private static Converter converter;

  private ServiceProvider() {
  }

  public static DataAccessService getDataAccessService() {
    if (dataAccessService == null) {
      dataAccessService = new DataAccessService();
    }
    return dataAccessService;
  }

  public static IOService getIOService() {
    if (ioService == null) {
      ioService = new IOService(getDataAccessService());
    }
    return ioService;
  }

  public static ScheduleService getScheduleService() {
    if (scheduleService == null) {
      scheduleService = new ScheduleService(getDataAccessService(), getRestrictionService(),
          getConverter());
    }
    return scheduleService;
  }

  public static RestrictionService getRestrictionService() {
    if (restrictionService == null) {
      restrictionService = new RestrictionService();
    }
    return restrictionService;
  }

   static Converter getConverter() {
    if (converter == null) {
      converter = new Converter();
    }
    if (scheduleService != null) {
      converter.setScheduleService(scheduleService);
    }
    return converter;
  }
}
