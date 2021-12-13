package de.fhwedel.klausps.controller.api.builders;

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

public class PruefungDTOBuilder {

  private static final String PRUEUNGS_NUMMER_DEFAULT = "";
  private static final String PRUEFUNGS_NAME_DEFAULT = "";
  private static final Duration DAUER_DEFAULT = Duration.ofMinutes(60);
  private static final LocalDateTime START_ZEITPUNKT_DEFAULT = null;
  private static final int SCORING_DEFAULT = 0;
  private final Set<String> PRUEFER_DEFAULT = new HashSet<>();
  private final Map<Teilnehmerkreis, Integer> TEILNEHMERKREIS_SCHAETZUNG_DEFAULT = new HashMap<>();
  private String pruefungsNummer;
  private String pruefungsName;
  private Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung;
  private Duration dauer;
  private LocalDateTime startZeitpunkt;
  private int scoring;
  private Set<String> pruefer;

  /**
   * Builder Konstruktor
   */
  public PruefungDTOBuilder() {
    this.pruefungsNummer = PRUEUNGS_NUMMER_DEFAULT;
    this.pruefungsName = PRUEFUNGS_NAME_DEFAULT;
    this.teilnehmerkreisSchaetzung = TEILNEHMERKREIS_SCHAETZUNG_DEFAULT;
    this.dauer = DAUER_DEFAULT;
    this.startZeitpunkt = START_ZEITPUNKT_DEFAULT;
    this.scoring = SCORING_DEFAULT;
    this.pruefer = PRUEFER_DEFAULT;
  }

  public PruefungDTOBuilder(PruefungDTO pruefung) {
    this.pruefungsNummer = pruefung.getPruefungsnummer();
    this.pruefungsName = pruefung.getName();
    this.teilnehmerkreisSchaetzung = pruefung.getTeilnehmerKreisSchaetzung();
    this.dauer = pruefung.getDauer();
    this.startZeitpunkt = pruefung.getTermin().orElse(START_ZEITPUNKT_DEFAULT);
    this.scoring = pruefung.getScoring();
    pruefer = pruefung.getPruefer();
  }

  public PruefungDTOBuilder(Pruefung pruefungModel) {
    this.pruefungsNummer = pruefungModel.getPruefungsnummer();
    this.pruefungsName = pruefungModel.getName();
    this.teilnehmerkreisSchaetzung = pruefungModel.getSchaetzungen();
    this.dauer = pruefungModel.getDauer();
    this.startZeitpunkt = pruefungModel.getStartzeitpunkt();
    this.scoring = SCORING_DEFAULT;
    this.pruefer = pruefungModel.getPruefer();
  }

  public PruefungDTOBuilder(ReadOnlyPruefung pruefung) {
    this.pruefungsNummer = pruefung.getPruefungsnummer();
    this.pruefungsName = pruefung.getName();
    this.teilnehmerkreisSchaetzung = pruefung.getTeilnehmerKreisSchaetzung();
    this.dauer = pruefung.getDauer();
    this.startZeitpunkt = pruefung.getTermin().orElse(START_ZEITPUNKT_DEFAULT);
    this.scoring = pruefung.getScoring();
    this.pruefer = pruefung.getPruefer();
  }

  public PruefungDTOBuilder withPruefungsNummer(String pruefungsNummer) {
    this.pruefungsNummer = pruefungsNummer;
    return this;
  }

  public PruefungDTOBuilder withPruefungsName(String pruefungsName) {
    this.pruefungsName = pruefungsName;
    return this;
  }

  public PruefungDTOBuilder withTeilnehmerKreisSchaetzung(
      Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung) {
    this.teilnehmerkreisSchaetzung = teilnehmerkreisSchaetzung;
    return this;
  }

  public PruefungDTOBuilder withAdditionalTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis) {
    teilnehmerkreisSchaetzung.putIfAbsent(teilnehmerkreis, 0);
    return this;
  }

  public PruefungDTOBuilder withAdditionalTeilnehmerkreisSchaetzung(
      Teilnehmerkreis teilnehmerkreis, Integer schaetzung) {
    teilnehmerkreisSchaetzung.put(teilnehmerkreis, schaetzung);
    return this;
  }

  public PruefungDTOBuilder withDauer(Duration dauer) {
    this.dauer = dauer;
    return this;
  }

  public PruefungDTOBuilder withStartZeitpunkt(LocalDateTime startZeitpunkt) {
    this.startZeitpunkt = startZeitpunkt;
    return this;
  }

  public PruefungDTOBuilder withScoring(int scoring) {
    this.scoring = scoring;
    return this;
  }

  public PruefungDTOBuilder withPruefer(Set<String> pruefer) {
    this.pruefer = pruefer;
    return this;
  }

  public PruefungDTOBuilder withAdditionalPruefer(String pruefer) {
    this.pruefer.add(pruefer);
    return this;
  }

  public PruefungDTO build() {
    return new PruefungDTO(
        this.pruefungsNummer,
        this.pruefungsName,
        this.startZeitpunkt,
        this.dauer,
        this.teilnehmerkreisSchaetzung,
        this.pruefer,
        this.scoring);
  }
}
