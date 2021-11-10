package de.fhwedel.klausps_examples.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestMain {

    @Test
    void TestdoStuff() {
        Main.ABC abc = mock(Main.ABC.class);
        when(abc.getStuff("Miwo")).thenReturn(5);
        assertEquals(0, new Main().doStuff(abc));
        //assertTrue(false);
    }
}
        
