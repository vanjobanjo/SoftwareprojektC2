package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;
import static de.fhwedel.klausps.controller.util.TestUtils.convertPruefungenToPlanungseinheiten;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPlannedPruefungen;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefung;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomPruefungenAt;
import static de.fhwedel.klausps.controller.util.TestUtils.getRandomUnplannedPruefung;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMinutes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.matchers.IsOneOfMatcher;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.BlockImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class AtSameTimeRestrictionTest {

  public AtSameTimeRestriction deviceUnderTest;
  public DataAccessService dataAccessService;

  @BeforeEach
  public void setUp() throws NoPruefungsPeriodeDefinedException {
    this.dataAccessService = mock(DataAccessService.class);
    MockSettings mockSettings = withSettings().useConstructor(this.dataAccessService,
        ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH, ZERO);
    this.deviceUnderTest = mock(AtSameTimeRestriction.class, mockSettings);
    when(deviceUnderTest.evaluate(any())).thenCallRealMethod();
  }

  @Test
  @DisplayName("Checking an unplanned pruefung results in no violation")
  void evaluate_callWithUnplannedPruefung() throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = getRandomUnplannedPruefung(5L);
    when(dataAccessService.getPlannedPruefungen()).thenReturn(Collections.emptySet());
    assertThat(deviceUnderTest.evaluate(pruefung)).isEmpty();
  }

  @Test
  @DisplayName("Multiple pruefungen do not violate the restriction when not at the same time")
  void evaluate_noSimultaneousPruefungen() throws NoPruefungsPeriodeDefinedException {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    LocalDateTime startSecondPruefung = startFirstPruefung.plusMinutes(180);
    LocalDateTime startThirdPruefung = startSecondPruefung.plusMinutes(180);
    List<Pruefung> pruefungen = getRandomPruefungenAt(5L, startFirstPruefung, startSecondPruefung,
        startThirdPruefung);

    when(dataAccessService.getPlannedPruefungen()).thenReturn(Collections.emptySet());

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0))).isEmpty();
    assertThat(deviceUnderTest.evaluate(pruefungen.get(1))).isEmpty();
    assertThat(deviceUnderTest.evaluate(pruefungen.get(2))).isEmpty();
  }

  @Test
  void evaluate_violatedRestrictionResultsInAnalyse()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Planungseinheit> pruefungen = convertPruefungenToPlanungseinheiten(
        getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15),
            startFirstPruefung.plusMinutes(30)));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    // direct violation of restriction
    when(deviceUnderTest.violatesRestriction(any())).thenReturn(true);

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0).asPruefung())).isPresent();
  }

  @Test
  void evaluate_violatedRestrictionResultsInAnalyse_()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Planungseinheit> pruefungen = convertPruefungenToPlanungseinheiten(
        getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15),
            startFirstPruefung.plusMinutes(30)));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    // direct violation of restriction
    when(deviceUnderTest.violatesRestriction(any())).thenReturn(true, false, true);

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0).asPruefung())).isPresent();
  }

  @Test
  void evaluate_notViolatedRestrictionResultsInNoAnalyse()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    LocalDateTime startFirstPruefung = LocalDateTime.of(1999, 12, 23, 8, 0);
    List<Planungseinheit> pruefungen = convertPruefungenToPlanungseinheiten(
        getRandomPruefungenAt(5L, startFirstPruefung, startFirstPruefung.plusMinutes(15),
            startFirstPruefung.plusMinutes(30)));

    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.copyOf(pruefungen));
    // direct violation of restriction
    when(deviceUnderTest.violatesRestriction(any())).thenReturn(true, false);

    assertThat(deviceUnderTest.evaluate(pruefungen.get(0).asPruefung())).isNotPresent();
  }

  @Test
  void evaluate_checkedPruefungIsInBlock_parallel()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // when checking for a pruefung use the start and end time of the pruefung itself when the blocks type is parallel
    Block block = getParallelBlockWith3Pruefungen().asBlock();
    List<Pruefung> pruefungenInBlock = new ArrayList<>(block.getPruefungen());

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));

    deviceUnderTest.evaluate(pruefungenInBlock.get(0));
    verify(dataAccessService).getAllPlanungseinheitenBetween(
        pruefungenInBlock.get(0).getStartzeitpunkt(), pruefungenInBlock.get(0).endzeitpunkt());
  }

  private Planungseinheit getParallelBlockWith3Pruefungen() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    Block result = new BlockImpl(pruefungsperiode, "name", Blocktyp.PARALLEL);
    for (Pruefung pruefung : getRandomPlannedPruefungen(2L, 3)) {
      result.addPruefung(pruefung);
    }
    result.setStartzeitpunkt(LocalDateTime.of(1998, 1, 2, 21, 39));
    return result;
  }

  @Test
  void evaluate_checkedPruefungIsInBlock_sequential()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // when checking for a pruefung use the start and end time of its block if the blocks type is sequential
    Block block = getSequentialBlockWith3Pruefungen().asBlock();
    List<Pruefung> pruefungenInBlock = new ArrayList<>(block.getPruefungen());

    when(dataAccessService.getBlockTo(any(Pruefung.class))).thenReturn(Optional.of(block));

    deviceUnderTest.evaluate(pruefungenInBlock.get(0));
    verify(dataAccessService).getAllPlanungseinheitenBetween(block.getStartzeitpunkt(),
        block.endzeitpunkt());
  }

  private Planungseinheit getSequentialBlockWith3Pruefungen() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    Block result = new BlockImpl(pruefungsperiode, "name", Blocktyp.SEQUENTIAL);
    for (Pruefung pruefung : getRandomPlannedPruefungen(2L, 3)) {
      result.addPruefung(pruefung);
    }
    result.setStartzeitpunkt(LocalDateTime.of(1998, 1, 2, 21, 39));
    return result;
  }

  @Test
  void sequentialBlocksOverlapBecauseOfAdditiveDuration()
      throws IllegalTimeSpanException, NoPruefungsPeriodeDefinedException {
    // test that sequential blocks are detected as overlapping with the checked pruefung if none of
    // the contained pruefungen overlaps but the combined time does.
    Block block = getSequentialBlockWithTotalDurationOf5Hours().asBlock();
    Pruefung toCheckFor = getRandomPruefung(block.getStartzeitpunkt().plusMinutes(120),
        block.endzeitpunkt().plusMinutes(10), 1L);
    List<Pruefung> pruefungenInBlock = new ArrayList<>(block.getPruefungen());
    Answer<Boolean> hasMoreThanOneElement = (InvocationOnMock invocation) ->
        invocation.getArgument(0, Collection.class).size() > 1;

    when(dataAccessService.getBlockTo(
        argThat(new IsOneOfMatcher<>(pruefungenInBlock)))).thenReturn(Optional.of(block));
    when(dataAccessService.getAllPlanungseinheitenBetween(any(), any())).thenReturn(
        Set.of(toCheckFor, block));
    when(deviceUnderTest.violatesRestriction(any())).thenAnswer(hasMoreThanOneElement);

    assertThat(deviceUnderTest.evaluate(toCheckFor)).isPresent();
  }

  private Planungseinheit getSequentialBlockWithTotalDurationOf5Hours() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    Block result = new BlockImpl(pruefungsperiode, "name", Blocktyp.SEQUENTIAL);
    for (Pruefung pruefung : getRandomPlannedPruefungen(2L, 3)) {
      pruefung.setDauer(ofMinutes(100));
      result.addPruefung(pruefung);
    }
    result.setStartzeitpunkt(LocalDateTime.of(1998, 1, 2, 10, 0));
    return result;
  }

}
