package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.WOCHE_VIER_FUER_MASTER;
import static de.fhwedel.klausps.model.api.Ausbildungsgrad.BACHELOR;
import static de.fhwedel.klausps.model.api.Ausbildungsgrad.MASTER;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDate;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WocheVierFuerMaster extends WeicheRestriktion {

  private final static int WEEK_FOUR = 4;
  private final static int DAYS_WEEK = 7;
  private final LocalDate startPeriode;


  public WocheVierFuerMaster() {
    super(ServiceProvider.getDataAccessService(), WOCHE_VIER_FUER_MASTER);
    startPeriode = ServiceProvider.getDataAccessService().getStartOfPeriode();
  }

  //Mock Konstruktor
  WocheVierFuerMaster(DataAccessService dataAccessService, LocalDate start) {
    super(dataAccessService, WOCHE_VIER_FUER_MASTER);
    startPeriode = start;
  }

  //package private for test
  boolean isWeekFourBachelor(Pruefung pruefung) {
    return getWeek(startPeriode, pruefung) == WEEK_FOUR
        && pruefung.getAusbildungsgrade().contains(BACHELOR);
  }

  /**
   * Evaluates for a {@link Pruefung} in which way it violates a restriction.
   *
   * @param pruefung The pruefung for which to check the violations of a restriction.
   * @return Either an {@link Optional} containing a {@link KriteriumsAnalyse} for the violated
   * restriction, or an empty Optional in case the Restriction was not violated.
   */
  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    if (!isWeekFourBachelor(pruefung)) {
      return Optional.empty();
    }

    Set<Teilnehmerkreis> tkWithoutMaster = pruefung.getTeilnehmerkreise().stream()
        .filter(this::isNotMaster)
        .collect(
            Collectors.toSet());

    int affectedWithoutMaster = pruefung.getSchaetzungen().entrySet().stream()
        .filter(entry -> isNotMaster(entry.getKey()))
        .mapToInt(Entry::getValue).sum();

    return Optional.of(new WeichesKriteriumAnalyse(Set.of(pruefung), WOCHE_VIER_FUER_MASTER,
        tkWithoutMaster,
        affectedWithoutMaster
    ));
  }

  private boolean isNotMaster(Teilnehmerkreis tk) {
    return tk.getAusbildungsgrad() != MASTER;
  }

  private int getWeek(LocalDate startPeriode, Pruefung pruefung) {
    return (pruefung.getStartzeitpunkt().getDayOfYear() - startPeriode.getDayOfYear())
        / DAYS_WEEK;
  }

}
