package de.fhwedel.klausps.controller.api.visitor;
public enum WeichesKriteriumVisitors {
    MEHRERE_PRUEFUNG_AM_TAG(new MehrereKlausurenAmTag());

    public final WeichesKriteriumVisitor visitor;

    WeichesKriteriumVisitors(WeichesKriteriumVisitor visitor) {
        this.visitor = visitor;
    }

    public WeichesKriteriumVisitor getWeichesKriteriumVisitor(){
        return this.visitor;
    }
}
