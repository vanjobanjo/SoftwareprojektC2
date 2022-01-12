package de.fhwedel.klausps.controller.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ServiceProviderTest {


  @Test
  void testCreationProviders() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    DataAccessService dataAccessService= ServiceProvider.getDataAccessService();
    dataAccessService.setPruefungsperiode(pruefungsperiode);
    Converter converter = ServiceProvider.getConverter();
    ScheduleService scheduleService = ServiceProvider.getScheduleService();
    Pruefung p = new PruefungImpl("nummer", "name", "ref", Duration.ofMinutes(120));
    assertThat(converter.convertToReadOnlyPruefung(p)).isNotNull();

  }

}