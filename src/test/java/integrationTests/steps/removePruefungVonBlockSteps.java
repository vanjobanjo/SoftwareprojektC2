package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.util.List;


public class removePruefungVonBlockSteps {


    //TODO eigentlich moechte ich gerne, dass hier ein List<String> mit pruefungen uebergeben werden kann
    @Angenommen("der Block geplante {string} hat die Pruefungen {}")
    public void derBlockGeplanteHatDiePruefungenListeVonPruefungenInnerhalbEinesBlockes(String block, String pruefungen) {

    }

    @Wenn("die {int} Pruefung aus dem Block entfernt wird")
    public void diePruefungAusDemBlockEntferntWird(int index) {

    }

    @Dann("ist {int} Pruefung ungeplant")
    public void istPruefungUngeplant(int index) {

    }

}
