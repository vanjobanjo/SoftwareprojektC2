package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefung;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnzahlPruefungenGleichzeitigRestriktionTest {

  public AnzahlPruefungenGleichzeitigRestriktion deviceUnderTest;
  public DataAccessService dataAccessService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new AnzahlPruefungenGleichzeitigRestriktion(this.dataAccessService);
  }

  /*
   * Grenzfaelle:
   * - Nur die aufgerufene Klausur ist geplant
   * - Aufruf mit ungeplanter Klausur
   * - Keine gleichzeitigen Klausuren
   * - Genau so viele Klausuren gleichzeitig wie erlaubt
   * - Eine Klausur mehr gleichzeitig als erlaubt
   * - Mehr klausuren gleichzeitig als erlaubt, ohne dass die getestete Pruefung involviert ist (nichts soll angezeigt werden)
   * - Mehr klausuren als erlaubt aber alle in einem Block zusammen
   * - Mehr klausuren als erlaubt aber alle einige zusammen in einem Block (so, dass erlaubt)
   * - Mehr klausuren als erlaubt aber in 2 Blöcken, sodass insgesamt erlaubt
   */

  @Test
  void evaluate_onlyCheckedPruefungIsPlanned() {
    Pruefung pruefung = getRandomPlannedPruefung(5L);
    when(dataAccessService.getGeplanteModelPruefung()).thenReturn(Set.of(pruefung));

    assertThat(deviceUnderTest.evaluate(pruefung)).isEmpty();
  }

}
