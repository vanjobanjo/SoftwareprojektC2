package integrationTests.steps;


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
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.AssumptionViolatedException;

public class addPruefungToBlock extends BaseSteps {

  @Angenommen("es existiert der leere Block {string}")
  public void esExistiertDerLeereBlock(String block) throws NoPruefungsPeriodeDefinedException {
    state.controller.createBlock(block, Blocktyp.PARALLEL);
  }

  @Dann("erhalte ich einen Block der die Pruefung {string} enthaelt")
  public void erhalteIchEinenBlockDerDiePruefungEnthaelt(String Pruefung) {
//    state
  }

  @Angenommen("es existiert der Block {string} mit der Pruefung {string}")
  public void esExistiertDerBlockMitDerPruefung(String block, String string) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("erhalte ich einen Block mit den Pruefungen {string} und {string}")
  public void erhalteIchEinenBlockMitDenPruefungenUnd(String pruefung1, String pruefung2) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es gibt am selben Tag einen geplanten Block {string} und die geplante Pruefung {string}")
  public void esGibtAmSelbenTagEinenGeplantenBlockUndDieGeplantePruefung(String block,
      String klausur) {
    throw new AssumptionViolatedException("not implemented");
  }

  @Wenn("ich die Pruefung {string} zum Block {string} hinzufuege")
  public void ichDiePruefungZuHinzufuege(String pruefung, String block)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    ReadOnlyBlock blockToChange = getBlockFromModel(block);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung(pruefung, pruefung,
        pruefung, emptySet(), Duration.ofHours(1), emptyMap());
    List<ReadOnlyPlanungseinheit> result = state.controller.addPruefungToBlock(blockToChange,
        pruefungToChange);
    state.results.put("planungseinheiten", result);
  }

  private ReadOnlyBlock getBlockFromModel(String name) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> bloecke = new HashSet<>();
    bloecke.addAll(state.controller.getGeplanteBloecke());
    bloecke.addAll(state.controller.getUngeplanteBloecke());
    return bloecke.stream().filter(block -> block.getName().equals(name)).findFirst().get();
  }

  @Dann("erhalte ich eine Fehlermeldung")
  public void erhalteIchEineFehlermeldung() {
    throw new AssumptionViolatedException("not implemented");
  }

  @Angenommen("es existiert keine Pruefungsperiode")
  public void esExistiertKeinePruefungsperiode() {
    throw new AssumptionViolatedException("not implemented");
  }

  @Dann("enthalten die Planungseinheiten, die ich erhalte den Block {string}")
  public void enthaltenDiePlanungseinheitenDieIchErhalteDenBlock(String blockName) {
    List<ReadOnlyPlanungseinheit> results = toPlanungseinheiten(
        state.results.get("planungseinheiten"));
    assertThat(results).anyMatch(
        (ReadOnlyPlanungseinheit p) -> p.isBlock() && p.asBlock().getName().equals(blockName));
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

  @Und("der Block {string} enthaelt {string}")
  public void derBlockEnthaelt(String block, String pruefung) {
    List<ReadOnlyPlanungseinheit> results = toPlanungseinheiten(
        state.results.get("planungseinheiten"));
    Optional<ReadOnlyBlock> roBlock = results.stream()
        .filter(ReadOnlyPlanungseinheit::isBlock)
        .filter(planungseinheit -> planungseinheit.getName().equals(block))
        .map(ReadOnlyPlanungseinheit::asBlock).findFirst();
    assertThat(roBlock).isPresent();
    assertThat(roBlock.get().getROPruefungen()).anyMatch(
        roPruefung -> roPruefung.getName().equals(pruefung));
  }

}
