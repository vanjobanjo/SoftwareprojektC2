package de.fhwedel.klausps.controller.api.scoring.bipred;

public enum BiPredKriterien {
    MEHRERE_PRUEFUNG_AM_TAG(new MehrereKlausurenAmTag());

    public final WeichesKriteriumBiPred visitor;

    BiPredKriterien(WeichesKriteriumBiPred visitor) {
        this.visitor = visitor;
    }

    public WeichesKriteriumBiPred getWeichesKriteriumVisitor(){
        return this.visitor;
    }
}
