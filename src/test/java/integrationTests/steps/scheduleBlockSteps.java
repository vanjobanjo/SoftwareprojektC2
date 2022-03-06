package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Blocktyp.PARALLEL;
import static de.fhwedel.klausps.model.api.Blocktyp.SEQUENTIAL;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class scheduleBlockSteps extends BaseSteps {

  @Und("es existiert ein ungeplanter Block {string} mit der Pruefung {string}")
  public void esExistiertEinUngeplanterBlockMitPruefung(String block, String pruefung) {
    try {
      ReadOnlyPruefung p = state.controller.createPruefung(pruefung, pruefung, pruefung, emptySet(),
          Duration.ofMinutes(90), emptyMap());
      state.controller.createBlock(block, PARALLEL, p);
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }
  }

  @Und("es existiert ein ungeplanter Block {string} mit der Pruefung {string} und dem Teilnehmerkreis {string}")
  public void esExistiertEinUngeplanterBlockMitPruefung(String block, String pruefung,
      String teilnehmerkreis) {
    try {
      ReadOnlyPruefung p = state.controller.createPruefung(pruefung, pruefung, pruefung, emptySet(),
          Duration.ofMinutes(90), Map.of(createTeilnehmerkreis(teilnehmerkreis), 10));
      state.controller.createBlock(block, PARALLEL, p);
    } catch (NoPruefungsPeriodeDefinedException e) {
      putExceptionInResult(e);
    }

  }


  @Wenn("ich den Block am {localDateTime} einplane")
  public void ichDenBlockAmEinplane(LocalDateTime dateTime) {
    try {
      ReadOnlyBlock b = getBlockFromModel("block");
      List<ReadOnlyPlanungseinheit> result = state.controller.scheduleBlock(b, dateTime);
      state.results.put("affected", result);

    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException | IllegalArgumentException | IllegalStateException e) {
      putExceptionInResult(e);
    }
  }

  @Dann("ist der Block {string} am {localDateTime} geplant")
  public void wirdDerBlockAmGeplant(String block, LocalDateTime dateTime)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("affected")).isNotNull();
    Set<ReadOnlyBlock> planned = state.controller.getGeplanteBloecke();
    assertThat(planned).hasSize(1);
    ReadOnlyBlock resultBlock = planned.toArray(new ReadOnlyBlock[0])[0];
    Optional<LocalDateTime> resultTermin = resultBlock.getTermin();
    assertThat(resultBlock.getName()).isEqualTo(block);
    assertThat(resultTermin).isPresent();
    assertThat(resultTermin).contains(dateTime);
  }

  @Und("es werden Restriktionen verletzt")
  public void esWerdenRestriktionenVerletzt() {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("affected")).isNotNull();
    assertThat((List<ReadOnlyPlanungseinheit>) state.results.get("affected")).hasSizeGreaterThan(2);

  }


  @Und("es existiert die geplante Pruefung {string} am {localDateTime} mit dem Teilnehmerkreis {string}")
  public void esExistiertDieGeplantePruefungAmMitDemTeilnehmerkreis(String pruefung,
      LocalDateTime dateTime, String teilnehmerkreis) {

    try {
      ReadOnlyPruefung p = state.controller.createPruefung(pruefung, pruefung, pruefung, emptySet(),
          Duration.ofMinutes(90), Map.of(createTeilnehmerkreis(teilnehmerkreis), 2));
      state.controller.schedulePruefung(p, dateTime);
    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException e) {
      putExceptionInResult(e);
    }
  }

  @Und("es werden keine Restriktionen verletzt")
  public void esWerdenKeineRestriktionenVerletzt() {
    assertThat(state.results.get(EXCEPTION)).isNull();
    assertThat(state.results.get("affected")).isNotNull();
    List<ReadOnlyPlanungseinheit> affected = (List<ReadOnlyPlanungseinheit>) state.results.get(
        "affected");
    assertThat(affected).hasSize(2);
    for (ReadOnlyPlanungseinheit p : affected) {
      if (p.isBlock()) {
        assertThat(p.asBlock().getName()).isEqualTo("block");
      } else {
        assertThat(p.asPruefung().getName()).isEqualTo("pruefung");
      }
    }
  }

  @Dann("wird eine harte Restriktion verletzt")
  public void wirdEineHarteRestriktionVerletzt() {
    assertThat(state.results.get(EXCEPTION)).isNotNull();
    assertThat(state.results.get(EXCEPTION)).isInstanceOf(HartesKriteriumException.class);
  }

  @Wenn("ich versuche den Block {string} am {localDateTime} einzuplanen")
  public void ichVersucheDenBlockEinzuplanen(String block, LocalDateTime localDateTime) {
    ReadOnlyBlock tryToPlan = new BlockDTO(block, null, Duration.ofMinutes(90), emptySet(),
        1, SEQUENTIAL);
    try {
      state.controller.scheduleBlock(tryToPlan, localDateTime);
    } catch (IllegalStateException e) {
      putExceptionInResult(e);
    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException e) {
      throw new AssertionError("This exception is not part of the test and should therefore"
          + " not happen", e);
    }
  }

  @Wenn("ich den Block mit der unbekannten Pruefung {string} am {localDateTime} einplane")
  public void ichDenBlockMitDerUnbekanntenPruefungAmEinplane(String pruefung,
      LocalDateTime localDateTime) {
    try {
      ReadOnlyPruefung doesNotExist = new PruefungDTO(pruefung, pruefung, localDateTime,
          Duration.ofMinutes(90), emptyMap(), emptySet(), 0);
      ReadOnlyBlock block = new BlockDTO("block", localDateTime, doesNotExist.getDauer(),
          Set.of(doesNotExist), 1, PARALLEL);
      state.controller.scheduleBlock(block, localDateTime);
    } catch (IllegalStateException e) {
      putExceptionInResult(e);
    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException e) {
      throw new AssertionError("This Exception is not part of the test, should therefore not occur",
          e);
    }
  }

  @Angenommen("es existiert der ungeplante Block {string} mit der Pruefung {string}")
  public void esExistiertDerUngeplanteBlockMitDerPruefung(String block, String pruefung) {
    try {
      ReadOnlyPruefung pruefungInBlock = state.controller.createPruefung(pruefung, pruefung,
          pruefung,
          emptySet(), Duration.ofMinutes(90), emptyMap());
      ReadOnlyBlock result = state.controller.createBlock(block, PARALLEL, pruefungInBlock);
      state.results.put("block", result);
    } catch (NoPruefungsPeriodeDefinedException e) {
      throw new AssertionError("This Exception is not part of the test, should therefore not occur",
          e);
    }
  }

  @Und("es existiert ein geplanter Block {string} mit der Pruefung {string} und dem Teilnehmerkreis {string} am {localDateTime}")
  public void esExistiertEinGeplanterBlockMitDerPruefungUndDemTeilnehmerkreisAmUmUhr(
      String blockName,
      String pruefungName, String teilnehmerkreis, LocalDateTime localDateTime) {
    try {
      Teilnehmerkreis tk = createTeilnehmerkreis(teilnehmerkreis);
      ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungName, pruefungName,
          pruefungName, emptySet(), Duration.ofMinutes(120), Map.of(tk, 10));
      ReadOnlyBlock block = state.controller.createBlock(blockName, SEQUENTIAL, pruefung);
      state.controller.scheduleBlock(block, localDateTime);
    } catch (NoPruefungsPeriodeDefinedException | HartesKriteriumException e) {
      throw new AssertionError("This Exception is not part of the test, should therefore not occur",
          e);
    }
  }

  @Dann("ist der Block wieder {string} am {localDateTime} geplant")
  public void istDerBlockWiederAmUmUhrGeplant(String block, LocalDateTime localDateTime)
      throws NoPruefungsPeriodeDefinedException {
    assertThat(state.results.get(EXCEPTION)).isInstanceOf(HartesKriteriumException.class);
    assertThat(getBlockFromModel(block).getTermin()).contains(localDateTime);
  }


  @Wenn("ich den Block {string} am {localDateTime} einplane bugfix")
  public void ichDenBlockAmUmUhrEinplaneBugfix(String block, LocalDateTime termin)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> planned = state.controller.getGeplanteBloecke();
    ReadOnlyBlock roblock = planned.stream().filter(s -> s.getName().equals(block)).findFirst().get();
    try {


      List<ReadOnlyPlanungseinheit> result = state.controller.scheduleBlock(roblock, termin);
      state.results.put("affected", result);
      state.results.put("bugfix", result);
    } catch (HartesKriteriumException e) {
      state.results.put("exception", e);
    }

  }



  @Und("die Pruefung {string} wird auch das Scoring verändert;")
  public void diePruefungWirdAuchDasScoringVerändert(String pruefung)
      throws NoPruefungsPeriodeDefinedException {




    Object exception = state.results.get("bugfix");
    assertThat(exception).isNotNull();
    List<ReadOnlyPlanungseinheit> newList = (List<ReadOnlyPlanungseinheit>) exception;

    assertThat(newList.contains(getPruefungFromModel(pruefung))).isTrue();
    }

  @Und("der Block {string} wird auch das Scoring verändert;")
  public void derBlockWirdAuchDasScoringVerändert(String block)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> planned = state.controller.getGeplanteBloecke();
    assertThat(planned).hasSize(2);
    planned.stream().filter(s -> s.getName().equals(block));
    ReadOnlyBlock resultBlock = planned.toArray(new ReadOnlyBlock[0])[0];

  }
}
