package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import integrationTests.DataTypes;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class getHardConflictedTimesSteps extends BaseSteps {

  @Wenn("ich abfrage, welche der folgenden Zeitpunkte fuer die Pruefung {string} verboten ist")
  public void ichAbfrageWelcheDerFolgendenZeitpunkteVerbotenIst(String pruefungName,
      List<List<String>> input)
      throws NoPruefungsPeriodeDefinedException {
    Set<LocalDateTime> timestamps = getAsTimeStamps(input);
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    try {
      Set<LocalDateTime> result = state.controller.getHardConflictedTimes(timestamps, pruefung);
      state.results.put("times", result);
    } catch (IllegalArgumentException exception) {
      putExceptionInResult(exception);
    }
  }

  Set<LocalDateTime> getAsTimeStamps(List<List<String>> timestamps) {
    Set<LocalDateTime> result = new HashSet<>();
    timestamps = timestamps.subList(1, timestamps.size());
    for (List<String> timestamp : timestamps) {
      result.add(DataTypes.localDateTime(timestamp.get(0)));
    }
    return result;
  }

  @Wenn("ich abfrage, welche der folgenden Zeitpunkte fuer die unbekannte Pruefung {string} verboten ist")
  public void ichAbfrageWelcheDerFolgendenZeitpunkteFuerDieUnbekanntePruefungVerbotenIst(
      String pruefungName,
      List<List<String>> input)
      throws NoPruefungsPeriodeDefinedException {
    Set<LocalDateTime> timestamps = getAsTimeStamps(input);
    ReadOnlyPruefung pruefung = new PruefungDTO(pruefungName, pruefungName,
        Duration.ofHours(1), emptyMap(), emptySet(), 12);
    try {
      Set<LocalDateTime> result = state.controller.getHardConflictedTimes(timestamps, pruefung);
      state.results.put("times", result);
    } catch (IllegalStateException exception) {
      putExceptionInResult(exception);
    }
  }

  @Dann("enthaelt das Ergebnis genau die Zeitpunkte")
  public void enthaeltDasErgebnisGenauDieZeitpunkte(List<List<String>> input) {
    Set<LocalDateTime> timestamps = getAsTimeStamps(input);
    Collection<LocalDateTime> result = (Collection<LocalDateTime>) state.results.get("times");
    assertThat(result).containsExactlyInAnyOrderElementsOf(timestamps);
  }

  @Dann("enthaelt das Ergebnis keine Zeitpunkte")
  public void enthaeltDasErgebnisKeineZeitpunkte() {
    Collection<LocalDateTime> result = (Collection<LocalDateTime>) state.results.get("times");
    assertThat(result).isEmpty();
  }

  @Und("es existiert der geplante sequentielle Block {string} am {localDateTime} mit den Pruefungen")
  public void esExistiertDerGeplanteSequentielleBlockAmUmMitDenPruefungen(String blockName,
      LocalDateTime schedule,
      List<List<String>> pruefungenInput)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Set<ReadOnlyPruefung> pruefungen = createPruefungen(pruefungenInput);
    ReadOnlyBlock block = state.controller.createBlock(blockName, Blocktyp.SEQUENTIAL,
        pruefungen.toArray(new ReadOnlyPruefung[]{}));
    state.controller.scheduleBlock(block, schedule);
  }

  private Set<ReadOnlyPruefung> createPruefungen(List<List<String>> input)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> result = new HashSet<>();
    input = input.subList(1, input.size());
    for (List<String> pruefungInput : input) {
      Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl(pruefungInput.get(1),
          pruefungInput.get(1), Integer.parseInt(pruefungInput.get(2)), Ausbildungsgrad.BACHELOR);
      ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungInput.get(0),
          pruefungInput.get(0), pruefungInput.get(0), emptySet(), Duration.ofHours(1),
          Map.of(teilnehmerkreis, 10));
      result.add(pruefung);
    }
    return result;
  }

}
