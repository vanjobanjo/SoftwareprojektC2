package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.FREIER_TAG_ZWISCHEN_PRUEFUNGEN;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_ANALYSIS_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_DM_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.RO_HASKELL_UNPLANNED;
import static de.fhwedel.klausps.controller.util.TestFactory.bwlBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.getPruefungOfReadOnlyPruefung;
import static de.fhwedel.klausps.controller.util.TestFactory.infBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.infMaster;
import static de.fhwedel.klausps.controller.util.TestFactory.infPtl;
import static de.fhwedel.klausps.controller.util.TestFactory.wingBachelor;
import static de.fhwedel.klausps.controller.util.TestFactory.wingMaster;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * Testfälle
 * <ol>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#not_planned() Prüfung ist nicht geplant} </li>
 *   <br>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#same_day_no_overlap() gleicher Tag  + keine Überschneidungen} </li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#same_day_overlap() gleicher Tag  + Überschneidung}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#same_day_overlap_multiple() gleicher Tag  + mehrere Überschneidungen}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#same_day_overlap_block()  gleicher Tag  + Überschneidungen  + im Block}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#same_day_overlap_one_in_block()  gleicher Tag  + Überschneidungen  + eine Block}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#same_day_overlap_different_blocks() gleicher Tag  + Überschneidungen  + unterschiedliche Blöcke}</li>
 *   <br>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_before_no_overlap()  ein Tag davor + keine Überschneidungen}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_before_overlap() ein Tag davor  + Überschneidung}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_before_overlap_multiple() ein Tag davor  + mehrere Überschneidungen}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_before_overlap_different_years() ein Tag davor + Überschneidungen + Jahreswechsel}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_before_overlap_different_blocks() ein Tag davor + Überschneidungen + unterschiedliche Blöcke}</li>
 *   <br>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_after_no_overlap() ein Tag danach + keine Überschneidungen}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_after_overlap() ein Tag danach  + Überschneidung}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_after_overlap_multiple() ein Tag danach  + mehrere Überschneidungen}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_after_overlap_different_years() ein Tag danach  + Überschneidungen + Jahreswechsel}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#day_after_overlap_different_blocks() ein Tag danach + Überschneidungen  + unterschiedliche Blöcke}</li>
 *   <br>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_no_overlap() keine Überschneidungen mehr als einen Tag davor}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_overlap() Überschneidung mehr als einen Tag davor}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_overlap_multiple() mehrere Überschneidungen mehr als einen Tag davor}</li>
 *   <br>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_after_no_overlap() keine Überschneidungen mehr als einen Tag danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_after_overlap() Überschneidung mehr als einen Tag danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_after_overlap_multiple() mehrere Überschneidungen mehr als einen Tag danach}</li>
 *   <br>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#one_day_before_and_after_no_overlap() keine Überschneidungen einen Tag davor + danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#one_day_before_and_after_overlap() Überschneidungen einen Tag davor + danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#one_day_before_and_after_overlap_before() einen Tag davor + danach + Überschneidung davor}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#one_day_before_and_after_overlap_after() einen Tag davor + danach + Überschneidung danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_and_after_no_overlap() keine Überschneidungen mehr als einen Tag davor + danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_and_after_overlap() Überschneidungen mehr als einen Tag davor + danach}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_and_after_overlap_before() mehr als  einen Tag davor + danach + Überschneidung davor}</li>
 *   <li>{@link FreierTagZwischenPruefungenRestrictionTest#more_than_one_day_before_and_after_overlap_after() mehr als einen Tag davor + danach + Überschneidung danach}</li>
 * </ol>
 */

class FreierTagZwischenPruefungenRestrictionTest {

  public FreierTagZwischenPruefungenRestriction deviceUnderTest;
  public DataAccessService dataAccessService;
  private static final int SCORING = FREIER_TAG_ZWISCHEN_PRUEFUNGEN.getWert();

  @BeforeEach
  public void setUp() {
    this.dataAccessService = mock(DataAccessService.class);
    this.deviceUnderTest = new FreierTagZwischenPruefungenRestriction(dataAccessService);
  }

  @Test
  @DisplayName("Prüfung ist nicht geplant")
  void not_planned() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    modelAnalysis.addTeilnehmerkreis(bwlBachelor);

    Pruefung modelDM = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    modelDM.addTeilnehmerkreis(infBachelor);
    modelDM.setStartzeitpunkt(date);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(modelDM));

    assertThat(deviceUnderTest.evaluate(modelAnalysis)).isEmpty();
  }

  // ----------------------------------------------------------------------------
  // ----------------------------- gleicher Tag ---------------------------------
  // ----------------------------------------------------------------------------

  @Test
  @DisplayName("gleicher Tag + keine Überschneidungen")
  void same_day_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    modelAnalysis.addTeilnehmerkreis(bwlBachelor);
    modelAnalysis.setStartzeitpunkt(date);
    Pruefung modelDM = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    modelDM.addTeilnehmerkreis(infBachelor);
    modelDM.setStartzeitpunkt(date);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(modelDM, modelAnalysis));

    assertThat(deviceUnderTest.evaluate(modelAnalysis)).isEmpty();
  }


  @Test
  @DisplayName("gleicher Tag + Überschneidungen")
  void same_day_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int amount_infBachelor = 10;
    analysis.addTeilnehmerkreis(bwlBachelor, 3);
    analysis.addTeilnehmerkreis(infBachelor, amount_infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(date);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, amount_infBachelor);
  }

  @Test
  @DisplayName("gleicher Tag + mehrere Überschneidungen")
  void same_day_overlap_multiple() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int infB = 20;
    int bwlB = 23;
    int affected = infB + bwlB;
    analysis.addTeilnehmerkreis(bwlBachelor, bwlB);
    analysis.addTeilnehmerkreis(infBachelor, infB);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, infB);
    dm.setStartzeitpunkt(date);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor, bwlB);
    haskell.setStartzeitpunkt(date);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    validateResults(analysis, Set.of(dm, haskell), Set.of(infBachelor, bwlBachelor), affected);
  }

  @Test
  @DisplayName("gleicher Tag + Überschneidungen + block")
  void same_day_overlap_block() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(date);
    getBlockWithPruefungen(pruefungsperiode, "b", date, analysis, dm);
    when(dataAccessService.areInSameBlock(analysis, dm)).thenReturn(true);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("gleicher Tag + Überschneidungen + unterschiedliche blöcke")
  void same_day_overlap_different_blocks() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int infB = 1;
    analysis.addTeilnehmerkreis(bwlBachelor, 23);
    analysis.addTeilnehmerkreis(infBachelor, infB);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, infB);
    dm.setStartzeitpunkt(date);
    getBlockWithPruefungen(pruefungsperiode, "b", date, analysis);
    getBlockWithPruefungen(pruefungsperiode, "b2", date, dm);
    when(dataAccessService.areInSameBlock(analysis, dm)).thenReturn(false);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, infB);
  }

  @Test
  @DisplayName("gleicher Tag + Überschneidungen + ein Block")
  void same_day_overlap_one_in_block() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 123;
    analysis.addTeilnehmerkreis(bwlBachelor, affected);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(date);
    getBlockWithPruefungen(pruefungsperiode, "b", date, analysis);
    when(dataAccessService.areInSameBlock(analysis, dm)).thenReturn(false);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, affected);
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- ein Tag davor ---------------------------------
  // ----------------------------------------------------------------------------

  @Test
  @DisplayName("Tag davor + keine Überschneidungen")
  void day_before_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2021, 12, 31, 8, 0);
    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    modelAnalysis.addTeilnehmerkreis(bwlBachelor);
    modelAnalysis.setStartzeitpunkt(date);
    Pruefung modelDM = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    modelDM.addTeilnehmerkreis(infBachelor);
    modelDM.setStartzeitpunkt(dayBefore);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(modelDM, modelAnalysis));

    assertThat(deviceUnderTest.evaluate(modelAnalysis)).isEmpty();
  }


  @Test
  @DisplayName("Tag davor + Überschneidungen")
  void day_before_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Random random = new Random();
    int affected = random.nextInt(400);
    analysis.addTeilnehmerkreis(bwlBachelor, affected);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(dayBefore);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));
    validateResults(analysis, dm, infBachelor, affected);
  }

  @Test
  @DisplayName("Tag davor + mehrere Überschneidungen")
  void day_before_overlap_multiple() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    Random random = new Random();
    int schaetzung = random.nextInt(300);
    int affected = 2 * schaetzung;
    analysis.addTeilnehmerkreis(bwlBachelor, schaetzung);
    analysis.addTeilnehmerkreis(infBachelor, schaetzung);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, schaetzung);
    dm.setStartzeitpunkt(dayBefore);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor, schaetzung);
    haskell.setStartzeitpunkt(dayBefore);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    validateResults(analysis, Set.of(dm, haskell), Set.of(infBachelor, bwlBachelor), affected);
  }

  @Test
  @DisplayName("Tag davor + Überschneidungen + jahreswechsel")
  void day_before_overlap_different_years() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2021, 12, 31, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 234;
    analysis.addTeilnehmerkreis(bwlBachelor, 1);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(dayBefore);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, affected);
  }


  @Test
  @DisplayName("Tag davor + Überschneidungen + unterschiedliche blöcke")
  void day_before_overlap_different_blocks() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    LocalDateTime date = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 34;
    analysis.addTeilnehmerkreis(bwlBachelor, 3);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(dayBefore);
    getBlockWithPruefungen(pruefungsperiode, "b", date, analysis);
    getBlockWithPruefungen(pruefungsperiode, "b2", date, dm);
    when(dataAccessService.areInSameBlock(analysis, dm)).thenReturn(false);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, affected);
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- ein Tag danach --------------------------------
  // ----------------------------------------------------------------------------

  @Test
  @DisplayName("Tag danach + keine Überschneidungen")
  void day_after_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime dayAfter = LocalDateTime.of(2022, 1, 1, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2021, 12, 31, 8, 0);
    Pruefung modelAnalysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    modelAnalysis.addTeilnehmerkreis(bwlBachelor);
    modelAnalysis.setStartzeitpunkt(dayBefore);
    Pruefung modelDM = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    modelDM.addTeilnehmerkreis(infBachelor);
    modelDM.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(modelDM, modelAnalysis));

    assertThat(deviceUnderTest.evaluate(modelAnalysis)).isEmpty();
  }


  @Test
  @DisplayName("Tag danach + Überschneidungen")
  void day_after_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime dayAfter = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 78;
    analysis.addTeilnehmerkreis(bwlBachelor, 3);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(dayBefore);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, affected);
  }

  @Test
  @DisplayName("Tag danach + Überschneidungen")
  void day_after_overlap_multiple() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime dayAfter = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int bwlB = 346;
    int infB = 300;
    int affected = bwlB + infB;
    analysis.addTeilnehmerkreis(bwlBachelor, bwlB);
    analysis.addTeilnehmerkreis(infBachelor, infB);
    analysis.setStartzeitpunkt(dayBefore);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, infB);
    dm.setStartzeitpunkt(dayAfter);

    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor, bwlB);
    haskell.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    validateResults(analysis, Set.of(dm, haskell), Set.of(infBachelor, bwlBachelor), affected);
  }

  @Test
  @DisplayName("Tag danach + Überschneidungen + jahreswechsel")
  void day_after_overlap_different_years() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime dayAfter = LocalDateTime.of(2022, 1, 1, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2021, 12, 31, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 10;
    analysis.addTeilnehmerkreis(bwlBachelor, affected);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(dayBefore);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, affected);
  }


  @Test
  @DisplayName("Tag danach + Überschneidungen + unterschiedliche blöcke")
  void day_after_overlap_different_blocks() throws NoPruefungsPeriodeDefinedException {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    LocalDateTime dayAfter = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 0;
    analysis.addTeilnehmerkreis(bwlBachelor, affected);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(dayBefore);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(dayAfter);
    getBlockWithPruefungen(pruefungsperiode, "b", dayBefore, analysis);
    getBlockWithPruefungen(pruefungsperiode, "b2", dayAfter, dm);
    when(dataAccessService.areInSameBlock(analysis, dm)).thenReturn(false);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, dm, infBachelor, affected);
  }

  // ----------------------------------------------------------------------------
  // ----------------------- mehr als einen Tag davor ---------------------------
  // ----------------------------------------------------------------------------

  @Test
  @DisplayName("mehr als einen Tag davor + keine Überschneidungen")
  void more_than_one_day_before_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2021, 12, 12, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }


  @Test
  @DisplayName("mehr als einen Tag davor + Überschneidungen")
  void more_than_one_day_before_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 10, 2, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 9, 15, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("mehr als einen Tag davor + mehrere Überschneidungen")
  void more_than_one_day_before_overlap_multiple() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 2, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor);
    haskell.setStartzeitpunkt(earlier);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  // ----------------------------------------------------------------------------
  // ---------------------- mehr als einen Tag danach ---------------------------
  // ----------------------------------------------------------------------------

  @Test
  @DisplayName("mehr als einen Tag danach + keine Überschneidungen")
  void more_than_one_day_after_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 1, 1, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 12, 12, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }


  @Test
  @DisplayName("mehr als einen Tag danach + Überschneidungen")
  void more_than_one_day_after_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 10, 2, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 12, 15, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("mehr als einen Tag danach + mehrere Überschneidungen")
  void more_than_one_day_after_overlap_multiple() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 9, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(later);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  // ----------------------------------------------------------------------------
  // --------------------------  davor und danach -------------------------------
  // ----------------------------------------------------------------------------

  @Test
  @DisplayName("einen Tag davor + danach + keine Überschneidungen")
  void one_day_before_and_after_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 4, 23, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 4, 21, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(infPtl);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("einen Tag davor + danach + Überschneidungen")
  void one_day_before_and_after_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 4, 23, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 4, 21, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int infB = 95;
    int bwlB = 80;
    int affected = infB + bwlB;
    analysis.addTeilnehmerkreis(bwlBachelor, bwlB);
    analysis.addTeilnehmerkreis(infBachelor, infB);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, infB);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor, bwlB);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    validateResults(analysis, Set.of(dm, haskell), Set.of(bwlBachelor, infBachelor), affected);
  }

  @Test
  @DisplayName("einen Tag davor + danach + Überschneidung davor")
  void one_day_before_and_after_overlap_before() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 4, 23, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 4, 21, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 1234;
    analysis.addTeilnehmerkreis(bwlBachelor, affected);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, affected);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(infPtl, affected);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    validateResults(analysis, dm, infBachelor, affected);
  }

  @Test
  @DisplayName("einen Tag davor + danach + Überschneidung danach")
  void one_day_before_and_after_overlap_after() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 4, 23, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 4, 21, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int affected = 678;
    analysis.addTeilnehmerkreis(bwlBachelor, affected);
    analysis.addTeilnehmerkreis(infBachelor, affected);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infPtl, affected);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(infBachelor, affected);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    validateResults(analysis, haskell, infBachelor, affected);
  }

  @Test
  @DisplayName("mehr als einen Tag davor + danach + keine Überschneidungen")
  void more_than_one_day_before_and_after_no_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 9, 1, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(infPtl);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("mehr als einen Tag davor + danach + Überschneidungen")
  void more_than_one_day_before_and_after_overlap() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 9, 1, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(bwlBachelor);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("mehr als einen Tag davor + danach + Überschneidungen davor")
  void more_than_one_day_before_and_after_overlap_before()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 9, 1, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(infMaster);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }

  @Test
  @DisplayName("mehr als einen Tag davor + danach + Überschneidungen danach")
  void more_than_one_day_before_and_after_overlap_after()
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime date = LocalDateTime.of(2022, 4, 22, 8, 0);
    LocalDateTime later = LocalDateTime.of(2022, 9, 1, 8, 0);
    LocalDateTime earlier = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    analysis.addTeilnehmerkreis(bwlBachelor);
    analysis.addTeilnehmerkreis(infBachelor);
    analysis.setStartzeitpunkt(date);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infMaster);
    dm.setStartzeitpunkt(earlier);
    Pruefung haskell = getPruefungOfReadOnlyPruefung(RO_HASKELL_UNPLANNED);
    haskell.addTeilnehmerkreis(infBachelor);
    haskell.setStartzeitpunkt(later);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis, haskell));

    assertThat(deviceUnderTest.evaluate(analysis)).isEmpty();
  }


  @Test
  void overlap_not_all_Teilnehmerkreise() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime dayAfter = LocalDateTime.of(2022, 1, 2, 8, 0);
    LocalDateTime dayBefore = LocalDateTime.of(2022, 1, 1, 8, 0);
    Pruefung analysis = getPruefungOfReadOnlyPruefung(RO_ANALYSIS_UNPLANNED);
    int bwl = 145;
    int inf = 456;
    int affected = bwl + inf;
    analysis.addTeilnehmerkreis(bwlBachelor, bwl);
    analysis.addTeilnehmerkreis(infBachelor, inf);
    analysis.addTeilnehmerkreis(wingBachelor, 3);
    analysis.addTeilnehmerkreis(infPtl, 23);
    analysis.setStartzeitpunkt(dayBefore);
    Pruefung dm = getPruefungOfReadOnlyPruefung(RO_DM_UNPLANNED);
    dm.addTeilnehmerkreis(infBachelor, inf);
    dm.addTeilnehmerkreis(infMaster, 23);
    dm.addTeilnehmerkreis(bwlBachelor, bwl);
    dm.addTeilnehmerkreis(wingMaster, 12);
    dm.setStartzeitpunkt(dayAfter);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Set.of(dm, analysis));

    validateResults(analysis, Set.of(dm), Set.of(infBachelor, bwlBachelor), affected);
  }

  // ----------------------------------------------------------------------------
  // -------------------------------- helper ------------------------------------
  // ----------------------------------------------------------------------------


  private void validateResults(Pruefung toEvaluate, Pruefung causingPruefungen,
      Teilnehmerkreis causingTeilnehmerkreise, int affected)
      throws NoPruefungsPeriodeDefinedException {
    validateResults(toEvaluate, Set.of(causingPruefungen), Set.of(causingTeilnehmerkreise), affected);
  }


  private void validateResults(Pruefung toEvaluate, Set<Pruefung> causingPruefungen,
      Set<Teilnehmerkreis> causingTeilnehmerkreise, int affected)
      throws NoPruefungsPeriodeDefinedException {

    Optional<SoftRestrictionAnalysis> result = deviceUnderTest.evaluate(toEvaluate);
    assertThat(result).isPresent();
    assertThat(result.get().getAffectedTeilnehmerKreise()).containsExactlyInAnyOrderElementsOf(
        causingTeilnehmerkreise);
    assertThat(result.get().getAffectedPruefungen()).containsExactlyInAnyOrderElementsOf(
        causingPruefungen);
    assertThat(result.get().getAmountAffectedStudents()).isEqualTo(affected);
    assertThat(result.get().getDeltaScoring()).isEqualTo(causingPruefungen.size() * SCORING);
  }


  private void getBlockWithPruefungen(Pruefungsperiode pruefungsperiode, String name,
      LocalDateTime termin, Pruefung... pruefungen) throws NoPruefungsPeriodeDefinedException {
    Block result = new BlockImpl(pruefungsperiode, name, SEQUENTIAL);
    for (Pruefung pruefung : pruefungen) {
      result.addPruefung(pruefung);
      when(dataAccessService.getBlockTo(pruefung)).thenReturn(Optional.of(result));
    }
    if (termin != null) {
      result.setStartzeitpunkt(termin);
    }
  }


}
