package de.fhwedel.klausps.dto;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Readonly DTO Pruefungen, die der View zur Verfügung gestellt werden.
 * TODO: Ist diese Klasse veraenderbar? Immutable?
 * @author NoNameNeeded
 */
public class DTOPruefung implements ReadOnlyPruefung{

    //TODO falls Immutable dann final.
    private final String pruefungsNummer;
    private final String pruefungsName;
    private final Map<Teilnehmerkreis, Integer> teilnehmerKreisSchaetzung;
    private final Duration dauer;
    private final LocalDateTime startZeitpunkt;
    private final int scoring;
    private final Set<String> pruefer;


    /**
     * Konstruktor fuer bereits geplante Pruefungen.
     * @param pruefungsNummer - Nummer der Pruefungen
     * @param pruefungsName - Name der Pruefungen
     * @param teilnehmerKreisSchaetzung - Teilnehmerkreis Schaetzungen
     * @param dauer - Dauer
     * @param startZeitpunkt - Zeitpunkt des Startes
     * @param scoring - Scoring
     * @param pruefer - Die Pruefer
     */
     public DTOPruefung(String pruefungsNummer,
                        String pruefungsName,
                        Map<Teilnehmerkreis, Integer> teilnehmerKreisSchaetzung,
                        Duration dauer,
                        LocalDateTime startZeitpunkt,
                        int scoring,
                        Set<String> pruefer) {
        this.pruefungsNummer = pruefungsNummer;
        this.pruefungsName = pruefungsName;
        this.teilnehmerKreisSchaetzung = teilnehmerKreisSchaetzung;
        this.dauer = dauer;
        this.startZeitpunkt = startZeitpunkt;
        this.scoring = scoring;
        this.pruefer = pruefer;
     }

    /**
     * Konstruktor fuer neue ungeplante Pruefungen.
     * Ohne Startzeitpunkt.
     * @param pruefungsNummer - Nummer der Pruefung
     * @param pruefungsName - Name der Pruefung
     * @param teilnehmerKreisSchaetzung - Teilnehmerkreis Schaetzungen
     * @param dauer - Dauer default ist Mindestdauer der Pruefung
     * @param pruefer - Pruefer evt. leer
     */
    public DTOPruefung(String pruefungsNummer,
                       String pruefungsName,
                       Map<Teilnehmerkreis, Integer> teilnehmerKreisSchaetzung,
                       Duration dauer, Set<String> pruefer) {
        this.pruefungsNummer = pruefungsNummer;
        this.pruefungsName = pruefungsName;
        this.teilnehmerKreisSchaetzung = teilnehmerKreisSchaetzung;
        this.dauer = dauer;
        this.startZeitpunkt = null;
        this.scoring = 0; // zu Beginn ist das Scoring 0
        this.pruefer = pruefer;
    }

    /**
     * @return die Prüfungsnummer
     */
    @Override
    public String getPruefungsnummer() {
        return pruefungsNummer;
    }

    /**
     * @return den Prüfungsnamen
     */
    @Override
    public String getName() {
        return pruefungsName;
    }

    /**
     * @return true wenn diese Prüfung bereits geplant wurde
     */
    @Override
    public boolean geplant() {
        return startZeitpunkt != null;
    }

    /**
     * @return true wenn diese Prüfung bisher nicht geplant wurde
     */
    @Override
    public boolean ungeplant() {
        return startZeitpunkt == null;
    }

    /**
     * @return gibt den Block der Prüfung zurück, Optional.Empty wenn die Prüfung nicht in einem
     * Block ist
     */
    @Override
    public Optional<ReadOnlyBlock> getBlock() {
        return Optional.empty();
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
        return teilnehmerKreisSchaetzung.values()
                .stream().mapToInt(value -> value)
                .reduce(0, Integer :: sum);
    }

    /**
     * @return Map mit Teilnehmerkreis und jeweilige Schaetzung
     */
    @Override
    public Map<Teilnehmerkreis, Integer> getTeilnehmerKreisSchaetzung() {
        return teilnehmerKreisSchaetzung;
    }

    /**
     * @return Menge der Prüferkürzel
     */
    @Override
    public Set<String> getPruefer() {
        return pruefer;
    }

    /**
     * @return Teilnehmerkreise
     */
    @Override
    public Set<Teilnehmerkreis> getTeilnehmerkreise() {
        return teilnehmerKreisSchaetzung.keySet();
    }

    /**
     * @return true wenn es sich bei der Planungseinheit um einen Block handelt
     */
    @Override
    public boolean isBlock() {
        return false;
    }
}
