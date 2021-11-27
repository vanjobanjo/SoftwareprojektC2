package de.fhwedel.klausps.controller.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessServiceTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();
  private Pruefungsperiode pruefungsperiode;
  private DataAccessService dataAccessService;

  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.dataAccessService = new DataAccessService(pruefungsperiode);
  }

  @Test
  @DisplayName("A Pruefung gets created by calling the respective method of the pruefungsPeriode")
  void createPruefungSuccessTest() {
    ReadOnlyPruefung pruefung =
        dataAccessService.createPruefung(
            "Analysis", "b123", "pruefer 1", Duration.ofMinutes(90), new HashMap<>());
    assertThat(pruefung).isNotNull();
    verify(pruefungsperiode, times(1)).addPlanungseinheit(any());
  }

  @Test
  @DisplayName("A created pruefung has the expected attributes")
  void createPruefungSuccessRightAttributesTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    ReadOnlyPruefung actual =
        dataAccessService.createPruefung(
            expected.getName(),
            expected.getPruefungsnummer(),
            expected.getPruefer(),
            expected.getDauer(),
            expected.getTeilnehmerKreisSchaetzung());

    ReadOnlyPruefungAssert.assertThat(actual).isTheSameAs(getReadOnlyPruefung());
  }

  @Test
  @DisplayName("A created pruefung is persisted in the data model")
  void createPruefungSaveInModelTest() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    ReadOnlyPruefung actual =
        dataAccessService.createPruefung(
            expected.getName(),
            expected.getPruefungsnummer(),
            expected.getPruefer(),
            expected.getDauer(),
            expected.getTeilnehmerKreisSchaetzung());
    verify(pruefungsperiode, times(1)).addPlanungseinheit(notNull());
    assertThat(actual).isNotNull();
  }

  @Test
  @DisplayName("A pruefung can not be created when one with the same pruefungsnummer")
  void createPruefung_existsAlready() {
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    Set<Planungseinheit> plannedPruefungen = new HashSet<>();
    plannedPruefungen.add(
        new PruefungImpl(
            expected.getPruefungsnummer(),
            expected.getName(),
            "ABCDEF",
            expected.getGesamtSchaetzung(),
            expected.getDauer()));
    when(pruefungsperiode.filteredPlanungseinheiten(any())).thenReturn(plannedPruefungen);
    assertThat(
            dataAccessService.createPruefung(
                expected.getName(),
                expected.getPruefungsnummer(),
                expected.getPruefer(),
                expected.getDauer(),
                expected.getTeilnehmerKreisSchaetzung()))
        .isNull();
  }

  /**
   * Gibt eine vorgegeben ReadOnlyPruefung zurueck
   * @return gibt eine vorgebene ReadOnlyPruefung zurueck
   */
  private ReadOnlyPruefung getReadOnlyPruefung() {
    // return new Pruefung()
    return new PruefungDTOBuilder()
        .withPruefungsName("Analysis")
        .withPruefungsNummer("b001")
        .withAdditionalPruefer("Harms")
        .withDauer(Duration.ofMinutes(90))
        .build();
  }
}
