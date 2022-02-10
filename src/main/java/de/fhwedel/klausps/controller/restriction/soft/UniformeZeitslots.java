package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class UniformeZeitslots extends WeicheRestriktion {

  public UniformeZeitslots() {
    this(ServiceProvider.getDataAccessService());
  }

  protected UniformeZeitslots(DataAccessService dataAccessService) {
    super(dataAccessService, UNIFORME_ZEITSLOTS);
  }


  @Override
  public Optional<WeichesKriteriumAnalyse> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    Set<Pruefung> pruefungenAtSameTime = new HashSet<>(
        dataAccessService.getPlannedPruefungen());
    pruefungenAtSameTime = filter(pruefung, pruefungenAtSameTime);

    if (pruefungenAtSameTime.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(buildAnalysis(pruefung, pruefungenAtSameTime));
  }

  @NotNull
  private Set<Pruefung> filter(@NotNull Pruefung pruefung,
      @NotNull final Set<Pruefung> pruefungenAtSameTime)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung other : pruefungenAtSameTime) {
      if (!dataAccessService.areInSameBlock(pruefung, other)
          && !isOutOfTimeRange(pruefung, other)
          && !hasSameDuration(pruefung, other)) {
        result.add(other);
      }
    }
    result.remove(pruefung);
    return result;
  }


  private boolean isOutOfTimeRange(Pruefung pruefung, Pruefung other) {
    return other.endzeitpunkt().isBefore(pruefung.getStartzeitpunkt()) || other.getStartzeitpunkt()
        .isAfter(pruefung.endzeitpunkt());
  }

  private boolean hasSameDuration(Pruefung pruefung, Pruefung other) {
    return pruefung.getDauer().equals(other.getDauer());
  }
}
