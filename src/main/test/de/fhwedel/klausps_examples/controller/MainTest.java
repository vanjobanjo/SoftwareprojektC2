package de.fhwedel.klausps_examples.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainTest {

    @org.junit.jupiter.api.Test
    void TestdoStuff() {
        Main.ABC abc = mock(Main.ABC.class);
        when(abc.getStuff("Miwo")).thenReturn(5);
        assertEquals(0, new Main().doStuff(abc));
    }
}
