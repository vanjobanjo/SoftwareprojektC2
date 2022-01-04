package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.FREIER_TAG_ZWISCHEN_PRUEFUNGEN;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class FreierTagZwischenPruefungen extends WeicheRestriktion {

  static final int MAX_DAY = 365;

  public FreierTagZwischenPruefungen() {
    this(ServiceProvider.getDataAccessService());
  }

  protected FreierTagZwischenPruefungen(@NotNull DataAccessService dataAccessService) {
    super(dataAccessService, FREIER_TAG_ZWISCHEN_PRUEFUNGEN);
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(@NotNull Pruefung pruefung) {

    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    Set<Pruefung> pruefungenWithSameTeilnehmerkreisen = new HashSet<>(
        dataAccessService.getGeplanteModelPruefung());
    // remove of no overlapping Teilnehmerkreise and more than one day apart
    pruefungenWithSameTeilnehmerkreisen.remove(pruefung);
    pruefungenWithSameTeilnehmerkreisen.removeIf(other ->
        testInSameBlock(pruefung, other)
            || testDayApart(pruefung, other)
            || testOverlappingTeilnehmerkreise(pruefung, other));

    if (pruefungenWithSameTeilnehmerkreisen.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(buildAnalysis(pruefungenWithSameTeilnehmerkreisen));
  }


  private boolean testOverlappingTeilnehmerkreise(Pruefung pruefung, Pruefung other) {
    return other.getTeilnehmerkreise().stream().noneMatch(teilnehmerkreis ->
        pruefung.getTeilnehmerkreise().contains(teilnehmerkreis));
  }

  private boolean testDayApart(Pruefung pruefung, Pruefung other) {

    int difference = Math.abs(
        pruefung.getStartzeitpunkt().getDayOfYear() - other.getStartzeitpunkt().getDayOfYear());
    // 365 -1
    // 1 - 365
    if (pruefung.getStartzeitpunkt().getYear() != other.getStartzeitpunkt().getYear()) {
      return difference < MAX_DAY - 1;
    }
    return difference > 1;
  }

  private boolean testInSameBlock(Pruefung pruefung, Pruefung other) {
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

}
