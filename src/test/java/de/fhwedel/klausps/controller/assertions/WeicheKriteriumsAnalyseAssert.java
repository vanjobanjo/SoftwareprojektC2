package de.fhwedel.klausps.controller.assertions;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.util.TestUtils;
import java.util.Set;
import org.assertj.core.api.AbstractAssert;

public class WeicheKriteriumsAnalyseAssert extends
    AbstractAssert<WeicheKriteriumsAnalyseAssert, SoftRestrictionAnalysis> {

  public WeicheKriteriumsAnalyseAssert(SoftRestrictionAnalysis actual) {
    super(actual, WeicheKriteriumsAnalyseAssert.class);
  }

  public static WeicheKriteriumsAnalyseAssert assertThat(SoftRestrictionAnalysis actual) {
    return new WeicheKriteriumsAnalyseAssert(actual);
  }

  public WeicheKriteriumsAnalyseAssert conflictingPruefungenAreExactly(
      Set<String> pruefungsNummern) {
    Set<String> actualPruefungsnummern = TestUtils.getPruefungsnummernFromModel(
        actual.getCausingPruefungen());
    if (actualPruefungsnummern.size() != pruefungsNummern.size()) {
      if (!actualPruefungsnummern.containsAll(pruefungsNummern)) {
        failWithMessage("The result is missing Pruefungen with numbers: %d",
            pruefungsNummern.removeAll(actualPruefungsnummern));
      }
      if (!pruefungsNummern.containsAll(actualPruefungsnummern)) {
        failWithMessage("The result contains unexpected Pruefungen with numbers: %d",
            actualPruefungsnummern.removeAll(pruefungsNummern));
      }
    }
    return this;
  }

  public WeicheKriteriumsAnalyseAssert affectsExactlyAsManyStudentsAs(int amount) {
    if (amount != actual.getAmountAffectedStudents()) {
      failWithMessage("The %s should affect %d students, but actually affects %d.",
          this.actual.getClass().getSimpleName(), amount, actual.getAmountAffectedStudents());
    }
    return this;
  }

}
