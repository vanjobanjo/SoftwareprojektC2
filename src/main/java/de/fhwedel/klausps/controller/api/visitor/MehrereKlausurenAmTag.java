package de.fhwedel.klausps.controller.api.visitor;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

public class MehrereKlausurenAmTag extends WeichesKriteriumVisitor {

    public MehrereKlausurenAmTag() {
        super(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
    }

    @Override
    public boolean test(Pruefung pruefung1, Pruefung pruefung2) {
        int year1 = pruefung1.getStartzeitpunkt().getYear();
        int year2 = pruefung2.getStartzeitpunkt().getYear();
        int day1 = pruefung1.getStartzeitpunkt().getDayOfYear();
        int day2 = pruefung2.getStartzeitpunkt().getDayOfYear();
        return year1 == year2 && day1 == day2;
    }
}
