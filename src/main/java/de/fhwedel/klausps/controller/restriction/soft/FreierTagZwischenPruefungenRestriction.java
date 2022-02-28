package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.FREIER_TAG_ZWISCHEN_PRUEFUNGEN;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * This restriction ensures that Pruefungen with the same Teilnehmerkreis should at least be one day
 * apart.
 */
public class FreierTagZwischenPruefungenRestriction extends SoftRestriction {

  /**
   * Number of days in a year
   */
  static final int MAX_DAY = 365;

  /**
   * Constructs a FreierTagZwischenPruefungenRestriction with the {@link DataAccessService} provided
   * by the {@link ServiceProvider}.
   */
  public FreierTagZwischenPruefungenRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  /**
   * Constructs a FreierTagZwischenPruefungenRestriction with a provided {@link DataAccessService}.
   *
   * @param dataAccessService the used DataAccessService
   */
  protected FreierTagZwischenPruefungenRestriction(@NotNull DataAccessService dataAccessService) {
    super(dataAccessService, FREIER_TAG_ZWISCHEN_PRUEFUNGEN);
  }

  @Override
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(@NotNull Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {

    Set<Pruefung> pruefungenWithSameTeilnehmerkreisen = new HashSet<>(
        dataAccessService.getPlannedPruefungen());
    // remove of no overlapping Teilnehmerkreise and more than one day apart
    pruefungenWithSameTeilnehmerkreisen = filterOnlyOverlappingTeilnehmerkreiseAndOnlyDayApart(
        pruefung, pruefungenWithSameTeilnehmerkreisen);

    // the restriction was not violated
    if (pruefungenWithSameTeilnehmerkreisen.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(buildAnalysis(pruefung, pruefungenWithSameTeilnehmerkreisen));
  }

  /**
   * Filters the Pruefungen that do not violate this restriction concerning a certain pruefung from
   * the result.<br> The Pruefung itself and any Pruefung included in the same Block as the Pruefung
   * to check for never violate a restriction.
   *
   * @param pruefung                            the pruefung to check for
   * @param pruefungenWithSameTeilnehmerkreisen Set of Pruefungen with the same Teilnehmerkreis
   * @return a Set of Pruefungen violating this restriction concerning the checked Pruefung
   * @throws NoPruefungsPeriodeDefinedException when no Pruefungsperiode is set
   */
  @NotNull
  private Set<Pruefung> filterOnlyOverlappingTeilnehmerkreiseAndOnlyDayApart(
      @NotNull final Pruefung pruefung,
      @NotNull final Set<Pruefung> pruefungenWithSameTeilnehmerkreisen)
      throws NoPruefungsPeriodeDefinedException {
    Set<Pruefung> result = new HashSet<>();
    for (Pruefung other : pruefungenWithSameTeilnehmerkreisen) {
      // Pruefungen from same Block don't need to be checked
      if (!dataAccessService.areInSameBlock(pruefung, other)
          // test if they are at least one day apart
          && !testDayApart(pruefung, other)
          // test if they have an overlap in Teilnehmerkreis
          && !testOverlappingTeilnehmerkreise(pruefung, other)) {
        result.add(other);
      }
    }
    result.remove(pruefung);
    return result;
  }


  /**
   * determines if two Pruefungen have at least one Teilnehmerkreis in common
   *
   * @param pruefung the pruefung evaluated in this restriction
   * @param other    another pruefung
   * @return true if they do not have overlapping Teilnehmerkreise, otherwise false
   */
  private boolean testOverlappingTeilnehmerkreise(Pruefung pruefung, Pruefung other) {
    return other.getTeilnehmerkreise().stream().noneMatch(teilnehmerkreis ->
        pruefung.getTeilnehmerkreise().contains(teilnehmerkreis));
  }

  /**
   * determines if two Pruefungen are at least one day apart
   *
   * @param pruefung the pruefung evaluated in this restriction
   * @param other    another pruefung
   * @return true if they are at least one day apart, otherwise false
   */
  private boolean testDayApart(Pruefung pruefung, Pruefung other) {

    int difference = Math.abs(
        pruefung.getStartzeitpunkt().getDayOfYear() - other.getStartzeitpunkt().getDayOfYear());
    // cases to be considered when year changes between two Pruefungen:
    // 365 -1
    // 1 - 365
    if (pruefung.getStartzeitpunkt().getYear() != other.getStartzeitpunkt().getYear()) {
      return difference < MAX_DAY - 1;
    }
    return difference > 1;
  }


  @Override
  protected Map<Teilnehmerkreis, Integer> getRelevantSchaetzungen(Pruefung pruefung,
      Pruefung affected) {
    Map<Teilnehmerkreis, Integer> result = new HashMap<>();
    if (pruefung != null) {
      for (Map.Entry<Teilnehmerkreis, Integer> schaetzung : pruefung.getSchaetzungen().entrySet()) {
        if (affected.getSchaetzungen().containsKey(schaetzung.getKey())) {
          result.put(schaetzung.getKey(), schaetzung.getValue());
        }
      }
    }
    return result;
  }
}
