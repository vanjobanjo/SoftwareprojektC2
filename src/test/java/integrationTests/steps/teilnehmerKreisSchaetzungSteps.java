package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.AssumptionViolatedException;

public class teilnehmerKreisSchaetzungSteps extends BaseSteps {

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
      private final char ausbildungsgrad = teilnehmerKreisDetails[0].charAt(0);
      private final String studiengang = teilnehmerKreisDetails[0].substring(1);
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

      @Override
      public Ausbildungsgrad getAusbildungsgrad() {
        return switch (ausbildungsgrad) {
          case 'M' -> Ausbildungsgrad.MASTER;
          case 'B' -> Ausbildungsgrad.BACHELOR;
          default -> Ausbildungsgrad.AUSBILDUNG;
        };
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
  public void diePruefungHatAlsTeilnehmerkreisschaetzung(String pruefung,
      String teilnehmerKreisSchatzung) {
    throw new AssumptionViolatedException("Not implemented yet!");
  }

  @Angenommen("die Pruefung {string} hat keinen Teilnehmerkreis")
  public void diePruefungHatKeinenTeilnehmerkreis(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
  }

  @Wenn("ich den Teilnehmerkreis {string} zu {string} hinzufuege")
  public void ichDenTeilnehmerkreisZuHinzufuege(String teilnehmerkreisName, String pruefungName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    List<ReadOnlyPlanungseinheit> results = state.controller.addTeilnehmerkreis(pruefung,
        teilnehmerkreis, 20);
    state.results.put("planungseinheiten", results);
  }



  private String getRandomString(Random random, int length) {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    return random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  @Und("{string} hat den Teilnehmerkreis {string}")
  public void hatDenTeilnehmerkreis(String pruefungName, String teilnehmerkreisName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    assertThat(pruefung.getTeilnehmerkreise()).contains(teilnehmerkreis);
  }

  @Und("die Pruefung {string} hat einen Teilnehmerkreis {string}")
  public void diePruefungHatEinenTeilnehmerkreis(String pruefungName, String teilnehmerkreisName)
      throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    state.controller.createPruefung(pruefungName, pruefungName,
        pruefungName, emptySet(), Duration.ofHours(1), Map.of(teilnehmerkreis, 11));
  }

  @Und("die Pruefung {string} hat einen Teilnehmerkreis {string} mit {int} Studenten")
  public void diePruefungHatEinenTeilnehmerkreisMitStudenten(String pruefungName,
      String teilnehmerkreisName, int amountStudenten)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    ReadOnlyPruefung p = getOrCreate(pruefungName);
    state.controller.addTeilnehmerkreis(p, teilnehmerkreis, amountStudenten);
  }

  @Wenn("ich den Teilnehmerkreis {string} mit {int} Studenten zu {string} hinzufuege")
  public void ichDenTeilnehmerkreisMitStudentenZuHinzufuege(String teilnehmerkreisName,
      int amountStudenten, String pruefungName)
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    try {
      List<ReadOnlyPlanungseinheit> results = state.controller.addTeilnehmerkreis(pruefung,
          teilnehmerkreis, amountStudenten);
      state.results.put("planungseinheiten", results);
    } catch (IllegalArgumentException | HartesKriteriumException exception) {
      state.results.put("exception", exception);
    }
  }

  @Und("{string} hat den Teilnehmerkreis {string} mit {int} Studenten")
  public void hatDenTeilnehmerkreisMitStudenten(String pruefungName, String teilnehmerkreisName,
      int amountStudenten)
      throws NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    assertThat(pruefung.getTeilnehmerkreise()).contains(teilnehmerkreis);
    assertThat(pruefung.getTeilnehmerKreisSchaetzung()).containsEntry(teilnehmerkreis, amountStudenten);
  }

  @Wenn("ich den Teilnehmerkreis {string} zu einer unbekannten Pruefung {string} hinzufuege")
  public void ichDenTeilnehmerkreisZuEinerUnbekanntenPruefungHinzufuege(String teilnehmerkreisName,
      String pruefungName)
      throws HartesKriteriumException, NoPruefungsPeriodeDefinedException {
    Teilnehmerkreis teilnehmerkreis = createTeilnehmerkreis(teilnehmerkreisName);
    ReadOnlyPruefung pruefung = new PruefungDTO(pruefungName, pruefungName, Duration.ZERO,
        emptyMap(), emptySet(), 0);
    try {
      List<ReadOnlyPlanungseinheit> result = state.controller.addTeilnehmerkreis(pruefung,
          teilnehmerkreis, 10);
      state.results.put("planungseinheiten", result);
    } catch (IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }
}
