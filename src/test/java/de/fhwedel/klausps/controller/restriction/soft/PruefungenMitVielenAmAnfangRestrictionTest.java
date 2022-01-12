package de.fhwedel.klausps.controller.restriction.soft;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Grenzfälle:
 * <ol>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_pruefung_is_not_planned() Wenn die Prüfung nicht geplant ist<br>&rarr; empty}</li>
 *   <li>Eine Prüfung hat genauso viele Teilnehmer, wie der minimale Wert, der das Kriterium verletzt und liegt innerhalb der Anfangszeit<br>&rarr; empty</li>
 *   <li>Eine Prüfung hat genauso viele Teilnehmer, wie der minimale Wert, der das Kriterium verletzt und liegt außerhalb der Anfangszeit<br>&rarr; Kriterium wird verletzt</li>
 *   <li>Eine Prüfung hat weniger Teilnehmer als der minimale Wert, der das Kriterium verletzt und liegt innerhalb der Anfangszeit<br>&rarr; empty</li>
 *   <li>Eine Prüfung hat weniger Teilnehmer als der minimale Wert, der das Kriterium verletzt und liegt außerhalb der Anfangszeit<br>&rarr; empty</li>
 *   <li>Eine Prüfung hat mehr Teilnehmer, als der minimale Wert, der das Kriterium verletzt und liegt innerhalb der Anfangszeit<br>&rarr; empty</li>
 *   <li>Eine Prüfung hat mehr Teilnehmer, als der minimale Wert, der das Kriterium verletzt und liegt außerhalb der Anfangszeit<br>&rarr; Kriterium wird verletzt</li>
 * </ol>
 */
class PruefungenMitVielenAmAnfangRestrictionTest {

  private PruefungenMitVielenAmAnfangRestriction deviceUnderTest;
  private DataAccessService dataAccessService;

  @BeforeEach
  void setup() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService);
  }

  @Test
  void pruefungenMitVielenAmAnfang_null_parameters() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.evaluate(null));
  }

 /*@Test
  void pruefungenMitVielenAmAnfang_pruefung_does_not_exist() {
   Pruefung pruefung = mock(Pruefung.class);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.evaluate(pruefung));
  }*/


  @Test
  void pruefungenMitVielenAmAnfang_pruefung_is_not_planned()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = mock(Pruefung.class);
    when(pruefung.isGeplant()).thenReturn(false);
    assertThat(deviceUnderTest.evaluate(pruefung)).isEmpty();
  }

}