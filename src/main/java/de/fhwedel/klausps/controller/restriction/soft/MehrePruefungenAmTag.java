package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class MehrePruefungenAmTag extends WeicheRestriktion {

  static final int START_ZEIT = 8;
  static final int END_ZEIT = 18;


  protected MehrePruefungenAmTag(DataAccessService dataAccessService) {
    super(dataAccessService, MEHRERE_PRUEFUNGEN_AM_TAG);
  }

  public MehrePruefungenAmTag() {
    this(ServiceProvider.getDataAccessService());
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    //TODO schön machen
    Set<ReadOnlyPruefung> setReadyOnly = new HashSet<>();
    Set<Pruefung> setPruefung = new HashSet<>();
    Set<Teilnehmerkreis> setTeilnehmer = new HashSet<>();
    Map<Teilnehmerkreis, Integer> mapTeilnehmerkreis = new HashMap<>();
    int countStudents = 0;


    if (pruefung != null && pruefung.isGeplant()) {

      LocalDateTime start = startDay(pruefung.getStartzeitpunkt());
      LocalDateTime end = endDay(pruefung.getStartzeitpunkt());

      List<Planungseinheit> testList = null;
      try {
        testList = dataAccessService.getAllPlanungseinheitenBetween(start, end);
      } catch (IllegalTimeSpanException e) {
        //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
        e.printStackTrace();
      }
      Set<Pruefung> pruefungenFromBlock;
      for (Planungseinheit planungseinheit : testList) {
        //Unterscheidung auf Block
        if (planungseinheit.isBlock()) {
          pruefungenFromBlock = planungseinheit.asBlock().getPruefungen();
          //Wenn der Block die Pruefung nicht beinhaltet, muss dieser nicht angeguckt werden
          if (!pruefungenFromBlock.contains(pruefung)) {
            // jede Pruefung im Block überprüfen
            for (Pruefung pruefungBlock : pruefungenFromBlock) {

              setPruefung.addAll(getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock, mapTeilnehmerkreis));
            }
          }
        } else {
          setPruefung.addAll(getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung(), mapTeilnehmerkreis));

        }
      }
    }
    if(setPruefung.contains(pruefung)){
      TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(mapTeilnehmerkreis,pruefung.getSchaetzungen());
    }

    return getWeichesKriteriumAnalyse(setPruefung, mapTeilnehmerkreis);
  }

  @NotNull
  private Optional<WeichesKriteriumAnalyse> getWeichesKriteriumAnalyse(Set<Pruefung> pruefungen,
     Map<Teilnehmerkreis,Integer> mapTeilnehmerkreis) {

    if (!pruefungen.isEmpty()) {
      int scoring = 0;
      int countStudents =0 ;
      scoring +=
          MEHRERE_PRUEFUNGEN_AM_TAG.getWert() * (pruefungen.size() - 2 + 1);
      for(Integer students : mapTeilnehmerkreis.values()){
        countStudents += students;
      }
      WeichesKriteriumAnalyse wKA = new WeichesKriteriumAnalyse(pruefungen,
          MEHRERE_PRUEFUNGEN_AM_TAG, mapTeilnehmerkreis.keySet(), countStudents, scoring);
      return Optional.of(wKA);
    } else {
      return Optional.empty();
    }
  }


  private Set<Pruefung> getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck, Map<Teilnehmerkreis, Integer> mapTeilnehmer) {

    Set<Pruefung> setConfliktPruefung = new HashSet<>();
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(mapTeilnehmer,toCheck.getSchaetzungen());
        setConfliktPruefung.add(toCheck);
        setConfliktPruefung.add(pruefung);
      }
    }
    return setConfliktPruefung;
  }

  private LocalDateTime startDay(LocalDateTime time) {
    return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), START_ZEIT, 0);
  }

  private LocalDateTime endDay(LocalDateTime time) {
    return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), END_ZEIT, 0);
  }

  @Override
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    throw new UnsupportedOperationException("not implemented");
  }

}
