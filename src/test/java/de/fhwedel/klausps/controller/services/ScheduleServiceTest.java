package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleServiceTest {


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


}
