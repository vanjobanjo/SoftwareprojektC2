package de.fhwedel.klausps.controller.assertions;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import org.assertj.core.api.AbstractAssert;

public class ReadOnlyPruefungAssert
    extends AbstractAssert<ReadOnlyPruefungAssert, ReadOnlyPruefung> {

  public ReadOnlyPruefungAssert(ReadOnlyPruefung actual) {
    super(actual, ReadOnlyPruefungAssert.class);
  }

  public static ReadOnlyPruefungAssert assertThat(ReadOnlyPruefung actual) {
    return new ReadOnlyPruefungAssert(actual);
  }

  public ReadOnlyPruefungAssert isTheSameAs(ReadOnlyPruefung readOnlyPruefung) {
    isNotNull();
    if (!actual.getName().equals(readOnlyPruefung.getName())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have name %s but was %s",
          actual.getName(), readOnlyPruefung.getName());
    }
    if (!actual.getPruefungsnummer().equals(readOnlyPruefung.getPruefungsnummer())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have pruefungsnummer %s but was %s",
          actual.getPruefungsnummer(), readOnlyPruefung.getPruefungsnummer());
    }
    if (!actual.getPruefer().containsAll(readOnlyPruefung.getPruefer())
        || !readOnlyPruefung.getPruefer().containsAll(actual.getPruefer())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have pruefer %s but was %s",
          actual.getPruefer().toString(), readOnlyPruefung.getPruefer().toString());
    }
    if (!actual.getDauer().equals(readOnlyPruefung.getDauer())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have duration %s but was %s",
          actual.getDauer().toString(), readOnlyPruefung.getDauer().toString());
    }
    if (!actual
        .getTeilnehmerKreisSchaetzung()
        .equals(readOnlyPruefung.getTeilnehmerKreisSchaetzung())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have duration %s but was %s",
          actual.getDauer().toString(), readOnlyPruefung.getDauer().toString());
    }
    return this;
  }
}
