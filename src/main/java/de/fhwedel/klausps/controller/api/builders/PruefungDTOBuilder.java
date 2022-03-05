package de.fhwedel.klausps.controller.api.builders;

import static de.fhwedel.klausps.controller.util.ParameterUtil.noNullParameters;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builder class for {@link PruefungDTO}.
 */
public class PruefungDTOBuilder {

  /**
   * The Pruefungsnummer of the pruefung.
   */
  private String pruefungsNummer;

  /**
   * The name of the pruefung.
   */
  private String pruefungsName;

  /**
   * The participants of the pruefung with their amount.
   */
  private Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung;

  /**
   * The duration of the pruefung.
   */
  private Duration dauer;

  /**
   * The start time of the pruefung.
   */
  private LocalDateTime startZeitpunkt;

  /**
   * The scoring of the pruefung.
   */
  private int scoring;

  /**
   * The pruefer conducting of the pruefung.
   */
  private Set<String> pruefer;

  /**
   * Create a new Builder.
   */
  public PruefungDTOBuilder() {
    this.pruefungsNummer = null;
    this.pruefungsName = "";
    this.teilnehmerkreisSchaetzung = new HashMap<>();
    this.dauer = Duration.ofHours(1);
    this.startZeitpunkt = null;
    this.scoring = 0;
    this.pruefer = new HashSet<>();
  }

  /**
   * Create a new Builder based on an existing pruefung.
   *
   * @param pruefung The pruefung to use as template.
   */
  public PruefungDTOBuilder(PruefungDTO pruefung) {
    this.pruefungsNummer = pruefung.getPruefungsnummer();
    this.pruefungsName = pruefung.getName();
    this.teilnehmerkreisSchaetzung = pruefung.getTeilnehmerKreisSchaetzung();
    this.dauer = pruefung.getDauer();
    this.startZeitpunkt = pruefung.getTermin().orElse(null);
    this.scoring = pruefung.getScoring();
    pruefer = pruefung.getPruefer();
  }

  /**
   * Create a new Builder based on an existing pruefung.
   *
   * @param pruefung The pruefung to use as template.
   */
  public PruefungDTOBuilder(Pruefung pruefung) {
    this.pruefungsNummer = pruefung.getPruefungsnummer();
    this.pruefungsName = pruefung.getName();
    this.teilnehmerkreisSchaetzung = pruefung.getSchaetzungen();
    this.dauer = pruefung.getDauer();
    this.startZeitpunkt = pruefung.getStartzeitpunkt();
    this.scoring = 0;
    this.pruefer = pruefung.getPruefer();
  }

  /**
   * Create a new Builder based on an existing pruefung.
   *
   * @param pruefung The pruefung to use as template.
   */
  public PruefungDTOBuilder(ReadOnlyPruefung pruefung) {
    this.pruefungsNummer = pruefung.getPruefungsnummer();
    this.pruefungsName = pruefung.getName();
    this.teilnehmerkreisSchaetzung = pruefung.getTeilnehmerKreisSchaetzung();
    this.dauer = pruefung.getDauer();
    this.startZeitpunkt = pruefung.getTermin().orElse(null);
    this.scoring = pruefung.getScoring();
    this.pruefer = pruefung.getPruefer();
  }

  /**
   * Use a certain pruefungsnummer.
   *
   * @param pruefungsNummer The pruefungsnummer to use.
   * @return The builder itself.
   * @throws IllegalArgumentException In case the parameter is null or an empty string.
   */
  public PruefungDTOBuilder withPruefungsNummer(String pruefungsNummer)
      throws IllegalArgumentException {
    noNullParameters(pruefungsNummer);
    checkIsNotEmpty(pruefungsNummer);
    this.pruefungsNummer = pruefungsNummer;
    return this;
  }

