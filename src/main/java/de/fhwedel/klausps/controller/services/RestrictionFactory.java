package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.restriction.hard.HardRestriction;
import de.fhwedel.klausps.controller.restriction.hard.ZweiPruefungenGleichzeitigRestriction;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungProWocheRestriction;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlPruefungenGleichzeitigRestriction;
import de.fhwedel.klausps.controller.restriction.soft.AnzahlTeilnehmerGleichzeitigRestriction;
import de.fhwedel.klausps.controller.restriction.soft.FreierTagZwischenPruefungenRestriction;
import de.fhwedel.klausps.controller.restriction.soft.KeinePruefungAmSonntagRestriction;
import de.fhwedel.klausps.controller.restriction.soft.MehrePruefungenAmTagRestriction;
import de.fhwedel.klausps.controller.restriction.soft.PruefungenMitVielenAmAnfangRestriction;
import de.fhwedel.klausps.controller.restriction.soft.SoftRestriction;
import de.fhwedel.klausps.controller.restriction.soft.UniformeZeitslotsRestriction;
import de.fhwedel.klausps.controller.restriction.soft.WocheVierFuerMasterRestriction;
import java.util.HashSet;
import java.util.Set;

/**
 * The RestrictionFactory is used to create all {@link SoftRestriction soft} and {@link
 * HardRestriction hard} restrictions.<br> Any new restriction must be added here to be considered
 * at runtime. Furthermore, the RestrictionFactory registers the created restrictions in the {@link
 * RestrictionService} where they get called to be evaluated.<br> The Factory ensures that there
 * will only be one instance of every Restriction used by the RestrictionService at runtime.
 */
public class RestrictionFactory {

  /**
   * Set of all used hard restrictions
   */
  private static final Set<HardRestriction> hardRestrictions = new HashSet<>();

  /**
   * Set of all used soft restrictions
   */
  private static final Set<SoftRestriction> softRestrictions = new HashSet<>();

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
          new AnzahlPruefungProWocheRestriction(),
          new AnzahlPruefungenGleichzeitigRestriction(),
          new AnzahlTeilnehmerGleichzeitigRestriction(),
          new FreierTagZwischenPruefungenRestriction(),
          new KeinePruefungAmSonntagRestriction(),
          new UniformeZeitslotsRestriction(),
          new MehrePruefungenAmTagRestriction(),
          new WocheVierFuerMasterRestriction(),
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
      hardRestrictions.addAll(Set.of(new ZweiPruefungenGleichzeitigRestriction()));
    }
    service.registerHardCriteria(hardRestrictions);
  }

}
