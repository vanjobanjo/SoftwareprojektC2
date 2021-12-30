package de.fhwedel.klausps.controller.restriction.hard;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.HartesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TwoKlausurenSameTime extends HartRestriktion implements Predicate<Pruefung> {

  static final long MINUTES_BETWEEN_PRUEFUNGEN = 30;

  public TwoKlausurenSameTime(DataAccessService dataAccessService, HartesKriterium kriterium, Pruefung pruefung) {
    super(dataAccessService, kriterium);
    this.pruefung = pruefung;
  }


  @Override
  public List<HartesKriteriumAnalyse> evaluate() throws HartesKriteriumException {

    List<HartesKriteriumAnalyse> returnList = new ArrayList<>();
    boolean hartKriterium = false;

    LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    LocalDateTime end = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
        .plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPruefungenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Set<Pruefung> pruefungenFromBlock;
    for (Planungseinheit planungseinheit : testList) {
      if (planungseinheit.isBlock()) {
        pruefungenFromBlock = planungseinheit.asBlock().getPruefungen();
        if (!pruefungenFromBlock.contains(pruefung)) {
          for (Pruefung pruefungBlock : pruefungenFromBlock) {
            hartKriterium =
                getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock) || hartKriterium;
          }
        }
      } else {
        hartKriterium =
            getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung()) || hartKriterium;
      }
    }
    if (hartKriterium) {
      this.inConflictROPruefung.add(pruefung);
      HartesKriteriumAnalyse hKA = new HartesKriteriumAnalyse(this.inConflictROPruefung,this.inConfilictTeilnehmerkreis,this.countStudents);
      returnList.add(hKA);
    }
    return returnList;
  }

  @Override
  public List<HartesKriteriumAnalyse> evaluate(Pruefung pruefung) throws HartesKriteriumException {

    List<HartesKriteriumAnalyse> returnList = new ArrayList<>();
    boolean hartKriterium = false;

    LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    LocalDateTime end = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
        .plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPruefungenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Set<Pruefung> pruefungenFromBlock;
    for (Planungseinheit planungseinheit : testList) {
      if (planungseinheit.isBlock()) {
        pruefungenFromBlock = planungseinheit.asBlock().getPruefungen();
        if (!pruefungenFromBlock.contains(pruefung)) {
          for (Pruefung pruefungBlock : pruefungenFromBlock) {
            hartKriterium =
                getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock) || hartKriterium;
          }
        }
      } else {
        hartKriterium =
            getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung()) || hartKriterium;
      }
    }
    if (hartKriterium) {
      this.inConflictROPruefung.add(pruefung);
      HartesKriteriumAnalyse hKA = new HartesKriteriumAnalyse(this.inConflictROPruefung,this.inConfilictTeilnehmerkreis,this.countStudents);
      returnList.add(hKA);
    }
    return returnList;
  }

  @Override
  public boolean test(Pruefung pruefung) {

    boolean hartKriterium = false;

    LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    LocalDateTime end = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
        .plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPruefungenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //start kann nicht vor ende liegen, da ich das berechne
      e.printStackTrace();
    }

    Set<Pruefung> pruefungenFromBlock;
    for (Planungseinheit planungseinheit : testList) {
      if (planungseinheit.isBlock()) {
        pruefungenFromBlock = planungseinheit.asBlock().getPruefungen();
        if (!pruefungenFromBlock.contains(pruefung)) {
          for (Pruefung pruefungBlock : pruefungenFromBlock) {
            hartKriterium =
                getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock) || hartKriterium;
          }
        }
      } else {
        hartKriterium =
            getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung()) || hartKriterium;
      }
    }
    if (hartKriterium) {
      this.inConflictROPruefung.add(pruefung);
    }
    return hartKriterium;
  }

  private boolean getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck) {
    boolean retBool = false;
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        if (!inConfilictTeilnehmerkreis.contains(teilnehmerkreis)) {
          //hier sollte ein Teilnehmerkreis nur einmal dazu addiert werden.
          this.countStudents += toCheck.getSchaetzungen().get(teilnehmerkreis);
        }
        //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
        this.inConfilictTeilnehmerkreis.add(teilnehmerkreis);
        this.inConflictROPruefung.add(toCheck);
        retBool = true;
      }
    }
    return retBool;
  }
}
