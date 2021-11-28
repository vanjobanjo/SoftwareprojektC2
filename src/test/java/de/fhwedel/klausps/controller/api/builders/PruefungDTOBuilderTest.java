package de.fhwedel.klausps.controller.api.builders;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PruefungDTOBuilderTest {

    private final PruefungDTOBuilder BUILDER = new PruefungDTOBuilder();

    @Test
    void withPruefungsNummer() {
        PruefungDTO tester = BUILDER.withPruefungsNummer("123").build();
        assertThat(tester.getPruefungsnummer()).isEqualTo("123");
    }

    @Test
    void withPruefungsName() {
        PruefungDTO tester = BUILDER.withPruefungsName("Hallo").build();
        assertThat(tester.getName()).isEqualTo("Hallo");
    }

    @Test
    void withTeilnehmerKreisSchaetzung() {
        Map<Teilnehmerkreis, Integer> temp = new HashMap<>();
        TeilnehmerkreisImpl analysis = new TeilnehmerkreisImpl("BWL", "10", 0);
        temp.put(analysis, 10);
        PruefungDTO tester = BUILDER.withTeilnehmerKreisSchaetzung(temp).build();
        assertThat(tester.getTeilnehmerKreisSchaetzung()).isEqualTo(temp);

    }

    @Test
    void withAdditionalTeilnehmerkreis() {
        Map<Teilnehmerkreis, Integer> temp = new HashMap<>();
        TeilnehmerkreisImpl analysis = new TeilnehmerkreisImpl("BWL", "10", 0);
        TeilnehmerkreisImpl additional = new TeilnehmerkreisImpl("adfd", ">SD", 3);
        temp.put(analysis, 10);
        PruefungDTO tester = BUILDER.withTeilnehmerKreisSchaetzung(temp).withAdditionalTeilnehmerkreis(additional).build();
        assertThat(tester.getTeilnehmerkreise().contains(additional)).isTrue();
        assertThat(tester.getTeilnehmerKreisSchaetzung().containsKey(additional)).isTrue();
        assertThat(tester.getTeilnehmerKreisSchaetzung().get(additional)).isEqualTo(0);

    }

    @Test
    void withAdditionalTeilnehmerkreisSchaetzung() {
        TeilnehmerkreisImpl analysis = new TeilnehmerkreisImpl("BWL", "10", 0);
        TeilnehmerkreisImpl additional = new TeilnehmerkreisImpl("adfd", ">SD", 3);

        PruefungDTO tester = BUILDER.withAdditionalTeilnehmerkreisSchaetzung(analysis, 0)
                .withAdditionalTeilnehmerkreisSchaetzung(analysis, 10)
                .withAdditionalTeilnehmerkreisSchaetzung(additional, 10)
                .build();
        assertThat(tester.getTeilnehmerKreisSchaetzung().containsKey(analysis)).isTrue();
        assertThat(tester.getTeilnehmerKreisSchaetzung().containsKey(additional)).isTrue();
        assertThat(tester.getGesamtschaetzung()).isEqualTo(20);
    }

    @Test
    void withDauer() {
        PruefungDTO tester = BUILDER.withDauer(Duration.ofMinutes(60)).build();
        assertThat(tester.getDauer()).isEqualTo(Duration.ofMinutes(60));
    }

    @Test
    void withStartZeitpunkt() {
        PruefungDTO tester = BUILDER.withStartZeitpunkt(LocalDateTime.MAX).build();
        assertThat(tester.getTermin()).isPresent();
        assertThat(tester.getTermin()).hasValue(LocalDateTime.MAX);
    }

    @Test
    void withScoring() {
        PruefungDTO tester = BUILDER.withScoring(20).build();
        assertThat(tester.getScoring()).isEqualTo(20);
    }

    @Test
    void withPruefer() {
        Set<String> pruefer = Arrays.stream(new String[]{"Hallo", "Welt"}).collect(Collectors.toSet());
        PruefungDTO tester = BUILDER.withPruefer(pruefer).build();
        assertThat(tester.getPruefer()).isEqualTo(pruefer);
    }

    @Test
    void withAdditionalPruefer() {
        Set<String> pruefer = Arrays.stream(new String[]{"Hallo", "Welt"}).collect(Collectors.toSet());
        PruefungDTO tester = BUILDER.withPruefer(pruefer).withAdditionalPruefer("Miwand").build();
        assertThat(tester.getPruefer()).contains("Hallo", "Welt", "Miwand");
    }

    @Test
    void withGeplant() {
        PruefungDTO tester = BUILDER.withGeplant(true).build();
        assertThat(tester.geplant()).isTrue();
        assertThat(tester.ungeplant()).isFalse();
    }

    @Test
    @DisplayName("Teilnehmerkreis Schaetzung wurde zur Prüfung hinzugefügt, geschaetze Zahl muss 0 sein")
    void addTeilnehmerkreisSchaetzung(){
        TeilnehmerkreisImpl tester = new TeilnehmerkreisImpl("test", "test", 99);
        PruefungDTOBuilder builder = new PruefungDTOBuilder().withAdditionalTeilnehmerkreis(tester);
        Integer expected_schaetzung_fuer_tester = 0;
        PruefungDTO pruefungDTO = builder.build();
        assertThat(pruefungDTO.getTeilnehmerkreise()).containsOnly(tester);
        assertThat(pruefungDTO.getTeilnehmerKreisSchaetzung().get(tester)).isEqualTo(expected_schaetzung_fuer_tester);
    }

    @Test
    @DisplayName("Name muss stimmen ")
    void addName(){
        PruefungDTOBuilder builder = new PruefungDTOBuilder().withPruefungsName("Hallo");
        PruefungDTO pruefungDTO = builder.build();
        assertThat(pruefungDTO.getName()).isEqualTo("Hallo");
    }
}