package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

public class randomSteps {

    @Given("I have hungry snake")
    public void i_have_hungry_snake() {
    }

    @When("When I feed my snake")
    public void when_i_feed_my_snake() {
        assertThat("text").startsWith("te");
    }

    @Then("I receive snek snek")
    public void receive_snek_snek()  {
    }

}
