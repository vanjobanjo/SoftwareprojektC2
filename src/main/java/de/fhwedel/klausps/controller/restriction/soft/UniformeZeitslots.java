package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UniformeZeitslots extends WeicheRestriktion {

  public UniformeZeitslots() {
    this(ServiceProvider.getDataAccessService());
  }

  protected UniformeZeitslots(DataAccessService dataAccessService) {
    super(dataAccessService, UNIFORME_ZEITSLOTS);
  }


  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    Set<Pruefung> pruefungenAtSameTime = new HashSet<>(
        dataAccessService.getGeplanteModelPruefung());
    pruefungenAtSameTime.remove(pruefung);
    pruefungenAtSameTime.removeIf(other ->
        dataAccessService.areInSameBlock(pruefung, other)
            || isOutOfTimeRange(pruefung, other)
            || hasSameDuration(pruefung, other));

    if (pruefungenAtSameTime.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(buildAnalysis(pruefung, pruefungenAtSameTime));
  }


  private boolean isOutOfTimeRange(Pruefung pruefung, Pruefung other) {
    return other.endzeitpunkt().isBefore(pruefung.getStartzeitpunkt()) || other.getStartzeitpunkt()
        .isAfter(pruefung.endzeitpunkt());
  }

  private boolean hasSameDuration(Pruefung pruefung, Pruefung other) {
    return pruefung.getDauer().equals(other.getDauer());
  }
}