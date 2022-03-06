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

/**
 * A Restriction describing that the amount of participants to {@link Pruefung Pruefungen} at the
 * same time must not exceed s threshold.
 */
public class AnzahlTeilnehmerGleichzeitigRestriction extends AtSameTimeRestriction {

  /**
   * The default maximal amount of students that should be in exams at the same time.
   */
  private static final int DEFAULT_MAX_TEILNEHMER_AT_A_TIME = 200;

  /**
   * The default amount on which to increase the scoring.
   */
  private static final int DEFAULT_SCORING_STEP_SIZE = 2;

  /**
   * The maximal amount of students that should be in exams at the same time.
   */
  private final int maxTeilnehmer;

  /**
   * The amount on which to increase the scoring. This means, that the scoring increases for every
   * multiple of this value that the maximal amount of students at a time passes
   */
  private final int scoringStepSize;

  /**
   * Instantiate a AnzahlTeilnehmerGleichzeitigRestriction.
   *
   * @param dataAccessService       The service to use for data access.
   * @param buffer                  The time buffer between {@link Planungseinheit
   *                                Planungseinheit}.
   * @param maxTeilnehmerAtSameTime The maximal amount of students that should be in exams at the
   *                                same time.
   */
  public AnzahlTeilnehmerGleichzeitigRestriction(DataAccessService dataAccessService,
      Duration buffer, int maxTeilnehmerAtSameTime) {
    this(dataAccessService, buffer, maxTeilnehmerAtSameTime, DEFAULT_SCORING_STEP_SIZE);
  }

  /**
   * Instantiate a AnzahlTeilnehmerGleichzeitigRestriction.
   *
   * @param dataAccessService       The service to use for data access.
   * @param buffer                  The time buffer between {@link Planungseinheit Planungseinheit}.
   * @param maxTeilnehmerAtSameTime The maximal amount of students that should be in exams at the
   *                                same time.
   * @param scoreStepSize           The amount on which to increase the scoring.
   */
  public AnzahlTeilnehmerGleichzeitigRestriction(DataAccessService dataAccessService,
      Duration buffer, int maxTeilnehmerAtSameTime, int scoreStepSize) {
    super(dataAccessService, ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH, buffer);
    this.maxTeilnehmer = maxTeilnehmerAtSameTime;
    if (scoreStepSize <= 0) {
      throw new IllegalArgumentException("Scoring step size must be positive!");
    }
    this.scoringStepSize = scoreStepSize;
  }

  /**
   * Instantiate a AnzahlTeilnehmerGleichzeitigRestriction. Uses the {@link DataAccessService} from
   * the {@link ServiceProvider} and the default values.
   */
  public AnzahlTeilnehmerGleichzeitigRestriction() {
    this(ServiceProvider.getDataAccessService(), DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN,
        DEFAULT_MAX_TEILNEHMER_AT_A_TIME, DEFAULT_SCORING_STEP_SIZE);
  }

  @Override
  protected void ignorePruefungenOf(@NotNull Set<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor) {
    /*
     * For counting the amount of participants at the same time it is crucial to count all
     * participants in the same block as well, therefore it is important that no pruefungen are ignored.
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
  protected int getAmountOfAttendingStudents(Collection<Planungseinheit> planungseinheiten) {
    int amount = 0;
    for (Planungseinheit planungseinheit : planungseinheiten) {
      amount += planungseinheit.schaetzung();
    }
    return amount;
  }

  @Override
  protected int calcScoringFor(Collection<Planungseinheit> violatingPlanungseinheiten) {
    int students = getAmountOfAttendingStudents(violatingPlanungseinheiten);
    return getScoringFactor(students) * this.kriterium.getWert();
  }

  /**
   * Get the factor on how strongly the restriction is violated.
   *
   * @param students The amount of students at the critical point in time.
   * @return The weight factor for the violation of this restriction.
   */
  private int getScoringFactor(int students) {
    return ((getAmountOfStudentsSurpassingLimit(students) / scoringStepSize) + 1);
  }

  /**
   * Get the amount of students by which the maximal amount of simultaneous students is surpassed.
   *
   * @param totalStudents The total amount of students from which to calculate.
   * @return The amount of students surpassing the limit.
   */
  private int getAmountOfStudentsSurpassingLimit(int totalStudents) {
    return totalStudents - maxTeilnehmer;
  }

}
