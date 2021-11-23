package de.fhwedel.klausps.dto;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Readonly DTO Pruefungen, die der View zur Verfügung gestellt werden.
 * @author NoNameNeeded
 */
public class DTOPruefung implements ReadOnlyPruefung {
    /**
     * @return die Prüfungsnummer
     */
    @Override
    public String getPruefungsnummer() {
        return null;
    }

    /**
     * @return den Prüfungsnamen
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * @return true wenn diese Prüfung bereits geplant wurde
     */
    @Override
    public boolean geplant() {
        return false;
    }

    /**
     * @return true wenn diese Prüfung bisher nicht geplant wurde
     */
    @Override
    public boolean ungeplant() {
        return false;
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
        return Optional.empty();
    }

    /**
     * @return die Prüfungsdauer
     */
    @Override
    public Duration getDauer() {
        return null;
    }

    /**
     * @return geschätzte Anzahl der teilnehmenden Studenten
     */
    @Override
    public int getSchaetzung() {
        return 0;
    }

    /**
     * @return Menge der Prüferkürzel
     */
    @Override
    public Set<String> getPruefer() {
        return null;
    }

    /**
     * @return Teilnehmerkreise
     */
    @Override
    public Set<Teilnehmerkreis> getTeilnehmerkreise() {
        return null;
    }

    /**
     * @return true wenn es sich bei der Planungseinheit um einen Block handelt
     */
    @Override
    public boolean isBlock() {
        return false;
    }
}
