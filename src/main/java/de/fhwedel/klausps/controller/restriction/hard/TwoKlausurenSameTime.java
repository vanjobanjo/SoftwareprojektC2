package de.fhwedel.klausps.controller.restriction.hard;

import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TwoKlausurenSameTime extends HartRestriktion implements Predicate<Pruefung> {

  static final long MINUTES_BETWEEN_PRUEFUNGEN = 30;

  TwoKlausurenSameTime(DataAccessService dataAccessService,
      HartesKriterium kriterium) {
    super(dataAccessService, kriterium);
  }


  @Override
  public boolean test(Pruefung pruefung) {

    boolean test = false;

    LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    LocalDateTime end = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
        .plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    List<Pruefung> testList = null;
    try {
      testList = dataAccessService.getAllPruefungenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Pruefung pruefungInTimeZone : testList) {
      for (Teilnehmerkreis teilnehmerkreis : pruefungInTimeZone.getTeilnehmerkreise()) {
        if (teilnehmer.contains(teilnehmerkreis)) {
          if (!inConfilictTeilnehmerkreis.contains(teilnehmerkreis)) {
            //hier sollte ein Teilnehmerkreis nur einmal dazu addiert werden.
            this.countStudents += pruefungInTimeZone.getSchaetzungen().get(teilnehmerkreis);
          }
          //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
          this.inConfilictTeilnehmerkreis.add(teilnehmerkreis);
          this.inConflictROPruefung.add(pruefungInTimeZone);
          this.inConflictROPruefung.add(pruefung);

          test = true;
        }
      }
    }
    return test;
  }
}
