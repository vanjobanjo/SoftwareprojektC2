package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public abstract class WeicheRestriktion {

  protected final DataAccessService dataAccessService;
  protected final WeichesKriterium kriterium;

  protected WeicheRestriktion(
      DataAccessService dataAccessService,
      WeichesKriterium kriterium) {
    this.dataAccessService = dataAccessService;
    this.kriterium = kriterium;
  }

  public abstract KriteriumsAnalyse evaluate(Pruefung toEvaluate);

  /**
   * Kumuliert die Prüfungen die innerhalb eines Blockes sind.
   * Wenn eine Prüfung innerhalb eines Blockes ist, wird der übergeordenete Block
   * in die Menge hinzugefügt.
   * @param pruefungen Pruefungen.
   * @return Menge mit Planungseinheiten
   */
  public Set<Planungseinheit> getPlanungseinheitenToPruefungen(Set<Pruefung> pruefungen) {
    Set<Planungseinheit> planungseinheiten = new HashSet<>();
    for (Pruefung p : pruefungen) {
      Optional<Block> blockOpt = dataAccessService.getBlockTo(p);
      if (blockOpt.isPresent()) {
        planungseinheiten.add(blockOpt.get());
      } else {
        planungseinheiten.add(p);
      }
    }
    return planungseinheiten;
  }
}
