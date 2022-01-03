package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;

public class AnzahlTeilnehmerGleichzeitigZuHochRestriction extends AtSameTimeRestriction {

  protected AnzahlTeilnehmerGleichzeitigZuHochRestriction(
      DataAccessService dataAccessService, Duration puffer) {
    super(dataAccessService, WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH, puffer);
  }

  public AnzahlTeilnehmerGleichzeitigZuHochRestriction(DataAccessService dataAccessService) {
    this(dataAccessService, DEFAULT_BUFFER);
  }

  @Override
  protected boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten) {
    return false;
  }

  @Override
  protected Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten) {
    return null;
  }

  @Override
  protected int getAffectedStudentsFrom(Set<Planungseinheit> violatingPlanungseinheiten) {
    return 0;
  }

  @Override
  protected int calcScoringFor(Set<Planungseinheit> violatingPlanungseinheiten) {
    return 0;
  }
}
