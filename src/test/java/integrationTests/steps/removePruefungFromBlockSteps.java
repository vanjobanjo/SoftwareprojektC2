package integrationTests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.model.api.Blocktyp;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class removePruefungFromBlockSteps extends BaseSteps {


  @Wenn("{string} aus dem Block {string} entfernt wird")
  public void ausDemBlockEntferntWird(String pruefung, String block)
      throws NoPruefungsPeriodeDefinedException {
    try {
      state.controller.removePruefungFromBlock(getBlockFromModel(block),
          getPruefungFromModel(pruefung));
      List<ReadOnlyPruefung> result = new ArrayList<>();
      result.add(getPruefungFromModel(pruefung));
      state.results.put("affected", result);
    } catch (IllegalStateException e) {
      state.results.put("exception", e);
    }
  }

  @Und("der Block {string} enthaelt die Pruefung {string} nicht")
  public void derBlockEnthaeltDiePruefungNicht(String block, String pruefung)
      throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    Set<ReadOnlyBlock> bloecke = state.controller.getUngeplanteBloecke();
    bloecke.addAll(state.controller.getGeplanteBloecke());
    assertThat(state.controller.getBlockOfPruefung(roPruefung)).isEmpty();
    assertThat(bloecke).isNotEmpty();

    assertThat(block).isNotEmpty();

    Iterator<ReadOnlyBlock> it = bloecke.iterator();
    boolean found = false;
    ReadOnlyBlock roBlock = null;
    while(it.hasNext() && !found){
       roBlock = it.next();
       if(roBlock.getName().equals(block)){
         found = !found;
       }
    }

    assertThat(roBlock.getROPruefungen().contains(roPruefung)).isFalse();

  }

  @Und("der Block {string} enthaelt die Pruefung {string}")
  public void derBlockEnthaeltDiePruefung(String block, String pruefung)
      throws NoPruefungsPeriodeDefinedException {

    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    Set<ReadOnlyBlock> bloecke = state.controller.getUngeplanteBloecke();
    bloecke.addAll(state.controller.getGeplanteBloecke());
    assertThat(state.controller.getBlockOfPruefung(roPruefung)).isNotEmpty();
    assertThat(bloecke).isNotEmpty();
    assertThat(block).isNotEmpty();
    Iterator<ReadOnlyBlock> it = bloecke.iterator();
    boolean found = false;
    ReadOnlyBlock roBlock = null;
    while(it.hasNext() && !found){
      roBlock = it.next();
      if(roBlock.getName().equals(block)){
        found = !found;
      }
    }
    assertThat(roBlock.getROPruefungen().contains(roPruefung)).isTrue();

  }



  @Und("der ungeplante Block {string} {string} enthaelt {stringList}")
  public void derUngeplanteBlockSEQUENTIALEnthaelt(String block,String blockart, List<String> pruefungen)
      throws NoPruefungsPeriodeDefinedException {

    ArrayList<ReadOnlyPruefung> listRO = new ArrayList<>();

    for (String pruefungname : pruefungen) {
      ReadOnlyPruefung roPruefung = getOrCreate(pruefungname);
      listRO.add(roPruefung);
    }

    if (blockart.equals("SEQUENTIAL")) {
      state.controller.createBlock(block, Blocktyp.SEQUENTIAL,
          listRO.toArray(new ReadOnlyPruefung[0]));
    }
    else{
      state.controller.createBlock(block, Blocktyp.PARALLEL,
          listRO.toArray(new ReadOnlyPruefung[0]));
    }


    ReadOnlyBlock roBlock = state.controller.getUngeplanteBloecke().iterator().next();
    for (String pruefungname : pruefungen) {
      assertThat(roBlock.getROPruefungen().contains(getPruefungFromModel(pruefungname))).isTrue();
    }

  }




  @Und("der geplante Block {string} {string} {localDateTime} enthaelt {stringList}")
  public void derGeplanteBlockEnthaelt(String blockname, String blockart,LocalDateTime termin,
     List<String> pruefungen) throws NoPruefungsPeriodeDefinedException, HartesKriteriumException {

    ReadOnlyBlock block;

    ArrayList<ReadOnlyPruefung> listRO = new ArrayList<>();

    for (String pruefungname : pruefungen) {
      ReadOnlyPruefung roPruefung = getOrCreate(pruefungname);
      listRO.add(roPruefung);
    }

    if (blockart.equals("SEQUENTIAL")) {
      block = state.controller.createBlock(blockname, Blocktyp.SEQUENTIAL,
          listRO.toArray(new ReadOnlyPruefung[0]));
    }
    else{
      block = state.controller.createBlock(blockname, Blocktyp.PARALLEL,
          listRO.toArray(new ReadOnlyPruefung[0]));
    }
    state.controller.scheduleBlock(block,termin);



    Iterator<ReadOnlyBlock> it = state.controller.getGeplanteBloecke().iterator();
    boolean found = false;
    ReadOnlyBlock roBlock = null;
    while(it.hasNext() && !found){
      roBlock = it.next();
      if(roBlock.getName().equals(blockname)){
        found = !found;
      }
    }

    for (String pruefungname : pruefungen) {
      assertThat(roBlock.getROPruefungen().contains(getPruefungFromModel(pruefungname))).isTrue();
    }


  }

  @Und("ist die Pruefung {string} geplant")
  public void istDiePruefungGeplant(String pruefung) throws NoPruefungsPeriodeDefinedException {
    ReadOnlyPruefung roPruefung = getPruefungFromModel(pruefung);
    assertThat(roPruefung.getTermin()).isNotEmpty();
  }



  @Wenn("wenn ich eine Unbekannte Pruefung aus einen Block {string} entfernen moechte")
  public void wennIchEineUnbekanntePruefungAusEinenBlockEntfernenMoechte(String arg0) {


    ReadOnlyPruefung ro = new PruefungDTOBuilder().withPruefungsName("a").withPruefungsNummer("b").build();
    try {
      state.controller.removePruefungFromBlock(getBlockFromModel(arg0),
          ro);
      List<ReadOnlyPruefung> result = new ArrayList<>();
      result.add(ro);
      state.results.put("affected", result);
    } catch (IllegalStateException | NoPruefungsPeriodeDefinedException e) {
      state.results.put("exception", e);
    }
  }
}
