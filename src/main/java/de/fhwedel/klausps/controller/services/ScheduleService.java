package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Pruefung;

import java.util.List;
import java.util.Set;

public class ScheduleService {

    List<ReadOnlyPruefung> reducedScoring(Set<Pruefung> geplantePruefungen, Pruefung ungeplant){

        return null; //TODO vernünftiges Scoring machen!
    }
}
