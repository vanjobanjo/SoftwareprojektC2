package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitor;
import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitors;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleService {
    private final Pruefungsperiode pruefungsperiode;
    private Map<Pruefung, Map<WeichesKriterium, Set<Pruefung>>> analysen;

    public ScheduleService(Pruefungsperiode pruefungsperiode) {
        this.pruefungsperiode = pruefungsperiode;
        this.analysen = analyseAll(WeichesKriteriumVisitors.values(), pruefungsperiode.geplantePruefungen().stream().toList());
    }

    private Map<Pruefung, Integer> getConflictedPruefungenAndDeltaScores(List<Pruefung> scheduledPruefungen,
                                                                         Pruefung toCheck,
                                                                         WeichesKriteriumVisitor kriterium) {
        return scheduledPruefungen
                .stream()
                .filter(x -> kriterium.test(x, toCheck))
                .collect(Collectors.toMap(x -> x, y -> kriterium.getWeichesKriterium().getWert()));
    }

    private Set<Pruefung> getConflictedPruefungenToKriterium(List<Pruefung> scheduledPruefungen,
                                                             Pruefung pruefung,
                                                             WeichesKriteriumVisitor kriterium) {
        return scheduledPruefungen
                .stream()
                .filter(x -> kriterium.test(x, pruefung))
                .collect(Collectors.toSet());
    }

    private Map<Pruefung, Integer> getConflictedUnscheduleDeltaScores(List<Pruefung> scheduledPruefungen,
                                                                      Pruefung toCheck,
                                                                      WeichesKriteriumVisitor kriterium) {

        return negateScoring(getConflictedPruefungenAndDeltaScores(scheduledPruefungen, toCheck, kriterium));
    }

    public static int scoringOfPruefung(Map<WeichesKriterium, Set<Pruefung>> analysen){
        return analysen.entrySet().stream().collect(Collectors.groupingBy(x -> x.getKey().getWert(), Collectors.summingInt(x -> x.getValue().size()))).entrySet().stream().map((x) -> x.getKey() * x.getValue()).collect(Collectors.summingInt(x -> x));
    }

    private Map<WeichesKriterium, Set<Pruefung>> getAnalysenMapOfPruefung(WeichesKriteriumVisitor kriterium,
                                                                          List<Pruefung> scheduledPruefungen,
                                                                          Pruefung toCheck) {
        return scheduledPruefungen
                .stream()
                .filter(x -> kriterium.test(x, toCheck))
                .collect(Collectors.groupingBy(x -> kriterium.getWeichesKriterium(), Collectors.toSet()));
    }

    private Map<WeichesKriterium, Map<Pruefung, Integer>> getKriteriumsAnalyseOfPruefungWithDeltaScoring(WeichesKriteriumVisitors[] kriterien,
                                                                                                         List<Pruefung> scheduledPruefungen,
                                                                                                         Pruefung toCheck) {
        Map<WeichesKriterium, Map<Pruefung, Integer>> kriteriumsAnalyse = new HashMap<>();

        for (WeichesKriteriumVisitors v : kriterien) {
            WeichesKriteriumVisitor kriterium = v.getWeichesKriteriumVisitor();
            Map<Pruefung, Integer> conflictedPruefungAndDeltaScores = getConflictedPruefungenAndDeltaScores(scheduledPruefungen, toCheck, kriterium);
            kriteriumsAnalyse.put(v.getWeichesKriteriumVisitor().getWeichesKriterium(), conflictedPruefungAndDeltaScores);
        }

        return kriteriumsAnalyse;
    }

    private Map<Pruefung, Integer> updateScoring(Map<Pruefung, Integer> oldScorings, Map<Pruefung, Integer> deltaNewScoring) {
        return Stream
                .concat(oldScorings.entrySet().stream(), deltaNewScoring.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
    }

    private Map<Pruefung, Integer> negateScoring(Map<Pruefung, Integer> deltaScoring) {
        return deltaScoring.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, y -> -y.getValue()));
    }
/*
    public int getScoringofPruefung(Pruefung pruefung) {
        Map<WeichesKriterium, Map<Pruefung, Integer>> analyseOfPruefung = this.analysenScheduled.get(pruefung);
        return analyseOfPruefung == null
                ? 0
                : analyseOfPruefung.values().stream().flatMap(x -> x.entrySet().stream()).mapToInt(Map.Entry::getValue).sum();
    }*/

    public List<Pruefung> schedulePruefung(Pruefung pruefung, LocalDateTime time) {
        Map<WeichesKriterium, Map<Pruefung, Integer>> analyseOfPruefung
                = getKriteriumsAnalyseOfPruefungWithDeltaScoring(WeichesKriteriumVisitors.values(),
                pruefungsperiode.geplantePruefungen().stream().toList(),
                pruefung);
        updateAnalyseScheduled(analyseOfPruefung);
        return analyseOfPruefung.values().stream().flatMap(x -> x.entrySet().stream()).map(Map.Entry::getKey).collect(Collectors.toList());
    }


    private void updateAnalyseScheduled(Map<WeichesKriterium, Map<Pruefung, Integer>> analyseOfPruefung) {
        for (WeichesKriterium w : analyseOfPruefung.keySet()) {
            int scoring = w.getWert();
            for (Pruefung p : analyseOfPruefung.get(w).keySet()) {
                //analysenScheduled.get(p).get(w).put(p, scoring);
            }
        }
    }

    // So okay
    public static Map<WeichesKriterium, Set<Pruefung>> analyseKriterienToPruefung(WeichesKriteriumVisitors[] kriterien,
                                                                                  List<Pruefung> scheduledPruefungen,
                                                                                  Pruefung toCheck) {
        return Arrays.stream(kriterien)
                .collect(Collectors.groupingBy(kriterium -> kriterium.visitor.getWeichesKriterium(),
                        Collectors.flatMapping(kriterium -> scheduledPruefungen.stream().filter(pruefung -> kriterium.visitor.test(pruefung, toCheck)),
                                Collectors.toSet())));
    }

    //So okay
    public static Map<Pruefung, Map<WeichesKriterium, Set<Pruefung>>> analyseAll(WeichesKriteriumVisitors[] kriterien,
                                                                                 List<Pruefung> scheduledPruefungen) {
        return scheduledPruefungen
                .stream().collect(Collectors.groupingBy(pruefung -> pruefung,
                        Collectors.flatMapping(pruefung -> ScheduleService.analyseKriterienToPruefung(kriterien, scheduledPruefungen.stream().filter(pruefung2 -> !pruefung2.equals(pruefung)).toList(),
                                pruefung).entrySet().stream(), Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }
}