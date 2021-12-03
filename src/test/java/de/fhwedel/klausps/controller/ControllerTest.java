package de.fhwedel.klausps.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import java.time.Duration;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControllerTest {

  private DataAccessService dataAccessService;
  private Controller deviceUnderTest;
  private Pruefungsperiode pruefungsperiode;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.deviceUnderTest = new Controller(dataAccessService);
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
  }

  @Test
  @DisplayName("Soll die Pruefung erfolgreich erstellen.")
  void createPruefung_Successful() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getReadOnlyPruefung();
    when(dataAccessService.createPruefung(
            pruefung.getName(),
            pruefung.getPruefungsnummer(),
            "Harms",
            pruefung.getDauer(),
            new HashMap<>()))
        .thenReturn(pruefung);
    ReadOnlyPruefungAssert.assertThat(
            deviceUnderTest.createPruefung(
                pruefung.getName(),
                pruefung.getPruefungsnummer(),
                "Harms",
                pruefung.getDauer(),
                new HashMap<>()))
        .isTheSameAs(getReadOnlyPruefung());
  }

  @Test
  void createPruefung_Successful_actualWriting() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getReadOnlyPruefung();
    deviceUnderTest.createPruefung(
        pruefung.getName(),
        pruefung.getPruefungsnummer(),
        "Harms",
        pruefung.getDauer(),
        new HashMap<>());
    verify(dataAccessService, times(1)).createPruefung(any(), any(), anyString(), any(), any());
  }

  @Test
  void createPruefung_existsAlready() throws NoPruefungsPeriodeDefinedException {
    // dataAccessService returns null as the Pruefung already exists
    when(dataAccessService.createPruefung(any(), any(), anyString(), any(), any()))
        .thenReturn(null);
    ReadOnlyPruefung pruefung = getReadOnlyPruefung();
    assertThat(
            deviceUnderTest.createPruefung(
                pruefung.getName(),
                pruefung.getPruefungsnummer(),
                "Harms",
                pruefung.getDauer(),
                new HashMap<>()))
        .isNull();
  }

  @Test
  @DisplayName("Can not create Pruefung as no PruefungsPeriode is set")
  void createPruefung_missingPruefungsPeriode() {
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(false);
    ReadOnlyPruefung expected = getReadOnlyPruefung();
    assertThrows(
        NoPruefungsPeriodeDefinedException.class,
        () ->
            deviceUnderTest.createPruefung(
                expected.getName(),
                expected.getPruefungsnummer(),
                "Harms",
                expected.getDauer(),
                expected.getTeilnehmerKreisSchaetzung()));
  }

  @Test
  void addPruefer_successs() throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefungToAddTo = getPruefungWithoutPruefer();
    String prueferToAdd = "Predeschly";
    deviceUnderTest.addPruefer(pruefungToAddTo, prueferToAdd);
    verify(dataAccessService, times(1))
        .addPruefer(pruefungToAddTo.getPruefungsnummer(), prueferToAdd);
  }

  /**
   * Gibt eine vorgegeben ReadOnlyPruefung zurueck
   *
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

  private ReadOnlyPruefung getPruefungWithoutPruefer() {
    // return new Pruefung()
    return new PruefungDTOBuilder()
        .withPruefungsName("Analysis")
        .withPruefungsNummer("b001")
        .withDauer(Duration.ofMinutes(90))
        .build();
  }
}
