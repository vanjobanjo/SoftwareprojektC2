package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UniformeZeitslots extends WeicheRestriktion {


  protected UniformeZeitslots(DataAccessService dataAccessService) {
    super(dataAccessService, UNIFORME_ZEITSLOTS);
  }

  protected UniformeZeitslots() {
    super(ServiceProvider.getDataAccessService(), UNIFORME_ZEITSLOTS);
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


  private WeichesKriteriumAnalyse buildAnalysis(Set<Pruefung> affectedPruefungen) {
    int scoring = 0;
    Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise = new HashMap<>();
    for (Pruefung pruefung : affectedPruefungen) {
      addTeilnehmerkreisAndGetSchaetzung(affectedTeilnehmerkreise, pruefung.getSchaetzungen());
      scoring += this.kriterium.getWert();

    }

    return new WeichesKriteriumAnalyse(affectedPruefungen, this.kriterium,
        affectedTeilnehmerkreise.keySet(), getAffectedStudents(affectedTeilnehmerkreise), scoring);
  }

  private void addTeilnehmerkreisAndGetSchaetzung(
      Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise,
      Map<Teilnehmerkreis, Integer> teilnehmerkreiseToAdd) {
    for (Map.Entry<Teilnehmerkreis, Integer> schaetzung : teilnehmerkreiseToAdd.entrySet()) {
      Integer foundSchaetzung = affectedTeilnehmerkreise.getOrDefault(schaetzung.getKey(), null);
      Integer newSchaetzung = schaetzung.getValue();
      if (foundSchaetzung == null) {
        affectedTeilnehmerkreise.put(schaetzung.getKey(), newSchaetzung);
      } else if (foundSchaetzung < newSchaetzung) {
        affectedTeilnehmerkreise.replace(schaetzung.getKey(), foundSchaetzung, newSchaetzung);
      }
    }
  }

  private int getAffectedStudents(Map<Teilnehmerkreis, Integer> affectedTeilnehmerkreise) {
    int result = 0;
    for (Integer schaetzung : affectedTeilnehmerkreise.values()) {
      result += schaetzung;
    }
    return result;
  }


}