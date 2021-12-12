package de.fhwedel.klausps.controller.api.scoring.pred;

public enum PredKriterien {
    PRUEFUNG_AM_SONNTAG(new SonntagPred());

    PredKriterien(WeichesKriteriumPred kriterium) {
        this.predicate = kriterium;
    }
    
    private final WeichesKriteriumPred predicate;

    public WeichesKriteriumPred getPredicate(){
        return predicate;
    }
}
