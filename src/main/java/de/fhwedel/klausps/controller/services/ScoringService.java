package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitors;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScoringService {
  private Map<Pruefung, Map<WeichesKriterium, Set<Pruefung>>> analysen;

  public ScoringService(Set<Pruefung> geplantePruefungen) {
    this.analysen =
        analyseAll(
            WeichesKriteriumVisitors.values(),
            geplantePruefungen);
  }


  // Für Prüfungen die sich in einem Block befinden!
  public int getScoring(Pruefung pruefung, List<Pruefung> ignore) {
    return analysen.get(pruefung).entrySet().stream()
        .mapToInt(
            x ->
                x.getValue().stream()
                        .filter(y -> !ignore.contains(y))
                        .collect(Collectors.toSet())
                        .size()
                    * x.getKey().getWert())
        .sum();
  }

  //Scoring zu einer Klausur
  public int getScoring(Pruefung pruefung) {

    return analysen.get(pruefung).entrySet().stream()
        .mapToInt(x -> x.getKey().getWert() * x.getValue().size())
        .sum();
  }

  public List<Pruefung> schedulePruefung(Pruefung pruefung, LocalDateTime termin) {
    pruefung.setStartzeitpunkt(termin);
    Map<WeichesKriterium, Set<Pruefung>> analyseToPruefung =
        analyseKriterienToPruefung(
            WeichesKriteriumVisitors.values(),
            getGeplantePruefungen(),
            pruefung);
    updateAnalyseScheduled(analyseToPruefung, pruefung);
    return getConflictedPruefungen(pruefung);
  }

  public List<Pruefung> unschedulePruefung(Pruefung pruefung) {
    pruefung.setStartzeitpunkt(null);
    List<Pruefung> pruefungen = getConflictedPruefungen(pruefung);
    updateAnalyseUnscheduled(pruefung);
    return pruefungen;
  }

  private List<Pruefung> getConflictedPruefungen(Pruefung pruefung) {
    return analysen.get(pruefung).entrySet().stream()
        .flatMap(x -> x.getValue().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  private void updateAnalyseScheduled(
      Map<WeichesKriterium, Set<Pruefung>> scheduledScores, Pruefung scheduledPruefung) {
    for (WeichesKriterium kriterium : scheduledScores.keySet()) {
      for (Pruefung pruefung : scheduledScores.get(kriterium)) {
        analysen.get(pruefung).get(kriterium).add(scheduledPruefung);
      }
    }
    this.analysen.put(scheduledPruefung, scheduledScores);
  }

  private void updateAnalyseUnscheduled(Pruefung unscheduledPruefunug) {
    this.analysen =
        analysen.entrySet().stream()
            .collect(
                Collectors.groupingBy(
                    x -> x.getKey(),
                    Collectors.flatMapping(
                        x -> x.getValue().entrySet().stream(),
                        Collectors.groupingBy(
                            x -> x.getKey(),
                            Collectors.flatMapping(
                                y ->
                                    y.getValue().stream()
                                        .filter(x -> !x.equals(unscheduledPruefunug)),
                                Collectors.toSet())))));
    this.analysen.remove(unscheduledPruefunug);
  }

  // So okay
  public static Map<WeichesKriterium, Set<Pruefung>> analyseKriterienToPruefung(
      WeichesKriteriumVisitors[] kriterien, Set<Pruefung> scheduledPruefungen, Pruefung toCheck) {
    return Arrays.stream(kriterien)
        .collect(
            Collectors.groupingBy(
                kriterium -> kriterium.visitor.getWeichesKriterium(),
                Collectors.flatMapping(
                    kriterium ->
                        scheduledPruefungen.stream()
                            .filter(pruefung -> kriterium.visitor.test(pruefung, toCheck)),
                    Collectors.toSet())));
  }

  // So okay
  public static Map<Pruefung, Map<WeichesKriterium, Set<Pruefung>>> analyseAll(
      WeichesKriteriumVisitors[] kriterien, Set<Pruefung> scheduledPruefungen) {
    return scheduledPruefungen.stream()
        .collect(
            Collectors.groupingBy(
                pruefung -> pruefung,
                Collectors.flatMapping(
                    pruefung ->
                        ScoringService.analyseKriterienToPruefung(
                            kriterien,
                            scheduledPruefungen.stream()
                                .filter(pruefung2 -> !pruefung2.equals(pruefung))
                                    .collect(Collectors.toSet()),
                            pruefung)
                            .entrySet()
                            .stream(),
                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
  }

   Set<Pruefung> getGeplantePruefungen(){
    return analysen.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toSet());
  }

  Map<Pruefung, Map<WeichesKriterium, Set<Pruefung>>> getAnalysen(){
    return analysen;
  }

  Map<WeichesKriterium, Set<Pruefung>> getKriteriumsAnalyise(Pruefung pruefung){
    return analysen.get(pruefung);
  }
}
