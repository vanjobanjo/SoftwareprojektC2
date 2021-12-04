package de.fhwedel.klausps.controller.assertions;


import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import java.util.List;
import java.util.Set;
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




  private String getPruefungsNames(Iterable<ReadOnlyPruefung> pruefungen) {
    StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
    for (ReadOnlyPruefung pruefung : pruefungen) {
      stringJoiner.add(pruefung.getName());
    }
    return stringJoiner.toString();
  }
}
