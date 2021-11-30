package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitor;
import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitors;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleService {
    private final Pruefungsperiode pruefungsperiode;
    private Map<Pruefung, Integer> scoringsScheduled;
    private Map<Pruefung, Map<WeichesKriterium, Map<Pruefung, Integer>>> analysenScheduled;

    public ScheduleService(Pruefungsperiode pruefungsperiode) {
        this.pruefungsperiode = pruefungsperiode;
        this.analysenScheduled = new HashMap<>();
        List<Pruefung> scheduledPruefungen = pruefungsperiode.geplantePruefungen().stream().toList();
        this.scoringsScheduled = scheduledPruefungen.stream().collect(Collectors.toMap(x -> x, y -> 0));

        for (Pruefung toCheck : scheduledPruefungen) {
            Map<WeichesKriterium, Map<Pruefung, Integer>> conflictPruefung = new HashMap<>();
            Map<Pruefung, Integer> conflictedScores;
            List<Pruefung> pruefungen = scheduledPruefungen.stream().filter(x -> !x.equals(toCheck)).toList();

            for (WeichesKriteriumVisitors visitor : WeichesKriteriumVisitors.values()) {
                WeichesKriteriumVisitor kriterium = visitor.visitor;
                conflictedScores = getConflictedScheduleDeltaScores(pruefungen, toCheck, kriterium);
                conflictPruefung.put(kriterium.getWeichesKriterium(), conflictedScores);
            }
            analysenScheduled.put(toCheck, conflictPruefung);
        }
    }

    private Map<Pruefung, Integer> getConflictedScheduleDeltaScores(List<Pruefung> scheduledPruefungen,
                                                                    Pruefung toCheck,
                                                                    WeichesKriteriumVisitor kriterium){
        return scheduledPruefungen
                .stream()
                .filter(x -> kriterium.test(x, toCheck))
                .collect(Collectors.toMap(x -> x, y -> kriterium.getWeichesKriterium().getWert()));
    }

    private Map<Pruefung, Integer> getConflictedUnscheduleDeltaScores(List<Pruefung> scheduledPruefungen,
                                                                      Pruefung toCheck,
                                                                      WeichesKriteriumVisitor kriterium){

        return negateScoring(getConflictedScheduleDeltaScores(scheduledPruefungen, toCheck, kriterium));
    }


    private Map<WeichesKriterium, Set<Pruefung>> getAnalysenMapOfPruefung(WeichesKriteriumVisitor kriterium,
                                                                          List<Pruefung> scheduledPruefungen,
                                                                          Pruefung toCheck){
        return scheduledPruefungen
                .stream()
                .filter(x -> kriterium.test(x, toCheck))
                .collect(Collectors.groupingBy(x -> kriterium.getWeichesKriterium(), Collectors.toSet()));
    }

    private Map<Pruefung, Integer> updateScoring (Map<Pruefung, Integer> oldScorings, Map<Pruefung, Integer> deltaNewScoring){
        return Stream
                .concat(oldScorings.entrySet().stream(), deltaNewScoring.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry :: getValue)));
    }

    private Map<Pruefung, Integer> negateScoring (Map<Pruefung, Integer> deltaScoring){
        return deltaScoring.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, y -> -y.getValue()));
    }

    List<ReadOnlyPruefung> reducedScoring(Set<Pruefung> geplantePruefungen, Pruefung ungeplant) {

        return null; //TODO vernünftiges Scoring machen!
    }
}
