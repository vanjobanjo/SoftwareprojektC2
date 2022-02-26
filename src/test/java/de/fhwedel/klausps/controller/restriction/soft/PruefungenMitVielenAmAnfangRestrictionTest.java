package de.fhwedel.klausps.controller.restriction.soft;


import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.PRUEFUNGEN_MIT_VIELEN_AN_ANFANG;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_ANALYSIS_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_DM_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.getPruefungOfReadOnlyPruefung;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.infMaster;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Grenzfälle:
 * <ol>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_pruefung_is_not_planned()
 *   Wenn die Prüfung nicht geplant ist<br>&rarr; empty}</li>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_too_many_teilnehmer_but_at_beginning_default_values()
 *   Eine Prüfung hat genauso viele Teilnehmer, wie der minimale Wert, der das Kriterium verletzt und liegt innerhalb der Anfangszeit<br>&rarr; empty}</li>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_too_many_teilnehmer_and_not_at_beginning_default_values()
 *   Eine Prüfung hat genauso viele Teilnehmer, wie der minimale Wert, der das Kriterium verletzt und liegt außerhalb der Anfangszeit<br>&rarr; Kriterium wird verletzt}</li>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_not_too_many_teilnehmer_and_at_beginning()
 *   Eine Prüfung hat weniger Teilnehmer als der minimale Wert, der das Kriterium verletzt und liegt innerhalb der Anfangszeit<br>&rarr; empty}</li>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_not_too_many_teilnehmer_not_at_beginning()
 *   Eine Prüfung hat weniger Teilnehmer als der minimale Wert, der das Kriterium verletzt und liegt außerhalb der Anfangszeit<br>&rarr; empty}</li>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_too_many_teilnehmer_but_at_beginning()
 *   Eine Prüfung hat mehr Teilnehmer, als der minimale Wert, der das Kriterium verletzt und liegt innerhalb der Anfangszeit<br>&rarr; empty}</li>
 *   <li>{@link PruefungenMitVielenAmAnfangRestrictionTest#pruefungenMitVielenAmAnfang_too_many_teilnehmer_and_not_at_beginning()
 *   Eine Prüfung hat mehr Teilnehmer, als der minimale Wert, der das Kriterium verletzt und liegt außerhalb der Anfangszeit<br>&rarr; Kriterium wird verletzt}</li>
 * </ol>
 */
class PruefungenMitVielenAmAnfangRestrictionTest {

  private PruefungenMitVielenAmAnfangRestriction deviceUnderTest;
  private DataAccessService dataAccessService;

