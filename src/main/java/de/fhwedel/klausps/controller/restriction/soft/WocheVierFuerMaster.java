package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.WOCHE_VIER_FUER_MASTER;
import static de.fhwedel.klausps.model.api.Ausbildungsgrad.AUSBILDUNG;
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

  private static final int WEEK_FOUR = 4;
  private static final int DAYS_WEEK = 7;
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

  //if the pruefung is on week 4 and one of the tk is not master, this function returns true.
  //terminIsInPeriodCheck must be called somewhere else but for security we can also check it here.
  boolean isWeekFourContainsNotOnlyMaster(Pruefung pruefung) {
    if (!dataAccessService.terminIsInPeriod(pruefung.getStartzeitpunkt())) {
      throw new IllegalArgumentException("Prüfung ist nicht in Periode.");
    }
    return getWeek(startPeriode, pruefung.getStartzeitpunkt().toLocalDate()) == WEEK_FOUR
        && (pruefung.getAusbildungsgrade().contains(BACHELOR)
        || pruefung.getAusbildungsgrade().contains(AUSBILDUNG));
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
    if (!isWeekFourContainsNotOnlyMaster(pruefung)) {
      return Optional.empty();
    }

    //filter the master tk, because they are not responsible for the violation,
    //so they are not part of the KriteriumsAnalyse
    Set<Teilnehmerkreis> tkWithoutMaster = pruefung.getTeilnehmerkreise().stream()
        .filter(this::isNotMaster)
        .collect(
            Collectors.toSet());

    //filter the master students, because they are not responsible for the violation and not part
    // of the KriteriumsAnalyse
    int affectedWithoutMaster = pruefung.getSchaetzungen().entrySet().stream()
        .filter(entry -> isNotMaster(entry.getKey()))
        .mapToInt(Entry::getValue).sum();

    int scoring = kriterium.getWert() * tkWithoutMaster.size();

    return Optional.of(new WeichesKriteriumAnalyse(Set.of(pruefung), WOCHE_VIER_FUER_MASTER,
        tkWithoutMaster,
        affectedWithoutMaster, scoring
    ));
  }

  private boolean isNotMaster(Teilnehmerkreis tk) {
    return tk.getAusbildungsgrad() != MASTER;
  }

  private int getWeek(LocalDate startPeriode, LocalDate termin) {
    return (termin.getDayOfYear() - startPeriode.getDayOfYear())
        / DAYS_WEEK;
  }

  @Override
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    throw new UnsupportedOperationException("not implemented");
  }

}
