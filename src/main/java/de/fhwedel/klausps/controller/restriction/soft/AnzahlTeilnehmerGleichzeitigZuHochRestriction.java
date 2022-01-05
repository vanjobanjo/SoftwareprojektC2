package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AnzahlTeilnehmerGleichzeitigZuHochRestriction extends AtSameTimeRestriction {

  private static final int DEFAULT_MAX_TEILNEHMER_AT_A_TIME = 200;

  private final int maxTeilnehmer;

  protected AnzahlTeilnehmerGleichzeitigZuHochRestriction(DataAccessService dataAccessService,
      Duration puffer) {
    this(dataAccessService, puffer, DEFAULT_MAX_TEILNEHMER_AT_A_TIME);
  }

  public AnzahlTeilnehmerGleichzeitigZuHochRestriction(DataAccessService dataAccessService,
      Duration duration, int maxTeilnehmerAtSameTime) {
    super(dataAccessService, ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH, duration);
    this.maxTeilnehmer = maxTeilnehmerAtSameTime;
  }

  public AnzahlTeilnehmerGleichzeitigZuHochRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  public AnzahlTeilnehmerGleichzeitigZuHochRestriction(DataAccessService dataAccessService) {
    this(dataAccessService, DEFAULT_BUFFER, DEFAULT_MAX_TEILNEHMER_AT_A_TIME);
  }

  @Override
  protected void ignorePruefungenOf(@NotNull List<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor) {
    /*
     * For counting the amount of participants at the same time it is crucial to count all
     * participants in the same block as well
     */
  }

  @Override
  protected boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten) {
    int amountStudents = 0;
    for (Planungseinheit planungseinheit : planungseinheiten) {
      amountStudents += planungseinheit.schaetzung();
    }
    return amountStudents > maxTeilnehmer;
  }

  @Override
  protected Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten) {
    Set<Teilnehmerkreis> result = new HashSet<>();
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      result.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return result;
  }

  @Override
  protected int getAffectedStudentsFrom(Set<Planungseinheit> violatingPlanungseinheiten) {
    int amount = 0;
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      amount += planungseinheit.schaetzung();
    }
    return amount;
  }

  @Override
  protected int calcScoringFor(Set<Planungseinheit> violatingPlanungseinheiten) {
    return 0;
  }

  @Override
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    throw new UnsupportedOperationException("not implemented");
  }
}
