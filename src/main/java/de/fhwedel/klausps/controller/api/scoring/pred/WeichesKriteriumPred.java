package de.fhwedel.klausps.controller.api.scoring.pred;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

import java.util.function.Predicate;

public abstract class WeichesKriteriumPred implements Predicate<Pruefung> {

    protected final WeichesKriterium kriterium;
    protected WeichesKriteriumPred(WeichesKriterium kriterium) {
        this.kriterium = kriterium;
    }
    public WeichesKriterium getWeichesKriterium(){
        return kriterium;
    }


    @Override
    public Predicate<Pruefung> and(Predicate<? super Pruefung> other) {
        return Predicate.super.and(other);
    }

    @Override
    public Predicate<Pruefung> negate() {
        return Predicate.super.negate();
    }

    @Override
    public Predicate<Pruefung> or(Predicate<? super Pruefung> other) {
        return Predicate.super.or(other);
    }
}
