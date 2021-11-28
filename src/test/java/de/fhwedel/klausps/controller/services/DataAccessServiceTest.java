package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.assertions.ReadOnlyPruefungAssert;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

class DataAccessServiceTest {

    String pruefungsName = "Computergrafik";
    String pruefungsNummer = "b123";
    Duration pruefungsDauer = Duration.ofMinutes(120);
    Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();
    private Pruefungsperiode pruefungsperiode;
    private DataAccessService dataAccessService;

    @BeforeEach
    void setUp() {
        this.pruefungsperiode = mock(Pruefungsperiode.class);
        this.dataAccessService = new DataAccessService(pruefungsperiode);
    }

    @Test
    @DisplayName("A Pruefung gets created by calling the respective method of the pruefungsPeriode")
    void createPruefungSuccessTest() {
        ReadOnlyPruefung pruefung =
                dataAccessService.createPruefung(
                        "Analysis", "b123", "pruefer 1", Duration.ofMinutes(90), new HashMap<>());
        assertThat(pruefung).isNotNull();
        verify(pruefungsperiode, times(1)).addPlanungseinheit(any());
    }

    @Test
    @DisplayName("A created pruefung has the expected attributes")
    void createPruefungSuccessRightAttributesTest() {
        ReadOnlyPruefung expected = getReadOnlyPruefung();
        ReadOnlyPruefung actual =
                dataAccessService.createPruefung(
                        expected.getName(),
                        expected.getPruefungsnummer(),
                        expected.getPruefer(),
                        expected.getDauer(),
                        expected.getTeilnehmerKreisSchaetzung());

        ReadOnlyPruefungAssert.assertThat(actual).isTheSameAs(getReadOnlyPruefung());
    }

    @Test
    @DisplayName("A created pruefung is persisted in the data model")
    void createPruefungSaveInModelTest() {
        ReadOnlyPruefung expected = getReadOnlyPruefung();
        ReadOnlyPruefung actual =
                dataAccessService.createPruefung(
                        expected.getName(),
                        expected.getPruefungsnummer(),
                        expected.getPruefer(),
                        expected.getDauer(),
                        expected.getTeilnehmerKreisSchaetzung());
        verify(pruefungsperiode, times(1)).addPlanungseinheit(notNull());
        assertThat(actual).isNotNull();
    }

    @Test
    @DisplayName("A pruefung can not be created when one with the same pruefungsnummer")
    void createPruefung_existsAlready() {
        ReadOnlyPruefung expected = getReadOnlyPruefung();
        Set<Planungseinheit> plannedPruefungen = new HashSet<>();
        plannedPruefungen.add(
                new PruefungImpl(
                        expected.getPruefungsnummer(),
                        expected.getName(),
                        "ABCDEF",
                        expected.getGesamtschaetzung(),
                        expected.getDauer()));
        when(pruefungsperiode.filteredPlanungseinheiten(any())).thenReturn(plannedPruefungen);
        assertThat(
                dataAccessService.createPruefung(
                        expected.getName(),
                        expected.getPruefungsnummer(),
                        expected.getPruefer(),
                        expected.getDauer(),
                        expected.getTeilnehmerKreisSchaetzung()))
                .isNull();
    }

    @Test
    @DisplayName("Change name of a Pruefung")
    void setName_successfully() {
        ReadOnlyPruefung before = getReadOnlyPruefung();
        Pruefung model = getPruefungOfReadOnlyPruefung(before);

        when(pruefungsperiode.pruefung(before.getPruefungsnummer())).thenReturn(model);

        assertThat(model.getName()).isEqualTo(before.getName());

        ReadOnlyPruefung after = dataAccessService.changeNameOfPruefung(before, "NoNameNeeded");
        assertThat(model.getPruefungsnummer()).isEqualTo(after.getPruefungsnummer());
        assertThat(model.getName()).isEqualTo(after.getName());
        assertThat(model.getDauer()).isEqualTo(after.getDauer());
        assertThat(model.getName()).isNotEqualTo(before.getName());
        assertThat(model.getDauer()).isEqualTo(before.getDauer());
        assertThat(model.getDauer()).isEqualTo(after.getDauer());
        assertThat(model.getPruefer()).isEqualTo(after.getPruefer());
//TODO darf nicht fehlschlagen später assertThat(model.getPruefer()).isEqualTo(before.getPruefer());
        assertThat(model.getTeilnehmerkreise()).isEqualTo(before.getTeilnehmerkreise());
        assertThat(model.getTeilnehmerkreise()).isEqualTo(after.getTeilnehmerkreise());
        assertThat(model.getDauer()).isEqualTo(after.getDauer());

    }

    @Test
    void testGetGeplantePruefungen() {
        PruefungDTO p1 = new PruefungDTOBuilder().withPruefungsName("Hallo").build();
        PruefungDTO p2 = new PruefungDTOBuilder().withPruefungsName("Welt").build();
        Pruefung pm1 = getPruefungOfReadOnlyPruefung(p1);
        Pruefung pm2 = getPruefungOfReadOnlyPruefung(p2);
        Set<Pruefung> pruefungen = new HashSet<>(Arrays.asList(pm1, pm2));
        when(pruefungsperiode.geplantePruefungen()).thenReturn(pruefungen);

        Set<ReadOnlyPruefung> result = dataAccessService.getGeplantePruefungen();
        assertThat(result).containsOnly(p1, p2);
        Iterator<ReadOnlyPruefung> it = result.iterator();
        ReadOnlyPruefung p1_new = it.next();
        ReadOnlyPruefung p2_new = it.next();
        assertThat(p1_new != p1 && p1_new != p2).isTrue();
        assertThat(p2_new != p1 && p2_new != p2).isTrue();
    }

    @Test
    void testUngeplanteKlausuren() {
        PruefungDTO p1 = new PruefungDTOBuilder().withPruefungsName("Hallo").build();
        PruefungDTO p2 = new PruefungDTOBuilder().withPruefungsName("Welt").build();
        Pruefung pm1 = getPruefungOfReadOnlyPruefung(p1);
        Pruefung pm2 = getPruefungOfReadOnlyPruefung(p2);
        Set<Pruefung> pruefungen = new HashSet<>(Arrays.asList(pm1, pm2));
        when(pruefungsperiode.ungeplantePruefungen()).thenReturn(pruefungen);

        Set<ReadOnlyPruefung> result = dataAccessService.getUngeplanteKlausuren();
        assertThat(result).containsOnly(p1, p2);
        Iterator<ReadOnlyPruefung> it = result.iterator();
        ReadOnlyPruefung p1_new = it.next();
        ReadOnlyPruefung p2_new = it.next();
        assertThat(p1_new != p1 && p1_new != p2).isTrue();
        assertThat(p2_new != p1 && p2_new != p2).isTrue();
    }

    /**
     * Gibt eine vorgegeben ReadOnlyPruefung zurueck
     *
     * @return gibt eine vorgebene ReadOnlyPruefung zurueck
     */
    private ReadOnlyPruefung getReadOnlyPruefung() {
        // return new Pruefung()
        return new PruefungDTOBuilder()
                .withPruefungsName("Analysis")
                .withPruefungsNummer("b001")
                .withAdditionalPruefer("Harms")
                .withDauer(Duration.ofMinutes(90))
                .build();
    }

    private Pruefung getPruefungOfReadOnlyPruefung(ReadOnlyPruefung roPruefung) {
        return new PruefungImpl(
                roPruefung.getPruefungsnummer(),
                roPruefung.getName(),
                "",
                roPruefung.getGesamtschaetzung(),
                roPruefung.getDauer()
        );
    }
}
