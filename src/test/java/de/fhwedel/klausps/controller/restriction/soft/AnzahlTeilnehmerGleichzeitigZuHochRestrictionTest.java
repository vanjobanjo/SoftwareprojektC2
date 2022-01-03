package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnzahlTeilnehmerGleichzeitigZuHochRestrictionTest {

  public AnzahlTeilnehmerGleichzeitigZuHochRestriction deviceUnderTest;
  public DataAccessService dataAccessService;

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(
        this.dataAccessService);
  }

  /*
   *   Grenzfälle:
   * O Grade so viele Personen, dass die Restriktion nicht verletzt wird (Klausur / Block)
   * O Genau so viele Personen, dass die Restriktion minimal verletzt wird (Klausur / Block)
   * O Genau die betroffenen Teilnehmerkreise sind enthalten (Klausur / Block)
   * O Die Anzahl der betroffenen Studenten ist korrekt (Klausur / Block)
   * O Das minimal mögliche scoring
   * O Nächst höheres scoring, ...
   */

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_onePruefung_oneTeilnehmerkreis()
      throws IllegalTimeSpanException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 199);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(Set.of(pruefung));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isNotPresent();
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_onePruefung_multipleTeilnehmerkreise()
      throws IllegalTimeSpanException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 100);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 50);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 49);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(Set.of(pruefung));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isNotPresent();
  }

}
