package de.fhwedel.klausps.controller.api.scoring.pred;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnzahlPruefungPred extends WeichesKriteriumPred {
  static final int LIMIT_PER_WEEK = 5;
  private final LocalDate startOfPeriod;
  Map<Integer, Set<Pruefung>> pruefungWeek;

  protected AnzahlPruefungPred(LocalDate startOfPeriod, Set<Pruefung> geplantePruefung) {
    super(WeichesKriterium.ANZAHL_PRUEFUNGEN_PRO_WOCHE);
    this.startOfPeriod = startOfPeriod;
    pruefungWeek =
        geplantePruefung.stream()
            .collect(Collectors.groupingBy(this::calcWeekOfPruefungInPeriode, Collectors.toSet()));
  }

  private Integer calcWeekOfPruefungInPeriode(Pruefung p1) {
    return (p1.getStartzeitpunkt().toLocalDate().getDayOfYear() - startOfPeriod.getDayOfYear()) / 7;
  }

  @Override
  public boolean test(Pruefung pruefung) {
    Integer week = calcWeekOfPruefungInPeriode(pruefung);
    return this.pruefungWeek.get(week).size() + 1 >= LIMIT_PER_WEEK;
    // wenn die übergeben pruefung noch nicht in set ist, dann prüfe es.
  }
}
