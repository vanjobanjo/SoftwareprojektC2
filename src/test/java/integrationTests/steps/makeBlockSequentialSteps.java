package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class makeBlockSequentialSteps extends BaseSteps {

  @Und("es existiert der geplante parallele Block {string} am {localDateTime} mit den Pruefungen")
  public void esExistiertDerGeplanteParalleleBlockAmUmMitDenPruefungen(String blockName,
      LocalDateTime schedule,
      List<List<String>> pruefungenInput)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Set<ReadOnlyPruefung> pruefungen = createPruefungen(pruefungenInput);
    ReadOnlyBlock block = state.controller.createBlock(blockName, PARALLEL,
        pruefungen.toArray(new ReadOnlyPruefung[]{}));
    state.controller.scheduleBlock(block, schedule);
  }

  private Set<ReadOnlyPruefung> createPruefungen(List<List<String>> input)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> result = new HashSet<>();
    input = input.subList(1, input.size());
    for (List<String> pruefungInput : input) {
      Optional<Teilnehmerkreis> teilnehmerkreis = createTeilnehmerkreis(pruefungInput);
      Map<Teilnehmerkreis, Integer> teilnehmerkreise = new HashMap<>();
      teilnehmerkreis.ifPresent(t -> teilnehmerkreise.put(t, 10));
      ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungInput.get(0),
          pruefungInput.get(0), pruefungInput.get(0), emptySet(), Duration.ofHours(1),
          teilnehmerkreise);
      result.add(pruefung);
    }
    return result;
  }

  private Optional<Teilnehmerkreis> createTeilnehmerkreis(List<String> pruefungInput) {
    if (pruefungInput.size() < 3) {
      return Optional.empty();
    }
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl(pruefungInput.get(1),
        pruefungInput.get(1), Integer.parseInt(pruefungInput.get(2)), Ausbildungsgrad.BACHELOR);
    return Optional.of(teilnehmerkreis);
  }

  @Wenn("ich den Block {string} auf sequentiell stelle")
  public void ichDenBlockAufSequentiellStelle(String blockName)
      throws NoPruefungsPeriodeDefinedException {
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.makeBlockSequential(
          getBlockFromModel(blockName));
      state.results.put("pruefungen", result);
    } catch (HartesKriteriumException e) {
      //  putExceptionInResult(e);
      state.results.put("exception", e);
    }
  }

  @Dann("ist der Block {string} sequentiell")
  public void istDerBlockSequential(String blockName) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = getBlockFromModel(blockName);
    assertThat(block.getTyp()).isEqualTo(SEQUENTIAL);
  }

  @Und("es existiert die geplante Pruefung {string} am {localDateTime} mit dem Teilnehmerkreis {string} und dem Semester {int}")
  public void esExistiertDieGeplantePruefungAmUmUhrMitDemTeilnehmerkreisUndDemSemester(
      String pruefungName, LocalDateTime localDateTime,
      String tkName, int semester)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {

    Teilnehmerkreis tk1 = new TeilnehmerkreisImpl(tkName, tkName, semester,
        Ausbildungsgrad.BACHELOR);
    Map<Teilnehmerkreis, Integer> newTeilnehmerMap = new HashMap<>();
    newTeilnehmerMap.put(tk1, 102);

    ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(),
        Duration.ofHours(2), newTeilnehmerMap);
    state.controller.schedulePruefung(pruefung, localDateTime);
  }

  @Wenn("ich einen unbekannten Block auf sequentiell stelle")
  public void ichEinenUnbekanntenBlockAufSequentiellStelle()
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock block = new BlockDTO("unknown", null, Duration.ZERO, emptySet(), 0, SEQUENTIAL);
    try {
      state.controller.makeBlockSequential(block);
    } catch (IllegalStateException exception) {
      putExceptionInResult(exception);
    }
  }
}
