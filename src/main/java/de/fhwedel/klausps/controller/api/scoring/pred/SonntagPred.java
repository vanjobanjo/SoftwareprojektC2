package de.fhwedel.klausps.controller.api.scoring.pred;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;

public class SonntagPred extends WeichesKriteriumPred{

    protected SonntagPred() {
        super(WeichesKriterium.SONNTAG);
    }

    @Override
    public boolean test(Pruefung pruefung) {
        Optional<LocalDateTime> termin = Optional.ofNullable(pruefung.getStartzeitpunkt());
        return termin.isEmpty() || termin.get().getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
