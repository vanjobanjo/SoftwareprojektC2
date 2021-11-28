package de.fhwedel.klausps.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import java.time.Duration;
import java.time.LocalDateTime;

import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PruefungDTOTest {

  @Test
  @DisplayName("Teilnehmerkreis hinzufügen")
  void addTeilnehmerkreis() {
    TeilnehmerkreisImpl t = new TeilnehmerkreisImpl("Inf", "10", 1);
    PruefungDTO pruefungDTO = new PruefungDTOBuilder()
        .withPruefungsName("Test")
        .withPruefungsNummer("1234")
        .withDauer(Duration.ofMinutes(90))
        .withAdditionalPruefer("Tester")
        .withAdditionalTeilnehmerkreis(t)
        .withStartZeitpunkt(LocalDateTime.now())
        .build();
    assertThat(pruefungDTO.getTeilnehmerkreise()).containsOnly(t);
  }

  @Test
  @DisplayName("Teilnehmerkreis wird probiert nochmal hinzugefügt zu werden, soll aber nicht klappen")
  void addTeilnehmerkreis_schon_Vorhanden() {
    TeilnehmerkreisImpl teilnehmerkreis = new TeilnehmerkreisImpl("Inf", "11", 1);
    PruefungDTOBuilder pruefungDTOBuilder = new PruefungDTOBuilder()
        .withPruefungsName("Test")
        .withPruefungsNummer("1234")
        .withDauer(Duration.ofMinutes(90))
        .withAdditionalPruefer("Tester")
        .withAdditionalTeilnehmerkreis(teilnehmerkreis)
        .withStartZeitpunkt(LocalDateTime.now());

    pruefungDTOBuilder.withAdditionalTeilnehmerkreis(teilnehmerkreis);

    assertThat(pruefungDTOBuilder.build().getTeilnehmerkreise()).hasSize(1);
    assertThat(pruefungDTOBuilder.build().getTeilnehmerkreise()).containsExactly(teilnehmerkreis);
  }

}