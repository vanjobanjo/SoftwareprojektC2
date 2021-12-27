package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.SONNTAG;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class KeineKlausurAmSonntag extends WeicheRestriktion{

  private static final DayOfWeek SUNDAY = DayOfWeek.SUNDAY;

  //Mock Konstruktor
  KeineKlausurAmSonntag(DataAccessService dataAccessService) {
    super(dataAccessService, SONNTAG);
  }

  public KeineKlausurAmSonntag() {
    super(ServiceProvider.getDataAccessService(), SONNTAG);
  }


  public boolean isScheduledOnSunday(Pruefung pruefung) {
    if (!isInPeriod(pruefung)) {
      throw new IllegalArgumentException("Prüfung liegt nicht im Zeitraum der Periode.");
    }
    return pruefung.getStartzeitpunkt().getDayOfWeek() == SUNDAY;
  }

  private boolean isInPeriod(Pruefung pruefung) {
    return dataAccessService.terminIsInPeriod(pruefung.getStartzeitpunkt());
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {

    if (!isScheduledOnSunday(pruefung)) {
      return Optional.empty();
    }

    return Optional.of(
        new WeichesKriteriumAnalyse(Set.of(pruefung), SONNTAG,
            new HashSet<>(pruefung.getTeilnehmerkreise()),
            pruefung.schaetzung()));
  }
}
