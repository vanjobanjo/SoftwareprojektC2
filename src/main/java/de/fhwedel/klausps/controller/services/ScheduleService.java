package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleService {

    /**
     * Plant eine uebergebene Pruefung ein! Die uebergebene Pruefung muss Teil des Rueckgabewertes sein.
     * @param pruefung - Pruefung die zu planen ist.
     * @param termin - Starttermin
     * @return Liste von veränderten Ergebnissen
     */
    public List<Pruefung> schedulePruefung(Pruefung pruefung, LocalDateTime termin) throws HartesKriteriumException {
      // todo please implement
      throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Nimmt eine uebergebene Pruefung aus der Planung.
     * Übergebene Pruefung muss Teil des Rückgabewertes sein.
     * @param pruefung Pruefung zum ausplanen
     * @return Liste von veraenderte Pruefungen
     */
    public List<Pruefung> unschedulePruefung(Pruefung pruefung) {
      // todo please implement
      throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Ändert die Dauer einer übergebenen Prüfung. Die übergebene Prüfung muss beim erfolgreichen
     * Verändern auch Teil der Rückgabe sein.
     * @param pruefung - Pruefung, dessen Dauer geändert werden muss.
     * @param minutes - die naue Dauer
     * @return Liste von Pruefung, jene die sich durch die Operation geändert haben.
     */
    public List<Pruefung> changeDuration(Pruefung pruefung, Duration minutes) throws HartesKriteriumException {
      // todo please implement
      throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Gibt das Scoring zu einer übergebenen Pruefung zurück. Wenn Klausur ungeplant, dann 0.
     * @param pruefung . Pruefung, dessen Scoring bestimmt werden soll
     * @return Scoring : ungeplant ? 0 : scoring
     */
    public int scoringOfPruefung(Pruefung pruefung) {
      // todo please implement
      throw new UnsupportedOperationException("not implemented");
    }
}
