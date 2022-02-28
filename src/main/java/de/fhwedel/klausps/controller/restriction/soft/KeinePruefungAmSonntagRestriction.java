package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.SONNTAG;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.Set;

public class KeinePruefungAmSonntagRestriction extends SoftRestriction {

  private static final DayOfWeek SUNDAY = DayOfWeek.SUNDAY;

  //Mock Konstruktor
  KeinePruefungAmSonntagRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, SONNTAG);
  }

  public KeinePruefungAmSonntagRestriction() {
    super(ServiceProvider.getDataAccessService(), SONNTAG);
  }

  /**
   * Checks the day of the passed pruefung
   *
   * @param pruefung scheduled pruefungs
   * @return true when passed pruefung is on sunday
   */
  public boolean isScheduledOnSunday(Pruefung pruefung) {
    if (!isInPeriod(pruefung)) {
      throw new IllegalArgumentException("Prüfung liegt nicht im Zeitraum der Periode.");
    }
    return pruefung.getStartzeitpunkt().getDayOfWeek() == SUNDAY;
  }

  /**
   * Checks if the passed pruefung is in period
   *
   * @param pruefung passed
   * @return true when passed pruefung is in period
   */
  private boolean isInPeriod(Pruefung pruefung) {
    return dataAccessService.terminIsInPeriod(pruefung.getStartzeitpunkt());
  }

  @Override
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(Pruefung pruefung) {

    if (!isScheduledOnSunday(pruefung)) {
      return Optional.empty();
    }

    return Optional.of(buildAnalysis(pruefung, Set.of(pruefung)));
  }


}
