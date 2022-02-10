package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.Restriktion;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Abstrakte Klasse fuer die harten Kriterien. Hier werden die einheitlichen Methoden bestimmt, die
 * gebraucht werden um auf ein hartes Kriterium getestet zu werden
 */

public abstract class HarteRestriktion extends Restriktion {

  /**
   * hardRestriction, hier wird das HarteKriterium, um welches es sich handelt gespeichert
   */
  protected final HartesKriterium hardRestriction;
  /**
   * Zum Speichern des DataAccessService
   */
  protected DataAccessService dataAccessService;

  /**
   * Super Konstruktor, welcher sich den DataAccessService speichert und das HarteKriterium
   *
   * @param dataAccessService der DataAccessService, der gespeichert wird
   * @param kriterium         das Kriterium, welches getestet werden soll
   */
  HarteRestriktion(DataAccessService dataAccessService, HartesKriterium kriterium) {
    this.hardRestriction = kriterium;
    this.dataAccessService = dataAccessService;
  }

  public Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung)
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
  protected abstract Optional<HartesKriteriumAnalyse> evaluateRestriction(Pruefung pruefung)
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
    return (obj instanceof HarteRestriktion harteRestriktion)
        && harteRestriktion.hardRestriction == this.hardRestriction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hardRestriction, dataAccessService);
  }
}
