package de.fhwedel.klausps.controller.assertions;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.util.TestUtils;
import java.util.Set;
import org.assertj.core.api.AbstractAssert;

public class WeicheKriteriumsAnalyseAssert extends
    AbstractAssert<WeicheKriteriumsAnalyseAssert, WeichesKriteriumAnalyse> {

  public WeicheKriteriumsAnalyseAssert(WeichesKriteriumAnalyse actual) {
    super(actual, WeicheKriteriumsAnalyseAssert.class);
  }

  public static WeicheKriteriumsAnalyseAssert assertThat(WeichesKriteriumAnalyse actual) {
    return new WeicheKriteriumsAnalyseAssert(actual);
  }

  public WeicheKriteriumsAnalyseAssert conflictingPruefungenAreExactly(Set<String> pruefungsNummern) {
    Set<String> actualPruefungsnummern = TestUtils.getPruefungsnummernFromModel(actual.getCausingPruefungen());
    if (actualPruefungsnummern.size() != pruefungsNummern.size()) {
      if (!actualPruefungsnummern.containsAll(pruefungsNummern)) {
        failWithMessage("The result is missing Pruefungen with numbers: {}",
            pruefungsNummern.removeAll(actualPruefungsnummern));
      }
      if (!pruefungsNummern.containsAll(actualPruefungsnummern)) {
        failWithMessage("The result contains unexpected Pruefungen with numbers: {}",
            actualPruefungsnummern.removeAll(pruefungsNummern));
      }
    }
    return this;
  }

}
