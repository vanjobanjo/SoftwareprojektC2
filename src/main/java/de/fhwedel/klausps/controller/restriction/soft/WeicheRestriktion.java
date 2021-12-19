package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class WeicheRestriktion {

  protected final DataAccessService dataAccessService;
  protected final WeichesKriterium kriterium;

  protected WeicheRestriktion(
      DataAccessService dataAccessService,
      WeichesKriterium kriterium) {
    this.dataAccessService = dataAccessService;
    this.kriterium = kriterium;
  }

  /**
   * Evaluates for a {@link Pruefung} in which way it violates a restriction.
   *
   * @param pruefung The pruefung for which to check for violations of a restriction.
   * @return Either an {@link Optional} containing a {@link KriteriumsAnalyse} for the violated
   * restriction, or an empty Optional in case the Restriction was not violated.
   */
  public abstract Optional<KriteriumsAnalyse> evaluate(Pruefung pruefung);

  public int getAffectedStudents(Set<ReadOnlyPruefung> roPruefung) {
    int affected = 0;
    LinkedList<Teilnehmerkreis> tk = roPruefung.stream()
        .flatMap(p -> p.getTeilnehmerkreise().stream())
        .distinct().collect(Collectors.toCollection(LinkedList::new));
    while (!tk.isEmpty()) {
      Teilnehmerkreis temp = tk.remove(0);
      affected += getSchaetzungToTk(temp, roPruefung);
    }
    return affected;
  }

  private int getSchaetzungToTk(Teilnehmerkreis teilnehmerkreis, Set<ReadOnlyPruefung> roPruefung) {
    return roPruefung.stream().filter(x -> x.getTeilnehmerkreise().contains(teilnehmerkreis))
        .findAny().orElseThrow(IllegalArgumentException::new).getTeilnehmerKreisSchaetzung()
        .get(teilnehmerkreis);
  }
}
