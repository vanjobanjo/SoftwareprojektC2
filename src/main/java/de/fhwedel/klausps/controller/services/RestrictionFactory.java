package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.restriction.hard.HarteRestriktion;
import de.fhwedel.klausps.controller.restriction.hard.TwoKlausurenSameTime;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungProWocheTeilnehmerkreis;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungenGleichzeitigRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlTeilnehmerGleichzeitigZuHochRestriction;
import de.fhwedel.klausps.controller.restriction.soft.FreierTagZwischenPruefungen;
import de.fhwedel.klausps.controller.restriction.soft.KeineKlausurAmSonntag;
import de.fhwedel.klausps.controller.restriction.soft.MehrePruefungenAmTag;
import de.fhwedel.klausps.controller.restriction.soft.UniformeZeitslots;
import de.fhwedel.klausps.controller.restriction.soft.WeicheRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.WocheVierFuerMaster;
import java.util.HashSet;
import java.util.Set;

public class RestrictionFactory {

  private static final Set<HarteRestriktion> hardRestrictions = new HashSet<>();

  private static final Set<WeicheRestriktion> softRestrictions = new HashSet<>();


  void createRestrictions(RestrictionService restrictionService) {
    registerHardCriteria(restrictionService);
    registerSoftCriteria(restrictionService);
  }

  private static void registerSoftCriteria(RestrictionService service) {
    if (softRestrictions.isEmpty()) {
      softRestrictions.addAll(Set.of(
          // new AnzahlPruefungProWoche(), deprecated.
          new AnzahlPruefungProWocheTeilnehmerkreis(),
          new AnzahlPruefungenGleichzeitigRestriktion(),
          new AnzahlTeilnehmerGleichzeitigZuHochRestriction(),
          new FreierTagZwischenPruefungen(),
          new KeineKlausurAmSonntag(),
          new UniformeZeitslots(),
          new MehrePruefungenAmTag(),
          new WocheVierFuerMaster()
          // todo register KlausurenMitVielenAmAnfang
      ));
    }
    service.registerSoftCriteria(softRestrictions);
  }


  private static void registerHardCriteria(RestrictionService service) {
    if (hardRestrictions.isEmpty()) {
      hardRestrictions.addAll(Set.of(new TwoKlausurenSameTime()));
    }
    service.registerHardCriteria(hardRestrictions);
  }

}
