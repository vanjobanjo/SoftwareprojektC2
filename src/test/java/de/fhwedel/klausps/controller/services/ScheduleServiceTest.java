package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.hu.Ha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleServiceTest {

  private Pruefungsperiode pruefungsperiode;
  private DataAccessService dataAccessService;

  @BeforeEach
  void setUp() {
    this.pruefungsperiode = mock(Pruefungsperiode.class);

  }

  @Test
  void test() {
      LocalDateTime _010121_0800 = LocalDateTime.of(2021, 1, 1, 8, 0);
      LocalDateTime _010121_0900 = LocalDateTime.of(2021, 1, 1, 9, 0);
      LocalDateTime _010121_1000 = LocalDateTime.of(2021, 1, 1, 10, 0);
      LocalDateTime _020121_1000 = LocalDateTime.of(2021, 1, 2, 10, 0);
      Teilnehmerkreis BWL_10_1 = new TeilnehmerkreisImpl("BWL", "10", 1);
      Teilnehmerkreis WING_10_1 = new TeilnehmerkreisImpl("WING", "10", 1);
      Pruefung ANALYSIS_GEPLANT_010121_0800_DAUER120 =
              new PruefungImpl("1", "Analysis", "", Duration.ofMinutes(120), _010121_0800);
      Pruefung DM_GEPLANT_010121_0900_DAUER120 =
              new PruefungImpl("2", "DM", "", Duration.ofMinutes(120), _010121_0900);
      Pruefung HASKELL_GEPLANT_0201221_1000_DAUER90 =
              new PruefungImpl("3", "Haskell", "", Duration.ofMinutes(90));

      ANALYSIS_GEPLANT_010121_0800_DAUER120.setSchaetzung(BWL_10_1, 10);
      ANALYSIS_GEPLANT_010121_0800_DAUER120.setSchaetzung(WING_10_1, 20);

      DM_GEPLANT_010121_0900_DAUER120.setSchaetzung(WING_10_1, 30);
      DM_GEPLANT_010121_0900_DAUER120.setSchaetzung(BWL_10_1, 20);

      HASKELL_GEPLANT_0201221_1000_DAUER90.setSchaetzung(WING_10_1, 10);

      Set<Pruefung> pruefungen =
              new HashSet<>(
                      List.of(
                              ANALYSIS_GEPLANT_010121_0800_DAUER120,
                              DM_GEPLANT_010121_0900_DAUER120));


      when(pruefungsperiode.geplantePruefungen()).thenReturn(pruefungen);
      this.dataAccessService = new DataAccessService(pruefungsperiode);
    ScheduleService scheduleService = dataAccessService.getScheduleService();
    Set<Pruefung> conflictedPruefungToDm = scheduleService.getKriteriumsAnalyise(DM_GEPLANT_010121_0900_DAUER120).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
    assertThat(conflictedPruefungToDm).containsOnly(ANALYSIS_GEPLANT_010121_0800_DAUER120);
    Set<Pruefung> conflictedPruefungToAnalysis = scheduleService.getKriteriumsAnalyise(ANALYSIS_GEPLANT_010121_0800_DAUER120).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
    assertThat(conflictedPruefungToAnalysis).containsOnly(DM_GEPLANT_010121_0900_DAUER120);

    //NUR AB HIER NEUE KLAUSUREN EINBINDEN, SONST FUNKTIONIERT DER TEST NICHT MEHR

      scheduleService.schedulePruefung(HASKELL_GEPLANT_0201221_1000_DAUER90, _010121_1000);

      //DM, ANALYSIS UND HASKELL ALLE AM SELBEN TAG
      conflictedPruefungToDm = scheduleService.getKriteriumsAnalyise(DM_GEPLANT_010121_0900_DAUER120).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
      conflictedPruefungToAnalysis = scheduleService.getKriteriumsAnalyise(ANALYSIS_GEPLANT_010121_0800_DAUER120).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
      Set<Pruefung> conflictedPruefungToHaskell = scheduleService.getKriteriumsAnalyise(HASKELL_GEPLANT_0201221_1000_DAUER90).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);

      assertThat(conflictedPruefungToDm).containsOnly(ANALYSIS_GEPLANT_010121_0800_DAUER120, HASKELL_GEPLANT_0201221_1000_DAUER90);
      assertThat(conflictedPruefungToAnalysis).containsOnly(DM_GEPLANT_010121_0900_DAUER120, HASKELL_GEPLANT_0201221_1000_DAUER90);
      assertThat(conflictedPruefungToHaskell).containsOnly(DM_GEPLANT_010121_0900_DAUER120, ANALYSIS_GEPLANT_010121_0800_DAUER120);


      //HASKELL WIRD ENTFERNT MUSS AUS KLAUSUREN RAUS SEIN!
      scheduleService.unschedulePruefung(HASKELL_GEPLANT_0201221_1000_DAUER90);
      conflictedPruefungToDm = scheduleService.getKriteriumsAnalyise(DM_GEPLANT_010121_0900_DAUER120).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
      assertThat(conflictedPruefungToDm).containsOnly(ANALYSIS_GEPLANT_010121_0800_DAUER120);
      conflictedPruefungToAnalysis = scheduleService.getKriteriumsAnalyise(ANALYSIS_GEPLANT_010121_0800_DAUER120).get(WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG);
      assertThat(conflictedPruefungToAnalysis).containsOnly(DM_GEPLANT_010121_0900_DAUER120);
  }
}
