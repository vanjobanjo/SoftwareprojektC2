package de.fhwedel.klausps.controller.integrationTests;

import de.fhwedel.klausps.controller.integrationTests.state.State;
import de.fhwedel.klausps.controller.integrationTests.steps.BaseSteps;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hook {

    @Before
    public void setUp(Scenario scenario) {
        BaseSteps.setState(getEmptyPruefungsperiode());
    }

    @After
    public void tearDown(Scenario scenario) {
        BaseSteps.setState(null);
    }

    private State getEmptyPruefungsperiode() {
        // TODO
        return null;
    }

}
