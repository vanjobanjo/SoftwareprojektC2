package de.fhwedel.klausps.controller.services;


public class ServiceProvider {

  private static DataAccessService dataAccessService;

  private ServiceProvider() {

  }

  public static DataAccessService getDataAccessService() {
    if (dataAccessService == null) {
      dataAccessService = new DataAccessService();
    }
    return dataAccessService;
  }
}
