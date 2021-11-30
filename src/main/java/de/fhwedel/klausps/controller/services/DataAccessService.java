package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataAccessService {

  private Pruefungsperiode pruefungsperiode;
  private final ScheduleService scheduleService;

  public DataAccessService(Pruefungsperiode pruefungsperiode) {

    this.pruefungsperiode = pruefungsperiode;
    this.scheduleService = new ScheduleService(pruefungsperiode);
  }

  public ReadOnlyPruefung createPruefung(
      String name,
      String pruefungsNr,
      Set<String> pruefer,
      Duration duration,
      Map<Teilnehmerkreis, Integer> teilnehmerkreise) {

    Set<Planungseinheit> filtered = getPlannedPlanungseinheitenWithPruefungsnummer(pruefungsNr);

    if (filtered.isEmpty()) {
      // todo contains static values as it is unclear where to retreave the data from
      pruefungsperiode.addPlanungseinheit(
          new PruefungImpl(pruefungsNr, name, "", duration, null)); // TODO Valerio
      return new PruefungDTOBuilder()
          .withPruefungsName(name)
          .withPruefungsNummer(pruefungsNr)
          .withPruefer(pruefer)
          .withDauer(duration)
          .withTeilnehmerKreisSchaetzung(
              teilnehmerkreise) // TODO an Valerio: davor stand da keySet()
          .build();
    }
    return null;
  }

  private boolean isPruefung(Planungseinheit planungseinheit) {
    return planungseinheit instanceof Pruefung;
  }

  private Set<Planungseinheit> getPlannedPlanungseinheitenWithPruefungsnummer(
      String pruefungsnummer) {
    // todo can we really only filter planungseinheiten?
    return pruefungsperiode.filteredPlanungseinheiten(
        (Planungseinheit planungseinheit) ->
            isPruefung(planungseinheit)
                && ((Pruefung) planungseinheit).getPruefungsnummer().equals(pruefungsnummer));
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

  public ReadOnlyPruefung changeNameOfPruefung(ReadOnlyPruefung toChange, String name) {
    Pruefung pruefungModel = pruefungsperiode.pruefung(toChange.getPruefungsnummer());

    if (pruefungModel == null) {
      throw new IllegalArgumentException();
    }

    pruefungModel.setName(name);
    int scoring = toChange.getScoring();
    return new PruefungDTOBuilder(pruefungModel).withScoring(scoring).build();
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen() {
    return pruefungsperiode.geplantePruefungen().stream()
        .map(
            pruefung ->
                new PruefungDTOBuilder(pruefung)
                    .withScoring(
                        scheduleService.getScoring(
                            pruefung,
                            pruefungsperiode.block(pruefung) == null
                                ? new LinkedList<>()
                                : pruefungsperiode.block(pruefung).getPruefungen().stream()
                                    .toList()))
                    .build())
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyPruefung> getUngeplantePruefungen() {
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
            x ->
                new BlockDTO(
                    "TODO", // TODO
                    x.getStartzeitpunkt(),
                    x.getDauer(),
                    x.isGeplant(),
                    x.getPruefungen().stream()
                        .map(
                            pruefung ->
                                new PruefungDTOBuilder(pruefung)
                                    .withScoring(
                                        scheduleService.getScoring(
                                            pruefung, x.getPruefungen().stream().toList()))
                                    .build())
                        .collect(Collectors.toSet())))
        .collect(Collectors.toSet());
  }

  public Set<ReadOnlyBlock> getUngeplanteBloecke() {
    return pruefungsperiode.ungeplanteBloecke().stream()
        .map(
            x ->
                new BlockDTO(
                    "TODO", // TODO
                    x.getStartzeitpunkt(),
                    x.getDauer(),
                    x.isGeplant(),
                    x.getPruefungen().stream()
                        .map(pruefung -> new PruefungDTOBuilder(pruefung).build())
                        .collect(Collectors.toSet())))
        .collect(Collectors.toSet());
  }

  public List<ReadOnlyPruefung> unschedulePruefung(ReadOnlyPruefung pruefung) {
    whenTrueThrowIllegalArgumentExpcetion(pruefung.ungeplant(), "Pruefung ist ungeplant!");

    Pruefung pruefungModel = pruefungsperiode.pruefung(pruefung.getPruefungsnummer());

    whenTrueThrowIllegalArgumentExpcetion(
        pruefungModel == null, pruefung.getPruefungsnummer() + " Pruefung nicht in Periode");

    whenTrueThrowIllegalArgumentExpcetion(
        pruefungsperiode.block(pruefungModel) != null,
        pruefung.getPruefungsnummer() + " Pruefung ist Teil eines Blockes");

    return scheduleService.unschedulePruefung(pruefungModel).stream()
        .map(
            x ->
                new PruefungDTOBuilder(x)
                    .withScoring(
                        scheduleService.getScoring(
                            x,
                            pruefungsperiode.block(x) == null
                                ? new LinkedList<Pruefung>()
                                : pruefungsperiode.block(x).getPruefungen().stream().toList()))
                    .build())
        .collect(Collectors.toList());
  }

  private void whenTrueThrowIllegalArgumentExpcetion(boolean condition, String text) {
    if (condition) {
      throw new IllegalArgumentException(text);
    }
  }
}
