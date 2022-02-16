package integrationTests.steps;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class analyseScoringSteps extends BaseSteps {

  @Wenn("ich die Analyse zu {string} anfrage")
  public void ichDieAnalyseZuAnfrage(String pruefungName)
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = getPruefungFromModel(pruefungName);
    List<KriteriumsAnalyse> violatedCriteria = state.controller.analyseScoring(pruefung);
    state.results.put("kriteriumsanalysen", violatedCriteria);
  }

  @Dann("sind keine Kriterien verletzt")
  public void sindKeineKriterienVerletzt() {
    List<KriteriumsAnalyse> violatedCriteria = (List<KriteriumsAnalyse>) state.results.get(
        "kriteriumsanalysen");
    assertThat(violatedCriteria).isEmpty();
  }

  @Und("es sind mehr Klausuren gleichzeitig geplant als erlaubt")
  public void esSindMehrKlausurenGleichzeitigGeplantAlsErlaubt()
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    LocalDateTime start = LocalDateTime.of(2022, 1, 31, 10, 0);
    for (int idx = 0; idx < 7; idx++) {
      ReadOnlyPruefung pruefung = state.controller.createPruefung(String.valueOf(idx),
          String.valueOf(idx),
          String.valueOf(idx), emptySet(), Duration.ofHours(1), emptyMap());
      state.controller.schedulePruefung(pruefung, start);
    }
  }

  @Wenn("ich die Analyse zu einer der geplanten Klausuren abfrage")
  public void ichDieAnalyseZuEinerDerGeplantenKlausurenAbfrage()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = state.controller.getGeplantePruefungen().stream().findFirst().get();
    List<KriteriumsAnalyse> violatedCriteria = state.controller.analyseScoring(pruefung);
    state.results.put("kriteriumsanalysen", violatedCriteria);
  }

  @Dann("ist das Kriterium {string} verlaetzt")
  public void istDasKriteriumVerlaetzt(String kriterium) {
    List<KriteriumsAnalyse> violatedCriteria = (List<KriteriumsAnalyse>) state.results.get(
        "kriteriumsanalysen");
    assertThat(
        violatedCriteria.stream().anyMatch(x -> x.getKriterium().toString().equals(kriterium)))
        .isTrue();
  }

  @Und("eine weitere Klausur mit anderer Laenge ist zum selben Zeitpunkt geplant")
  public void eineWeitereKlausurMitAndererLaengeIstZumSelbenZeitpunktGeplant()
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    String name = "other";
    LocalDateTime start = LocalDateTime.of(2022, 1, 31, 10, 0);
    ReadOnlyPruefung pruefung = state.controller.createPruefung(name, name, name, emptySet(),
        Duration.ofHours(3), emptyMap());
    state.controller.schedulePruefung(pruefung, start);
  }

  @Und("es existiert ein Teilnehmerkreis mit jeweils einer Klausur an drei aufeinander folgenden Tagen")
  public void esExistiertEinTeilnehmerkreisMitJeweilsEinerKlausurAnDreiAufeinanderFolgendenTagen()
      throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {
    LocalDateTime start = LocalDateTime.of(2022, 1, 31, 10, 0);
    Teilnehmerkreis teilnehmerkreis = new TeilnehmerkreisImpl("INF", "ORD", 2,
        Ausbildungsgrad.BACHELOR);
    for (int idx = 0; idx < 3; idx++) {
      ReadOnlyPruefung pruefung = getOrCreate(String.valueOf(idx));
      state.controller.schedulePruefung(pruefung, start.plusDays(idx));
      state.controller.addTeilnehmerkreis(pruefung, teilnehmerkreis, 10);
    }
  }

  @Wenn("ich die Analyse zur mittleren der drei Klausuren abfrage")
  public void ichDieAnalyseZurMittlerenDerDreiKlausurenAbfrage()
      throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung pruefung = state.controller.getGeplantePruefungen().stream()
        .sorted(Comparator.comparing(ReadOnlyPlanungseinheit::getName))
        .toList().get(1);
    List<KriteriumsAnalyse> violatedCriteria = state.controller.analyseScoring(pruefung);
    state.results.put("kriteriumsanalysen", violatedCriteria);
  }

  @Und("es gibt nur eine Kriteriumsverletzung")
  public void esGibtNurEineKriteriumsverletzung() {
    List<KriteriumsAnalyse> violatedCriteria = (List<KriteriumsAnalyse>) state.results.get(
        "kriteriumsanalysen");
    assertThat(violatedCriteria).hasSize(1);
  }

  @Wenn("ich die Analyse zu einer unbekannten Klausur abfrage")
  public void ichDieAnalyseZuEinerUnbekanntenKlausurAbfrage()
      throws NoPruefungsPeriodeDefinedException {
    String pruefungName = "unknown";
    ReadOnlyPruefung pruefung = new PruefungDTOBuilder()
        .withPruefungsName(pruefungName)
        .withPruefungsNummer(pruefungName)
        .withDauer(Duration.ofHours(1))
        .build();
    try {
      state.controller.analyseScoring(pruefung);
    } catch (IllegalStateException exception) {
      state.results.put("exception", exception);
    }
  }
}
