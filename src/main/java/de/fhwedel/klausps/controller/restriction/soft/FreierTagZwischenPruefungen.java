package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.FREIER_TAG_ZWISCHEN_PRUEFUNGEN;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
  public Optional<WeichesKriteriumAnalyse> evaluate(@NotNull Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {

    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    Set<Pruefung> pruefungenWithSameTeilnehmerkreisen = new HashSet<>(
        dataAccessService.getGeplantePruefungen());
    // remove of no overlapping Teilnehmerkreise and more than one day apart
    pruefungenWithSameTeilnehmerkreisen = filterOnlyOverlappingTeilnehmerkreiseAndOnlyDayApart(
        pruefung, pruefungenWithSameTeilnehmerkreisen);

    if (pruefungenWithSameTeilnehmerkreisen.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(buildAnalysis(pruefung, pruefungenWithSameTeilnehmerkreisen));
  }

  @NotNull
  private Set<Pruefung> filterOnlyOverlappingTeilnehmerkreiseAndOnlyDayApart(
      @NotNull final Pruefung pruefung,
      @NotNull final Set<Pruefung> pruefungenWithSameTeilnehmerkreisen)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung pruefung1 : pruefungenWithSameTeilnehmerkreisen) {
      if (!dataAccessService.areInSameBlock(pruefung, pruefung1)
          && !testDayApart(pruefung, pruefung1)
          && !testOverlappingTeilnehmerkreise(pruefung, pruefung1)) {
        result.add(pruefung1);
      }
    }
    result.remove(pruefung);
    return result;
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


  @Override
  protected Map<Teilnehmerkreis, Integer> getRelevantSchaetzungen(Pruefung pruefung,
      Pruefung affected) {
    Map<Teilnehmerkreis, Integer> result = new HashMap<>();
    if (pruefung != null) {
      for (Map.Entry<Teilnehmerkreis, Integer> schaetzung : pruefung.getSchaetzungen().entrySet()) {
        if (affected.getSchaetzungen().containsKey(schaetzung.getKey())) {
          result.put(schaetzung.getKey(), schaetzung.getValue());
        }
      }
    }
    return result;
  }
}
