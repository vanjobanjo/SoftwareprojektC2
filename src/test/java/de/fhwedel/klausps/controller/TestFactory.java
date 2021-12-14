package de.fhwedel.klausps.controller;

import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestFactory {

  public static ReadOnlyPruefung RO_ANALYSIS_UNPLANNED =
      new PruefungDTOBuilder()
          .withPruefungsName("Analysis")
          .withPruefungsNummer("1")
          .withDauer(Duration.ofMinutes(120))
          .build();
  public static ReadOnlyPruefung RO_DM_UNPLANNED =
      new PruefungDTOBuilder()
          .withPruefungsName("DM")
          .withPruefungsNummer("2")
          .withDauer(Duration.ofMinutes(120))
          .build();

  public static ReadOnlyPruefung RO_HASKELL_UNPLANNED =
      new PruefungDTOBuilder()
          .withPruefungsName("HASKELL")
          .withPruefungsNummer("3")
          .withDauer(Duration.ofMinutes(120))
          .build();

  public static Pruefung getPruefungOfReadOnlyPruefung(ReadOnlyPruefung roPruefung) {
    PruefungImpl modelPruefung =
        new PruefungImpl(
            roPruefung.getPruefungsnummer(),
            roPruefung.getName(),
            "",
            roPruefung.getDauer(),
            roPruefung.getTermin().orElse(null));
    for (String pruefer : roPruefung.getPruefer()) {
      modelPruefung.addPruefer(pruefer);
    }
    roPruefung.getTeilnehmerKreisSchaetzung().forEach(modelPruefung::setSchaetzung);
    return modelPruefung;
  }

  public static void configureMock_getPruefungToROPruefung(final Pruefungsperiode MOCKED_PERIODE,
      ReadOnlyPruefung... pruefungen) {
    for (ReadOnlyPruefung p : pruefungen) {
      Pruefung temp = getPruefungOfReadOnlyPruefung(p);
      when(MOCKED_PERIODE.pruefung(p.getPruefungsnummer())).thenReturn(temp);
    }
  }

  public static void configureMock_getPruefungFromPeriode(final Pruefungsperiode MOCKED_PERIODE,
      Pruefung pruefung) {
    when(MOCKED_PERIODE.pruefung(pruefung.getPruefungsnummer())).thenReturn(pruefung);

  }

  public static TestFactory build() {
    return new TestFactory();
  }

  public static ReadOnlyPruefung planRoPruefung(ReadOnlyPruefung ro, LocalDateTime time) {
    return new PruefungDTOBuilder(ro).withStartZeitpunkt(time).build();
  }

  public static void configureMock_setStartEndOfPeriode(Pruefungsperiode mocked_periode,
      LocalDate start_periode,
      LocalDate end_periode) {
    when(mocked_periode.getStartdatum()).thenReturn(start_periode);
    when(mocked_periode.getEnddatum()).thenReturn(end_periode);
  }

}
