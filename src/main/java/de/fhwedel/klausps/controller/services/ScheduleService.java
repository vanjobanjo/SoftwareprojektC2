package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class ScheduleService {

    /**
     * Plant eine uebergebene Pruefung ein! Die uebergebene Pruefung muss Teil des Rueckgabewertes sein.
     * @param pruefung - Pruefung die zu planen ist.
     * @param termin - Starttermin
     * @return Liste von veränderten Ergebnissen
     */
    public abstract List<Pruefung> schedulePruefung(Pruefung pruefung, LocalDateTime termin) throws HartesKriteriumException;

    /**
     * Nimmt eine uebergebene Pruefung aus der Planung.
     * Übergebene Pruefung muss Teil des Rückgabewertes sein.
     * @param pruefung Pruefung zum ausplanen
     * @return Liste von veraenderte Pruefungen
     */
    public abstract List<Pruefung> unschedulePruefung(Pruefung pruefung);

    /**
     * Ändert die Dauer einer übergebenen Prüfung. Die übergebene Prüfung muss beim erfolgreichen
     * Verändern auch Teil der Rückgabe sein.
     * @param pruefung - Pruefung, dessen Dauer geändert werden muss.
     * @param minutes - die naue Dauer
     * @return Liste von Pruefung, jene die sich durch die Operation geändert haben.
     */
    public abstract List<Pruefung> changeDuration(Pruefung pruefung, Duration minutes) throws HartesKriteriumException;

    /**
     * Gibt das Scoring zu einer übergebenen Pruefung zurück. Wenn Klausur ungeplant, dann 0.
     * @param pruefung . Pruefung, dessen Scoring bestimmt werden soll
     * @return Scoring : ungeplant ? 0 : scoring
     */
    public abstract int scoringOfPruefung(Pruefung pruefung);
}
