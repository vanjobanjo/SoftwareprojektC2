package de.fhwedel.klausps.controller.api.builders;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class PruefungDTOBuilder {
  private static final String PRUEUNGS_NUMMER_DEFAULT = "";
  private static final String PRUEFUNGS_NAME_DEFAULT = "";
  private static final Integer TEILNEHMERKREIS_SCHAETZUNG_DEFAULT = 0;
  private static final Duration DAUER_DEFAULT = Duration.ofMinutes(60);
  private static final LocalDateTime START_ZEITPUNKT_DEFAULT = null;
  private static final int SCORING_DEFAULT = 0;
  private static final Set<String> PRUEFER_DEFAULT = new HashSet<>();
  private static final Set<Teilnehmerkreis> TEILNEHMERKREISE_DEFAULT = new HashSet<>();

  private String pruefungsNummer;
  private String pruefungsName;
  private Integer teilnehmerSchaetzung;
  private Set<Teilnehmerkreis> teilnehmerKreise;
  private Duration dauer;
  private LocalDateTime startZeitpunkt;
  private int scoring;
  private Set<String> pruefer;

  /** Builder Konstruktor */
  public PruefungDTOBuilder() {
    pruefungsNummer = PRUEUNGS_NUMMER_DEFAULT;
    pruefungsName = PRUEFUNGS_NAME_DEFAULT;
    teilnehmerSchaetzung = TEILNEHMERKREIS_SCHAETZUNG_DEFAULT;
    teilnehmerKreise = TEILNEHMERKREISE_DEFAULT;
    dauer = DAUER_DEFAULT;
    startZeitpunkt = START_ZEITPUNKT_DEFAULT;
    scoring = SCORING_DEFAULT;
    pruefer = PRUEFER_DEFAULT;
  }

  /**
   * Copy Konstruktor, bei Bedarf kann man die Werte setzen.
   *
   * @param pruefung - Zu kopierende DTOPruefung
   */
  public PruefungDTOBuilder(PruefungDTO pruefung) {
    pruefungsNummer = pruefung.getPruefungsnummer();
    pruefungsName = pruefung.getName();
    // teilnehmerSchaetzung = pruefung.; // TODO shoud be apart from the Teilnehmerkreise
    dauer = pruefung.getDauer();
    startZeitpunkt = pruefung.getTermin().orElse(START_ZEITPUNKT_DEFAULT);
    // scoring = pruefung.; TODO where does the scoring come from?
    pruefer = pruefung.getPruefer();
  }

  public PruefungDTOBuilder withPruefungsNummer(String pruefungsNummer) {
    this.pruefungsNummer = pruefungsNummer;
    return this;
  }

  public PruefungDTOBuilder withPruefungsName(String pruefungsName) {
    this.pruefungsName = pruefungsName;
    return this;
  }

  public PruefungDTOBuilder withTeilnehmerKreisSchaetzung(Integer teilnehmerKreisSchaetzung) {
    this.teilnehmerSchaetzung = teilnehmerKreisSchaetzung;
    return this;
  }

  public PruefungDTOBuilder withTeilnehmerKreisen(Set<Teilnehmerkreis> teilnehmerKreise) {
    this.teilnehmerKreise = teilnehmerKreise;
    return this;
  }

  public PruefungDTOBuilder withAdditionalTeilnehmerKreis(Teilnehmerkreis teilnehmerKreis) {
    this.teilnehmerKreise.add(teilnehmerKreis);
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
        this.pruefungsName,
        this.pruefungsNummer,
        this.pruefer,
        this.dauer,
        this.teilnehmerKreise,
        this.teilnehmerSchaetzung);
  }
}
