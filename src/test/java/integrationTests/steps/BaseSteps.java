package integrationTests.steps;

import static de.fhwedel.klausps.model.api.Semestertyp.WINTERSEMESTER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import de.fhwedel.klausps.controller.Controller;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.SemesterImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import integrationTests.state.State;
import io.cucumber.java.ParameterType;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A common point for helper methods required in many step definitions.
 */
public class BaseSteps {

  public static State state;

  protected static final String EXCEPTION = "exception";


  protected Duration parseDuration(String durationTxt) {
    String[] tmp = durationTxt.split(":");
    return Duration.ofHours(Integer.parseInt(tmp[0]))
        .plus(Duration.ofMinutes(Integer.parseInt(tmp[1])));
  }

  protected void createSemester() throws IllegalTimeSpanException {
    Semester semester = new SemesterImpl(WINTERSEMESTER, Year.of(2022));
    LocalDate start = LocalDate.of(2022, 1, 31);
    LocalDate end = LocalDate.of(2022, 2, 27);
    LocalDate ankertag = start.plusDays(7);
    state.controller.createEmptyPeriode(semester, start, end, ankertag, 400);
  }




  protected void putExceptionInResult(Exception exception) {
    state.results.put(EXCEPTION, exception);
  }

  protected Object getExceptionFromResult() {
    return state.results.get(EXCEPTION);
  }

