package integrationTests.steps;


import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import java.util.Map;
import java.util.Set;
import org.junit.AssumptionViolatedException;

public class setTeilnehmerkeisSchaetzung extends BaseSteps {





    @Angenommen("die Prüfung {string} noch keinen Teilnehmerkreis mit Schätzung")
    public void diePrüfungNochKeinenTeilnehmerkreisMitSchätzung(String pruefung)
        throws NoPruefungsPeriodeDefinedException {
        getOrCreate(pruefung);
    }

    @Angenommen("die Prüfung {string} hat den Teilnehmerkreis {string} und die Schätzung {int}")
    public void diePrüfungHatDenTeilnehmerkreisUndDieSchätzung(String pruefung, String teilnehmerkreis, int schaetzung)
        throws NoPruefungsPeriodeDefinedException {

        getOrCreate(pruefung, teilnehmerkreis,1, "BACHELOR", schaetzung);

    }

    @Wenn("ich die Schätzung von {string} mit den Teilnehmerkreis {string} zu {int} ändere")
    public void ichDieSchätzungVonMitDenTeilnehmerkreisZuÄndere(String pruefung, String teilnehmerkreis,
        int schaetzung) {
        Teilnehmerkreis t = new TeilnehmerkreisImpl(teilnehmerkreis,teilnehmerkreis,1,
            Ausbildungsgrad.BACHELOR);
        try {
            state.controller.setTeilnehmerkreisSchaetzung(getPruefungFromModel(pruefung), t,
                schaetzung);
        }catch(IllegalArgumentException | IllegalStateException | NoPruefungsPeriodeDefinedException e){
            state.results.put("exception", e);
        }
    }

    @Dann("ist die Schätzung von {string} mit den Teilnehmerkreis {string} den Wert {int}")
    public void istDieSchätzungVonMitDenTeilnehmerkreisDenWert(String pruefung, String teilnehmerkreis, int count)
        throws NoPruefungsPeriodeDefinedException {
        ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
        Set<Teilnehmerkreis> teilnehmerkreisSet = roPruefung.getTeilnehmerkreise();
        Map<Teilnehmerkreis,Integer> teilnehmerMap= roPruefung.getTeilnehmerKreisSchaetzung();
        assertThat(teilnehmerkreisSet).isNotEmpty();

        Teilnehmerkreis teilnehmer = new TeilnehmerkreisImpl(teilnehmerkreis,teilnehmerkreis,1,Ausbildungsgrad.BACHELOR);

        assertThat(teilnehmerkreisSet).contains(teilnehmer);
        assertThat(teilnehmerMap.get(teilnehmer)).isEqualTo(count);
    }

    @Wenn("ich die Schätzung einer Unbekannten Pruefung ändere")
    public void ichDieSchätzungEinerUnbekanntenPruefungÄndere() {
        Teilnehmerkreis t = new TeilnehmerkreisImpl("test","test",1,
            Ausbildungsgrad.BACHELOR);
        try {
            ReadOnlyPruefung ro = new PruefungDTOBuilder().withPruefungsNummer("ein").withPruefungsName("tets").build();
            state.controller.setTeilnehmerkreisSchaetzung(ro, t,
                1);
        }catch(IllegalArgumentException | IllegalStateException | NoPruefungsPeriodeDefinedException e){
            state.results.put("exception", e);
        }

    }

    /*

     try {
      state.controller.schedulePruefung(r, time);
    } catch (HartesKriteriumException | NoPruefungsPeriodeDefinedException | IllegalArgumentException | IllegalStateException e) {
      state.results.put("exception", e);
    }


    Object exception = state.results.get("exception");
    assertThat(exception).isNotNull();
    assertThat(exception).isInstanceOf(IllegalStateException.class);
    */

}
