package de.fhwedel.klausps.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.*;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ControllerTest {

/*
  @Test
  void deletePruefung_keine_PruefungsPeriode_definiert() {
    Controller newCon = new Controller(null);
    Exception exception = assertThrows(NoPruefungsPeriodeDefinedException.class, () -> {
      newCon.deletePruefung(null);
    });
    String expectedMessage = "Es wurde noch keine PruefungsPeriode definiert.";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void deletePruefung_Pruefung_nicht_vorhanden() {
    Pruefungsperiode p = mock(Pruefungsperiode.class);
    Controller newCon = new Controller(p);

    ReadOnlyPruefung readOnlyPrufung = null;
    try {
      Duration duration = Duration.ofMinutes(90);
      readOnlyPrufung = newCon.createPruefung("Test", "test1234", "Tester", duration,
          null);
    } catch (NoPruefungsPeriodeDefinedException e) {
      fail();
    }

    try {
      //Ungeplant
      assertFalse(readOnlyPrufung.geplant());
      newCon.deletePruefung(readOnlyPrufung);
      newCon.getGeplantePruefungen().contains(readOnlyPrufung);
      assertFalse(readOnlyPrufung.geplant());
    } catch (NoPruefungsPeriodeDefinedException e) {
      fail();
    }
  }

  @Test
  void deletePruefung_Pruefung_vorhanden() {
    Pruefungsperiode p = mock(Pruefungsperiode.class);
    Controller newCon = new Controller(p);

    ReadOnlyPruefung readOnlyPrufung = null;
    try {
      Duration duration = Duration.ofMinutes(90);
      readOnlyPrufung = newCon.createPruefung("Test", "test1234", "Tester",duration, null          );
    } catch (NoPruefungsPeriodeDefinedException e) {
      fail();
    }

    LocalDateTime startTermin = LocalDateTime.now();
    try {
      newCon.schedulePruefung(readOnlyPrufung, startTermin);
    } catch (HartesKriteriumException | NoPruefungsPeriodeDefinedException e) {
      fail();
    }
    try {
      //das es geplant ist
      assertTrue(readOnlyPrufung.geplant());
      newCon.deletePruefung(readOnlyPrufung);
      assertFalse(newCon.getGeplantePruefungen().contains(readOnlyPrufung));
      assertFalse(readOnlyPrufung.geplant());
    } catch (NoPruefungsPeriodeDefinedException e) {
      fail();
    }
  }
 */

}