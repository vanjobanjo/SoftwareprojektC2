package de.fhwedel.klausps.controller.assertions;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import java.time.LocalDateTime;
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

  public ReadOnlyPruefungAssert hasPruefer(String pruefer) {
    if (!actual.getPruefer().contains(pruefer)) {
      failWithMessage("Expected to have pruefer %s but did not.", pruefer);
    }
    return this;
  }

  public ReadOnlyPruefungAssert hasNotPruefer(String pruefer) {
    if (actual.getPruefer().contains(pruefer)) {
      failWithMessage("Expected not to have pruefer %s but did.", pruefer);
    }
    return this;
  }

  public ReadOnlyPruefungAssert isScheduledAt(LocalDateTime localDateTime) {
    if (actual.getTermin().isEmpty()) {
      failWithMessage(
          "Expected pruefung to be scheduled at %s but was not scheduled at all.", localDateTime);
    }
    if (!actual.getTermin().get().equals(localDateTime)) {
      failWithMessage(
          "Expected pruefung to be scheduled at %s but was scheduled at %s.",
          localDateTime, actual.getTermin().get());
    }
    return this;
  }

  public ReadOnlyPruefungAssert isNotScheduled() {
    if (actual.getTermin().isPresent()) {
      failWithMessage(
          "Expected pruefung not to be scheduled but was scheduled at %s.",
          actual.getTermin().get());
    }
    return this;
  }

  public ReadOnlyPruefungAssert hasName(String expected) {
    if (!actual.getName().equals(expected)) {
      failWithMessage(
          "Expected pruefung not have the name %s but was %s.", expected, actual.getName());
    }
    return this;
  }

  public ReadOnlyPruefungAssert differsOnlyInNameFrom(ReadOnlyPruefung pruefung) {
    isNotNull();
    if (actual.getName().equals(pruefung.getName())) {
      failWithMessage(
          "Expected ReadOnlyPruefungs name to differ from %s but was the same.",
          pruefung.getName());
    }
    if (!actual.getPruefungsnummer().equals(pruefung.getPruefungsnummer())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have pruefungsnummer %s but was %s",
          actual.getPruefungsnummer(), pruefung.getPruefungsnummer());
    }
    if (!actual.getPruefer().containsAll(pruefung.getPruefer())
        || !pruefung.getPruefer().containsAll(actual.getPruefer())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have pruefer %s but was %s",
          actual.getPruefer().toString(), pruefung.getPruefer().toString());
    }
    if (!actual.getDauer().equals(pruefung.getDauer())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have duration %s but was %s",
          actual.getDauer().toString(), pruefung.getDauer().toString());
    }
    if (!actual.getTeilnehmerKreisSchaetzung().equals(pruefung.getTeilnehmerKreisSchaetzung())) {
      failWithMessage(
          "Expected ReadOnlyPruefung to have duration %s but was %s",
          actual.getDauer().toString(), pruefung.getDauer().toString());
    }
    return this;
  }
}
