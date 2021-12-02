package de.fhwedel.klausps.controller.api.visitor;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;

import java.util.function.BiPredicate;

public abstract class WeichesKriteriumVisitor implements BiPredicate<Pruefung, Pruefung>{
    protected final WeichesKriterium kriterium;

    protected WeichesKriteriumVisitor(WeichesKriterium kriterium) {
        this.kriterium = kriterium;
    }

    public WeichesKriterium getWeichesKriterium(){
        return kriterium;
    }

    @Override
    public BiPredicate<Pruefung, Pruefung> and(BiPredicate<? super Pruefung, ? super Pruefung> other) {
        return BiPredicate.super.and(other);
    }

    @Override
    public BiPredicate<Pruefung, Pruefung> negate() {
        return BiPredicate.super.negate();
    }

    @Override
    public BiPredicate<Pruefung, Pruefung> or(BiPredicate<? super Pruefung, ? super Pruefung> other) {
        return BiPredicate.super.or(other);
    }
}
