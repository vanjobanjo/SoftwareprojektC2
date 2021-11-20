package integrationTests.steps;

import io.cucumber.java.ParameterType;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.AssumptionViolatedException;


public class removePruefungVonBlockSteps {

    @ParameterType("(.*(, .*)*)")
    public List<String> stringList(String strings) {
        return Arrays.stream(strings.split(", ")).collect(Collectors.toList());
    }

    //TODO das muss evtl geaendert werden
    @Und("das Entfernen hat Auswirkungen auf das Scoring")
    public void hatDasEntfernenAuswirkungenAufDasScoring() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("die {stringList} sind Teil des geplanten Block {string}")
    public void dieKlausurenSindTeilDesGeplantenBlock(List<String> klausuren, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("die erste der {stringList} aus dem Block entfernt wird")
    public void dieErsteDerKlausurenAusDemBlockEntferntWird(List<String> klausuren) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("ist die erste der {stringList} ungeplant")
    public void istDieErsteDerKlausurenUngeplant(List<String> klausuren) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Und("die erste der {stringList} ist nicht mehr im Block {string}")
    public void dieErsteDerKlausurenIstNichtMehrImBlock(List<String> klausuren, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Und("der Block {string} ist immernoch geplant")
    public void derBlockIstImmernochGeplant(String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Und("es sind {int} Klausuren im Block {string}")
    public void esSindKlausurenImBlock(int amountKlausuren, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("der geplante Block {string} hat die Pruefung {string}")
    public void derGeplanteBlockHatDiePruefung(String blockName, String klausurName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("{string} aus dem Block entfernt wird")
    public void ausDemBlockEntferntWird(String klausurName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("ist {string} ungeplant")
    public void istUngeplant(String klausurName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Und("der Block {string} ist noch geplant")
    public void derBlockIstNochGeplant(String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Und("der Block {string} hat keine Pruefungen mehr")
    public void derBlockHatKeinePruefungenMehr(String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("die {stringList} sind Teil des ungeplanten Block {string}")
    public void dieKlausurenSindTeilDesUngeplantenBlock(List<String> klausuren, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Und("der Block {string} ist immer noch ungeplant")
    public void derBlockIstImmerNochUngeplant(String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("{stringList} sind Teil des geplanten Block {string}")
    public void sindTeilDesGeplantenBlock(List<String> klausuren, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Wenn("die Pruefung {string} aus dem Block {string} entfernt wird")
    public void diePruefungAusDemBlockEntferntWird(String klausurName, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Dann("werfe IllegalArgumentException")
    public void werfeIllegalArgumentException() {
        throw new AssumptionViolatedException("Not implemented yet!");
    }

    @Angenommen("{stringList} sind Teil des ungeplanten Block {string}")
    public void sindTeilDesUngeplantenBlock(List<String> klausuren, String blockName) {
        throw new AssumptionViolatedException("Not implemented yet!");
    }
}
