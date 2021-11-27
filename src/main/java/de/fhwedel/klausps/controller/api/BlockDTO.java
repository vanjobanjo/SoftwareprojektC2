package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BlockDTO implements ReadOnlyBlock {

    private final String name;
    private final List<PruefungDTO> pruefungen;
    private final LocalDateTime startZeitpunkt;
    private final Duration dauer;

    public BlockDTO(String name,
                    List<PruefungDTO> pruefungen,
                    LocalDateTime startZeitpunkt,
                    Duration dauer) {
        this.name = name;
        this.pruefungen = pruefungen;
        this.startZeitpunkt = startZeitpunkt;
        this.dauer = dauer;
    }

    /**
     * @return Prüfungen die in diesem Block enthalten sind
     */
    @Override
    public Set<ReadOnlyPruefung> getROPruefungen() {
        return new HashSet<>(pruefungen);
    }

    /**
     * @return Starttermin der Planungseinheit, null wenn noch nicht geplant
     */
    @Override
    public Optional<LocalDateTime> getTermin() {
        return Optional.ofNullable(startZeitpunkt);
    }

    /**
     * @return die Prüfungsdauer
     */
    @Override
    public Duration getDauer() {
        return dauer;
    }

    /**
     * @return geschätzte Anzahl der aller teilnehmenden Studenten
     */
    @Override
    public int getGesamtSchaetzung() {
        return 0; //TODO
    }

    /**
     * @return Map mit Teilnehmerkreis und jeweilige Schaetzung
     */
    @Override
    public Map<Teilnehmerkreis, Integer> getTeilnehmerKreisSchaetzung() {
        return new HashMap<>(); //TODO
    }

    /**
     * @return Menge der Prüferkürzel
     */
    @Override
    public Set<String> getPruefer() {
        return pruefungen
                .stream()
                .flatMap(pruefungen -> pruefungen.getPruefer().stream()).collect(Collectors.toSet());
    }

    /**
     * @return Teilnehmerkreise
     */
    @Override
    public Set<Teilnehmerkreis> getTeilnehmerkreise() {
        return pruefungen.stream().flatMap(
                pruefungen -> pruefungen.getTeilnehmerkreise().stream()).collect(Collectors.toSet());
    }

    /**
     * @return true wenn es sich bei der Planungseinheit um einen Block handelt
     */
    @Override
    public boolean isBlock() {
        return true;
    }
}
