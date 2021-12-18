package de.fhwedel.klausps.controller.restriction.hard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwoKlausurenSameTimeTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();


  private Pruefungsperiode pruefungsperiode;
  private DataAccessService dataAccessService;
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

  Pruefung getPruefung(String name){
    return new Pruefung() {
      String pruefungsnummer;
      String name;
      @Override
      public String getPruefungsnummer() {
        return "1";
      }

      @Override
      public void setPruefungsnummer(String pruefungsnummer) {
  this.pruefungsnummer = pruefungsnummer;
      }

      @Override
      public String getName() {
        return this.name;
      }

      @Override
      public void setName(String name) {
  this.name = name;
      }

      @Override
      public String getReferenzVerwaltungsystem() {
        return null;
      }

      @Override
      public void setDauer(Duration dauer) {

      }

      @Override
      public void setSchaetzung(Teilnehmerkreis teilnehmerkreis, int schaetzung) {

      }

      @Override
      public boolean addPruefer(String kuerzel) {
        return false;
      }

      @Override
      public boolean removePruefer(String kuerzel) {
        return false;
      }

      @Override
      public boolean addTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis) {
        return false;
      }

      @Override
      public boolean addTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis, int schaetzung) {
        return false;
      }

      @Override
      public boolean removeTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis) {
        return false;
      }

      @Override
      public boolean isBlock() {
        return false;
      }

      @Override
      public Block asBlock() {
        return null;
      }

      @Override
      public Pruefung asPruefung() {
        return null;
      }

      @Override
      public LocalDateTime getStartzeitpunkt() {
        return null;
      }

      @Override
      public void setStartzeitpunkt(LocalDateTime startzeitpunkt) {

      }

      @Override
      public Duration getDauer() {
        return null;
      }

      @Override
      public Set<String> getPruefer() {
        return null;
      }

      @Override
      public Map<Teilnehmerkreis, Integer> getSchaetzungen() {
        return null;
      }

      @Override
      public Set<Teilnehmerkreis> getTeilnehmerkreise() {
        return null;
      }

      @Override
      public Set<Ausbildungsgrad> getAusbildungsgrade() {
        return null;
      }

      @Override
      public boolean isGeplant() {
        return false;
      }

      @Override
      public int schaetzung() {
        return 0;
      }

      @Override
      public LocalDateTime endzeitpunkt() {
        return null;
      }
    };
  }


  @BeforeEach
  void setUp() {
   // this.pruefungsperiode = mock(Pruefungsperiode.class);
    this.dataAccessService = mock(DataAccessService.class);
    // todo make sure the mocked class is not tested
  //  this.scheduleService = mock(ScheduleService.class);
  //  dataAccessService.setPruefungsperiode(pruefungsperiode);
   // dataAccessService.setScheduleService(this.scheduleService);
  }

  @Test
  void twoKlausurenSameTimeTest_twoSameTime() {

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    Planungseinheit analysisPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(analysis);


    Set<Planungseinheit> setOfPruefungen = new HashSet<>();
    setOfPruefungen.add(analysisPL);


    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();

    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);

    int studends = 8;

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, Integer.valueOf(8));

    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(haskel.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);


    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
   // when(pruefungsperiode.planungseinheitenBetween(start, start.plusMinutes(120))).thenReturn(setOfPruefungen);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    assertTrue(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmer, h.inConfilictTeilnehmerkreis);
    assertEquals(studends, h.countStudents);
  }



  @Test
  void twoKlausurenSameTime_NotSameTime() {

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    Planungseinheit analysisPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();



    Set<Planungseinheit> setOfPruefungen = new HashSet<>();
    setOfPruefungen.add(analysisPL);


    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();



    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();


    int studends = 0;

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, Integer.valueOf(8));

    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(haskel.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);


    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    // when(pruefungsperiode.planungseinheitenBetween(start, start.plusMinutes(120))).thenReturn(setOfPruefungen);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    assertFalse(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmer, h.inConfilictTeilnehmerkreis);
    assertEquals(studends, h.countStudents);
  }

  @Test
  void twoKlausurenSameTime_ThreeSameTime() {

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    Planungseinheit analysisPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);
    Planungseinheit dmPL = mock(Planungseinheit.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(dm);
    listOfPruefungen.add(haskel);
    listOfPruefungen.add(analysis);


    Set<Planungseinheit> setOfPruefungen = new HashSet<>();
    setOfPruefungen.add(analysisPL);
    setOfPruefungen.add(dmPL);



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
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(dm.getSchaetzungen()).thenReturn(teilnehmerCount);

    when(haskel.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(dm.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);


    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    when(dmPL.asPruefung()).thenReturn(dm);
    // when(pruefungsperiode.planungseinheitenBetween(start, start.plusMinutes(120))).thenReturn(setOfPruefungen);

    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    assertTrue(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmer, h.inConfilictTeilnehmerkreis);
    assertEquals(studends, h.countStudents);
  }




  @Test
  void twoKlausurenSameTime_ThreeSameTime_two_DiffrentTeilnehmerkreis() {

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService,
        HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG);

    Planungseinheit analysisPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);
    Planungseinheit dmPL = mock(Planungseinheit.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");
    teilnehmer1.add(informatik);

    Set<Teilnehmerkreis> teilnehmer2 = new HashSet<>();
    Teilnehmerkreis irgendwasMitMedien = getTeilnehmerKreis("irgendwasMitMedien");
    teilnehmer2.add(irgendwasMitMedien);

    Set<Teilnehmerkreis> teilnehmer3= new HashSet<>();
    teilnehmer3.add(irgendwasMitMedien);
    teilnehmer3.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer2);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer3);




