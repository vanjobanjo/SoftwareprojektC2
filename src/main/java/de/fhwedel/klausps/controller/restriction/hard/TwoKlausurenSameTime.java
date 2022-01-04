package de.fhwedel.klausps.controller.restriction.hard;

import static de.fhwedel.klausps.controller.kriterium.HartesKriterium.ZWEI_KLAUSUREN_GLEICHZEITIG;

import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class TwoKlausurenSameTime extends HartRestriktion implements Predicate<Pruefung> {

  static final long MINUTES_BETWEEN_PRUEFUNGEN = 30;


  public TwoKlausurenSameTime() {
    this(ServiceProvider.getDataAccessService());
  }

  protected TwoKlausurenSameTime(DataAccessService dataAccessService) {
    super(dataAccessService, ZWEI_KLAUSUREN_GLEICHZEITIG);
  }

  @Override
  public Optional<HartesKriteriumAnalyse> evaluate(Pruefung pruefung) {

    boolean hartKriterium = false;

    LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    LocalDateTime end = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
        .plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPlanungseinheitenBetween(start, end);
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
      return Optional.of(new HartesKriteriumAnalyse(this.inConflictROPruefung,
          this.inConfilictTeilnehmerkreis, this.countStudents));
    }
    return Optional.empty();
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

  @Override
  public boolean test(Pruefung pruefung) {

    boolean hartKriterium = false;

    LocalDateTime start = pruefung.getStartzeitpunkt().minusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    LocalDateTime end = pruefung.getStartzeitpunkt().plus(pruefung.getDauer())
        .plusMinutes(MINUTES_BETWEEN_PRUEFUNGEN);
    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPlanungseinheitenBetween(start, end);
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
}
