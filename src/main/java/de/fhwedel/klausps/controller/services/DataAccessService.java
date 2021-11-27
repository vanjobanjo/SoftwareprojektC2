package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import java.util.stream.Collectors;

public class DataAccessService {

  private static final int MINUTSBETWEENPRUEFUNGEN = 30;

  private Pruefungsperiode pruefungsperiode;

  public DataAccessService(Pruefungsperiode pruefungsperiode) {
    this.pruefungsperiode = pruefungsperiode;
  }

  public ReadOnlyPruefung createPruefung(
      String name,
      String pruefungsNr,
      Set<String> pruefer,
      Duration duration,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {

    Pruefung isAvailable = pruefungsperiode.pruefung(pruefungsNr);
    if (isAvailable == null) {
      // todo contains static values as it is unclear where to retreave the data from
      Pruefung pruefungModel = new PruefungImpl(pruefungsNr, name, "", duration, null);
      addTeilnehmerKreisSchaetzungToModelPruefung(pruefungModel, teilnehmerkreise);
      pruefungsperiode.addPlanungseinheit(pruefungModel);
      return new PruefungDTOBuilder()
          .withPruefungsName(name)
          .withPruefungsNummer(pruefungsNr)
          .withPruefer(pruefer)
          .withDauer(duration)
          .withTeilnehmerKreisSchaetzung(teilnehmerkreise)
          .build();
    }
    return null;
  }

  private void addTeilnehmerKreisSchaetzungToModelPruefung(
      Pruefung pruefungModel, Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    teilnehmerkreise.forEach(pruefungModel::setSchaetzung);
  }

  public ReadOnlyPruefung createPruefung(
      String name,
      String pruefungsNr,
      String pruefer,
      Duration duration,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {
    return createPruefung(name, pruefungsNr, Set.of(pruefer), duration, teilnehmerkreise);
  }

  public boolean isPruefungsperiodeSet() {
    return false;
  }


  public List<ReadOnlyPruefung> changeDauerEinerPruefung(ReadOnlyPruefung pruefung,
      Duration minutes) throws HartesKriteriumException {
    Set<Planungseinheit> filtered = getPlannedPlanungseinheitenWithPruefungsnummer(
        pruefung.getPruefungsnummer());
    List<ReadOnlyPruefung> retList = new ArrayList<>();
    if (filtered.size() == 1) {
      pruefungsperiode.pruefung(pruefung.getName()).setDauer(minutes);
      //TODO Map noch entfernen
      Map<Teilnehmerkreis, Integer> retMap = new HashMap<>();
      for (Teilnehmerkreis t : pruefung.getTeilnehmerkreise()) {
        retMap.put(t, 0);
      }

      ReadOnlyPruefung newPruefung = createPruefung(pruefung.getName(),
          pruefung.getPruefungsnummer(), pruefung.getPruefer(),
          minutes, retMap);
      //TODO HartesKriterium überprüfen
      // lookAtAllTeilnehmerkreise(newPruefung);
      retList.add(newPruefung);

      return retList;
    }

    return null;
  }


  public List<ReadOnlyPruefung> schedulePruefung(ReadOnlyPruefung pruefung,
      LocalDateTime startTermin)
      throws HartesKriteriumException {

    PruefungDTOBuilder newPruefung = new PruefungDTOBuilder();
    for (Teilnehmerkreis teilnehmerkreis : pruefung.getTeilnehmerkreise()) {
      newPruefung.withAdditionalTeilnehmerkreis(teilnehmerkreis);
    }

    newPruefung.withPruefer(pruefung.getPruefer());
    newPruefung.withPruefungsName(pruefung.getName());
    newPruefung.withPruefungsNummer(pruefung.getPruefungsnummer());
    newPruefung.withDauer(pruefung.getDauer());
    newPruefung.withStartZeitpunkt(startTermin);

    //TODO HartesKriterium überprüfen

    //TODO ChangeScoring
    //TODO return alle von Changescoring prüfungen als Liste

    return Collections.emptyList();
  }


  public ReadOnlyPruefung changeNameOfPruefung(ReadOnlyPruefung toChange, String name) {
    Pruefung pruefung = pruefungsperiode.pruefung(toChange.getPruefungsnummer());
    pruefung.setName(name);
    int scoring = toChange.getScoring();
    return new PruefungDTOBuilder(pruefung).withScoring(scoring).build();
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen() {
    return pruefungsperiode.geplantePruefungen().stream()
        .map(
            pruefung ->
                new PruefungDTOBuilder(pruefung) // TODO pruefung.getScoring(); Scoring berechnen
                    .build())
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyPruefung> getUngeplanteKlausuren() {
    return pruefungsperiode.ungeplantePruefungen().stream()
        .map(
            pruefung ->
                new PruefungDTOBuilder(pruefung) // TODO pruefung.getScoring();
                    .build())
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyBlock> getGeplanteBloecke() {
    return pruefungsperiode.geplanteBloecke().stream()
        .map(
            block ->
                new BlockDTO(
                    block.getName(),
                    block.getStartzeitpunkt(),
                    block.getDauer(),
                    block.isGeplant(),
                    block.getPruefungen().stream()
                        .map(pruefung -> new PruefungDTOBuilder(pruefung).build())
                        .collect(Collectors.toSet())))
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyBlock> getUngeplanteBloecke() {
    return pruefungsperiode.ungeplanteBloecke().stream()
        .map(
            x ->
                new BlockDTO(
                    x.getName(),
                    x.getStartzeitpunkt(),
                    x.getDauer(),
                    x.isGeplant(),
                    x.getPruefungen().stream()
                        .map(pruefung -> new PruefungDTOBuilder(pruefung).build())
                        .collect(Collectors.toSet())))
        .collect(Collectors.toSet());
  }
}
