package de.fhwedel.klausps.dto;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Readonly DTOPrüfungen, die der View zur Verfügung gestellt werden.
 *
 * @author NoNameNeeded
 */
public class DTOPruefung implements ReadOnlyPruefung {
    //TODO: Immutable derzeit.
    private final String pruefungsNummer;
    private final String pruefungsName;
    private final Map<Teilnehmerkreis, Integer> teilnehmerKreisSchaetzung;
    private final Duration dauer;
    private final Optional<LocalDateTime> startZeitpunkt;
    private final int scoring;
    private final Set<String> pruefer;

    private DTOPruefung(DTOPruefungBuilder dtoPruefungBuilder) {
        pruefungsNummer = dtoPruefungBuilder.pruefungsNummer;
        pruefungsName = dtoPruefungBuilder.pruefungsName;
        teilnehmerKreisSchaetzung = dtoPruefungBuilder.teilnehmerKreisSchaetzung;
        dauer = dtoPruefungBuilder.dauer;
        startZeitpunkt = dtoPruefungBuilder.startZeitpunkt;
        scoring = dtoPruefungBuilder.scoring;
        pruefer = dtoPruefungBuilder.pruefer;
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
        return startZeitpunkt.isPresent();
    }

    /**
     * @return true wenn diese Prüfung bisher nicht geplant wurde
     */
    @Override
    public boolean ungeplant() {
        return startZeitpunkt.isEmpty();
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
        return startZeitpunkt;
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
                .reduce(0, Integer::sum);
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
        return new HashSet<>(teilnehmerKreisSchaetzung.keySet());
    }

    /**
     * @return true wenn es sich bei der Planungseinheit um einen Block handelt
     */
    @Override
    public boolean isBlock() {
        return false;
    }

    /**
     * Builder Klasse, weil es zu viele unterschiedliche Konstruktor Varianten
     * geben würde.
     */
    public static class DTOPruefungBuilder {
        private String pruefungsNummer;
        private String pruefungsName;
        private Map<Teilnehmerkreis, Integer> teilnehmerKreisSchaetzung;
        private Duration dauer;
        private Optional<LocalDateTime> startZeitpunkt;
        private int scoring;
        private Set<String> pruefer;

        /**
         * Builder Konstruktor
         */
        public DTOPruefungBuilder() {
            pruefungsNummer = "";
            pruefungsName = "";
            teilnehmerKreisSchaetzung = new HashMap<>();
            dauer = Duration.ofMinutes(60); //TODO
            startZeitpunkt = Optional.empty();
            scoring = 0;
            pruefer = new HashSet<>();
        }

        /**
         * Copy Konstruktor, bei Bedarf kann man die Werte setzen.
         * @param pruefung - Zu kopierende DTOPruefung
         */
        public DTOPruefungBuilder(DTOPruefung pruefung) {
            pruefungsNummer = pruefung.pruefungsNummer;
            pruefungsName = pruefung.pruefungsName;
            teilnehmerKreisSchaetzung = pruefung.teilnehmerKreisSchaetzung;
            dauer = pruefung.dauer;
            startZeitpunkt = pruefung.startZeitpunkt;
            scoring = pruefung.scoring;
            pruefer = pruefung.pruefer;
        }

        public DTOPruefungBuilder setPruefungsNummer(String pruefungsNummer) {
            this.pruefungsNummer = pruefungsNummer;
            return this;
        }

        public DTOPruefungBuilder setPruefungsName(String pruefungsName) {
            this.pruefungsName = pruefungsName;
            return this;
        }

        public DTOPruefungBuilder setTeilnehmerKreisSchaetzung(Map<Teilnehmerkreis, Integer> teilnehmerKreisSchaetzung) {
            this.teilnehmerKreisSchaetzung = teilnehmerKreisSchaetzung;
            return this;
        }

        public DTOPruefungBuilder setDauer(Duration dauer) {
            this.dauer = dauer;
            return this;
        }

        public DTOPruefungBuilder setStartZeitpunkt(LocalDateTime startZeitpunkt) {
            this.startZeitpunkt = Optional.of(startZeitpunkt);
            return this;
        }

        public DTOPruefungBuilder setScoring(int scoring) {
            this.scoring = scoring;
            return this;
        }

        public DTOPruefungBuilder setPruefer(Set<String> pruefer) {
            this.pruefer = pruefer;
            return this;
        }

        public DTOPruefung build() {
            return new DTOPruefung(this);
        }
    }
}
