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
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AnzahlTeilnehmerGleichzeitigRestriction extends AtSameTimeRestriction {

  private static final int DEFAULT_MAX_TEILNEHMER_AT_A_TIME = 200;

  private static final int DEFAULT_SCORING_STEP_SIZE = 2;

  private final int maxTeilnehmer;

  private final int scoringStepSize;

  public AnzahlTeilnehmerGleichzeitigRestriction(DataAccessService dataAccessService,
      Duration buffer, int maxTeilnehmerAtSameTime) {
    this(dataAccessService, buffer, maxTeilnehmerAtSameTime, DEFAULT_SCORING_STEP_SIZE);
  }

  public AnzahlTeilnehmerGleichzeitigRestriction(DataAccessService dataAccessService,
      Duration buffer, int maxTeilnehmerAtATime, int scoreStepSize) {
    super(dataAccessService, ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH, buffer);
    this.maxTeilnehmer = maxTeilnehmerAtATime;
    if (scoreStepSize <= 0) {
      throw new IllegalArgumentException("Scoring step size must be positive!");
    }
    this.scoringStepSize = scoreStepSize;
  }

  public AnzahlTeilnehmerGleichzeitigRestriction() {
    this(ServiceProvider.getDataAccessService(), DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN,
        DEFAULT_MAX_TEILNEHMER_AT_A_TIME, DEFAULT_SCORING_STEP_SIZE);
  }

  @Override
  protected void ignorePruefungenOf(@NotNull Set<Planungseinheit> planungseinheiten,
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
  @NotNull
  protected Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten) {
    Set<Teilnehmerkreis> result = new HashSet<>();
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      result.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return result;
  }


  @Override
  protected int getAffectedStudentsFrom(Collection<Planungseinheit> violatingPlanungseinheiten) {
    int amount = 0;
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      amount += planungseinheit.schaetzung();
    }
    return amount;
  }

  @Override
  protected int calcScoringFor(Collection<Planungseinheit> violatingPlanungseinheiten) {
    int students = getAffectedStudentsFrom(violatingPlanungseinheiten);
    return getScoringFactor(students) * this.kriterium.getWert();
  }

  private int getScoringFactor(int students) {
    return ((getAmountOfStudentsSurpassingLimit(students) / scoringStepSize) + 1);
  }

  private int getAmountOfStudentsSurpassingLimit(int totalStudents) {
    return totalStudents - maxTeilnehmer;
  }

}
