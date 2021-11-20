package integrationTests.steps;

import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;


public class removePruefungVonBlockSteps {


    //TODO eigentlich moechte ich gerne, dass hier ein List<String> mit pruefungen uebergeben werden kann
    @Angenommen("der geplante Block {string} hat die Pruefungen {}")
    public void derBlockGeplanteHatDiePruefungenListeVonPruefungenInnerhalbEinesBlockes(String block, String pruefungen) {

    }

    @Wenn("die {int} Pruefung aus dem Block entfernt wird")
    public void diePruefungAusDemBlockEntferntWird(int index) {

    }

    @Dann("ist {int} Pruefung ungeplant")
    public void istPruefungUngeplant(int index) {

    }

    @Angenommen("der ungeplante Block {string} hat die Pruefungen {}")
    public void derUngeplanteBlockHatDiePruefungenListeVonPruefungenInnerhalbEinesBlockes(String block, String pruefungen) {
    }

    @Dann("ist {int} Prufung nicht mehr im Block {string}")
    public void istPrufungNichtMehrImBlock(int index, String block) {
    }

    @Dann("ist Block {string} immernoch ungeplant")
    public void istBlockImmernochUngeplant(String block) {
    }

    @Dann("ist Block {string} immernoch geplant")
    public void istBlockImmernochGeplant(String block) {
    }

    @Dann("hat der Block {string} noch {int} Pruefungen")
    public void hatDerBlockNochPruefungen(String block, int anzahlPruefungen) {
    }

    @Wenn("die Pruefung {string} aus dem Block {string} entfernt wird")
    public void diePruefungAusDemBlockEntferntWird(String pruefung, String block) {
    }

    @Dann("werfe IllegalArgumentException")
    public void werfeIllegalArgumentException() {

    }

    //TODO das muss evtl geaendert werden
    @Dann("hat das Entfernen Auswirkungen auf das Scoring")
    public void hatDasEntfernenAuswirkungenAufDasScoring() {
    }
}
