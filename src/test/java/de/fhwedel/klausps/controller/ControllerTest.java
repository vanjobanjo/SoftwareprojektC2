package de.fhwedel.klausps.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.builders.PruefungBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ControllerTest {

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void createPruefung_Successful() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    DataAccessService dataAccessService = new DataAccessService(pruefungsperiode);
    Controller controller = new Controller(dataAccessService);
    ReadOnlyPruefung pruefung = getReadOnlyPruefung();
    when(pruefungsperiode.filteredPlanungseinheiten(any())).thenReturn(new HashSet<>());
    assertThat(
    controller.createPruefung(pruefung.getName(), pruefung.getPruefungsnummer(),
        "Harms", pruefung.getDauer(), new HashMap<>()))
        .isNotNull();
  }

  @Test
  public void createPruefung_Successful_actualWriting() throws NoPruefungsPeriodeDefinedException {
    DataAccessService dataAccessService = mock(DataAccessService.class);
    Controller controller = new Controller(dataAccessService);
    ReadOnlyPruefung pruefung = getReadOnlyPruefung();
    controller.createPruefung(pruefung.getName(), pruefung.getPruefungsnummer(),
        "Harms", pruefung.getDauer(), new HashMap<>());
    verify(dataAccessService, times(1))
        .createPruefung(any(), any(), anyString(), any(), any());
  }

  private ReadOnlyPruefung getReadOnlyPruefung() {
    //return new Pruefung()
    return new PruefungBuilder()
        .withPruefungsName("Analysis")
        .withPruefungsNummer("b001")
        .withAdditionalPruefer("Harms")
        .withDauer(Duration.ofMinutes(90))
        .build();
  }
}
