package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class KeineKlausurAmSonntag extends WeicheRestriktion implements Predicate<Pruefung> {

  private static final DayOfWeek SUNDAY = DayOfWeek.SUNDAY;

  //Mock Konstruktor
  KeineKlausurAmSonntag(DataAccessService dataAccessService) {
    super(dataAccessService, WeichesKriterium.SONNTAG);
  }

  public KeineKlausurAmSonntag() {
    super(ServiceProvider.getDataAccessService(), WeichesKriterium.SONNTAG);
  }

  @Override
  public boolean test(Pruefung pruefung) {
    if (!isInPeriod(pruefung)) {
      throw new IllegalArgumentException("Prüfung liegt nicht im Zeitraum der Periode.");
    }
    return pruefung.getStartzeitpunkt().getDayOfWeek() == SUNDAY;
  }

  private boolean isInPeriod(Pruefung pruefung) {
    return dataAccessService.terminIsInPeriod(pruefung.getStartzeitpunkt());
  }

  @Override
  public KriteriumsAnalyse evaluate(Pruefung toEvaluate) {
    if (test(toEvaluate)) {
      return new KriteriumsAnalyse(Set.of(new PruefungDTOBuilder(toEvaluate).build()), //TODO soll das Scoring hier auch drin sein?
          WeichesKriterium.SONNTAG, new HashSet<>(toEvaluate.getTeilnehmerkreise()),
          toEvaluate.schaetzung());
  public Optional<KriteriumsAnalyse> evaluate(Pruefung pruefung) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }
  public KriteriumsAnalyse evaluate(Pruefung toPlan) {
    if (test(toPlan)) {
      return new KriteriumsAnalyse(Set.of(new PruefungDTOBuilder(toPlan).build()),
          WeichesKriterium.SONNTAG, new HashSet<>(toPlan.getTeilnehmerkreise()),
          toPlan.schaetzung());
    } else {
      return null;
    }
  }
}
