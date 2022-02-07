package integrationTests.steps;


import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.api.Planungseinheit;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.List;
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
    ReadOnlyBlock blockToChange = new BlockDTO(block, null, Duration.ZERO, emptySet(),
        block.hashCode(), Blocktyp.PARALLEL);
    ReadOnlyPruefung pruefungToChange = state.controller.createPruefung(pruefung, pruefung,
        pruefung, emptySet(), Duration.ofHours(1), emptyMap());
    List<ReadOnlyPlanungseinheit> result = state.controller.addPruefungToBlock(blockToChange,
        pruefungToChange);
    state.results.put("planungseinheiten", result);
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
    List<Planungseinheit> results = (List<Planungseinheit>) state.results.get("planungseinheiten");
    assertThat(results).anyMatch(
        (Planungseinheit p) -> p.isBlock() && p.asBlock().getName().equals(blockName));
  }
}
