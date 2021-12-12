package de.fhwedel.klausps.controller.api.scoring.pred;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Pruefung;

import java.time.LocalDateTime;
import java.util.Optional;

public class WocheMasterPred extends WeichesKriteriumPred{
    private final LocalDateTime startMaster;
    public WocheMasterPred(LocalDateTime startOfMaster){
        super(WeichesKriterium.WOCHE_VIER_FUER_MASTER);
        startMaster = startOfMaster;
    }

    @Override
    public boolean test(Pruefung pruefung) {
        Optional<LocalDateTime> termin = Optional.ofNullable(pruefung.getStartzeitpunkt());
        return termin.isPresent() && termin.get().isAfter(startMaster) && pruefung.getAusbildungsgrade().stream().anyMatch(x -> x.equals(Ausbildungsgrad.MASTER));
    }
}
