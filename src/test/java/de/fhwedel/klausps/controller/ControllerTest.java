package de.fhwedel.klausps.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.Mockito;

class ControllerTest {

  private DataAccessService dataAccessService;
  private Controller controller;
  private Pruefungsperiode pruefungsperiode;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    when(dataAccessService.isPruefungsperiodeSet()).thenReturn(true);
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.controller = new Controller(pruefungsperiode,dataAccessService);
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
            controller.createPruefung(
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
    controller.createPruefung(
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
            controller.createPruefung(
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
        () -> {
          controller.createPruefung(
              expected.getName(),
              expected.getPruefungsnummer(),
              "Harms",
              expected.getDauer(),
              expected.getTeilnehmerKreisSchaetzung());
        });
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
