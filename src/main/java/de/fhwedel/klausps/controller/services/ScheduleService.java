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

    public int getScoring(Pruefung pruefung){
        return analysen.get(pruefung)
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(x -> x.getKey().getWert(), Collectors.flatMapping(x -> x.getValue().stream(), Collectors.summingInt(x -> 1))))
                .entrySet().stream().map(x-> x.getKey() * x.getValue()).mapToInt(x -> x).sum();
    }

    public List<Pruefung> schedulePruefung(Pruefung pruefung, LocalDateTime termin){
        pruefung.setStartzeitpunkt(termin);
        Map<WeichesKriterium, Set<Pruefung>> analyseKriterienToPruefung = analyseKriterienToPruefung(WeichesKriteriumVisitors.values(), pruefungsperiode.geplantePruefungen().stream().toList(), pruefung);
        updateAnalyseScheduled(analyseKriterienToPruefung, pruefung);
        return getConflictedPruefungen(pruefung);
    }

    public List<Pruefung> unschedulePruefung(Pruefung pruefung){
        pruefung.setStartzeitpunkt(null);
        List<Pruefung> pruefungen = getConflictedPruefungen(pruefung);
        updateAnalyseUnscheduled(pruefung);
        return pruefungen;
    }

    private List<Pruefung> getConflictedPruefungen(Pruefung pruefung) {
        return analysen.get(pruefung).entrySet().stream().flatMap(x -> x.getValue().stream().distinct()).distinct().collect(Collectors.toList());
    }

    private void updateAnalyseScheduled(Map<WeichesKriterium, Set<Pruefung>> scheduledScores, Pruefung scheduledPruefung){
        for(WeichesKriterium kriterium : scheduledScores.keySet()){
            for(Pruefung pruefung : scheduledScores.get(kriterium)){
                analysen.get(pruefung).get(kriterium).add(scheduledPruefung);
            }
        }
        this.analysen.put(scheduledPruefung, scheduledScores);
    }

    private void updateAnalyseUnscheduled(Pruefung unscheduledPruefunug){
        this.analysen = analysen.entrySet().stream().collect(Collectors.groupingBy(x -> x.getKey(),
                Collectors.flatMapping(x ->  x.getValue().entrySet().stream(), Collectors.groupingBy(x -> x.getKey(), Collectors.flatMapping(y -> y.getValue().stream().filter( x -> !x.equals(unscheduledPruefunug)), Collectors.toSet())))));
        this.analysen.remove(unscheduledPruefunug);
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