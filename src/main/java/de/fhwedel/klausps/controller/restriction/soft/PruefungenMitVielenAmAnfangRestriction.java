package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.PRUEFUNGEN_MIT_VIELEN_AN_ANFANG;
import static de.fhwedel.klausps.controller.services.ServiceProvider.getDataAccessService;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.Duration;
import java.util.Optional;

/**
 * This restriction ensures that Pruefungen with many students should be at the beginning of the
 * Pruefungsperiode.
 */
public class PruefungenMitVielenAmAnfangRestriction extends SoftRestriction {


  /**
   * Default amount definition of "many students" as requested by customer
   */
  private static final int DEFAULT_AMOUNT_MANY_STUDENTS = 100;

  /**
   * default amount of days defining the beginning of the Pruefungsperiode
   */
  private static final Duration DEFAULT_BEGIN_AFTER_ANKER = Duration.ofDays(7);

  /**
   * the amount defining "many students"
   */
  private final int amountManyStudents;
  /**
   * the duration considered "the beginning" of the Pruefungsperiode
   */
  private final Duration beginAfterAnker;

  /**
   * constructor that allows other values for amountOfStudents and beginAfterAnker than the default
   * values
   *
   * @param dataAccessService  the DataAccessService in use
   * @param amountManyStudents the definition of many students
   * @param beginAfterAnker    the period of time after the Ankertag, considered "the beginning"
   */
  protected PruefungenMitVielenAmAnfangRestriction(DataAccessService dataAccessService,
      int amountManyStudents, Duration beginAfterAnker) {
    super(dataAccessService, PRUEFUNGEN_MIT_VIELEN_AN_ANFANG);
    this.amountManyStudents = amountManyStudents;
    this.beginAfterAnker = beginAfterAnker;
  }

  /**
   * constructs a new PruefungenMitVielenAmAnfangRestriction.<br> uses the default amounts of {@link
   * PruefungenMitVielenAmAnfangRestriction#amountManyStudents} and {@link
   * PruefungenMitVielenAmAnfangRestriction#beginAfterAnker} and the {@link DataAccessService}
   * provided by the {@link de.fhwedel.klausps.controller.services.ServiceProvider}
   */
  public PruefungenMitVielenAmAnfangRestriction() {
    this(getDataAccessService());
  }

  /**
   * constructs a new PruefungenMitVielenAmAnfangRestriction.<br> uses the default amounts of {@link
   * PruefungenMitVielenAmAnfangRestriction#amountManyStudents} and {@link
   * PruefungenMitVielenAmAnfangRestriction#beginAfterAnker}
   *
   * @param dataAccessService the used DataAccessService
   */
  protected PruefungenMitVielenAmAnfangRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, PRUEFUNGEN_MIT_VIELEN_AN_ANFANG);
    amountManyStudents = DEFAULT_AMOUNT_MANY_STUDENTS;
    beginAfterAnker = DEFAULT_BEGIN_AFTER_ANKER;
  }


  @Override
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    // check if pruefung is at the beginning of the Pruefungsperiode
    if (pruefung.getStartzeitpunkt().toLocalDate()
        .isBefore(dataAccessService.getAnkertag().plusDays(beginAfterAnker.toDays()))) {
      return Optional.empty();
    }

    // Pruefung is not at the beginning, therefore check if it has more participants than "many"
    if (pruefung.schaetzung() >= amountManyStudents) {
      // Pruefung violates the restriction
      return Optional.of(new SoftRestrictionAnalysis(emptySet(), this.kriterium,
          pruefung.getTeilnehmerkreise(), pruefung.schaetzung(),
          this.kriterium.getWert()));
    }
    // Pruefung did not violate the restriction, nothing to report
    return Optional.empty();
  }
}
