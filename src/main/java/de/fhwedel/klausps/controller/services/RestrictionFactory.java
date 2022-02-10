package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.restriction.hard.HarteRestriktion;
import de.fhwedel.klausps.controller.restriction.hard.TwoKlausurenSameTime;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungProWocheTeilnehmerkreis;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungenGleichzeitigRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlTeilnehmerGleichzeitigZuHochRestriction;
import de.fhwedel.klausps.controller.restriction.soft.FreierTagZwischenPruefungen;
import de.fhwedel.klausps.controller.restriction.soft.KeineKlausurAmSonntag;
import de.fhwedel.klausps.controller.restriction.soft.MehrePruefungenAmTag;
import de.fhwedel.klausps.controller.restriction.soft.PruefungenMitVielenAmAnfangRestriction;
import de.fhwedel.klausps.controller.restriction.soft.UniformeZeitslots;
import de.fhwedel.klausps.controller.restriction.soft.WeicheRestriktion;
import de.fhwedel.klausps.controller.restriction.soft.WocheVierFuerMaster;
import java.util.HashSet;
import java.util.Set;

/**
 * The RestrictionFactory is used to create all {@link WeicheRestriktion soft} and {@link
 * HarteRestriktion hard} restrictions.<br> Any new restriction must be added here to be considered
 * at runtime. Furthermore, the RestrictionFactory registers the created restrictions in the {@link
 * RestrictionService} where they get called to be evaluated.<br> The Factory ensures that there
 * will only be one instance of every Restriction used by the RestrictionService at runtime.
 */
public class RestrictionFactory {

  /**
   * Set of all used hard restrictions
   */
  private static final Set<HarteRestriktion> hardRestrictions = new HashSet<>();

  /**
   * Set of all used soft restrictions
   */
  private static final Set<WeicheRestriktion> softRestrictions = new HashSet<>();

  /**
   * creates all restrictions and registers them with the RestrictionService
   *
   * @param restrictionService the used RestrictionService
   */
  void createRestrictions(RestrictionService restrictionService) {
    registerHardCriteria(restrictionService);
    registerSoftCriteria(restrictionService);
  }

  /**
   * Creates all soft Restrictions. Any new soft Restriction must be added here to be considered at
   * runtime.
   *
   * @param service the RestrictionService
   */
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
          new WocheVierFuerMaster(),
          new PruefungenMitVielenAmAnfangRestriction()
      ));
    }
    service.registerSoftCriteria(softRestrictions);
  }

  /**
   * Creates all hard Restrictions. Any new hard Restriction must be added here to be considered at
   * runtime.
   *
   * @param service the RestrictionService
   */
  private static void registerHardCriteria(RestrictionService service) {
    if (hardRestrictions.isEmpty()) {
      hardRestrictions.addAll(Set.of(new TwoKlausurenSameTime()));
    }
    service.registerHardCriteria(hardRestrictions);
  }

}
