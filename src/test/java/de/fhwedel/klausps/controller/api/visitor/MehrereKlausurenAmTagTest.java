package de.fhwedel.klausps.controller.api.visitor;


import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.services.ScheduleService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class MehrereKlausurenAmTagTest {
    private LocalDate date_101021 = LocalDate.of(2021, 10, 10);
    private LocalDate date_111121 = LocalDate.of(21, 11,11);
    private LocalDateTime date_101021_8am = LocalDateTime.of(date_101021, LocalTime.of(8, 0));
    private LocalDateTime date_101021_10pm = LocalDateTime.of(date_101021, LocalTime.of(22, 0));
    private LocalDateTime date_111121_8am = LocalDateTime.of(date_111121, LocalTime.of(8, 0));
    WeichesKriteriumVisitor kriterium = WeichesKriteriumVisitors.MEHRERE_PRUEFUNG_AM_TAG.getWeichesKriteriumVisitor();

    @Test
    public void testSameDay_schouldBeTrue() {
        Pruefung impl1 = new PruefungImpl("Hallo", "Mathe", "", Duration.ZERO, date_101021_8am);
        Pruefung impl2 = new PruefungImpl("Bwl", "KeineAhnung", "", Duration.ZERO, date_101021_10pm);

        assertThat(kriterium.test(impl1, impl2)).isTrue();
    }

    @Test
    public void testSameDay_schouldBeFalse() {
        Pruefung impl1 = new PruefungImpl("Hallo", "Mathe", "", Duration.ZERO, date_101021_8am);
        Pruefung impl2 = new PruefungImpl("Bwl", "KeineAhnung", "", Duration.ZERO, date_111121_8am);

        assertThat(kriterium.test(impl1, impl2)).isFalse();
    }

    @Test
    public void blabla(){
        WeichesKriteriumVisitors visitors = WeichesKriteriumVisitors.MEHRERE_PRUEFUNG_AM_TAG;
        Pruefung impl1 = new PruefungImpl("Hallo", "Mathe", "", Duration.ZERO, date_101021_8am);
        Pruefung impl3 = new PruefungImpl("asd", "esel", "", Duration.ZERO, date_101021_8am);
        Pruefung impl2 = new PruefungImpl("Bwl", "KeineAhnung", "", Duration.ZERO, date_101021_10pm);
        List<Pruefung> liste = Arrays.asList(impl1, impl2, impl3);
        System.out.println(ScheduleService.analyseAll(WeichesKriteriumVisitors.values(), liste));
        System.out.println(ScheduleService.scoringOfPruefung(ScheduleService.analyseAll(WeichesKriteriumVisitors.values(), liste).get(impl1)));
        System.out.println(ScheduleService.analyseAll(WeichesKriteriumVisitors.values(), liste).get(impl1));
    }

}