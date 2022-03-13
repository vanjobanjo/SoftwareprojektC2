package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.UNIFORME_ZEITSLOTS;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * This restriction ensures that Pruefungen that are at the same time, should ideally have the same
 * duration.
 */
public class UniformeZeitslotsRestriction extends SoftRestriction {

  /**
   * Constructor
   */
  public UniformeZeitslotsRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  /**
   * Constructor ensures that the {@link de.fhwedel.klausps.controller.kriterium.WeichesKriterium}
   * is always Uniforme Zeitslots
   *
   * @param dataAccessService the used DataAccessService
   */
  protected UniformeZeitslotsRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, UNIFORME_ZEITSLOTS);
  }


  @Override
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> pruefungenAtSameTime = new HashSet<>(
        dataAccessService.getPlannedPruefungen());
    pruefungenAtSameTime = filter(pruefung, pruefungenAtSameTime);

    if (pruefungenAtSameTime.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(buildAnalysis(pruefung, pruefungenAtSameTime));
  }

  /**
   * Creates a Set of only those Pruefungen that are relevant for this restriction.<br> A Pruefung
   * is only relevant, if it does not have the same duration and if it does not overlap with the
   * Pruefung that gets checked in this restriction.<br> The Pruefung itself and Pruefungen in the
   * same Block as the Pruefung never violate a restriction.
   *
   * @param pruefung             the Pruefung to check for
   * @param pruefungenAtSameTime the Pruefungen that are at the same time as <code>pruefung</code>
   * @return the filtered pruefungen
   * @throws NoPruefungsPeriodeDefinedException if no {@link Pruefungsperiode} is set
   */
  @NotNull
  private Set<Pruefung> filter(@NotNull Pruefung pruefung,
      @NotNull final Set<Pruefung> pruefungenAtSameTime)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung other : pruefungenAtSameTime) {
      // Pruefungen in same Block don't need to be checked
      if (!dataAccessService.areInSameBlock(pruefung, other)
          // check conditions of restriction
          && !isOutOfTimeRange(pruefung, other)
          && !hasSameDuration(pruefung, other)) {
        // restriction violated
        result.add(other);
      }
    }
    // pruefung itself never violates
    result.remove(pruefung);
    return result;
  }


  /**
   * tests if a {@link Pruefung} overlaps a certain pruefung in any way
   *
   * @param pruefung the pruefung to check for
   * @param other    the other pruefung
   * @return true if they do not overlap, otherwise false
   */
  private boolean isOutOfTimeRange(Pruefung pruefung, Pruefung other) {
    return other.endzeitpunkt().isBefore(pruefung.getStartzeitpunkt())
        || other.getStartzeitpunkt().isAfter(pruefung.endzeitpunkt())
        || other.getStartzeitpunkt().equals(pruefung.endzeitpunkt())
        || pruefung.getStartzeitpunkt().equals(other.endzeitpunkt());
  }

  /**
   * tests if two {@link Pruefung Pruefungen} have the same duration
   *
   * @param pruefung first pruefung
   * @param other    second pruefung
   * @return true if they have the same duration, otherwise false
   */
  private boolean hasSameDuration(Pruefung pruefung, Pruefung other) {
    return pruefung.getDauer().equals(other.getDauer());
  }
}
