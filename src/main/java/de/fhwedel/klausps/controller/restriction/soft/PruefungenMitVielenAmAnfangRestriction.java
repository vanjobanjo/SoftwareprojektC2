package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.PRUEFUNGEN_MIT_VIELEN_AN_ANFANG;
import static de.fhwedel.klausps.controller.services.ServiceProvider.getDataAccessService;
import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class PruefungenMitVielenAmAnfangRestriction extends WeicheRestriktion {


  private static final int DEFAULT_AMOUNT_MANY_STUDENTS = 100;

  private static final Duration DEFAULT_BEGIN_AFTER_ANKER = Duration.ofDays(7);

  private final int amountManyStudents;
  private final Duration beginAfterAnker;

  protected PruefungenMitVielenAmAnfangRestriction(DataAccessService dataAccessService,
      int amountManyStudents, Duration beginAfterAnker) {
    super(dataAccessService, PRUEFUNGEN_MIT_VIELEN_AN_ANFANG);
    this.amountManyStudents = amountManyStudents;
    this.beginAfterAnker = beginAfterAnker;
  }

  public PruefungenMitVielenAmAnfangRestriction() {
    this(getDataAccessService());
  }

  protected PruefungenMitVielenAmAnfangRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, PRUEFUNGEN_MIT_VIELEN_AN_ANFANG);
    amountManyStudents = DEFAULT_AMOUNT_MANY_STUDENTS;
    beginAfterAnker = DEFAULT_BEGIN_AFTER_ANKER;
  }


  @Override
  public Optional<WeichesKriteriumAnalyse> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    noNullParameters(pruefung);
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    if (pruefung.getStartzeitpunkt().toLocalDate()
        .isBefore(dataAccessService.getAnkertag().plusDays(beginAfterAnker.toDays()))) {
      return Optional.empty();
    }

    if (pruefung.schaetzung() >= amountManyStudents) {
      return Optional.of(new WeichesKriteriumAnalyse(Set.of(pruefung), this.kriterium,
          pruefung.getTeilnehmerkreise(), pruefung.schaetzung(),
          this.kriterium.getWert()));
    }

    return Optional.empty();
  }
}
