import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@SuppressWarnings("NewClassNamingConvention")
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/CucumberReport.html"},
    features = "src/test/resources/features",
    glue = {"integrationTests"})
public class CucumberRunnerIT {


}
