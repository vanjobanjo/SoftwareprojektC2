package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.restriction.hard.TwoKlausurenSameTime;
import de.fhwedel.klausps.controller.services.DataAccessService;
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
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MehrePruefungenAmTagTest {


  private DataAccessService dataAccessService;

  @BeforeEach
  void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
  }


  @Test
  void klausurSameDay() {

    MehrePruefungenAmTag mehrePruefungenAmTag = new MehrePruefungenAmTag(dataAccessService);

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

    Set<ReadOnlyPruefung> setOfConflictROPruefunge = new HashSet<>();
    setOfConflictROPruefunge.add(new PruefungDTOBuilder(analysis).build());
    setOfConflictROPruefunge.add(new PruefungDTOBuilder(haskel).build());

    Set<Pruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(analysis);
    setOfConflictPruefunge.add(haskel);

    Set<Teilnehmerkreis> setOfConflictTeilnehmerkreis = new HashSet<>();
    setOfConflictTeilnehmerkreis.add(informatik);

    Set<Teilnehmerkreis> haskelTeilnehmer3 = new HashSet<>();
    haskelTeilnehmer3.add(informatik);

    Set<Teilnehmerkreis> analysisTeilnehmer1 = new HashSet<>();
    analysisTeilnehmer1.add(informatik);

    int studends = 8;

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

    ArrayList<Planungseinheit> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(haskelPL);
    listOfPruefungen.add(analysisPL);

    Set<Planungseinheit> setOfPruefungen = new HashSet<>();
    setOfPruefungen.add(haskelPL);
    setOfPruefungen.add(analysisPL);

    try {
      when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
          listOfPruefungen);
    } catch (IllegalTimeSpanException e) {

      //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);
    when(haskel.isGeplant()).thenReturn(true);



    Optional<WeichesKriteriumAnalyse> analyse = mehrePruefungenAmTag.evaluate(haskel);
    assertTrue(analyse.isPresent());
    assertThat(analyse.get().getCausingPruefungen()).containsAll(setOfConflictPruefunge);
    assertEquals(setOfConflictTeilnehmerkreis, analyse.get().getAffectedTeilnehmerKreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());

    assertThat(mehrePruefungenAmTag.setReadyOnly).containsAll(setOfConflictROPruefunge);
    Assertions.assertEquals(setOfConflictROPruefunge, mehrePruefungenAmTag.setReadyOnly);
    Assertions.assertEquals(setOfConflictTeilnehmerkreis, mehrePruefungenAmTag.setTeilnehmer);
    assertEquals(studends, mehrePruefungenAmTag.countStudents);
  }


  @Test
  void klausurSameDay_TestWithBloeck() {

    MehrePruefungenAmTag mehrePruefungenAmTag = new MehrePruefungenAmTag(dataAccessService);

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

    Set<ReadOnlyPruefung> setOfConflictROPruefunge = new HashSet<>();
    setOfConflictROPruefunge.add(new PruefungDTOBuilder(analysis).build());
    setOfConflictROPruefunge.add(new PruefungDTOBuilder(haskel).build());

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

    int studends = 8;

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

    ArrayList<Planungseinheit> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(haskelPL);
    listOfPruefungen.add(blockPL);

    //Block alles einfügen
    Set<Teilnehmerkreis> blockTeilnehmerKreise = new HashSet<>();
    blockTeilnehmerKreise.add(bwl);
    blockTeilnehmerKreise.add(informatik);

    when(blockPL.getTeilnehmerkreise()).thenReturn(blockTeilnehmerKreise);

    Set<Pruefung> blockPrufungen = new HashSet<>();
    blockPrufungen.add(dm);
    blockPrufungen.add(analysis);

    Block block = mock(Block.class);
    when(block.getTeilnehmerkreise()).thenReturn(blockTeilnehmerKreise);
    when(block.getPruefungen()).thenReturn(blockPrufungen);
    when(blockPL.isBlock()).thenReturn(true);

    when(blockPL.asBlock()).thenReturn(block);

    try {
      when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
          listOfPruefungen);
    } catch (IllegalTimeSpanException e) {

      //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);
    when(haskel.isGeplant()).thenReturn(true);

    Optional<WeichesKriteriumAnalyse> analyse = mehrePruefungenAmTag.evaluate(haskel);

    assertTrue(analyse.isPresent());
    assertThat(analyse.get().getCausingPruefungen()).containsAll(setOfConflictPruefunge);
    assertEquals(setOfConflictTeilnehmerkreis, analyse.get().getAffectedTeilnehmerKreise());
    assertEquals(studends, analyse.get().getAmountAffectedStudents());
    assertTrue(analyse.isPresent());

    assertThat(mehrePruefungenAmTag.setReadyOnly).containsExactlyElementsOf(setOfConflictROPruefunge);
    Assertions.assertEquals(setOfConflictROPruefunge, mehrePruefungenAmTag.setReadyOnly);
    Assertions.assertEquals(setOfConflictTeilnehmerkreis, mehrePruefungenAmTag.setTeilnehmer);
    assertEquals(studends, mehrePruefungenAmTag.countStudents);
  }

  private void setNameAndNummer(Pruefung analysis, String name) {
    when(analysis.getPruefungsnummer()).thenReturn(name);
    when(analysis.getName()).thenReturn(name);
  }

}
