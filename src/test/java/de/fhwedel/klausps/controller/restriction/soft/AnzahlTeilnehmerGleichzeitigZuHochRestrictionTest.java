package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomTeilnehmerkreis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.BlockImpl;
import java.time.LocalDateTime;
import java.util.List;
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
   * x Grade so viele Personen, dass die Restriktion nicht verletzt wird (Klausur / Block)
   * O Genau so viele Personen, dass die Restriktion minimal verletzt wird (Klausur / Block)
   * O Genau die betroffenen Teilnehmerkreise sind enthalten (Klausur / Block)
   * O Die Anzahl der betroffenen Studenten ist korrekt (Klausur / Block)
   * O Das minimal moegliche scoring
   * O Naechst hoeheres scoring, ...
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

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_multiplePruefungen()
      throws IllegalTimeSpanException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal199Students();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isNotPresent();
  }

  private List<Pruefung> get3PruefungenWithTotal199Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 111);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 42);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 46);
    return pruefungen;
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_multiplePruefungenInOneBlock()
      throws IllegalTimeSpanException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal199Students();
    Block block = getBlockWith(pruefungen);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isNotPresent();
  }

  private Block getBlockWith(Iterable<Pruefung> pruefungen) {
    Block block = new BlockImpl(mock(Pruefungsperiode.class), "name", Blocktyp.PARALLEL);
    for (Pruefung pruefung : pruefungen) {
      block.addPruefung(pruefung);
    }
    return block;
  }

}
