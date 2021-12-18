package de.fhwedel.klausps.controller.restriction.soft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    MehrePruefungenAmTag mehrePruefungenAmTag = new MehrePruefungenAmTag(dataAccessService,
        WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);

    Planungseinheit analysisPL = mock(Planungseinheit.class);
    Planungseinheit haskelPL = mock(Planungseinheit.class);

    Pruefung analysis = mock(Pruefung.class);
    Pruefung haskel = mock(Pruefung.class);

    when(analysis.getPruefungsnummer()).thenReturn("analysis");
    when(haskel.getPruefungsnummer()).thenReturn("haskel");

    when(analysis.getName()).thenReturn("analysis");
    when(haskel.getName()).thenReturn("haskel");

    Set<Teilnehmerkreis> teilnehmer1 = new HashSet<>();
    Teilnehmerkreis informatik = mock(Teilnehmerkreis.class);
    teilnehmer1.add(informatik);

    when(analysis.getTeilnehmerkreise()).thenReturn(teilnehmer1);
    when(haskel.getTeilnehmerkreise()).thenReturn(teilnehmer1);

    Set<ReadOnlyPruefung> setOfConflictPruefunge = new HashSet<>();
    setOfConflictPruefunge.add(new PruefungDTOBuilder(analysis).build());
    setOfConflictPruefunge.add(new PruefungDTOBuilder(haskel).build());

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

    ArrayList<Pruefung> listOfPruefungen = new ArrayList<>();
    listOfPruefungen.add(haskel);
    listOfPruefungen.add(analysis);

    try {
      when(dataAccessService.getAllPruefungenBetween(any(), any())).thenReturn(listOfPruefungen);
    } catch (IllegalTimeSpanException e) {

      //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
      e.printStackTrace();
    }

    Duration duration = Duration.ofMinutes(120);

    when(haskel.getStartzeitpunkt()).thenReturn(start);
    when(haskel.getDauer()).thenReturn(duration);

    assertTrue(mehrePruefungenAmTag.test(haskel));

    assertThat(mehrePruefungenAmTag.setReadyOnly).containsExactlyElementsOf(setOfConflictPruefunge);
    Assertions.assertEquals(setOfConflictPruefunge, mehrePruefungenAmTag.setReadyOnly);
    Assertions.assertEquals(setOfConflictTeilnehmerkreis, mehrePruefungenAmTag.setTeilnehmer);
    assertEquals(studends, mehrePruefungenAmTag.countStudents);
  }

}