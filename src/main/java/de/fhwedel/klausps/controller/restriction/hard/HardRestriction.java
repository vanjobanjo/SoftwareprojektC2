package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.Restriction;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This abstract class is for the hard criteria.
 * Here are the standard methods for testing the hard criteria. n
 */

public abstract class HardRestriction extends Restriction {

  /**
   * this is to safe witch criterion is tested
   */
  protected final HartesKriterium kriterium;
  /**
   * to safe the DataAccessService for getting other Pruefungen
   */
  protected DataAccessService dataAccessService;

  /**
   * Super Konstruktor
   *
   * @param dataAccessService der DataAccessService, der gespeichert wird
   * @param kriterium         das Kriterium, welches getestet werden soll
   */
  HardRestriction(DataAccessService dataAccessService, HartesKriterium kriterium) {
    this.kriterium = kriterium;
    this.dataAccessService = dataAccessService;
  }

  public Optional<HardRestrictionAnalysis> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    if (!pruefung.isGeplant()) {
      return Optional.empty();
    }
    return evaluateRestriction(pruefung);
  }


  /**
   * Diese Methode wertet zu der übergebenen Pruefung das HarteKriterium aus. Und gibt ein Optional
   * von einer harten KriteriumsAnalyse zurück.
   *
   * @param pruefung die Pruefung die neu hinzugekommen ist, und mit der das HarteKriterium getestet
   *                 werden soll
   * @return ein Optional, welches entweder leer ist, wenn das HarteKriterium nicht zutrifft, oder
   * eine HarteKriteriumsAnalyse, mit den Klausuren und Teilnehmerkreisen, welche gegen das
   * Kriterium verstößt
   */
  protected abstract Optional<HardRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException;

  /**
   * //TODO nochmal gucken, ob dieser Kommentar stimmt Methode, um alle Pruefungen, zu bekommen, an
   * der die übergebene Planungseinheit, nicht stattfinden soll
   *
   * @param planungseinheitToCheckFor die Planungseinheit, welche zu testen ist, wo sie nicht
   *                                  stattfinden darf
   * @return ein Set von Pruefungen, wo die Planungseinheit nicht stattfinden darf
   */
  public abstract Set<Pruefung> getAllPotentialConflictingPruefungenWith(
      Planungseinheit planungseinheitToCheckFor) throws NoPruefungsPeriodeDefinedException;

  /**
   * Methode, die testet, ob der übergebene Zeitpunkt und mit der Planungseinheit das HarteKriterium
   * missachtet
   *
   * @param startTime       der Zeitpunkt der mit der Planungseinheit getestet werden soll
   * @param planungseinheit die Planungseinheit, für die getestet werden soll
   * @return true, wenn die Planungseinheit, an den übergebenen Zeitpunkt stattfinden darf
   * @throws NoPruefungsPeriodeDefinedException, wenn keine Pruefungsperiode definiert ist
   */
  public abstract boolean wouldBeHardConflictAt(LocalDateTime startTime,
      Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException;


  @Override
  public boolean equals(Object obj) {
    return (obj instanceof HardRestriction harteRestriktion)
        && harteRestriktion.kriterium == this.kriterium;
  }

  @Override
  public int hashCode() {
    return Objects.hash(kriterium, dataAccessService);
  }
}