  @BeforeEach
  void setup() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService);
  }

  @Test
  void pruefungenMitVielenAmAnfang_null_parameters() {
    assertThrows(NullPointerException.class, () -> deviceUnderTest.evaluateRestriction(null));
  }

 /*@Test
  void pruefungenMitVielenAmAnfang_pruefung_does_not_exist() {
   Pruefung pruefung = mock(Pruefung.class);
    assertThrows(IllegalArgumentException.class, () -> deviceUnderTest.evaluateRestriction(pruefung));
  }*/


  @Test
  void pruefungenMitVielenAmAnfang_pruefung_is_not_planned()
      throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = mock(Pruefung.class);
    when(pruefung.isGeplant()).thenReturn(false);
    assertThat(deviceUnderTest.evaluateRestriction(pruefung)).isEmpty();
  }


  @Test
  void pruefungenMitVielenAmAnfang_too_many_teilnehmer_but_at_beginning_default_values()
      throws NoPruefungsPeriodeDefinedException {
    Duration beginAfterAnker = Duration.ofDays(7);
    deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService, 100,
        beginAfterAnker);
    LocalDate ankerTag = LocalDate.of(2022, 1, 12);

    Pruefung moreTeilnehmer = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung lessTeilnehmer = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    moreTeilnehmer.addTeilnehmerkreis(infBachelor, 100);
    lessTeilnehmer.addTeilnehmerkreis(infBachelor, 10);
    moreTeilnehmer.setStartzeitpunkt(ankerTag.atTime(8, 0));
    when(dataAccessService.getPlannedPruefungen()).thenReturn(
        Set.of(moreTeilnehmer, lessTeilnehmer));
    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);
    assertThat(deviceUnderTest.evaluateRestriction(moreTeilnehmer)).isEmpty();
  }

  @Test
  void pruefungenMitVielenAmAnfang_too_many_teilnehmer_but_at_beginning()
      throws NoPruefungsPeriodeDefinedException {
    Duration beginAfterAnker = Duration.ofDays(7);
    deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService, 100,
        beginAfterAnker);
    LocalDate ankerTag = LocalDate.of(2022, 1, 12);

    Pruefung moreTeilnehmer = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung lessTeilnehmer = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    moreTeilnehmer.addTeilnehmerkreis(infBachelor, 300);
    lessTeilnehmer.addTeilnehmerkreis(infBachelor, 10);
    moreTeilnehmer.setStartzeitpunkt(ankerTag.atTime(8, 0));
    lessTeilnehmer.setStartzeitpunkt(ankerTag.atTime(8, 0));
    when(dataAccessService.getPlannedPruefungen()).thenReturn(
        Set.of(moreTeilnehmer, lessTeilnehmer));
    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);
    assertThat(deviceUnderTest.evaluateRestriction(moreTeilnehmer)).isEmpty();
  }

  @Test
  void pruefungenMitVielenAmAnfang_too_many_teilnehmer_and_not_at_beginning()
      throws NoPruefungsPeriodeDefinedException {

    Duration beginAfterAnker = Duration.ofDays(7);
    deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService, 100,
        beginAfterAnker);
    LocalDate ankerTag = LocalDate.of(2022, 1, 12);

    Pruefung moreTeilnehmer = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung lessTeilnehmer = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    int schaetzungMore = 200;
    moreTeilnehmer.addTeilnehmerkreis(infBachelor, schaetzungMore);
    lessTeilnehmer.addTeilnehmerkreis(infBachelor, 10);
    moreTeilnehmer.setStartzeitpunkt(ankerTag.plusDays(7).atTime(8, 0));
    lessTeilnehmer.setStartzeitpunkt(ankerTag.plusDays(7).atTime(8, 0));

    when(dataAccessService.getPlannedPruefungen()).thenReturn(
        Set.of(moreTeilnehmer, lessTeilnehmer));
    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);

    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(moreTeilnehmer);
    assertThat(result).isPresent();
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(schaetzungMore);
    assertThat(result.get().getDeltaScoring()).isEqualTo(PRUEFUNGEN_MIT_VIELEN_AN_ANFANG.getWert());
    assertThat(result.get().getCausingPruefungen()).containsOnly(moreTeilnehmer);
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsOnly(infBachelor);
    assertThat(result.get().getKriterium()).isEqualTo(PRUEFUNGEN_MIT_VIELEN_AN_ANFANG);
  }


  @Test
  void pruefungenMitVielenAmAnfang_not_too_many_teilnehmer_and_at_beginning()
      throws NoPruefungsPeriodeDefinedException {

    Duration beginAfterAnker = Duration.ofDays(7);
    deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService, 100,
        beginAfterAnker);
    LocalDate ankerTag = LocalDate.of(2022, 1, 12);

    Pruefung moreTeilnehmer = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung lessTeilnehmer = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    int schaetzungMore = 100;
    moreTeilnehmer.addTeilnehmerkreis(infBachelor, schaetzungMore);
    lessTeilnehmer.addTeilnehmerkreis(infBachelor, 10);
    lessTeilnehmer.setStartzeitpunkt(ankerTag.atTime(8, 0));
    moreTeilnehmer.setStartzeitpunkt(ankerTag.atTime(8, 0));

    when(dataAccessService.getPlannedPruefungen()).thenReturn(
        Set.of(moreTeilnehmer, lessTeilnehmer));
    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(lessTeilnehmer);
    assertThat(result).isEmpty();
  }

  @Test
  void pruefungenMitVielenAmAnfang_not_too_many_teilnehmer_not_at_beginning()
      throws NoPruefungsPeriodeDefinedException {
    Duration beginAfterAnker = Duration.ofDays(7);
    deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService, 100,
        beginAfterAnker);

    LocalDate ankerTag = LocalDate.of(2022, 1, 12);

    Pruefung moreTeilnehmer = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung lessTeilnehmer = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    int schaetzungMore = 10;
    moreTeilnehmer.addTeilnehmerkreis(infBachelor, schaetzungMore);
    lessTeilnehmer.addTeilnehmerkreis(infBachelor, 10);
    lessTeilnehmer.setStartzeitpunkt(ankerTag.plusDays(beginAfterAnker.toDays()).atTime(8, 0));
    moreTeilnehmer.setStartzeitpunkt(ankerTag.plusDays(beginAfterAnker.toDays()).atTime(8, 0));

    when(dataAccessService.getPlannedPruefungen()).thenReturn(
        Set.of(moreTeilnehmer, lessTeilnehmer));
    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(lessTeilnehmer);
    assertThat(result).isEmpty();
  }

  @Test
  void pruefungenMitVielenAmAnfang_too_many_teilnehmer_and_not_at_beginning_default_values()
      throws NoPruefungsPeriodeDefinedException {
    Duration beginAfterAnker = Duration.ofDays(7);
    LocalDate ankerTag = LocalDate.of(2022, 1, 12);
    deviceUnderTest = new PruefungenMitVielenAmAnfangRestriction(dataAccessService, 100,
        beginAfterAnker);
    Pruefung moreTeilnehmer = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Pruefung lessTeilnehmer = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    int schaetzungMore = 100;
    moreTeilnehmer.addTeilnehmerkreis(infBachelor, schaetzungMore);
    lessTeilnehmer.addTeilnehmerkreis(infMaster, 10);
    moreTeilnehmer.setStartzeitpunkt(
        ankerTag.plusDays(beginAfterAnker.toDays()).atTime(8, 0));
    when(dataAccessService.getPlannedPruefungen()).thenReturn(
        Set.of(moreTeilnehmer, lessTeilnehmer));
    when(dataAccessService.getAnkertag()).thenReturn(ankerTag);
    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluateRestriction(moreTeilnehmer);
    assertThat(result).isPresent();
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(schaetzungMore);
    assertThat(result.get().getCausingPruefungen()).containsOnly(moreTeilnehmer);
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsOnly(infBachelor);
    assertThat(result.get().getDeltaScoring()).isEqualTo(PRUEFUNGEN_MIT_VIELEN_AN_ANFANG.getWert());
    assertThat(result.get().getKriterium()).isEqualTo(PRUEFUNGEN_MIT_VIELEN_AN_ANFANG);
  }

}