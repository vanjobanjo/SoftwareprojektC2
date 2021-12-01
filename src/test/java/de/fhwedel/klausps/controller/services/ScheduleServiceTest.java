package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleServiceTest {

    private Pruefungsperiode pruefungsperiode;
    private DataAccessService dataAccessService;

    @BeforeEach
    void setUp() {
        LocalDateTime _010121_0800 = LocalDateTime.of(2021, 1, 1, 8, 0);
        LocalDateTime _010121_0900 = LocalDateTime.of(2021, 1, 1, 9, 0);
        LocalDateTime _010121_1000 = LocalDateTime.of(2021, 1, 1, 10, 0);
        LocalDateTime _020121_1000 = LocalDateTime.of(2021, 1, 2, 10, 0);
        Teilnehmerkreis BWL_10_1 = new TeilnehmerkreisImpl("BWL", "10", 1);
        Teilnehmerkreis WING_10_1 = new TeilnehmerkreisImpl("WING", "10", 1);
        Pruefung ANALYSIS_GEPLANT_010121_0800_DAUER120 = new PruefungImpl("1", "Analysis", "", Duration.ofMinutes(120), _010121_0800);
        Set<Pruefung> pruefungen = new HashSet<>(List.of(ANALYSIS_GEPLANT_010121_0800_DAUER120));
        this.pruefungsperiode = mock(Pruefungsperiode.class);
        when(pruefungsperiode.geplantePruefungen()).thenReturn(pruefungen);
        this.dataAccessService = new DataAccessService(pruefungsperiode);
    }

    @Test
    void test(){
        ScheduleService scheduleService = dataAccessService.getScheduleService();
        System.out.println(scheduleService.getGeplantePruefungen());
        System.out.println(scheduleService.getAnalysen());
    }


}
