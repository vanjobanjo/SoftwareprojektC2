package integrationTests.steps;


import static java.time.Month.FEBRUARY;
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
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class addPruefungToBlock extends BaseSteps {

  @Angenommen("es existiert der leere Block {string}")
  public void esExistiertDerLeereBlock(String block) throws NoPruefungsPeriodeDefinedException {
    state.controller.createBlock(block, Blocktyp.PARALLEL);
  }

  @Angenommen("es existiert der Block {string} mit der Pruefung {string}")
  public void esExistiertDerBlockMitDerPruefung(String block, String pruefung)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = state.controller.createBlock(block, Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung(pruefung, pruefung,
        pruefung, emptySet(), Duration.ofHours(1), emptyMap());
    state.controller.addPruefungToBlock(blockToChange, pruefungToChange);
  }

  @Dann("der Block {string} enthaelt {stringList}")
  public void erhalteIchEinenBlockMitDenPruefungenUnd(String blockName, List<String> pruefungen) {
    List<ReadOnlyPlanungseinheit> results = toPlanungseinheiten(
        state.results.get("planungseinheiten"));
    Optional<ReadOnlyBlock> block = results.stream()
        .filter(ReadOnlyPlanungseinheit::isBlock)
        .map(ReadOnlyPlanungseinheit::asBlock)
        .filter(b -> b.getName().equals(blockName))
        .findFirst();
    assertThat(block).isPresent();
    assertThat(block.get().getROPruefungen()).anyMatch(
        roPruefung -> pruefungen.contains(roPruefung.getName()));
  }

  private List<ReadOnlyPlanungseinheit> toPlanungseinheiten(Object obj) {
    List<ReadOnlyPlanungseinheit> result = new ArrayList<>();
    List<Object> input = (List<Object>) obj;
    for (Object planungseinheit : input) {
      if (planungseinheit instanceof BlockDTO) {
        result.add((BlockDTO) planungseinheit);
      } else if (planungseinheit instanceof PruefungDTO) {
        result.add((PruefungDTO) planungseinheit);
      } else {
        throw new IllegalStateException();
      }
    }
    return result;
  }

  @Wenn("ich die Pruefung {string} zum Block {string} hinzufuege")
  public void ichDiePruefungZuHinzufuege(String pruefung, String block)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock blockToChange = getBlockFromModel(block);
    ReadOnlyPruefung pruefungToChange = getOrCreate(pruefung);
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.addPruefungToBlock(blockToChange,
          pruefungToChange);
      state.results.put("planungseinheiten", result);
    } catch (IllegalArgumentException | HartesKriteriumException exception) {
      state.results.put("exception", exception);
    }
  }

  private ReadOnlyBlock getBlockFromModel(String name) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> bloecke = new HashSet<>();
    bloecke.addAll(state.controller.getGeplanteBloecke());
    bloecke.addAll(state.controller.getUngeplanteBloecke());
    return bloecke.stream().filter(block -> block.getName().equals(name)).findFirst().get();
  }

  private ReadOnlyPruefung getOrCreate(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung;
    if (existsPruefungWith(pruefungName)) {
      pruefung = getPruefungFromControllerWith(pruefungName);
    } else {
      pruefung = state.controller.createPruefung(pruefungName, pruefungName,
          pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
    }
    return pruefung;
  }

  @Wenn("ich die geplante Pruefung {string} zum Block {string} hinzufuege")
  public void ichDieGeplantePruefungZumBlockHinzufuege(String pruefungName, String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = getBlockFromModel(blockName);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
    state.controller.schedulePruefung(pruefungToChange, LocalDateTime.of(2022, FEBRUARY, 7, 8, 0));
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.addPruefungToBlock(blockToChange,
          pruefungToChange);
      state.results.put("planungseinheiten", result);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Dann("aendert sich nichts")
  public void aendertSichNichts() {
    List<ReadOnlyPlanungseinheit> results = toPlanungseinheiten(
        state.results.get("planungseinheiten"));
    assertThat(results).isEmpty();
  }

  @Dann("enthalten die Planungseinheiten, die ich erhalte den Block {string}")
  public void enthaltenDiePlanungseinheitenDieIchErhalteDenBlock(String blockName) {
    List<ReadOnlyPlanungseinheit> results = toPlanungseinheiten(
        state.results.get("planungseinheiten"));
    assertThat(results).anyMatch(
        (ReadOnlyPlanungseinheit p) -> p.isBlock() && p.asBlock().getName().equals(blockName));
  }

  @Wenn("ich die unbekannte Pruefung {string} zum Block {string} hinzufuege")
  public void ichDieUnbekanntePruefungZumBlockHinzufuege(String pruefungsName, String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = getBlockFromModel(blockName);
    ReadOnlyPruefung pruefungToChange = new PruefungDTO(pruefungsName, pruefungsName,
        Duration.ofHours(1), emptyMap(), emptySet(), 12);
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.addPruefungToBlock(blockToChange,
          pruefungToChange);
      state.results.put("planungseinheiten", result);
    } catch (IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich die Pruefung {string} zu einem unbekannten Block {string} hinzufuege")
  public void ichDiePruefungZuEinemUnbekanntenBlockHinzufuege(String pruefungName, String blockName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = new BlockDTO(blockName, null, Duration.ZERO, emptySet(), 1234,
        Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.addPruefungToBlock(blockToChange,
          pruefungToChange);
      state.results.put("planungseinheiten", result);
    } catch (IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }

  @Und("es existiert der geplante Block {string} mit der Pruefung {string}")
  public void esExistiertDerGeplanteBlockMitDerPruefung(String blockName, String pruefungName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = state.controller.createBlock(blockName, Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
    state.controller.addPruefungToBlock(blockToChange, pruefungToChange);
    state.controller.scheduleBlock(blockToChange,
        state.controller.getStartDatumPeriode().atStartOfDay());
  }

  @Und("die Pruefung {string} ist direkt nach {string} geplant")
  public void esExistiertEineGeplantePruefung(String pruefungName, String other)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung pruefung = getOrCreate(pruefungName);
    ReadOnlyPlanungseinheit before = getPlanungseinheitFromModel(other);
    // 30 min buffer // TODO make buffer a property and use it here
    LocalDateTime schedule = before.getTermin().get().plus(before.getDauer()).plusMinutes(30);
    state.controller.schedulePruefung(pruefung, schedule);
  }

  private ReadOnlyPlanungseinheit getPlanungseinheitFromModel(String name)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit result;
    try {
      result = getPruefungFromModel(name);
    } catch (NoSuchElementException exception) {
      result = getBlockFromModel(name);
    }
    return result;
  }

  private ReadOnlyPruefung getPruefungFromModel(String name)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    pruefungen.addAll(state.controller.getGeplantePruefungen());
    pruefungen.addAll(state.controller.getUngeplantePruefungen());
    return pruefungen.stream().filter(pruefung -> pruefung.getName().equals(name)).findFirst()
        .get();
  }

  @Und("es existiert eine ungeplante Pruefung {string}")
  public void esExistiertEineUngeplantePruefung(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(pruefungName,
        pruefungName,
        pruefungName,
        emptySet(),
        Duration.ofHours(1),
        emptyMap());
  }

  @Und("{string} und {string} haben einen gemeinsamen Teilnehmerkreis")
  public void undHabenEinenGemeinsamenTeilnehmerkreis(String fstPruefung, String sndPruefung)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl("Informatik", "14.0", 1,
        Ausbildungsgrad.BACHELOR);
    ReadOnlyPruefung p1 = getPruefungFromModel(fstPruefung);
    ReadOnlyPruefung p2 = getPruefungFromModel(sndPruefung);
    state.controller.addTeilnehmerkreis(p1, teilnehmerkreis, 22);
    state.controller.addTeilnehmerkreis(p2, teilnehmerkreis, 33);
  }

  @Dann("ist {string} Teil der beeinflussten Planungseinheiten")
  public void istTeilDerBeeinflusstenPlanungseinheiten(String planungseinheitName)
      throws NoPruefungsPeriodeDefinedException {
    List<ReadOnlyPlanungseinheit> results = toPlanungseinheiten(
        state.results.get("planungseinheiten"));
    ReadOnlyPlanungseinheit planungseinheit = getPlanungseinheitFromModel(planungseinheitName);
    assertThat(results).contains(planungseinheit);
  }

  @Und("es existiert der geplante Block {string} mit der Pruefung {string} direkt nach {string}")
  public void esExistiertDerGeplanteBlockMitDerPruefungDirektNach(String blockName,
      String pruefungName,
      String other) throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyBlock blockToChange = state.controller.createBlock(blockName, Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefung = state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
    ReadOnlyPlanungseinheit previousPlanungseinheit = getPlanungseinheitFromModel(other);
    LocalDateTime schedule = previousPlanungseinheit.getTermin().get()
        .plus(previousPlanungseinheit.getDauer()).plusMinutes(30);
    state.controller.addPruefungToBlock(blockToChange, pruefung);
    state.controller.scheduleBlock(blockToChange, schedule);
  }

  @Und("die Pruefung {string} ist zeitgleich mit {string} geplant")
  public void diePruefungIstZeitgleichMitGeplant(String pruefungName, String planungseinheitName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    ReadOnlyPruefung pruefung = getOrCreate(pruefungName);
    ReadOnlyPlanungseinheit other = getPlanungseinheitFromModel(planungseinheitName);
    LocalDateTime schedule = other.getTermin().get();
    state.controller.schedulePruefung(pruefung, schedule);
  }

  @Dann("ist ein hartes Kriterium verletzt")
  public void istEinHartesKriteriumVerletzt() {
    assertThat((Exception) state.results.get("exception")).isInstanceOf(
        HartesKriteriumException.class);
  }
}
