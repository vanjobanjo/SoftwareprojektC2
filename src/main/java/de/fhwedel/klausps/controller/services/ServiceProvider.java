package de.fhwedel.klausps.controller.services;

public class ServiceProvider {

  private static DataAccessService dataAccessService;

  private static IOService ioService;

  private ServiceProvider() {}

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
}
