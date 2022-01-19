package integrationTests.state;

import de.fhwedel.klausps.controller.Controller;
import java.util.HashMap;
import java.util.Map;

/**
 * A state to work on during a scenario.
 */
public class State {

  public final Controller controller;
  public final Map<String, Object> results;

  public State() {
    this.controller = new Controller();
    this.results = new HashMap<>();
  }
}
