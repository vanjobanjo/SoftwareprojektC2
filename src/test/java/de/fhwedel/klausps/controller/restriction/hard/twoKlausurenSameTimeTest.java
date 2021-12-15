package de.fhwedel.klausps.controller.restriction.hard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class twoKlausurenSameTimeTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();


  private Pruefungsperiode pruefungsperiode;
  private DataAccessService deviceUnderTest;
  private ScheduleService scheduleService;


  Teilnehmerkreis getTeilnehmerKreis(String name) {
    return new Teilnehmerkreis() {
      @Override
      public String getStudiengang() {
        return name;
      }

      @Override
      public String getPruefungsordnung() {
        return "1";
      }

      @Override
      public int getFachsemester() {
        return 0;
      }

      @Override
      public Ausbildungsgrad getAusbildungsgrad() {
        return null;
      }
    };
  }


  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.deviceUnderTest = ServiceProvider.getDataAccessService();
    // todo make sure the mocked class is not tested
    this.scheduleService = mock(ScheduleService.class);
    deviceUnderTest.setPruefungsperiode(pruefungsperiode);
    deviceUnderTest.setScheduleService(this.scheduleService);
  }

  @Test
  void twoKlausurenSameTimeTest_test() {

    Pruefung analysis = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(analysis);
    listOfPruefungen.add(dm);

    twoKlausurenSameTime h = new twoKlausurenSameTime(this.deviceUnderTest,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    when(deviceUnderTest.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);

    assertTrue(h.test(haskel));
  }

  @Test
  void twoKlausurenSameTimeTest() {

    Pruefung analysis = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(analysis);
    listOfPruefungen.add(dm);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(dm);
    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);

    int studends = 8;

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, Integer.valueOf(8));

    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(dm.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount);

    when(haskel.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(dm.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);

    twoKlausurenSameTime h = new twoKlausurenSameTime(this.deviceUnderTest,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    when(deviceUnderTest.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    /*
    when(analysis.getStartzeitpunkt()).thenReturn(start);
    when(analysis.getDauer()).thenReturn(duration);

    when(dm.getStartzeitpunkt()).thenReturn(start);
    when(dm.getDauer()).thenReturn(duration);*/

    assertTrue(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmer, h.inConfilictTeilnehmerkreis);
    assertEquals(studends, h.countStudents);

  }




  @Test
  void twoKlausurenSameTimeTest_DiffrentTeilnehmerkreise() {

    Pruefung analysis = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Set<Teilnehmerkreis> teilnehmer2 = new HashSet<>();
    Set<Teilnehmerkreis> teilnehmer3 = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");
    teilnehmer1.add(informatik);
    Teilnehmerkreis bwl = getTeilnehmerKreis("bwl");
    teilnehmer2.add(bwl);
    Teilnehmerkreis sleep = getTeilnehmerKreis("sleep");
    teilnehmer3.add(sleep);

    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer2);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer3);

    ArrayList<Pruefung> listOfPruefungenSameTime = new ArrayList<>();
    listOfPruefungenSameTime.add(analysis);
    listOfPruefungenSameTime.add(dm);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();


    //Hier die Werte einfügen, die erwartet werden
    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();


    int affectedStudends = 0;

    Map<Teilnehmerkreis, Integer> teilnehmerCount1 = new HashMap<>();
    teilnehmerCount1.put(informatik,Integer.valueOf(8));
    Map<Teilnehmerkreis, Integer> teilnehmerCount2 = new HashMap<>();
    teilnehmerCount2.put(bwl,Integer.valueOf(8));
    Map<Teilnehmerkreis, Integer> teilnehmerCount3 = new HashMap<>();
    teilnehmerCount3.put(sleep,Integer.valueOf(8));


    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount1);
    when(dm.getSchaetzungen()).thenReturn(teilnehmerCount2);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount3);


    Set<Teilnehmerkreis> setOfTeilnehmer1 = new HashSet<>();
    setOfTeilnehmer1.add(informatik);
    Set<Teilnehmerkreis> setOfTeilnehmer2 = new HashSet<>();
    setOfTeilnehmer2.add(bwl);
    Set<Teilnehmerkreis> setOfTeilnehmer3 = new HashSet<>();
    setOfTeilnehmer3.add(sleep);
    when(haskel.getTeilnehmerkreise()).thenReturn(setOfTeilnehmer1);
    when(dm.getTeilnehmerkreise()).thenReturn(setOfTeilnehmer2);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfTeilnehmer3);

    twoKlausurenSameTime h = new twoKlausurenSameTime(this.deviceUnderTest,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    when(deviceUnderTest.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungenSameTime);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    /*
    when(analysis.getStartzeitpunkt()).thenReturn(start);
    when(analysis.getDauer()).thenReturn(duration);

    when(dm.getStartzeitpunkt()).thenReturn(start);
    when(dm.getDauer()).thenReturn(duration);*/

    assertFalse(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmer, h.inConfilictTeilnehmerkreis);
    assertEquals(affectedStudends, h.countStudents);

  }


  @Test
  void twoKlausurenSameTimeTest_DiffrentTime() {

    Pruefung analysis = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Set<Teilnehmerkreis> teilnehmer2 = new HashSet<>();
    Set<Teilnehmerkreis> teilnehmer3 = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");
    teilnehmer1.add(informatik);

    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer1);

    ArrayList<Pruefung> listOfPruefungenSameTime = new ArrayList<>();


    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();


    //Hier die Werte einfügen, die erwartet werden
    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();


    int affectedStudends = 0;

    Map<Teilnehmerkreis, Integer> teilnehmerCount1 = new HashMap<>();
    teilnehmerCount1.put(informatik,Integer.valueOf(8));
    Map<Teilnehmerkreis, Integer> teilnehmerCount2 = new HashMap<>();
    teilnehmerCount2.put(informatik,Integer.valueOf(8));
    Map<Teilnehmerkreis, Integer> teilnehmerCount3 = new HashMap<>();
    teilnehmerCount3.put(informatik,Integer.valueOf(8));


    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount1);
    when(dm.getSchaetzungen()).thenReturn(teilnehmerCount2);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount3);


    Set<Teilnehmerkreis> setOfTeilnehmer1 = new HashSet<>();
    setOfTeilnehmer1.add(informatik);

    when(haskel.getTeilnehmerkreise()).thenReturn(setOfTeilnehmer1);
    when(dm.getTeilnehmerkreise()).thenReturn(setOfTeilnehmer1);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfTeilnehmer1);

    twoKlausurenSameTime h = new twoKlausurenSameTime(this.deviceUnderTest,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    when(deviceUnderTest.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungenSameTime);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    /*
    when(analysis.getStartzeitpunkt()).thenReturn(start);
    when(analysis.getDauer()).thenReturn(duration);

    when(dm.getStartzeitpunkt()).thenReturn(start);
    when(dm.getDauer()).thenReturn(duration);*/

    assertFalse(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmer, h.inConfilictTeilnehmerkreis);
    assertEquals(affectedStudends, h.countStudents);

  }


}