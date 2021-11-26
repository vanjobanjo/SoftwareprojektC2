package de.fhwedel.klausps.controller.services;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

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
      pruefungsperiode.addPlanungseinheit(new PruefungImpl(pruefungsNr, name, "", 50, duration));
      return new PruefungDTOBuilder()
          .withPruefungsName(name)
          .withPruefungsNummer(pruefungsNr)
          .withPruefer(pruefer)
          .withDauer(duration)
          .withTeilnehmerKreisen(teilnehmerkreise.keySet())
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
}
