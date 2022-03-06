package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
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
   * The default amount on which to increase the scoring.
   */
  private static final int DEFAULT_SCORING_STEP_SIZE = 2;

  /**
   * The amount on which to increase the scoring. This means, that the scoring increases for every
   * multiple of this value that the maximal amount of students at a time passes
   */
  private final int scoringStepSize;


  /**
   * Instantiate a AnzahlTeilnehmerGleichzeitigRestriction.
   *
   * @param dataAccessService The service to use for data access.
   * @param buffer            The time buffer between {@link Planungseinheit Planungseinheit}.
   * @param scoreStepSize     The amount on which to increase the scoring.
   */
  public AnzahlTeilnehmerGleichzeitigRestriction(DataAccessService dataAccessService,
      Duration buffer, int scoreStepSize) {
    super(dataAccessService, ANZAHL_TEILNEHMER_GLEICHZEITIG_ZU_HOCH, buffer);
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
        DEFAULT_SCORING_STEP_SIZE);
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
  protected boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten)
      throws NoPruefungsPeriodeDefinedException {
    int amountStudents = 0;
    for (Planungseinheit planungseinheit : planungseinheiten) {
      amountStudents += planungseinheit.schaetzung();
    }
    return amountStudents > dataAccessService.getPeriodenKapazitaet();
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
  protected int calcScoringFor(Collection<Planungseinheit> violatingPlanungseinheiten)
      throws NoPruefungsPeriodeDefinedException {
    int students = getAmountOfAttendingStudents(violatingPlanungseinheiten);
    return getScoringFactor(students) * this.kriterium.getWert();
  }

  /**
   * Get the factor on how strongly the restriction is violated.
   *
   * @param students The amount of students at the critical point in time.
   * @return The weight factor for the violation of this restriction.
   */
  private int getScoringFactor(int students) throws NoPruefungsPeriodeDefinedException {
    return ((getAmountOfStudentsSurpassingLimit(students) / scoringStepSize) + 1);
  }

  /**
   * Get the amount of students by which the maximal amount of simultaneous students is surpassed.
   *
   * @param totalStudents The total amount of students from which to calculate.
   * @return The amount of students surpassing the limit.
   */
  private int getAmountOfStudentsSurpassingLimit(int totalStudents)
      throws NoPruefungsPeriodeDefinedException {
    return totalStudents - dataAccessService.getPeriodenKapazitaet();
  }

}
