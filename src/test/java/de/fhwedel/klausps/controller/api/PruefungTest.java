package de.fhwedel.klausps.controller.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PruefungTest {

  @Test
  void addTeilnehmerkreis() {
    Pruefung p = new Pruefung("Test", "Test", "Tester", Duration.ofMinutes(90),null);
    assertNull(p.getTeilnehmerkreise());
    Teilnehmerkreis t = new Teilnehmerkreis("Inf", "10",1);
    p.addTeilnehmerkreis(t,0);
    Map<Teilnehmerkreis, Integer> map = new HashMap<>();
    map.put(t,0);
    assertEquals(map,p.getTeilnehmerkreise());
  }
  @Test
  void addTeilnehmerkreis_schon_Vorhanden() {
    Pruefung pruefung = new Pruefung("Test", "Test", "Tester", Duration.ofMinutes(90),null);
    assertTrue(pruefung.getTeilnehmerkreise().isEmpty());
    Teilnehmerkreis teilnehmerkreis = new Teilnehmerkreis("Inf", "10",1);
    pruefung.addTeilnehmerkreis(teilnehmerkreis,0);
    assertNotNull(pruefung.getTeilnehmerkreise());
    pruefung.addTeilnehmerkreis(teilnehmerkreis,0);
    Map<Teilnehmerkreis, Integer> map = new HashMap<>();
    map.put(teilnehmerkreis,0);
    assertEquals(map,pruefung.getTeilnehmerkreise());
  }
  @Test
  void addTeilnehmerkreis_schon_Vorhanden_two() {
    de.fhwedel.klausps.model.api.Teilnehmerkreis teilnehmerkreis = new Teilnehmerkreis("Inf", "10",1);
    Map<de.fhwedel.klausps.model.api.Teilnehmerkreis, Integer> beginMap = new HashMap<>();
    beginMap.put(teilnehmerkreis,0);
    Pruefung pruefung = new Pruefung("Test", "Test", "Tester", Duration.ofMinutes(90),beginMap);
    assertFalse(pruefung.getTeilnehmerkreise().isEmpty());
    pruefung.addTeilnehmerkreis(teilnehmerkreis,0);
    Map<de.fhwedel.klausps.model.api.Teilnehmerkreis, Integer> map = new HashMap<>();
    map.put(teilnehmerkreis,0);
    assertEquals(map,pruefung.getTeilnehmerkreise());
  }

  @Test
  void removeTeilnehmerkreis() {
    de.fhwedel.klausps.model.api.Teilnehmerkreis teilnehmerkreis = new Teilnehmerkreis("Inf", "10",1);
    Map<de.fhwedel.klausps.model.api.Teilnehmerkreis, Integer> beginMap = new HashMap<>();
    beginMap.put(teilnehmerkreis,0);
    Pruefung pruefung = new Pruefung("Test", "Test", "Tester", Duration.ofMinutes(90),beginMap);
    assertNotNull(pruefung.getTeilnehmerkreise());
    pruefung.removeTeilnehmerkreis(teilnehmerkreis);

    assertTrue(pruefung.getTeilnehmerkreise().isEmpty());
  }


  @Test
  void removeTeilnehmerkreis_zwei() {
    de.fhwedel.klausps.model.api.Teilnehmerkreis teilnehmerkreis = new Teilnehmerkreis("Inf", "10",1);
    Map<de.fhwedel.klausps.model.api.Teilnehmerkreis, Integer> beginMap = new HashMap<>();
    beginMap.put(teilnehmerkreis,0);
    Pruefung pruefung = new Pruefung("Test", "Test", "Tester", Duration.ofMinutes(90),beginMap);
    assertNotNull(pruefung.getTeilnehmerkreise());
    pruefung.removeTeilnehmerkreis(teilnehmerkreis);

    assertTrue(pruefung.getTeilnehmerkreise().isEmpty());
  }
}