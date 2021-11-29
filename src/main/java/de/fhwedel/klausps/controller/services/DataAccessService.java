package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.BlockImpl;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
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

    Set<Planungseinheit> filtered = getPlannedPlanungseinheitenWithPruefungsnummer(pruefungsNr);

    if (filtered.isEmpty()) {
      // todo contains static values as it is unclear where to retreave the data from
      pruefungsperiode.addPlanungseinheit(
              new PruefungImpl(pruefungsNr, name, "", duration, null)); //TODO Valerio
      return new PruefungDTOBuilder()
          .withPruefungsName(name)
          .withPruefungsNummer(pruefungsNr)
          .withPruefer(pruefer)
          .withDauer(duration)
          .withTeilnehmerKreisSchaetzung(teilnehmerkreise) //TODO an Valerio: davor stand da keySet()
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

  public ReadOnlyPruefung changeNameOfPruefung(ReadOnlyPruefung toChange, String name){
    Pruefung pruefungModel = pruefungsperiode.pruefung(toChange.getPruefungsnummer());
    pruefungModel.setName(name);
    int scoring = toChange.getScoring();
    return new PruefungDTOBuilder(pruefungModel).withScoring(scoring).build();
  }

  public Set<ReadOnlyPruefung> getGeplantePruefungen(){
    return pruefungsperiode
            .geplantePruefungen()
            .stream()
            .map(pruefung -> new PruefungDTOBuilder(pruefung) //TODO pruefung.getScoring(); Scoring berechnen
                    .build())
            .collect(Collectors.toSet());
  }

  public Set<ReadOnlyPruefung> getUngeplanteKlausuren(){
    return pruefungsperiode
            .ungeplantePruefungen()
            .stream()
            .map(pruefung -> new PruefungDTOBuilder(pruefung) //TODO pruefung.getScoring();
                    .build())
            .collect(Collectors.toSet());
  }

  public Set<ReadOnlyBlock> getGeplanteBloecke(){
    return pruefungsperiode
            .geplanteBloecke()
            .stream().map(x ->
                    new BlockDTO("TODO",
                            x.getStartzeitpunkt(),
                            x.getDauer(),
                            x.isGeplant(),
                            x.getPruefungen()
                                    .stream()
                                    .map(pruefung -> new PruefungDTOBuilder(pruefung).build())
                                    .collect(Collectors.toSet())
                    )).collect(Collectors.toSet());
  }
}