//   Set<Planungseinheit> setOfPruefungen = new HashSet<>();
//   setOfPruefungen.add(analysisPL);
//   setOfPruefungen.add(dmPL);



    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(dm);
    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);


    Set<Teilnehmerkreis> setOfConflictTeilnehmerkreis = new HashSet<>();
    setOfConflictTeilnehmerkreis.add(informatik);
    setOfConflictTeilnehmerkreis.add(irgendwasMitMedien);

    Set<Teilnehmerkreis> haskelTeilnehmer3 = new HashSet<>();
    haskelTeilnehmer3.add(informatik);
    haskelTeilnehmer3.add(irgendwasMitMedien);

    Set<Teilnehmerkreis> analysisTeilnehmer1 = new HashSet<>();
    analysisTeilnehmer1.add(informatik);


    Set<Teilnehmerkreis> dmTeilnehmer2 = new HashSet<>();

    dmTeilnehmer2.add(irgendwasMitMedien);

    int studends = 16;

    Map<Teilnehmerkreis, Integer> teilnehmerCount1 = new HashMap<>();
    teilnehmerCount1.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> teilnehmerCount2 = new HashMap<>();
    teilnehmerCount2.put(irgendwasMitMedien, 8);

    Map<Teilnehmerkreis, Integer> teilnehmerCount3 = new HashMap<>();
    teilnehmerCount3.put(irgendwasMitMedien, 8);
    teilnehmerCount3.put(informatik, 8);

    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount1);
    when(dm.getSchaetzungen()).thenReturn(teilnehmerCount2);
    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount3);

    when(analysis.getTeilnehmerkreise()).thenReturn(analysisTeilnehmer1);
    when(dm.getTeilnehmerkreise()).thenReturn(dmTeilnehmer2);
    when(haskel.getTeilnehmerkreise()).thenReturn(haskelTeilnehmer3);


    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    when(dmPL.asPruefung()).thenReturn(dm);
    // when(pruefungsperiode.planungseinheitenBetween(start, start.plusMinutes(120))).thenReturn(setOfPruefungen);


    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(dm);
    listOfPruefungen.add(haskel);
    listOfPruefungen.add(analysis);


    when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);

    Duration duration = Duration.ofMinutes(120);


    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    assertTrue(h.test(haskel));

    assertEquals(setOfConflictPruefunge, h.inConflictROPruefung);
    assertEquals(setOfConflictTeilnehmerkreis, h.inConfilictTeilnehmerkreis);
    assertEquals(studends, h.countStudents);
  }
}