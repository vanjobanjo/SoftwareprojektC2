package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Ausbildungsgrad.MASTER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class createPruefungSteps extends BaseSteps {

  @Wenn("ich die Pruefung {string} erstelle")
  public void ichDiePruefungErstelle(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    try {
      state.controller.createPruefung(pruefungName, pruefungName, pruefungName, emptySet(),
          Duration.ofHours(1), emptyMap());
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Dann("existiert die Pruefung {string} ungeplant")
  public void existiertDiePruefungUngeplant(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> unplannedPruefungen = state.controller.getUngeplantePruefungen();
    assertThat(unplannedPruefungen).anyMatch(x -> x.getName().equals(pruefungName));
  }

  @Wenn("ich eine Pruefung ohne {string} erstelle")
  public void ichDiePruefungOhnePruefungsnummerErstelle(String eigenschaft)
      throws NoPruefungsPeriodeDefinedException {
    String reference = (eigenschaft.equals("Referenz")) ? null : "reference";
    String name = (eigenschaft.equals("Name")) ? null : "name";
    String pruefungsnummer = (eigenschaft.equals("Pruefungsnummer")) ? null : "num";
    Map<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
    if (eigenschaft.equals("Teilnehmerkreis")) {
      schaetzungen.put(null, 10);
    }
    Set<String> pruefer;
    if (eigenschaft.equals("Pruefer")) {
      pruefer = null;
    } else if (eigenschaft.equals("PrueferName")) {
      pruefer = new HashSet<>();
      pruefer.add(null);
    } else {
      pruefer = emptySet();
    }
    try {
      state.controller.createPruefung(reference, name, pruefungsnummer, pruefer,
          Duration.ofHours(1), schaetzungen);
    } catch (NullPointerException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich eine Pruefung mit leerer {string} erstelle")
  public void ichDiePruefungMitLeererPruefungsnummerErstelle(String eigenschaft)
      throws NoPruefungsPeriodeDefinedException {
    String reference = (eigenschaft.equals("Referenz")) ? "" : "reference";
    String name = (eigenschaft.equals("Name")) ? "" : "name";
    String pruefungsnummer = (eigenschaft.equals("Pruefungsnummer")) ? "" : "num";
    Set<String> pruefer;
    if (eigenschaft.equals("PrueferName")) {
      pruefer = new HashSet<>();
      pruefer.add("");
    } else {
      pruefer = emptySet();
    }
    try {
      state.controller.createPruefung(reference, name, pruefungsnummer, pruefer,
          Duration.ofHours(1), emptyMap());
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich die Pruefung {string} mit ungueltiger Dauer erstelle")
  public void ichDiePruefungMitUngueltigerDauerErstelle(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    try {
      state.controller.createPruefung(pruefungName, pruefungName, pruefungName, emptySet(),
          Duration.ZERO, emptyMap());
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich eine Pruefung erstelle dessen Teilnehmerkreis eine negative Teilnehmerzahl hat")
  public void ichEinePruefungErstelleDessenTeilnehmerkreisEineNegativeTeilnehmerzahlHat()
      throws NoPruefungsPeriodeDefinedException {
    String pruefungName = "Name";
    Map<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
    schaetzungen.put(new TeilnehmerkreisImpl("Inf", "ord", 1, MASTER), -1);
    try {
      state.controller.createPruefung(pruefungName, pruefungName, pruefungName, emptySet(),
          Duration.ofHours(1), schaetzungen);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich eine Pruefung erstelle, mit einem von mehreren Teilen Teilnehmerkreisen mit negativer Anzahl")
  public void ichEinePruefungErstelleMitEinemVonMehrerenTeilenTeilnehmerkreisenMitNegativerAnzahl()
      throws NoPruefungsPeriodeDefinedException {
    String pruefungName = "Name";
    Map<Teilnehmerkreis, Integer> schaetzungen = new HashMap<>();
    schaetzungen.put(new TeilnehmerkreisImpl("Inf", "ord", 1, MASTER), -1);
    schaetzungen.put(new TeilnehmerkreisImpl("BWL", "or2", 1, MASTER), 2);
    try {
      state.controller.createPruefung(pruefungName, pruefungName, pruefungName, emptySet(),
          Duration.ofHours(1), schaetzungen);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }
}
