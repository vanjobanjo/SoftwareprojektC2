package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
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
        isInSameBlock(pruefung, other)
            || isOutOfTimeRange(pruefung, other)
            || hasSameDuration(pruefung, other));

    if (pruefungenAtSameTime.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(buildAnalysis(pruefungenAtSameTime));
  }


  private boolean isInSameBlock(Pruefung pruefung, Pruefung other) {
    // todo auslagern
    Optional<Block> pruefungBlock = dataAccessService.getBlockTo(pruefung);
    Optional<Block> otherBlock = dataAccessService.getBlockTo(other);
    if (pruefungBlock.isEmpty()) {
      return false;
    }
    if (otherBlock.isEmpty()) {
      return false;
    }
    return pruefungBlock.equals(otherBlock);
  }

  private boolean isOutOfTimeRange(Pruefung pruefung, Pruefung other) {
    return other.endzeitpunkt().isBefore(pruefung.getStartzeitpunkt()) || other.getStartzeitpunkt()
        .isAfter(pruefung.endzeitpunkt());
  }

  private boolean hasSameDuration(Pruefung pruefung, Pruefung other) {
    return pruefung.getDauer().equals(other.getDauer());
  }

  @Override
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    return affectedPruefungen.size() * this.kriterium.getWert();
  }


}