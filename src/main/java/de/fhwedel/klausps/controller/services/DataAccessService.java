package de.fhwedel.klausps.controller.services;

import static java.util.Objects.nonNull;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Semester;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.PruefungsperiodeImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataAccessService {

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

    if (!existsPruefung(pruefungsNr)) {
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

  private boolean existsPruefung(String pruefungsNummer) {
    return pruefungsperiode.pruefung(pruefungsNummer) != null;
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
    return nonNull(pruefungsperiode);
  }

  public List<ReadOnlyPruefung> changeDurationOf(ReadOnlyPruefung pruefung, Duration minutes)
      throws HartesKriteriumException {
    List<ReadOnlyPruefung> result = new ArrayList<>();
    Pruefung pruefungFromModel = pruefungsperiode.pruefung(pruefung.getName());
    pruefungFromModel.setDauer(minutes);

    ReadOnlyPruefung newPruefung =
        new PruefungDTO(
            pruefungFromModel.getPruefungsnummer(),
            pruefungFromModel.getName(),
            pruefungFromModel.getStartzeitpunkt(),
            pruefungFromModel.getDauer(),
            pruefungFromModel.getSchaetzungen(),
            pruefung.getPruefer(),
            0); // TODO where do we get the scoring from?
    // TODO HartesKriterium überprüfen
    result.add(newPruefung);

    return result;
  }

  public List<ReadOnlyPruefung> schedulePruefung(
      ReadOnlyPruefung pruefung, LocalDateTime startTermin) throws HartesKriteriumException {

    PruefungDTOBuilder newPruefung = new PruefungDTOBuilder();
    for (Teilnehmerkreis teilnehmerkreis : pruefung.getTeilnehmerkreise()) {
      newPruefung.withAdditionalTeilnehmerkreis(teilnehmerkreis);
    }

    newPruefung.withPruefer(pruefung.getPruefer());
    newPruefung.withPruefungsName(pruefung.getName());
    newPruefung.withPruefungsNummer(pruefung.getPruefungsnummer());
    newPruefung.withDauer(pruefung.getDauer());
    newPruefung.withStartZeitpunkt(startTermin);

    // TODO HartesKriterium überprüfen

    // TODO ChangeScoring
    // TODO return alle von Changescoring prüfungen als Liste

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
                new PruefungDTOBuilder(pruefung) // TODO use scoring
                    .build())
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyBlock> getGeplanteBloecke() {
    Set<ReadOnlyBlock> result = new HashSet<>();
    for (Block block : pruefungsperiode.geplanteBloecke()) {
      result.add(fromModelToDTOBlock(block));
    }
    return result;

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

  public ReadOnlyPruefung addPruefer(String pruefungsNummer, String pruefer) {
    if (existsPruefung(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.addPruefer(pruefer);
      return new PruefungDTOBuilder(pruefung).build();
      // TODO add scoring to result
    }
    throw new IllegalArgumentException("Passed unknown pruefung!");
  }

  public ReadOnlyPruefung removePruefer(String pruefungsNummer, String pruefer) {
    if (existsPruefung(pruefungsNummer)) {
      Pruefung pruefung = pruefungsperiode.pruefung(pruefungsNummer);
      pruefung.removePruefer(pruefer);
      return new PruefungDTOBuilder(pruefung).build();
      // TODO add scoring to result
    }
    throw new IllegalArgumentException("Passed unknown pruefung!");
  }

  public void createEmptyPeriode(
      Semester semester, LocalDate start, LocalDate end, int kapazitaet) {
    this.pruefungsperiode = new PruefungsperiodeImpl(semester, start, end, kapazitaet);
  }


  private ReadOnlyBlock fromModelToDTOBlock(Block modelBlock) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>();
    for (Pruefung pruefung : modelBlock.getPruefungen()) {
      pruefungen.add(fromModelToDTOPruefung(pruefung));
    }
    return new BlockDTO(modelBlock.getName(),
        modelBlock.getStartzeitpunkt(),
        modelBlock.getDauer(),
        modelBlock.isGeplant(),
        pruefungen
    );
  }


  private ReadOnlyPruefung fromModelToDTOPruefung(Pruefung modelPruefung) {
    return new PruefungDTOBuilder()
        .withPruefungsName(modelPruefung.getName())
        .withPruefungsNummer(modelPruefung.getPruefungsnummer())
        .withDauer(modelPruefung.getDauer())
        .withStartZeitpunkt(modelPruefung.getStartzeitpunkt())
        .withPruefer(modelPruefung.getPruefer())
        .withTeilnehmerKreisSchaetzung(modelPruefung.getSchaetzungen())
        .build();
  }
}