  /**
   * Assert that a string is not empty.
   *
   * @param text The text to check.
   * @throws IllegalArgumentException In case the text is empty.
   */
  private void checkIsNotEmpty(String text) {
    if (text.isEmpty()) {
      throw new IllegalArgumentException("Pruefungsnummer can not be empty.");
    }
  }

  /**
   * Use a certain name for the pruefung.
   *
   * @param pruefungsName The name to use for the pruefung.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withPruefungsName(String pruefungsName) {
    this.pruefungsName = pruefungsName;
    return this;
  }

  /**
   * Use a certain set of {@link Teilnehmerkreis} with their corresponding student amounts.
   *
   * @param teilnehmerkreisSchaetzung The set of Teilnehmerkreis with their corresponding student
   *                                  amounts.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withTeilnehmerKreisSchaetzung(
      Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung) {
    this.teilnehmerkreisSchaetzung = teilnehmerkreisSchaetzung;
    return this;
  }

  /**
   * Use a certain {@link Teilnehmerkreis} with no students in addition to the existing.
   *
   * @param teilnehmerkreis The teilnehmerkreis to use.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withAdditionalTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis) {
    teilnehmerkreisSchaetzung.putIfAbsent(teilnehmerkreis, 0);
    return this;
  }

  /**
   * Use a certain {@link Teilnehmerkreis} with no students in addition to the existing.
   *
   * @param teilnehmerkreis The teilnehmerkreis to use.
   * @param schaetzung      The amount of students to associate with the teilnehmerkreis.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withAdditionalTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis,
      Integer schaetzung) {
    teilnehmerkreisSchaetzung.put(teilnehmerkreis, schaetzung);
    return this;
  }

  /**
   * Use a certain {@link Duration}.
   *
   * @param duration The duration to use.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withDauer(Duration duration) {
    this.dauer = duration;
    return this;
  }

  /**
   * Use a certain time for the pruefung to start at.
   *
   * @param start The time for the pruefung to start.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withStartZeitpunkt(LocalDateTime start) {
    this.startZeitpunkt = start;
    return this;
  }

  /**
   * Use a certain scoring.
   *
   * @param scoring The scoring to use for the pruefung.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withScoring(int scoring) {
    this.scoring = scoring;
    return this;
  }

  /**
   * Use a certain set of professors.
   *
   * @param pruefer The professors to use for the pruefung.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withPruefer(Set<String> pruefer) {
    this.pruefer = pruefer;
    return this;
  }

  /**
   * Use a professor for the pruefung additionally to the set ones.
   *
   * @param pruefer The pruefer to use.
   * @return The builder itself.
   */
  public PruefungDTOBuilder withAdditionalPruefer(String pruefer) {
    this.pruefer.add(pruefer);
    return this;
  }

  /**
   * Build a {@link PruefungDTO} from this builder.
   *
   * @return The pruefung resembled by this builder.
   */
  public PruefungDTO build() {
    if (isPlanned()) {
      return buildPlannedPruefung();
    } else {
      return buildUnplannedPruefung();
    }
  }

  /**
   * Determine whether the builder describes a planned pruefung in its current state.
   *
   * @return Whether the builder describes a planned pruefung in its current state.
   */
  private boolean isPlanned() {
    return startZeitpunkt != null;
  }

  /**
   * Build a planned {@link PruefungDTO} from this builder.
   *
   * @return The pruefung resembled by this builder.
   */
  private PruefungDTO buildPlannedPruefung() {
    return new PruefungDTO(this.pruefungsNummer, this.pruefungsName, this.startZeitpunkt,
        this.dauer, this.teilnehmerkreisSchaetzung, this.pruefer, this.scoring);
  }

  /**
   * Build an unplanned {@link PruefungDTO} from this builder.
   *
   * @return The pruefung resembled by this builder.
   */
  private PruefungDTO buildUnplannedPruefung() {
    return new PruefungDTO(this.pruefungsNummer, this.pruefungsName, this.dauer,
        this.teilnehmerkreisSchaetzung, this.pruefer, this.scoring);
  }

}
