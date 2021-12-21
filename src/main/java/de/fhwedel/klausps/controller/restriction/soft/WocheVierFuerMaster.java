package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.WOCHE_VIER_FUER_MASTER;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class WocheVierFuerMaster extends WeicheRestriktion implements Predicate<Pruefung> {

  private final static int WEEK_FOUR = 4;
  private final int DAYS_WEEK = 7;
  private final LocalDate START_PERIODE;


  public WocheVierFuerMaster() {
    super(ServiceProvider.getDataAccessService(), WOCHE_VIER_FUER_MASTER);
    START_PERIODE = ServiceProvider.getDataAccessService().getStartOfPeriode();
  }

  //Mock Konstruktor
  WocheVierFuerMaster(DataAccessService dataAccessService, LocalDate start) {
    super(dataAccessService, WOCHE_VIER_FUER_MASTER);
    START_PERIODE = start;
  }

  /**
   * True when the given pruefung starts on week 4.
   *
   * @param pruefung the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(Pruefung pruefung) {
    return getWeek(START_PERIODE, pruefung) == WEEK_FOUR;
  }

  /**
   * Evaluates for a {@link Pruefung} in which way it violates a restriction.
   *
   * @param pruefung The pruefung for which to check for violations of a restriction.
   * @return Either an {@link Optional} containing a {@link KriteriumsAnalyse} for the violated
   * restriction, or an empty Optional in case the Restriction was not violated.
   */
  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    boolean violation = test(pruefung);
    if (!violation) {
      return Optional.empty();
    }
    return Optional.of(new WeichesKriteriumAnalyse(Set.of(pruefung), WOCHE_VIER_FUER_MASTER,
        new HashSet<>(pruefung.getTeilnehmerkreise()),
        pruefung.schaetzung()));
  }

  private int getWeek(LocalDate startPeriode, Pruefung pruefung) {
    return (pruefung.getStartzeitpunkt().getDayOfYear() - startPeriode.getDayOfYear())
        / DAYS_WEEK;
  }

}
