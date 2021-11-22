package integrationTests.steps;

import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.AssumptionViolatedException;

public class teilnehmerKreisSchaetzungSteps {

  @ParameterType("(\\S+ \\d+ \\S+ \\d+(, \\S+ \\d+ \\S+ \\d+)+)")
  public List<Teilnehmerkreis> listOfTeilnehmerKreis(String input) {
    String[] s = input.split(", ");
    return Arrays.stream(s).map(this::teilnehmerkreis).collect(Collectors.toList());
  }

  @ParameterType("\\S+ \\d+ \\S+ \\d+")
  public Teilnehmerkreis teilnehmerkreis(String input) {
    // TODO use teilnehmerKreis of real implementation instead when existent
    //    Add Schaetzung to Teilnehmerkreis (last element in teilnehmerKreisDetails)
    String[] teilnehmerKreisDetails = input.split(" ");
    return new Teilnehmerkreis() {
      private final String studiengang = teilnehmerKreisDetails[0];
      private final int fachSemester = Integer.parseInt(teilnehmerKreisDetails[1]);
      private final String pruefungsOrdnung = teilnehmerKreisDetails[2];

      @Override
      public String getStudiengang() {
        return studiengang;
      }

      @Override
      public String getPruefungsordnung() {
        return pruefungsOrdnung;
      }

      @Override
      public int getFachsemester() {
        return fachSemester;
      }
    };
  }

  @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {teilnehmerkreis}")
  public void diePruefungHatAlsTeilnehmerkreisschaetzung(
      String pruefung, Teilnehmerkreis teilnehmerKreisSchatzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {listOfTeilnehmerKreis}")
  public void diePruefungHatTeilnehmerkreise(
      String pruefung, List<Teilnehmerkreis> teilnehmerKreise) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Wenn(
      "ich den Studiengang {string} Fachsemester {int} mit Ordnung {string} und {int} schaetze und hinzufuege")
  public void ichDenStudiengangFachsemesterMitOrdnungUndSchaetzeUndHinzufuege(
      String studiengang, int sememster, String ordnung, int schaetzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Dann("hat die Pruefung {string} die Teilnehmerkreischaetzungen: {string}")
  public void hatDiePruefungDieTeilnehmerkreischaetzungen(
      String pruefung, String teilnehmerKreisSchaetzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }



  @Wenn(
      "ich den Studiengang {string} Fachsemester {int} mit Ordnung {string} und {int} schaetze und entferne")
  public void ichDenStudiengangFachsemesterMitOrdnungUndSchaetzeUndEntferne(
      String pruefung, int semester, String ordnung, int schaetzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat als Teilnehmerkreisschaetzung: {string}")
  public void diePruefungHatAlsTeilnehmerkreisschaetzung(String pruefung, String teilnehmerKreisSchatzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }
}
