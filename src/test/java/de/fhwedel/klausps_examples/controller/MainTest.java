package de.fhwedel.klausps_examples.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainTest {

    @Test
    void TestdoStuff() {
        assertEquals(Main.someMethod(-1), -1);/*
        assertThat(Main.someMethod(-1)).isEqualTo(-1);*/
    }

    @Test
    void Other() {
        assertEquals(-1, Main.other("Hallo Welt"));
        assertEquals(0, Main.other("cooli"));
        assertEquals(0, Main.other("Ha"));
    }

    /*@Test
    void TestdoStuff2() {
        assertThat(Main.someMethod(0)).isEqualTo(0);
    }*/
}
        
