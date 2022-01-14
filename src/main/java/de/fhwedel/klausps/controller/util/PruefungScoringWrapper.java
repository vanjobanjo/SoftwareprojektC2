package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Objects;

public class PruefungScoringWrapper {

  private final Pruefung pruefung;
  private final int scoring;


  public PruefungScoringWrapper(Pruefung pruefung, int scoring) {
    this.pruefung = pruefung;
    this.scoring = scoring;
  }


  public Pruefung getPruefung() {
    return pruefung;
  }

  public int getScoring() {
    return scoring;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PruefungScoringWrapper that = (PruefungScoringWrapper) o;
    return scoring == that.scoring && pruefung.getPruefungsnummer()
        .equals(that.pruefung.getPruefungsnummer());
  }

  @Override
  public int hashCode() {
    return Objects.hash(pruefung.getPruefungsnummer(), scoring);
  }

}
