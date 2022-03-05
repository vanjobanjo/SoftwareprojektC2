package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MehrePruefungenAmTagRestrictionTest {


  private DataAccessService dataAccessService;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
  }


  @Test
  void klausurSameDay() throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {

    MehrePruefungenAmTagRestriction mehrePruefungenAmTagRestriction = new MehrePruefungenAmTagRestriction(dataAccessService);

    Pruefung analysisPL = mock(Pruefung.class);
    Pruefung haskelPL = mock(Pruefung.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    setNameAndNummer(analysis, "analysis");
    setNameAndNummer(haskel, "haskel");

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    teilnehmer1.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer1);

    Set<Pruefung> setOfConflictPruefungen = new HashSet<>();
    setOfConflictPruefungen.add(analysis);
    setOfConflictPruefungen.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmerkreis = new HashSet<>();
    setOfConflictTeilnehmerkreis.add(informatik);

    Set<Teilnehmerkreis> haskelTeilnehmer3 = new HashSet<>();
    haskelTeilnehmer3.add(informatik);

    Set<Teilnehmerkreis> analysisTeilnehmer1 = new HashSet<>();
    analysisTeilnehmer1.add(informatik);

    int students = 8;

    Map<Teilnehmerkreis, Integer> teilnehmerCount1 = new HashMap<>();
    teilnehmerCount1.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> teilnehmerCount3 = new HashMap<>();
    teilnehmerCount3.put(informatik, 8);

    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount1);
    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount3);

    when(analysis.getTeilnehmerkreise()).thenReturn(analysisTeilnehmer1);
    when(haskel.getTeilnehmerkreise()).thenReturn(haskelTeilnehmer3);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(haskelPL, analysisPL));

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);
    when(haskel.isGeplant()).thenReturn(true);

    Optional<SoftRestrictionAnalysis> analyse = mehrePruefungenAmTagRestriction.evaluateRestriction(haskel);
    assertTrue(analyse.isPresent());
    assertThat(analyse.get().getAffectedPruefungen()).containsAll(setOfConflictPruefungen);
    assertEquals(setOfConflictTeilnehmerkreis, analyse.get().getAffectedTeilnehmerKreise());
    assertEquals(students, analyse.get().getAmountAffectedStudents());

    assertThat(analyse.get().getAffectedTeilnehmerKreise()).containsAll(
        setOfConflictTeilnehmerkreis);
    assertThat(analyse.get().getAffectedPruefungen()).containsAll(setOfConflictPruefungen);
    assertThat(analyse.get().getAmountAffectedStudents()).isEqualTo(students);


  }


  @Test
  void klausurSameDay_TestWithBlock()
      throws NoPruefungsPeriodeDefinedException, IllegalTimeSpanException {

    MehrePruefungenAmTagRestriction mehrePruefungenAmTagRestriction = new MehrePruefungenAmTagRestriction(dataAccessService);

    Planungseinheit analysisPL = mock(Pruefung.class);
    Planungseinheit haskelPL = mock(Pruefung.class);
    Planungseinheit dmPL = mock(Pruefung.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);
    Pruefung dm = mock(Pruefung.class);

    Planungseinheit blockPL = mock(Block.class);

    setNameAndNummer(analysis, "analysis");
    setNameAndNummer(haskel, "haskel");
    setNameAndNummer(dm, "dm");

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    teilnehmer1.add(informatik);

    Set<Teilnehmerkreis> teilnehmer2 = new HashSet<>();
    Teilnehmerkreis bwl = mock(Teilnehmerkreis.class);
    teilnehmer2.add(bwl);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(dm.getTeilnehmerkreise()).thenReturn(teilnehmer2);

    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer1);

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmerkreis = new HashSet<>();
    setOfConflictTeilnehmerkreis.add(informatik);

    Set<Teilnehmerkreis> haskelTeilnehmer3 = new HashSet<>();
    haskelTeilnehmer3.add(informatik);

    Set<Teilnehmerkreis> dmTeilnehmer2 = new HashSet<>();
    dmTeilnehmer2.add(bwl);

    Set<Teilnehmerkreis> analysisTeilnehmer1 = new HashSet<>();
    analysisTeilnehmer1.add(informatik);

    int students = 8;

    Map<Teilnehmerkreis, Integer> teilnehmerCount1 = new HashMap<>();
    teilnehmerCount1.put(informatik, 8);

    Map<Teilnehmerkreis, Integer> teilnehmerCount2 = new HashMap<>();
    teilnehmerCount2.put(bwl, 8);

    Map<Teilnehmerkreis, Integer> teilnehmerCount3 = new HashMap<>();
    teilnehmerCount3.put(informatik, 8);

    when(analysis.getSchaetzungen()).thenReturn(teilnehmerCount1);
    when(haskel.getSchaetzungen()).thenReturn(teilnehmerCount3);
    when(dm.getSchaetzungen()).thenReturn(teilnehmerCount2);

    when(analysis.getTeilnehmerkreise()).thenReturn(analysisTeilnehmer1);
    when(haskel.getTeilnehmerkreise()).thenReturn(haskelTeilnehmer3);
    when(dm.getTeilnehmerkreise()).thenReturn(dmTeilnehmer2);

    LocalDateTime start = LocalDateTime.of(2021, 8, 11, 9, 0);

    when(analysisPL.asPruefung()).thenReturn(analysis);
    when(haskelPL.asPruefung()).thenReturn(haskel);
    when(dmPL.asPruefung()).thenReturn(dm);

    //Block alles einfügen
    Set<Teilnehmerkreis> blockTeilnehmerKreise = new HashSet<>();
    blockTeilnehmerKreise.add(bwl);
    blockTeilnehmerKreise.add(informatik);

    when(blockPL.getTeilnehmerkreise()).thenReturn(blockTeilnehmerKreise);

    Set<Pruefung> blockPruefungen = new HashSet<>();
    blockPruefungen.add(dm);
    blockPruefungen.add(analysis);

    Block block = mock(Block.class);
    when(block.getTeilnehmerkreise()).thenReturn(blockTeilnehmerKreise);
    when(block.getPruefungen()).thenReturn(blockPruefungen);
    when(blockPL.isBlock()).thenReturn(true);

    when(blockPL.asBlock()).thenReturn(block);

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(haskelPL, blockPL));

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);
    when(haskel.isGeplant()).thenReturn(true);

    Optional<SoftRestrictionAnalysis> analyse = mehrePruefungenAmTagRestriction.evaluateRestriction(haskel);

    assertTrue(analyse.isPresent());
    assertThat(analyse.get().getAffectedPruefungen()).containsAll(setOfConflictPruefunge);
    assertEquals(setOfConflictTeilnehmerkreis, analyse.get().getAffectedTeilnehmerKreise());
    assertEquals(students, analyse.get().getAmountAffectedStudents());

    assertThat(analyse.get().getAffectedTeilnehmerKreise()).containsAll(
        setOfConflictTeilnehmerkreis);
    assertThat(analyse.get().getAffectedPruefungen()).containsAll(setOfConflictPruefunge);
    assertThat(analyse.get().getAmountAffectedStudents()).isEqualTo(students);


  }

  private void setNameAndNummer(Pruefung analysis, String name) {
    when(analysis.getPruefungsnummer()).thenReturn(name);
    when(analysis.getName()).thenReturn(name);
  }

}
