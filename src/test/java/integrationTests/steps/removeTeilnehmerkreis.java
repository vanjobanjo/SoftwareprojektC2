package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
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
import java.rmi.AlreadyBoundException;
import java.util.List;
import java.util.Set;

public class removeTeilnehmerkreis extends BaseSteps {

  @Wenn("ich der Pruefung {string} den Teilnehmerkreis {string} Fachsemester {int} entfernen moechte")
  public void ichDerPruefungDenTeilnehmerkreisFachsemesterEntfernenMoechte(String pruefung,
      String teilnehmerkreis,
      int semster) throws NoPruefungsPeriodeDefinedException {

    Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis, teilnehmerkreis, semster,
        Ausbildungsgrad.BACHELOR);
    state.controller.removeTeilnehmerkreis(getPruefungFromModel(pruefung), t);

  }



  @Dann("hat die Pruefung {string} nicht den Teilnehmerkreis {string} im Fachsemster {int}")
  public void hatDiePruefungNichtDenTeilnehmerkreisImFachsemster(String pruefung,
      String teilnehmerkreis,
      int semster) throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis, teilnehmerkreis, semster,
        Ausbildungsgrad.BACHELOR);
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);

    assertThat(roPruefung.getTeilnehmerkreise().contains(t)).isFalse();
  }


  @Und("die Pruefung {string} hat den Teilnehmerkreis {string} im Fachsemster {int}")
  public void diePruefungHatDenTeilnehmerkreisImFachsemster(String pruefung, String teilnehmerkreis,
      int semster)
      throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis, teilnehmerkreis, semster,
        Ausbildungsgrad.BACHELOR);
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);

    assertThat(roPruefung.getTeilnehmerkreise().contains(t)).isTrue();
  }

  @Und("es existiert die ungeplante Pruefung {string} mit dem Teilnehmerkreis {string} im {int} Semester und {string} im {int} Semster")
  public void esExistiertDieUngeplantePruefungMitDemTeilnehmerkreisImSemesterUndImSemster(
      String pruefung, String teilnehmerkreis1, int semster1, String teilnehmerkreis2, int semster2)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {

    getOrCreate(pruefung, teilnehmerkreis1, semster1);
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
        Ausbildungsgrad.BACHELOR, teilnehmerkreis2, teilnehmerkreis2, semster2);

    state.controller.addTeilnehmerkreis(roPruefung, teilnehmerkreis, 10);
  }

  @Wenn("ich einer Pruefung einen Teilnehmerkreis enfernen möchte")
  public void ichEinerPruefungEinenTeilnehmerkreisEnfernenMoechte()
      throws NoPruefungsPeriodeDefinedException {

    try {
      ReadOnlyPruefung roPruefung = new PruefungDTOBuilder().withPruefungsName("nicht da").withPruefungsNummer("test").build();
      state.controller.removeTeilnehmerkreis(roPruefung,
          new TeilnehmerkreisImpl("nicht vorhanden", "nichtrelevant", 1, Ausbildungsgrad.BACHELOR));
    } catch (IllegalStateException e) {
      state.results.put("exception", e);
    }
  }


  @Dann("hat die Pruefung {string} nicht den Teilnehmerkreis {string} im Fachsemster {int} bufix")
  public void hatDiePruefungNichtDenTeilnehmerkreisImFachsemsterBufix(String pruefung, String teilnehmerkreis,
      int semester) throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis, teilnehmerkreis, semester,
        Ausbildungsgrad.BACHELOR);
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);

    assertThat(roPruefung.getTeilnehmerkreise().contains(t)).isFalse();


    assertThat(state.results).isNotEmpty();
    Object buffix = state.results.get("bugfix");
    assertThat(buffix).isInstanceOf(List.class);
    List bugfix1 = (List)buffix;
    assertThat(bugfix1).isNotEmpty();
    Object bugfix2 = bugfix1.get(0);
    assertThat(bugfix2).isInstanceOf(ReadOnlyPlanungseinheit.class);
    ReadOnlyPruefung roBug = ((ReadOnlyPlanungseinheit) bugfix2).asPruefung();
    assertThat(roBug.getTeilnehmerkreise()).isNotEmpty();
    assertThat(roBug.getTeilnehmerkreise()).doesNotContain(t);



  }

  @Und("die Pruefung {string} hat den Teilnehmerkreis {string} im Fachsemster {int} bugfix")
  public void diePruefungHatDenTeilnehmerkreisImFachsemsterBugfix(String pruefung, String teilnehmerkreis,
      int semester) throws NoPruefungsPeriodeDefinedException {

    Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis, teilnehmerkreis, semester,
        Ausbildungsgrad.BACHELOR);
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);

    assertThat(roPruefung.getTeilnehmerkreise().contains(t)).isTrue();


    assertThat(state.results).isNotEmpty();
    Object buffix = state.results.get("bugfix");
    assertThat(buffix).isInstanceOf(List.class);
    List bugfix1 = (List)buffix;
    assertThat(bugfix1).isNotEmpty();
    Object bugfix2 = bugfix1.get(0);
    assertThat(bugfix2).isInstanceOf(ReadOnlyPlanungseinheit.class);
    ReadOnlyPruefung roBug = ((ReadOnlyPlanungseinheit) bugfix2).asPruefung();

    assertThat(roBug.getTeilnehmerkreise()).contains(t);
  }

  @Wenn("ich der Pruefung {string} den Teilnehmerkreis {string} Fachsemester {int} entfernen moechte bugfix")
  public void ichDerPruefungDenTeilnehmerkreisFachsemesterEntfernenMoechteBugfix(String pruefung,
      String teilnehmerkreis, int semster) throws NoPruefungsPeriodeDefinedException {



    Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis, teilnehmerkreis, semster,
        Ausbildungsgrad.BACHELOR);
    state.results.put("bugfix",state.controller.removeTeilnehmerkreis(getPruefungFromModel(pruefung), t));

  }
}
