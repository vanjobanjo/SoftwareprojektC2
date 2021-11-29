package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitor;
import de.fhwedel.klausps.controller.api.visitor.WeichesKriteriumVisitors;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleService {
    private Map<Pruefung, Integer> scorings;
    private Map<Pruefung, Map<WeichesKriterium, Set<Pruefung>>> analysen;

    public ScheduleService(Set<Pruefung> geplantePruefungen) {
        this.scorings = geplantePruefungen.stream().collect(Collectors.toMap(x -> x, y -> 1));
        this.analysen = new HashMap<>();

        List<Pruefung> leftPruefung = geplantePruefungen.stream().toList();

        while (!leftPruefung.isEmpty()) {
            Pruefung toCheck = leftPruefung.remove(0);

            WeichesKriteriumVisitor visitor = WeichesKriteriumVisitors.MEHRERE_PRUEFUNG_AM_TAG.getWeichesKriteriumVisitor();

            Map<WeichesKriterium, Set<Pruefung>> analyse = leftPruefung
                    .stream()
                    .filter(x -> visitor.test(x, toCheck))
                    .collect(Collectors.groupingBy(x -> visitor.getWeichesKriterium(), Collectors.toSet()));

            analysen.put(toCheck, analyse);

            Map<Pruefung, Integer> updatedScores = leftPruefung
                    .stream()
                    .filter(x -> visitor.test(x, toCheck))
                    .collect(Collectors.toMap(x -> x, y -> 0)); //HIER SCORING VOM WEICHEN KRITERIUM HOLEN

            scorings = Stream.concat(scorings.entrySet().stream(), updatedScores.entrySet()
                    .stream())
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry :: getValue)));

        }
    }

    List<ReadOnlyPruefung> reducedScoring(Set<Pruefung> geplantePruefungen, Pruefung ungeplant) {

        return null; //TODO vernünftiges Scoring machen!
    }
}
