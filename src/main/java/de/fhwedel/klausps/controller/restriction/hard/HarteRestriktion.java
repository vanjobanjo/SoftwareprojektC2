package de.fhwedel.klausps.controller.restriction.hard;


import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.restriction.Restriktion;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Abstracte Klasse fuer die Harten Kriteren. Hier werden die einheitlichen Methoden bestimmt, die
 * gebraucht werden um auf eine Hartes Kriterum getestet zu werden
 */

public abstract class HarteRestriktion extends Restriktion {

  /**
   * hardRestriction, hier wird das HarteKriterum, um welches es sich handelt gespeichert
   */
  protected final HartesKriterium hardRestriction;
  /**
   * Zum speichern des DataAccessService
   */
  protected DataAccessService dataAccessService;

  /**
   * Super Konstruker, welcher sich den DataAccessService speichert und das HarteKriterum
   *
   * @param dataAccessService der DataAccessService, der gespeichert wird
   * @param kriterium         das Kriterum, welches getestet werden soll
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
   * Dise Methode wertet zu der Übergebenen Pruefung das HarteKriterum aus. Und gibt ein Optional
   * von einer Harten KriterumsAnalyse zurück.
   *
   * @param pruefung die Pruefung die neu hinzugekommen ist, und mit der das HarteKritum getestet
   *                 werden soll
   * @return ein Optional, welches entweder leer ist, wenn das HarteKriterum nicht zutrifft, oder
   * eine HarteKrteriumsAnalyse, mit den Klausuren und Teilnehmerkreisen, welche gegen das Kriterum
   * verstößt
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
      Planungseinheit planungseinheitToCheckFor);

  /**
   * Methode, die testet, ob der Übergebene Zeitpunkt und mit der Planungseinheit das HarteKriterum
   * missachtet
   *
   * @param startTime       der Zeitpunkt der mit der Planungseinheit getestet werden soll
   * @param planungseinheit die Planungseinheit, für die getestet werden soll
   * @return true, wenn die Planungseinheit, an den Übergebenen Zeitpunkt stattfinden darf
   * @throws NoPruefungsPeriodeDefinedException, wenn keien Pruefungsperiode Definiert ist
   */
  public abstract boolean wouldBeHardConflictAt(LocalDateTime startTime,
      Planungseinheit planungseinheit)
      throws NoPruefungsPeriodeDefinedException;
}
