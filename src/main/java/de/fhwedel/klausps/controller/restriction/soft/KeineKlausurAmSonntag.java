package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.DayOfWeek;
import java.util.function.Predicate;

public class KeineKlausurAmSonntag extends WeicheRestriktion implements Predicate<Pruefung> {
  private static final DayOfWeek SUNDAY = DayOfWeek.SUNDAY;

  //Mock Konstruktor
  KeineKlausurAmSonntag(
      DataAccessService dataAccessService){
    super(dataAccessService, WeichesKriterium.SONNTAG);
  }

  public KeineKlausurAmSonntag(){
    super(ServiceProvider.getDataAccessService(), WeichesKriterium.SONNTAG);
  }

  @Override
  public boolean test(Pruefung pruefung) {
    if(!super.isInPeriod(pruefung)){
      throw new IllegalArgumentException("Prüfung liegt nicht im Zeitraum der Periode.");
    }
    return pruefung.getStartzeitpunkt().getDayOfWeek() == SUNDAY;
  }
}
