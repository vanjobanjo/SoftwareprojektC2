package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG;

import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MehrePruefungenAmTagRestriction extends SoftRestriction {

  protected MehrePruefungenAmTagRestriction(DataAccessService dataAccessService) {
    super(dataAccessService, MEHRERE_PRUEFUNGEN_AM_TAG);
  }

  public MehrePruefungenAmTagRestriction() {
    this(ServiceProvider.getDataAccessService());
  }

  @Override
  public Optional<SoftRestrictionAnalysis> evaluateRestriction(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {

    Set<Pruefung> setPruefung = new HashSet<>();
    Map<Teilnehmerkreis, Integer> mapTeilnehmerkreis = new HashMap<>();

    if (pruefung != null && pruefung.isGeplant()) {

      List<Planungseinheit> listWithPruefungenInTimeSpace = getPruefungnInTimeSpace(pruefung);
      for (Planungseinheit planungseinheit : listWithPruefungenInTimeSpace) {
        //Unterscheidung auf Block
        if (planungseinheit.isBlock()) {
          goThrowBlock(pruefung, setPruefung, mapTeilnehmerkreis, planungseinheit.asBlock());
        } else {
          setPruefung.addAll(
              testTwoPruefungenKonftikt(pruefung, planungseinheit.asPruefung(),
                  mapTeilnehmerkreis));
        }
      }
      if (setPruefung.contains(pruefung)) {
        TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(mapTeilnehmerkreis,
            pruefung.getSchaetzungen());
      }
    }

    return getWeichesKriteriumAnalyse(setPruefung, mapTeilnehmerkreis);
  }

  /**
   * Methode die durch ein Block durch iteriert und mit jeder Pruefung einzeln vergleicht
   *
   * @param pruefung           mit der verglichen werden soll
   * @param setPruefung        die Pruefungen, die ein Konflikt bilden
   * @param mapTeilnehmerkreis die Teilnehmer mit der Anzahl von studenten
   * @param block              der Block, durch den gagangen werden soll
   */
  private void goThrowBlock(Pruefung pruefung, Set<Pruefung> setPruefung,
      Map<Teilnehmerkreis, Integer> mapTeilnehmerkreis, Block block) {
    Set<Pruefung> pruefungenFromBlock;
    pruefungenFromBlock = block.getPruefungen();
    //Wenn der Block die Pruefung nicht beinhaltet, muss dieser nicht angeguckt werden
    if (!pruefungenFromBlock.contains(pruefung)) {
      // jede Pruefung im Block überprüfen
      for (Pruefung pruefungBlock : pruefungenFromBlock) {
        setPruefung.addAll(
            testTwoPruefungenKonftikt(pruefung, pruefungBlock, mapTeilnehmerkreis));
      }
    }
  }

  /**
   * Methode für das bekommen, von allen Pruefungen in einer bestimmten Zeitspanne
   *
   * @param pruefung die Pruefung, mit der die Zeitspanne ausgerchnet wird
   * @return eine Liste, die alle Pruefungen beinhaltet, die in den berechneten Zeitraum sind
   * @throws NoPruefungsPeriodeDefinedException Wenn keine PruefungsPeriode definiert ist
   */
  private List<Planungseinheit> getPruefungnInTimeSpace(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    LocalDateTime start = startDay(pruefung.getStartzeitpunkt());
    LocalDateTime end = endDay(pruefung.getStartzeitpunkt());

    List<Planungseinheit> listWithPruefungenInTimeSpace = new ArrayList<>();
    try {
      listWithPruefungenInTimeSpace.addAll(dataAccessService.getAllPlanungseinheitenBetween(start,
          end));
    } catch (IllegalTimeSpanException e) {
      //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
      e.printStackTrace();
    }
    listWithPruefungenInTimeSpace.remove(pruefung);
    return listWithPruefungenInTimeSpace;
  }

  /**
   * Methode die die WeicheKriterumAnalyse erstellt, wenn es zu einen Konflikt kommt
   *
   * @param pruefungen         ein Set von Pruefungen, welche zu einen Konflikt führen
   * @param mapTeilnehmerkreis die Teilnehmerkreise mit ihrer Anzahl
   * @return die WeicheKriterumsAnalyse, wenn diese vorhanden ist, sonst Optional.empty()
   */
  private Optional<SoftRestrictionAnalysis> getWeichesKriteriumAnalyse(Set<Pruefung> pruefungen,
      Map<Teilnehmerkreis, Integer> mapTeilnehmerkreis) {

    if (!pruefungen.isEmpty()) {
      int scoring = addDeltaScoring(pruefungen);
      int countStudents = 0;
      for (Integer students : mapTeilnehmerkreis.values()) {
        countStudents += students;
      }
      SoftRestrictionAnalysis wKA = new SoftRestrictionAnalysis(pruefungen,
          MEHRERE_PRUEFUNGEN_AM_TAG, mapTeilnehmerkreis.keySet(), countStudents, scoring);
      return Optional.of(wKA);
    } else {
      return Optional.empty();
    }
  }


  /**
   * Methode die pruefung mit toCeck Pruefung vergleicht und falls es zu einen Konflikt führt,
   * werden diese Pruefungen zurück gegeben. Außerdem wird die Map mit den Teilnehmerkreisen und
   * deren Anzahl befühllt.
   *
   * @param pruefung      die Pruefung, die neu Hinzugefügt wurde und für die dieses Kriterum
   *                      gececkt werden soll
   * @param toCheck       die Pruefung, die schon ein geplant war und mit der verglichen wird
   * @param mapTeilnehmer eine Map mit der Anzahl von den unterschiedlichen Teilnehmerkreisen
   * @return ein Set von Pruefungen, die mit einander in Konflikt stehen
   */
  private Set<Pruefung> testTwoPruefungenKonftikt(Pruefung pruefung, Pruefung toCheck,
      Map<Teilnehmerkreis, Integer> mapTeilnehmer) {

    Set<Pruefung> setConflictPruefung = new HashSet<>();
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(mapTeilnehmer, toCheck.getSchaetzungen());
        setConflictPruefung.add(toCheck);
        setConflictPruefung.add(pruefung);
      }
    }
    return setConflictPruefung;
  }

  /**
   * Methode die aus einem LocalDateTime die StartZeit auf START_ZEIT setzt
   *
   * @param time Der Tag, an den die Pruefung stattfindet, aber wo die StartZeit neu gesetztt werden
   *             soll
   * @return der gleiche Tag wie in time, aber mit der START_ZEIT
   */
  private LocalDateTime startDay(LocalDateTime time) {
    return time.toLocalDate().atStartOfDay();
  }

  /**
   * Methdoe die aus einem LocalDateTIme die EndZeit auf END_ZEit setzt
   *
   * @param time Der Tag, an den die Pruefung stattfindet, aber wo die END_ZEIT neu gesetztt werden
   *             soll
   * @return der gleiche Tag wie in time, aber mit der END_ZEIT
   */
  private LocalDateTime endDay(LocalDateTime time) {
    return time.toLocalDate().plusDays(1).atStartOfDay();
  }

  @Override
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    int scoring = 0;
    scoring +=
        MEHRERE_PRUEFUNGEN_AM_TAG.getWert() * (affectedPruefungen.size() - 2 + 1);
    return scoring;
  }

}
