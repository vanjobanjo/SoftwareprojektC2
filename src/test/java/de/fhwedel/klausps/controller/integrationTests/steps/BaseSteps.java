package de.fhwedel.klausps.controller.integrationTests.steps;

import de.fhwedel.klausps.controller.integrationTests.state.State;

/**
 * A common point for helper methods required in many step definitions.
 */
public class BaseSteps {

    private static State STATE;

    public State getState() {
        return STATE;
    }

    public static void setState(State state) {
        STATE = state;
    }
}
