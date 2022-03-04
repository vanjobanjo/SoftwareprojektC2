package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Ausbildungsgrad.MASTER;
import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class getAnzahlStudentenZeitpunktSteps extends BaseSteps {

  @Wenn("ich die Anzahl an Studenten die am {localDateTime} eine Pruefung schreiben anfrage")
  public void ichDieAnzahlAnStudentenDieAmUmUhrEinePruefungSchreibenAnfrage(
      LocalDateTime localDateTime) {
    try {
      int result = state.controller.getAnzahlStudentenZeitpunkt(localDateTime);
      state.results.put("amount", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }
  }


  @Dann("erhalte ich die Anzahl {int}")
  public void erhalteIchDieAnzahl(int amount) {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results).containsEntry("amount", amount);
  }

  @Und("es existiert die geplante Pruefung {string} am {localDateTime} mit {int} Teilnehmern")
  public void esExistiertDieGeplantePruefungAmUmUhrMitTeilnehmern(String pruefungName,
      LocalDateTime localDateTime, int amountTeilnehmer)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl(pruefungName, pruefungName, 1,
        MASTER);
    ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(),
        Duration.ofHours(1), Map.of(teilnehmerkreis, amountTeilnehmer));
    state.controller.schedulePruefung(pruefung, localDateTime);
  }

  @Und("es existiert der am {localDateTime} geplante {string} Block {string} mit den Pruefungen")
  public void esExistiertDerAmUmGeplanteBlockMitDerPruefung(LocalDateTime localDateTime,
      String type,
      String blockName, List<Map<String, String>> pruefungen)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Set<ReadOnlyPruefung> pruefungenForBlock = new HashSet<>();
    for (Map<String, String> pruefung : pruefungen) {
      String name = pruefung.get("Pruefung");
      Teilnehmerkreis tk = createTeilnehmerkreis("teilnehmerkreis_" + name);
      Duration duration = parseDuration(pruefung.get("Dauer"));
      ReadOnlyPruefung temp = state.controller.createPruefung(name, name, name,
          emptySet(), duration,
          Map.of(tk, Integer.valueOf(pruefung.get("Teilnehmeranzahl"))));
      pruefungenForBlock.add(temp);
    }
    Blocktyp blocktyp = type.toLowerCase().contains("parallel") ? PARALLEL : SEQUENTIAL;
    ReadOnlyBlock block = state.controller.createBlock(blockName, blocktyp,
        pruefungenForBlock.toArray(new ReadOnlyPruefung[0]));
    List<ReadOnlyPlanungseinheit> result = state.controller.scheduleBlock(block, localDateTime);
    state.results.put("result", result);
  }
}
