package de.fhwedel.klausps.controller.restriction.hard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TwoKlausurenSameTimeTest {


  private DataAccessService dataAccessService;


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
    this.dataAccessService = mock(DataAccessService.class);
  }

  @Test
  void twoKlausurenSameTimeTest_twoSameTime() {

    Planungseinheit analysisPL = mock(Pruefung.class);
    Planungseinheit haskelPL = mock(Pruefung.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    setNameAndNummer(analysis, "analysis");
    setNameAndNummer(haskel, "haskel");

    when(analysis.isGeplant()).thenReturn(true);
    when(haskel.isGeplant()).thenReturn(true);

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Planungseinheit> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(analysisPL);
    listOfPruefungen.add(haskelPL);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();

    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);

    int studends = 8;

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, 8);

    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(haskel.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    // when(pruefungsperiode.planungseinheitenBetween(start, start.plusMinutes(120))).thenReturn(setOfPruefungen);

    try {
      when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
          listOfPruefungen);
    } catch (IllegalTimeSpanException e) {

      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);

    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(haskel);
    assertTrue(analyse.isPresent());
    assertEquals(setOfConflictPruefunge, analyse.get().getCausingPruefungen());
    assertEquals(setOfConflictTeilnehmer, analyse.get().getAffectedTeilnehmerkreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());
  }


  @Test
  void twoKlausurenSameTime_NotSameTime() {

    Planungseinheit analysisPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    setNameAndNummer(analysis, "analysis");
    setNameAndNummer(haskel, "haskel");

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Planungseinheit> listOfPruefungen = new ArrayList<>();



    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();



    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, 8);

    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount);
    when(haskel.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);
    when(analysis.getTeilnehmerkreise()).thenReturn(setOfConflictTeilnehmer);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    // when(pruefungsperiode.planungseinheitenBetween(start, start.plusMinutes(120))).thenReturn(setOfPruefungen);

    try {
      when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
          listOfPruefungen);
    } catch (IllegalTimeSpanException e) {

      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);

    Optional<HartesKriteriumAnalyse> should = Optional.empty();
    assertEquals(should, h.evaluate(haskel));

  }

  @Test
  void twoKlausurenSameTime_ThreeSameTime() {

    Planungseinheit analysisPL = mock(Pruefung.class);
    Planungseinheit haskelPL = mock(Pruefung.class);
    Planungseinheit dmPL = mock(Pruefung.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);

    setNameAndNummer(analysis, "analysis");
    setNameAndNummer(haskel, "haskel");
    setNameAndNummer(dm, "dm");

    Set<Teilnehmerkreis> teilnehmer = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");

    teilnehmer.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer);

    ArrayList<Planungseinheit> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(dmPL);
    listOfPruefungen.add(haskelPL);
    listOfPruefungen.add(analysisPL);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(dm);
    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);

    int studends = 8;

    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();
    teilnehmerCount.put(informatik, 8);

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

    try {
      when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
          listOfPruefungen);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer);
    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);
    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(haskel);
    assertTrue(analyse.isPresent());
    assertEquals(setOfConflictPruefunge, analyse.get().getCausingPruefungen());
    assertEquals(setOfConflictTeilnehmer, analyse.get().getAffectedTeilnehmerkreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());

  }

  @Test
  void twoKlausurenSameTime_ThreeSameTime_two_DiffrentTeilnehmerkreis() {

    Planungseinheit analysisPL = mock(Pruefung.class);
    Planungseinheit haskelPL = mock(Pruefung.class);
    Planungseinheit dmPL = mock(Pruefung.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);

    setNameAndNummer(analysis, "analysis");
    setNameAndNummer(haskel, "haskel");
    setNameAndNummer(dm, "dm");

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Teilnehmerkreis informatik = getTeilnehmerKreis("Informatik");
    teilnehmer1.add(informatik);

    Set<Teilnehmerkreis> teilnehmer2 = new HashSet<>();
    Teilnehmerkreis irgendwasMitMedien = getTeilnehmerKreis("irgendwasMitMedien");
    teilnehmer2.add(irgendwasMitMedien);

    Set<Teilnehmerkreis> teilnehmer3 = new HashSet<>();
    teilnehmer3.add(irgendwasMitMedien);
    teilnehmer3.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer2);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer3);

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

    ArrayList<Planungseinheit> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(dmPL);
    listOfPruefungen.add(haskelPL);
    listOfPruefungen.add(analysisPL);

    try {
      when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
          listOfPruefungen);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);

    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(haskel);
    assertTrue(analyse.isPresent());

    assertEquals(setOfConflictPruefunge, analyse.get().getCausingPruefungen());
    assertEquals(setOfConflictTeilnehmerkreis, analyse.get().getAffectedTeilnehmerkreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());
  }


  @Test
  @DisplayName("HartesKriterium: TwoKlausurenSameTIme in Block Parallel Überschneidung mit kürzere Klausur")
  void test_Blocke2_Parallen_successful() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.PARALLEL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(59);
    Duration pruefungBDuration = Duration.ofMinutes(180);
    Duration pruefungCDuration = Duration.ofMinutes(59);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);

    when(aPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungADuration));
    when(bPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungBDuration));
    when(cPruefung.endzeitpunkt()).thenReturn(startBlock.plusMinutes(90).plus(pruefungCDuration));

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    //Fängt eine Stunde später erst an
    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock.plusMinutes(90));

    Set<Teilnehmerkreis> acTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> bTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(informatik);
    blockTeilnehmerkreisSet.add(bwl);
    bTeilnehmerkreisSet.add(bwl);
    acTeilnehmerkreisSet.add(informatik);

    Map<Teilnehmerkreis, Integer> acTeilnehmerKreis = new HashMap<>();
    acTeilnehmerKreis.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> bTeilnehmerKreis = new HashMap<>();
    bTeilnehmerKreis.put(bwl, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(bTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(acTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(acTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(bTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);

    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isEmpty());
  }


  @Test
  @DisplayName("HartesKriterium: TwoKlausurenSameTIme in Block Parallel Überschneidung mit kürzere Klausur Block liegt nach Pruefung")
  void test_Blocke2_Parallen_successful_Pruefung_Vor_Block() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.PARALLEL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 9, 30);
    Duration pruefungADuration = Duration.ofMinutes(59);
    Duration pruefungBDuration = Duration.ofMinutes(180);
    Duration pruefungCDuration = Duration.ofMinutes(59);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);

    when(aPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungADuration));
    when(bPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungBDuration));
    when(cPruefung.endzeitpunkt()).thenReturn(startBlock.minusMinutes(90).plus(pruefungCDuration));

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    //Fängt eine Stunde später erst an
    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock.minusMinutes(90));

    Set<Teilnehmerkreis> acTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> bTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(informatik);
    blockTeilnehmerkreisSet.add(bwl);
    bTeilnehmerkreisSet.add(bwl);
    acTeilnehmerkreisSet.add(informatik);

    Map<Teilnehmerkreis, Integer> acTeilnehmerKreis = new HashMap<>();
    acTeilnehmerKreis.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> bTeilnehmerKreis = new HashMap<>();
    bTeilnehmerKreis.put(bwl, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(bTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(acTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(acTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(bTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);

    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isEmpty());
  }


  @Test
  void test_Blocke2_SEQUENTIAL() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.SEQUENTIAL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    //Fängt eine Stunde später erst an
    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock.plusMinutes(60));

    Set<Teilnehmerkreis> acTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> bTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(informatik);
    blockTeilnehmerkreisSet.add(bwl);
    bTeilnehmerkreisSet.add(bwl);
    acTeilnehmerkreisSet.add(informatik);

    Map<Teilnehmerkreis, Integer> acTeilnehmerKreis = new HashMap<>();
    acTeilnehmerKreis.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> bTeilnehmerKreis = new HashMap<>();
    bTeilnehmerKreis.put(bwl, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(bTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(acTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(acTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(bTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(cPruefung);
    setOfConflictPruefunge.add(aPruefung);
    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);

    int studends = 8;

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);
    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isPresent());
    assertEquals(setOfConflictPruefunge, analyse.get().getCausingPruefungen());
    assertEquals(setOfConflictTeilnehmer, analyse.get().getAffectedTeilnehmerkreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());
  }

  @Test
  void test_Blocke2_Parallel_No_SameTeilnehmerkreise() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.PARALLEL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    //Fängt eine Stunde später erst an
    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock.plusMinutes(60));

    Set<Teilnehmerkreis> abTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> cTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(bwl);
    abTeilnehmerkreisSet.add(bwl);
    cTeilnehmerkreisSet.add(informatik);

    Map<Teilnehmerkreis, Integer> acTeilnehmerKreis = new HashMap<>();
    acTeilnehmerKreis.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> bTeilnehmerKreis = new HashMap<>();
    bTeilnehmerKreis.put(bwl, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(bTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(cTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(abTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(abTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);
    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isEmpty());

  }


  @Test
  void test_Blocke2_SEQUENTIAL_No_SameTeilnehmerkreise() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.SEQUENTIAL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    //Fängt eine Stunde später erst an
    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock.plusMinutes(60));

    Set<Teilnehmerkreis> abTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> cTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(informatik);
    blockTeilnehmerkreisSet.add(bwl);
    abTeilnehmerkreisSet.add(bwl);
    cTeilnehmerkreisSet.add(informatik);

    Map<Teilnehmerkreis, Integer> acTeilnehmerKreis = new HashMap<>();
    acTeilnehmerKreis.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> bTeilnehmerKreis = new HashMap<>();
    bTeilnehmerKreis.put(bwl, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(acTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(bTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(cTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(abTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(abTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);
    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isEmpty());

  }


  @Test
  void test_Blocke2_Sequential_LotPruefugnen() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.SEQUENTIAL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(90);
    Duration pruefungCDuration = Duration.ofMinutes(60);
    Duration pruefungDDuration = Duration.ofMinutes(60);
    Duration pruefungEDuration = Duration.ofMinutes(60);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);
    Planungseinheit d = mock(Planungseinheit.class);
    Planungseinheit e = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);
    Pruefung dPruefung = mock(Pruefung.class);
    Pruefung ePruefung = mock(Pruefung.class);

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);
    blockSetPruefungen.add(dPruefung);
    blockSetPruefungen.add(ePruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);
    when(ePruefung.isGeplant()).thenReturn(true);
    when(dPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);
    when(d.asPruefung()).thenReturn(dPruefung);
    when(e.asPruefung()).thenReturn(ePruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);
    when(dPruefung.getDauer()).thenReturn(pruefungDDuration);
    when(ePruefung.getDauer()).thenReturn(pruefungEDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(ePruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(dPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    //Fängt eine Stunde später erst an
    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock.plusMinutes(60));

    Set<Teilnehmerkreis> abdeTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> cTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(informatik);
    blockTeilnehmerkreisSet.add(bwl);
    abdeTeilnehmerkreisSet.add(bwl);
    abdeTeilnehmerkreisSet.add(informatik);

    cTeilnehmerkreisSet.add(informatik);
    cTeilnehmerkreisSet.add(bwl);

    Map<Teilnehmerkreis, Integer> abdeTeilnehmerKreis = new HashMap<>();
    abdeTeilnehmerKreis.put(informatik, 8);
    abdeTeilnehmerKreis.put(bwl, 8);

    Map<Teilnehmerkreis, Integer> cTeilnehmerKreis = new HashMap<>();
    cTeilnehmerKreis.put(bwl, 8);
    cTeilnehmerKreis.put(informatik, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(cTeilnehmerKreis);
    when(dPruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);
    when(ePruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(cTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);
    when(ePruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);
    when(dPruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(cPruefung);
    setOfConflictPruefunge.add(aPruefung);
    setOfConflictPruefunge.add(bPruefung);
    setOfConflictPruefunge.add(dPruefung);
    setOfConflictPruefunge.add(ePruefung);

    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);
    setOfConflictTeilnehmer.add(bwl);

    int studends = 16;

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);
    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isPresent());
    assertEquals(setOfConflictPruefunge, analyse.get().getCausingPruefungen());
    assertEquals(setOfConflictTeilnehmer, analyse.get().getAffectedTeilnehmerkreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());
  }


  @Test
  void test_Blocke2_PARALLEL_LotPruefugnen() throws IllegalTimeSpanException {

    Planungseinheit blockPlan = mock(Planungseinheit.class);

    when(blockPlan.isBlock()).thenReturn(true);

    Block blockA = mock(Block.class);

    when(blockPlan.asBlock()).thenReturn(blockA);
    when(blockA.getTyp()).thenReturn(Blocktyp.PARALLEL);

    LocalDateTime startBlock = LocalDateTime.of(2021, 8, 1, 8, 0);
    Duration pruefungADuration = Duration.ofMinutes(60);
    Duration pruefungBDuration = Duration.ofMinutes(60);
    Duration pruefungCDuration = Duration.ofMinutes(60);
    Duration pruefungDDuration = Duration.ofMinutes(60);
    Duration pruefungEDuration = Duration.ofMinutes(60);

    Planungseinheit a = mock(Planungseinheit.class);
    Planungseinheit b = mock(Planungseinheit.class);
    Planungseinheit c = mock(Planungseinheit.class);
    Planungseinheit d = mock(Planungseinheit.class);
    Planungseinheit e = mock(Planungseinheit.class);

    Pruefung aPruefung = mock(Pruefung.class);
    Pruefung bPruefung = mock(Pruefung.class);
    Pruefung cPruefung = mock(Pruefung.class);
    Pruefung dPruefung = mock(Pruefung.class);
    Pruefung ePruefung = mock(Pruefung.class);

    Set<Pruefung> blockSetPruefungen = new HashSet<>();
    blockSetPruefungen.add(bPruefung);
    blockSetPruefungen.add(aPruefung);
    blockSetPruefungen.add(dPruefung);
    blockSetPruefungen.add(ePruefung);

    when(blockA.getPruefungen()).thenReturn(blockSetPruefungen);
    when(blockA.isBlock()).thenReturn(true);

    when(aPruefung.isGeplant()).thenReturn(true);
    when(bPruefung.isGeplant()).thenReturn(true);
    when(cPruefung.isGeplant()).thenReturn(true);
    when(ePruefung.isGeplant()).thenReturn(true);
    when(dPruefung.isGeplant()).thenReturn(true);

    when(a.asPruefung()).thenReturn(aPruefung);
    when(b.asPruefung()).thenReturn(bPruefung);
    when(c.asPruefung()).thenReturn(cPruefung);
    when(d.asPruefung()).thenReturn(dPruefung);
    when(e.asPruefung()).thenReturn(ePruefung);

    when(aPruefung.getDauer()).thenReturn(pruefungADuration);
    when(bPruefung.getDauer()).thenReturn(pruefungBDuration);
    when(cPruefung.getDauer()).thenReturn(pruefungCDuration);
    when(dPruefung.getDauer()).thenReturn(pruefungDDuration);
    when(ePruefung.getDauer()).thenReturn(pruefungEDuration);

    when(blockA.getStartzeitpunkt()).thenReturn(startBlock);
    when(aPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(bPruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(ePruefung.getStartzeitpunkt()).thenReturn(startBlock);
    when(dPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    when(aPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungADuration));
    when(bPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungBDuration));
    when(cPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungCDuration));
    when(dPruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungDDuration));
    when(ePruefung.endzeitpunkt()).thenReturn(startBlock.plus(pruefungEDuration));

    when(cPruefung.getStartzeitpunkt()).thenReturn(startBlock);

    Set<Teilnehmerkreis> abdeTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> cTeilnehmerkreisSet = new HashSet<>();
    Set<Teilnehmerkreis> blockTeilnehmerkreisSet = new HashSet<>();

    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);

    blockTeilnehmerkreisSet.add(informatik);
    blockTeilnehmerkreisSet.add(bwl);
    abdeTeilnehmerkreisSet.add(bwl);
    abdeTeilnehmerkreisSet.add(informatik);

    cTeilnehmerkreisSet.add(informatik);
    cTeilnehmerkreisSet.add(bwl);

    Map<Teilnehmerkreis, Integer> abdeTeilnehmerKreis = new HashMap<>();
    abdeTeilnehmerKreis.put(informatik, 8);
    abdeTeilnehmerKreis.put(bwl, 8);

    Map<Teilnehmerkreis, Integer> cTeilnehmerKreis = new HashMap<>();
    cTeilnehmerKreis.put(bwl, 8);
    cTeilnehmerKreis.put(informatik, 8);

    when(aPruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);
    when(bPruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);
    when(cPruefung.getSchaetzungen()).thenReturn(cTeilnehmerKreis);
    when(dPruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);
    when(ePruefung.getSchaetzungen()).thenReturn(abdeTeilnehmerKreis);

    when(cPruefung.getTeilnehmerkreise()).thenReturn(cTeilnehmerkreisSet);
    when(aPruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);
    when(blockA.getTeilnehmerkreise()).thenReturn(blockTeilnehmerkreisSet);

    when(bPruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);
    when(ePruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);
    when(dPruefung.getTeilnehmerkreise()).thenReturn(abdeTeilnehmerkreisSet);

    when(blockA.getDauer()).thenReturn(Duration.ofMinutes(60));

    ArrayList<Planungseinheit> listToReturn = new ArrayList<>();
    listToReturn.add(blockPlan);
    when(this.dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        listToReturn);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(cPruefung);
    setOfConflictPruefunge.add(aPruefung);
    setOfConflictPruefunge.add(bPruefung);
    setOfConflictPruefunge.add(dPruefung);
    setOfConflictPruefunge.add(ePruefung);

    Set<Teilnehmerkreis> setOfConflictTeilnehmer = new HashSet<>();
    setOfConflictTeilnehmer.add(informatik);
    setOfConflictTeilnehmer.add(bwl);

    int studends = 16;

    TwoKlausurenSameTime h = new TwoKlausurenSameTime(this.dataAccessService);
    Optional<HartesKriteriumAnalyse> analyse = h.evaluate(cPruefung);
    assertTrue(analyse.isPresent());
    assertEquals(setOfConflictPruefunge, analyse.get().getCausingPruefungen());
    assertEquals(setOfConflictTeilnehmer, analyse.get().getAffectedTeilnehmerkreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());
  }


  private void setNameAndNummer(Pruefung analysis, String name) {
    when(analysis.getPruefungsnummer()).thenReturn(name);
    when(analysis.getName()).thenReturn(name);
    when(analysis.isGeplant()).thenReturn(true);
  }
}
