package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * By default, a {@link Pruefung} does not contain the scoring of the Pruefung.
 * This wrapper class combines a Pruefung with its calculated scoring.
 */
public record PruefungWithScoring(@NotNull Pruefung pruefung, int scoring) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PruefungWithScoring that = (PruefungWithScoring) o;
    return scoring == that.scoring && pruefung.getPruefungsnummer()
        .equals(that.pruefung.getPruefungsnummer());
  }

  @Override
  public int hashCode() {
    return Objects.hash(pruefung.getPruefungsnummer(), scoring);
  }

}
