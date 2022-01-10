package de.fhwedel.klausps.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PruefungDTOTest {

  @Test
  @DisplayName("Teilnehmerkreis hinzufügen")
  void addTeilnehmerkreis() {
    TeilnehmerkreisImpl t = new TeilnehmerkreisImpl("Inf", "10", 1, Ausbildungsgrad.BACHELOR);
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
    TeilnehmerkreisImpl teilnehmerkreis = new TeilnehmerkreisImpl("Inf", "11", 1,
        Ausbildungsgrad.BACHELOR);
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

  @Test
  @DisplayName("Änderung der Duration von einer Pruefung im Builder erfolgreich")
  void setDurationFromPruefung() {
    TeilnehmerkreisImpl teilnehmerkreis = new TeilnehmerkreisImpl("Inf", "11", 1,
        Ausbildungsgrad.BACHELOR);
    PruefungDTOBuilder pruefungDTOBuilder = new PruefungDTOBuilder()
        .withPruefungsName("Test")
        .withPruefungsNummer("1234")
        .withDauer(Duration.ofMinutes(90))
        .withAdditionalPruefer("Tester")
        .withAdditionalTeilnehmerkreis(teilnehmerkreis)
        .withStartZeitpunkt(LocalDateTime.now());
    assertThat(pruefungDTOBuilder.build().getDauer()).hasMinutes(90);

    //pruefungsDTOBuilder die Duraiton ändern
    pruefungDTOBuilder.withDauer(Duration.ofMinutes(120));
    assertThat(pruefungDTOBuilder.build().getDauer()).hasMinutes(120);

  }

  @Test
  @DisplayName("Baue DTO von Modelpruefung")
  void buildDTOFromModelPruefung() {
    PruefungImpl model = new PruefungImpl("Hallo", "Hallo", "", Duration.ofMinutes(60));
    TeilnehmerkreisImpl bwl = new TeilnehmerkreisImpl("BWL", "10", 10, Ausbildungsgrad.BACHELOR);
    TeilnehmerkreisImpl inf = new TeilnehmerkreisImpl("inf", "10", 10, Ausbildungsgrad.BACHELOR);
    model.setSchaetzung(bwl, 10);
    model.setSchaetzung(inf, 10);
    PruefungDTO dtoController = new PruefungDTOBuilder(model).build();
    assertThat(dtoController.getTeilnehmerKreisSchaetzung()).isEqualTo(model.getSchaetzungen());
    assertThat(dtoController.getGesamtschaetzung()).isEqualTo(model.schaetzung());
  }

}