  /**
   * Gets any Planungseinheit (either Block or Pruefung) with a specific name out of the model.
   * Throws exception if not existent!
   *
   * @param name The name of the planungseinheit to find.
   * @return The requested Planungseinheit.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyPlanungseinheit getPlanungseinheitFromModel(String name)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPlanungseinheit result;
    try {
      result = getPruefungFromModel(name);
    } catch (NoSuchElementException exception) {
      result = getBlockFromModel(name);
    }
    return result;
  }

  /**
   * Gets a pruefung with a specific name out of the model. Throws exception if not existent!
   *
   * @param name The name of the pruefung to find.
   * @return The requested Pruefung.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyPruefung getPruefungFromModel(String name)
      throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    pruefungen.addAll(state.controller.getGeplantePruefungen());
    pruefungen.addAll(state.controller.getUngeplantePruefungen());
    return pruefungen.stream().filter(pruefung -> pruefung.getName().equals(name)).findFirst()
        .get();
  }

  /**
   * Gets a block with a specific name out of the model. Throws exception if not existent!
   *
   * @param name The name of the block to find.
   * @return The requested block.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyBlock getBlockFromModel(String name) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> bloecke = new HashSet<>();
    bloecke.addAll(state.controller.getGeplanteBloecke());
    bloecke.addAll(state.controller.getUngeplanteBloecke());
    return bloecke.stream().filter(block -> block.getName().equals(name)).findFirst().get();
  }

  /**
   * Gets a pruefung from model if existent or else creates it.
   *
   * @param pruefungName The name of the requested pruefung.
   * @return The requested pruefung.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyPruefung getOrCreate(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung;
    if (existsPruefungWith(pruefungName)) {
      pruefung = getPruefungFromModel(pruefungName);
    } else {
      pruefung = state.controller.createPruefung(pruefungName, pruefungName,
          pruefungName, emptySet(), Duration.ofHours(1), emptyMap());
    }
    return pruefung;
  }


  /**
   * Gets a pruefung from model if existent or else creates it.
   *
   * @param pruefungName          The name of the requested pruefung.
   * @param teilnehmerkreisString name of the Teilnehmerkreises
   * @param semster               witch semster the Teilnehmerkreis is
   * @return The requested pruefung.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyPruefung getOrCreate(String pruefungName, String teilnehmerkreisString,
      int semster)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung;
    if (existsPruefungWith(pruefungName)) {
      pruefung = getPruefungFromModel(pruefungName);
    } else {
      Map<Teilnehmerkreis, Integer> teilnehmerMap = new HashMap<>();
      Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
          Ausbildungsgrad.BACHELOR, teilnehmerkreisString, teilnehmerkreisString, semster);
      teilnehmerMap.put(teilnehmerkreis, 10);
      pruefung = state.controller.createPruefung(pruefungName, pruefungName,
          pruefungName, emptySet(), Duration.ofHours(1), teilnehmerMap);
    }
    return pruefung;

  }

  /**
   * Gets a pruefung from model if existent or else creates it.
   *
   * @param pruefungName          The name of the requested pruefung.
   * @param teilnehmerkreisString name of the Teilnehmerkreises
   * @param semster               witch semster the Teilnehmerkreis is
   * @param count                 the count of the students in this Teilnehmerkreis
   * @return The requested pruefung.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyPruefung getOrCreate(String pruefungName, String teilnehmerkreisString,
      int semster, String ausbildunggrade, int count)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung;
    if (existsPruefungWith(pruefungName)) {
      pruefung = getPruefungFromModel(pruefungName);
    } else {
      Map<Teilnehmerkreis, Integer> teilnehmerMap = new HashMap<>();
      Ausbildungsgrad aG = Ausbildungsgrad.BACHELOR;
      try {
        aG = Ausbildungsgrad.valueOf(ausbildunggrade);
      } catch (IllegalArgumentException ignored) {
      }
      Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
          aG, teilnehmerkreisString, teilnehmerkreisString, semster);
      teilnehmerMap.put(teilnehmerkreis, count);
      pruefung = state.controller.createPruefung(pruefungName, pruefungName,
          pruefungName, emptySet(), Duration.ofHours(1), teilnehmerMap);
    }
    return pruefung;

  }

  /**
   * Gets a pruefung from model if existent or else creates it.
   *
   * @param pruefungName          The name of the requested pruefung.
   * @param teilnehmerkreisString name of the Teilnehmerkreises
   * @param semster               witch semster the Teilnehmerkreis is
   * @param duration              the duration in Minutes for the pruefung.
   * @return The requested pruefung.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected ReadOnlyPruefung getOrCreate(String pruefungName, String teilnehmerkreisString,
      int semster, int duration)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung;
    if (existsPruefungWith(pruefungName)) {
      pruefung = getPruefungFromModel(pruefungName);
    } else {
      Map<Teilnehmerkreis, Integer> teilnehmerMap = new HashMap<>();
      Ausbildungsgrad aG = Ausbildungsgrad.BACHELOR;

      Teilnehmerkreis teilnehmerkreis = state.controller.createTeilnehmerkreis(
          aG, teilnehmerkreisString, teilnehmerkreisString, semster);
      teilnehmerMap.put(teilnehmerkreis, 10);
      pruefung = state.controller.createPruefung(pruefungName, pruefungName,
          pruefungName, emptySet(), Duration.ofMinutes(duration), teilnehmerMap);
    }
    return pruefung;

  }


  /**
   * Check whether a pruefung with a specific name exists in the model.
   *
   * @param pruefungName The name of the pruefung to check for.
   * @return True in case the pruefung exists in the model, otherwise False.
   * @throws NoPruefungsPeriodeDefinedException In case there is no Pruefungsperiode.
   */
  protected boolean existsPruefungWith(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    return getAllPruefungen().stream().anyMatch(
        (ReadOnlyPruefung readOnlyPruefung) -> readOnlyPruefung.getPruefungsnummer()
            .equals(pruefungName));
  }

  private Set<ReadOnlyPruefung> getAllPruefungen() throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> allPruefungen = new HashSet<>();
    allPruefungen.addAll(state.controller.getGeplantePruefungen());
    allPruefungen.addAll(state.controller.getUngeplantePruefungen());
    return allPruefungen;
  }

  /**
   * Methode um alle static Felder von dem ServiceProvider auf null zu setzen
   *
   * @throws IllegalAccessException wenn das Feld nicht sichtbar ist
   */
  protected void resetAll() throws IllegalAccessException {
    // Multiple Fields access
    Field[] fields = ServiceProvider.class.getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      if (!field.getName().equals("LOGGER")) {
        field.set(field, null);
      }
      System.out.println(field.getName());
    }

    state.controller = new Controller();
  }

  protected Teilnehmerkreis createTeilnehmerkreis(String teilnehmerkreisName) {
    return new TeilnehmerkreisImpl(teilnehmerkreisName, teilnehmerkreisName, 1,
        Ausbildungsgrad.BACHELOR);
  }
}
