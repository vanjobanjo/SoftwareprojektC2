package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * A Restriction describing that the amount of {@link Pruefung}en at the same time must not exceed s
 * threshold.
 */
public class AnzahlPruefungenGleichzeitigRestriction extends AtSameTimeRestriction {

  /**
   * By default the maximal amount of Planungseinheiten that should be planned at the same time.
   */
  protected static final int DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME = 6;

  /**
   * The actual maximal amount of Planungseinheiten that should be planned at the same time
   */
  private final int maxPruefungenAtATime;

  /**
   * Instantiates a new AnzahlPruefungenGleichzeitigRestriction using the {@link DataAccessService}
   * from the {@link ServiceProvider}.
   */
  public AnzahlPruefungenGleichzeitigRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  /**
   * Instantiates a new AnzahlPruefungenGleichzeitigRestriction.
   *
   * @param dataAccessService The {@link DataAccessService} to use for accessing the data model.
   */
  protected AnzahlPruefungenGleichzeitigRestriction(@NotNull DataAccessService dataAccessService) {
    this(dataAccessService, DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME,
        DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN);
  }

  /**
   * Instantiates a new AnzahlPruefungenGleichzeitigRestriction.
   *
   * @param dataAccessService    The {@link DataAccessService} to use for accessing the data model.
   * @param maxPruefungenAtATime The maximal amount of simultaneous Planungseinheiten that should
   *                             exist.
   * @param buffer               The buffer to set between Planungseinheiten.
   */
  protected AnzahlPruefungenGleichzeitigRestriction(@NotNull DataAccessService dataAccessService,
      int maxPruefungenAtATime, @NotNull Duration buffer) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH, buffer);
    this.maxPruefungenAtATime = maxPruefungenAtATime;
  }

  /**
   * Instantiates a new AnzahlPruefungenGleichzeitigRestriction.
   *
   * @param dataAccessService    The {@link DataAccessService} to use for accessing the data model.
   * @param maxPruefungenAtATime The maximal amount of simultaneous Planungseinheiten that should
   *                             exist.
   */
  protected AnzahlPruefungenGleichzeitigRestriction(@NotNull DataAccessService dataAccessService,
      int maxPruefungenAtATime) {
    this(dataAccessService, maxPruefungenAtATime, DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN);
  }

  @Override
  protected void ignorePruefungenOf(@NotNull Set<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor) throws NoPruefungsPeriodeDefinedException {
    Optional<Block> block = dataAccessService.getBlockTo(toFilterFor);
    if (block.isPresent()) {
      planungseinheiten.removeAll(block.get().getPruefungen());
      planungseinheiten.remove(block.get());
      planungseinheiten.add(toFilterFor);
    }
  }

  @Override
  protected boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten) {
    return planungseinheiten.size() > maxPruefungenAtATime;
  }

  @Override
  @NotNull
  protected Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten) {
    Set<Teilnehmerkreis> teilnehmerkreise = new HashSet<>();
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      teilnehmerkreise.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return teilnehmerkreise;
  }

  @Override
  protected int getAmountOfAttendingStudents(Collection<Planungseinheit> planungseinheiten) {
    HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis = new HashMap<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      collectMaxAmountOfStudentsInFor(maxTeilnehmerPerTeilnehmerkreis, planungseinheit);
    }
    return getSumm(maxTeilnehmerPerTeilnehmerkreis.values());
  }

  @Override
  protected int calcScoringFor(Collection<Planungseinheit> violatingPlanungseinheiten) {
    int scoring = violatingPlanungseinheiten.size() - maxPruefungenAtATime;
    scoring *= this.kriterium.getWert();
    return Math.max(scoring, 0);
  }

  /**
   * Collect the maximal amount of students affected by each {@link Teilnehmerkreis} in a provided
   * Map.
   *
   * @param maxTeilnehmerPerTeilnehmerkreis A collection describing the maximal amount of affected
   *                                        students to each Teilnehmerkreis.
   * @param planungseinheit                 A Planungseinheit whose Planungseinheiten to add to the
   *                                        Map.
   */
  private void collectMaxAmountOfStudentsInFor(
      HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis,
      Planungseinheit planungseinheit) {
    for (Map.Entry<Teilnehmerkreis, Integer> entry : planungseinheit.getSchaetzungen().entrySet()) {
      if (isNotContainedWithHigherValueIn(maxTeilnehmerPerTeilnehmerkreis, entry)) {
        maxTeilnehmerPerTeilnehmerkreis.put(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Summ up Integers.
   *
   * @param values The integers to summ.
   * @return The summ of the passed values.
   */
  private int getSumm(@NotNull Iterable<Integer> values) {
    int result = 0;
    for (Integer value : values) {
      result += value;
    }
    return result;
  }

  /**
   * Check whether there is an entry for a {@link Teilnehmerkreis} with a higher value than a
   * provided one in a provided map.
   *
   * @param maxTeilnehmerPerTeilnehmerkreis The currently known maximal amounts of students affected
   *                                        for each Teilnehmerkreis.
   * @param entry                           A map entry consisting of a Teilnehmerkreis and an
   *                                        amount of students belonging to it.
   * @return True in case there is no entry with a higher value than the new one, otherwise false.
   */
  private boolean isNotContainedWithHigherValueIn(
      HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis,
      Entry<Teilnehmerkreis, Integer> entry) {
    return !maxTeilnehmerPerTeilnehmerkreis.containsKey(entry.getKey())
        || maxTeilnehmerPerTeilnehmerkreis.get(entry.getKey()) <= entry.getValue();
  }
}
