package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class ScheduleService {

    /**
     * Plant eine übergebene Pruefung ein! Die uebergebene Pruefung muss Teil des Rueckgabewertes sein.
     * @param pruefung - Pruefung die zu planen ist.
     * @param termin - Starttermin
     * @return Liste von veränderten Ergebnissen
     */
    public abstract List<Pruefung> schedulePruefung(Pruefung pruefung, LocalDateTime termin) throws HartesKriteriumException;

    /**
     * Ändert die Dauer einer übergebenen Prüfung. Die übergebene Prüfung muss beim erfolgreichen
     * Verändern auch Teil der Rückgabe sein.
     * @param pruefung - Pruefung, dessen Dauer geändert werden muss.
     * @param minutes - die naue Dauer
     * @return Liste von Pruefung, jene die sich durch die Operation geändert haben.
     */
    public abstract List<Pruefung> changeDuration(Pruefung pruefung, Duration minutes) throws HartesKriteriumException;

    /**
     * Gibt das Scoring zu einer übergebenen Pruefung zurück.
     * @param pruefung . Pruefung, dessen Scoring bestimmt werden soll
     * @return Scoring
     */
    public abstract int scoringOfPruefung(Pruefung pruefung);
}
