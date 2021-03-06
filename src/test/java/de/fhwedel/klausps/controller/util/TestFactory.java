package de.fhwedel.klausps.controller.util;

import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class TestFactory {

  public static final Pruefung P_ANALYSIS_UNPLANNED = new PruefungImpl("1", "Analysis", "1",
      Duration.ofMinutes(120));
  public static final Pruefung P_BWL_UNPLANNED = new PruefungImpl("2", "Bwl", "2",
      Duration.ofMinutes(120));

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


  public static Teilnehmerkreis bwlBachelor = new TeilnehmerkreisImpl("bwl", "1", 1,
      Ausbildungsgrad.BACHELOR);

  public static Teilnehmerkreis infBachelor = new TeilnehmerkreisImpl("inf", "1", 1,
      Ausbildungsgrad.BACHELOR);

  public static Teilnehmerkreis wingBachelor = new TeilnehmerkreisImpl("wing", "1", 1,
      Ausbildungsgrad.BACHELOR);

  public static Teilnehmerkreis bwlMaster = new TeilnehmerkreisImpl("bwl", "1", 1,
      Ausbildungsgrad.MASTER);

  public static Teilnehmerkreis infMaster = new TeilnehmerkreisImpl("inf", "1", 1,
      Ausbildungsgrad.MASTER);

  public static Teilnehmerkreis wingMaster = new TeilnehmerkreisImpl("wing", "1", 1,
      Ausbildungsgrad.MASTER);

  public static Teilnehmerkreis bwlPtl = new TeilnehmerkreisImpl("bwl", "1", 1,
      Ausbildungsgrad.AUSBILDUNG);

  public static Teilnehmerkreis infPtl = new TeilnehmerkreisImpl("inf", "1", 1,
      Ausbildungsgrad.AUSBILDUNG);

  public static Teilnehmerkreis wingPtl = new TeilnehmerkreisImpl("wing", "1", 1,
      Ausbildungsgrad.AUSBILDUNG);

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
      Pruefung... pruefung) {
    for (Pruefung p : pruefung) {
      when(MOCKED_PERIODE.pruefung(p.getPruefungsnummer())).thenReturn(p);
    }
  }

  public static void configureMock_geplantePruefungenFromPeriode(
      final Pruefungsperiode MOCKED_PERIODE,
      Set<Pruefung> pruefungen) {
    when(MOCKED_PERIODE.geplantePruefungen()).thenReturn(pruefungen);
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


  public static Block configureMock_addPruefungToBlockModel(final Pruefungsperiode mocked_periode,
      String name,
      LocalDateTime termin, Pruefung... pruefungen) {
    Block block = new BlockImpl(mocked_periode, name, Blocktyp.SEQUENTIAL);
    for (Pruefung p : pruefungen) {
      block.addPruefung(p);
      block.setStartzeitpunkt(termin);
      when(mocked_periode.block(p)).thenReturn(block);
    }
    return block;
  }
}
