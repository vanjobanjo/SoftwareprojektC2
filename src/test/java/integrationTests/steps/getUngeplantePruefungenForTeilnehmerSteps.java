package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Ausbildungsgrad.BACHELOR;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Wenn;
import java.util.Collection;

public class getUngeplantePruefungenForTeilnehmerSteps extends BaseSteps {

  @Wenn("ich die ungeplanten Pruefungen zum Teilnehmerkreis {string} Semester {int} abfrage")
  public void ichDieUngeplantenPruefungenZumTeilnehmerkreisSemesterAbfrage(
      String teilnehmerkreisName, int semester)
      throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl(teilnehmerkreisName,
        teilnehmerkreisName, semester, BACHELOR);
    Collection<ReadOnlyPruefung> result = state.controller.getUngeplantePruefungenForTeilnehmer(
        teilnehmerkreis);
    state.results.put("pruefungen", result);
  }
}
