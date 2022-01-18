package integrationTests;

import integrationTests.state.State;
import integrationTests.steps.BaseSteps;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hook {

  @Before
  public void setUp(Scenario scenario) {
    BaseSteps.setState(getNewState());
  }

  @After
  public void tearDown(Scenario scenario) {
    BaseSteps.setState(null);
  }

  private State getNewState() {
    return new State();
  }

}
