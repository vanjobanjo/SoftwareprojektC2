package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Ausbildungsgrad.BACHELOR;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

public class createTeilnehmerkreis extends BaseSteps {

  @Wenn("ich einen Bachelor Teilnehmerkreis erstelle mit {string}, {string} und {int} erstelle")
  public void ichEinenTeilnehmerkreisErstelleMitAusbildungsgradStudiengangStudienordnungUndSemesterErstelle(
      String studiengang, String studienordnung, Integer semester) {
    Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
        BACHELOR, studiengang, studienordnung, semester);
    state.results.put("teilnehmerkreis", teilnehmerkreis);
  }

  @Dann("erhalte ich einen Bachelor Teilnehmerkreis mit {string}, {string} und {int}")
  public void erhalteIchEinenBachelorTeilnehmerkreisMitStudiengangStudienordnungUndSemester(
      String studiengang, String studienordnung, Integer semester) {
    Teilnehmerkreis teilnehmerkreis = (Teilnehmerkreis) state.results.get("teilnehmerkreis");
    assertThat(teilnehmerkreis.getAusbildungsgrad()).isEqualTo(BACHELOR);
    assertThat(teilnehmerkreis.getStudiengang()).isEqualTo(studiengang);
    assertThat(teilnehmerkreis.getPruefungsordnung()).isEqualTo(studienordnung);
    assertThat(teilnehmerkreis.getFachsemester()).isEqualTo(semester);
  }

  @Wenn("ich einen Teilnehmerkreis ohne Studiengang erstelle")
  public void ichEinenTeilnehmerkreisOhneStudiengangErstelle() {
    try {
      Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
          BACHELOR, "", "ord", 1);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich einen Teilnehmerkreis ohne Studienordnung erstelle")
  public void ichEinenTeilnehmerkreisOhneStudienordnungErstelle() {
    try {
      Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
          BACHELOR, "Inf", "", 1);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }

  @Wenn("ich einen Teilnehmerkreis ohne Semester erstelle")
  public void ichEinenTeilnehmerkreisOhneSemesterErstelle() {
    try {
      Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
          BACHELOR, "Inf", "ord", 0);
    } catch (IllegalArgumentException exception) {
      state.results.put("exception", exception);
    }
  }
}
