package de.fhwedel.klausps.controller.util;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PlanungseinheitUtilTest {

  @Test
  void changedAllScoringTest() {
    Set<PruefungScoringWrapper> before = new HashSet<>();
    Set<PruefungScoringWrapper> after = new HashSet<>();
    LocalDateTime start = LocalDateTime.of(2000, 1, 1, 1, 0);
    Pruefung analysis = TestFactory.P_ANALYSIS_UNPLANNED;
    Pruefung bwl = TestFactory.P_BWL_UNPLANNED;
    analysis.setStartzeitpunkt(start);
    bwl.setStartzeitpunkt(start);
    before.add(new PruefungScoringWrapper(analysis, 10));
    before.add(new PruefungScoringWrapper(bwl, 10));
    after.add(new PruefungScoringWrapper(analysis, 20));
    after.add(new PruefungScoringWrapper(bwl, 9));
    Set<Pruefung> result = PlanungseinheitUtil.changedScoring(before, after);
    assertThat(result).containsOnly(bwl, analysis);
  }


  @Test
  void changedOneScoringTest() {
    Set<PruefungScoringWrapper> before = new HashSet<>();
    Set<PruefungScoringWrapper> after = new HashSet<>();
    LocalDateTime start = LocalDateTime.of(2000, 1, 1, 1, 0);
    Pruefung analysis = TestFactory.P_ANALYSIS_UNPLANNED;
    Pruefung bwl = TestFactory.P_BWL_UNPLANNED;
    analysis.setStartzeitpunkt(start);
    bwl.setStartzeitpunkt(start);
    before.add(new PruefungScoringWrapper(analysis, 10));
    before.add(new PruefungScoringWrapper(bwl, 10));
    after.add(new PruefungScoringWrapper(analysis, 10));
    after.add(new PruefungScoringWrapper(bwl, 9));
    Set<Pruefung> result = PlanungseinheitUtil.changedScoring(before, after);
    assertThat(result).containsOnly(bwl);
  }

  @Test
  void changedNoScoringTest() {
    Set<PruefungScoringWrapper> before = new HashSet<>();
    Set<PruefungScoringWrapper> after = new HashSet<>();
    LocalDateTime start = LocalDateTime.of(2000, 1, 1, 1, 0);
    Pruefung analysis = TestFactory.P_ANALYSIS_UNPLANNED;
    Pruefung bwl = TestFactory.P_BWL_UNPLANNED;
    analysis.setStartzeitpunkt(start);
    bwl.setStartzeitpunkt(start);
    before.add(new PruefungScoringWrapper(analysis, 10));
    before.add(new PruefungScoringWrapper(bwl, 10));
    after.add(new PruefungScoringWrapper(analysis, 10));
    after.add(new PruefungScoringWrapper(bwl, 10));
    Set<Pruefung> result = PlanungseinheitUtil.changedScoring(before, after);
    assertThat(result).isEmpty();
  }
}