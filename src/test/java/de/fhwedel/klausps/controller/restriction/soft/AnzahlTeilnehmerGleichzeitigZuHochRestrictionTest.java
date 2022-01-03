package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.util.TestUtils.convertPruefungenToPlanungseinheiten;
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
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.BlockImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    int maxTeilnehmerAtSameTime = 200;
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new AnzahlTeilnehmerGleichzeitigZuHochRestriction(this.dataAccessService,
        Duration.ZERO, maxTeilnehmerAtSameTime);
  }

  /*
   *   Grenzfälle:
   * x Grade so viele Personen, dass die Restriktion nicht verletzt wird (Klausur / Block)
   * x Genau so viele Personen, dass die Restriktion minimal verletzt wird (Klausur / Block)
   * O Genau die betroffenen Teilnehmerkreise sind enthalten (Klausur / Block)
   * O Die Anzahl der betroffenen Studenten ist korrekt (Klausur / Block)
   * O Das minimal moegliche scoring
   * O Naechst hoeheres scoring, ...
   */

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_onePruefung_oneTeilnehmerkreis()
      throws IllegalTimeSpanException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 200);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(Set.of(pruefung));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isNotPresent();
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_onePruefung_multipleTeilnehmerkreise()
      throws IllegalTimeSpanException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 100);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 51);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 49);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(Set.of(pruefung));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isNotPresent();
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_multiplePruefungen()
      throws IllegalTimeSpanException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal200Students();

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isNotPresent();
  }

  private List<Pruefung> get3PruefungenWithTotal200Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 111);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 43);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 46);
    return pruefungen;
  }

  @Test
  void restrictionNotViolatedWhenSlightlyLessStudentsThanPermitted_multiplePruefungenInOneBlock()
      throws IllegalTimeSpanException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal200Students();
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

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_onePruefung_oneTeilnehmerkreis()
      throws IllegalTimeSpanException {
    List<Planungseinheit> planungseinheiten = new ArrayList<>(1);
    planungseinheiten.add(getRandomPlannedPruefung(1L));
    planungseinheiten.add(planungseinheiten.get(0));
    planungseinheiten.get(0).asPruefung().addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 201);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        planungseinheiten);
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(planungseinheiten.get(0).asPruefung())).isPresent();
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_onePruefung_multipleTeilnehmerkreise()
      throws IllegalTimeSpanException {
    Pruefung pruefung = getRandomPlannedPruefung(1L);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 101);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 51);
    pruefung.addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 49);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(List.of(pruefung)));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefung)).isPresent();
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_multiplePruefungen()
      throws IllegalTimeSpanException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal201Students();

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));
    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.empty());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isPresent();
  }

  private List<Pruefung> get3PruefungenWithTotal201Students() {
    LocalDateTime startTime = LocalDateTime.of(2012, 12, 31, 1, 30);
    List<Pruefung> pruefungen = getRandomPlannedPruefungen(1L, 3);
    for (Pruefung pruefung : pruefungen) {
      pruefung.setStartzeitpunkt(startTime);
    }
    pruefungen.get(0).addTeilnehmerkreis(getRandomTeilnehmerkreis(1L), 111);
    pruefungen.get(1).addTeilnehmerkreis(getRandomTeilnehmerkreis(2L), 43);
    pruefungen.get(2).addTeilnehmerkreis(getRandomTeilnehmerkreis(3L), 47);
    return pruefungen;
  }

  @Test
  void restrictionViolatedWhenSlightlyMoreStudentsThanPermitted_multiplePruefungenInOneBlock()
      throws IllegalTimeSpanException {
    List<Pruefung> pruefungen = get3PruefungenWithTotal201Students();
    Block block = getBlockWith(pruefungen);
    block.setStartzeitpunkt(LocalDateTime.of(1999, 11, 12, 13, 14));

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        convertPruefungenToPlanungseinheiten(pruefungen));

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isPresent();
  }

}
