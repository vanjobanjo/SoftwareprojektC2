package de.fhwedel.klausps.controller.assertions;


import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import java.util.List;
import java.util.StringJoiner;
import org.assertj.core.api.AbstractAssert;

public class ReadOnlyBlockAssert extends AbstractAssert<ReadOnlyBlockAssert, ReadOnlyBlock> {

  public ReadOnlyBlockAssert(ReadOnlyBlock actual) {
    super(actual, ReadOnlyBlockAssert.class);
  }

  public static ReadOnlyBlockAssert assertThat(ReadOnlyBlock actual) {
    return new ReadOnlyBlockAssert(actual);
  }

  public ReadOnlyBlockAssert containsOnlyPruefungen(ReadOnlyPruefung... pruefungen) {
    if (!actual.getROPruefungen().containsAll(List.of(pruefungen))) {
      failWithMessage("Block expected: %s but found: %s",
          getPruefungsNames(actual.getROPruefungen()), getPruefungsNames(List.of(pruefungen)));
    }
    if (!(actual.getROPruefungen().size() == pruefungen.length)) {
      failWithMessage("Block expected %d Pruefungen but found %d",
          actual.getROPruefungen().size(),
          pruefungen.length);
    }
    return this;
  }

  public ReadOnlyBlockAssert isSameAs(ReadOnlyBlock other) {
    if (other.geplant() && actual.ungeplant()) {
      failWithMessage("Block was expected to not be planned but was.");
    }
    if (other.ungeplant() && actual.geplant()) {
      failWithMessage("Block was expected to be planned but was not.");
    }

    if (!other.getName().equals(actual.getName())) {
      failWithMessage("Expected Name was %s, but was actually %s", actual.getName(),
          other.getName());
    }
    if (!other.getTermin().equals(actual.getTermin())) {
      failWithMessage("Termin was expected to be %s, but was actually %s",
          actual.getTermin().toString(), other.getTermin().toString());
    }

    if (!other.getDauer().equals(actual.getDauer())) {
      failWithMessage("Dauer was expected to be %s, but was %s", actual.getDauer().toMinutes(),
          other.getDauer().toMinutes());
    }

    containsOnlyPruefungen(other.getROPruefungen().toArray(new ReadOnlyPruefung[0]));

    return this;
  }


  private String getPruefungsNames(Iterable<ReadOnlyPruefung> pruefungen) {
    StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
    for (ReadOnlyPruefung pruefung : pruefungen) {
      stringJoiner.add(pruefung.getName());
    }
    return stringJoiner.toString();
  }
}
