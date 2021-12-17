package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class MehrePruefungenAmTag extends WeicheRestriktion implements Predicate<Pruefung> {

  Set<ReadOnlyPruefung> setReadyOnly = new HashSet<>();
  Set<Teilnehmerkreis> setTeilnehmer = new HashSet<>();
  int countStudents = 0;

  KriteriumsAnalyse kA = new KriteriumsAnalyse(setReadyOnly,WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG,setTeilnehmer,countStudents);

  protected MehrePruefungenAmTag(
      DataAccessService dataAccessService,
      WeichesKriterium kriterium) {
    super(dataAccessService, kriterium);
  }

  @Override
  public boolean test(Pruefung pruefung) {
    boolean test = false;

    LocalDateTime start = startDay(pruefung.getStartzeitpunkt());
    LocalDateTime end = endDay(pruefung.getStartzeitpunkt());
        ;
    List<Pruefung> testList = dataAccessService.getAllPruefungenBetween(start,end);
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for(Pruefung pruefungInTimeZone : testList){
      for(Teilnehmerkreis teilnehmerkreis : pruefungInTimeZone.getTeilnehmerkreise()){
        if(teilnehmer.contains(teilnehmerkreis)){
          if(!this.setReadyOnly.contains(pruefungInTimeZone)) {
            //hier sollte ein Teilnehmerkreis nur einmal dazu addiert werden.
            this.countStudents += pruefungInTimeZone.getSchaetzungen().get(teilnehmerkreis);
          }
          //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
          this.setTeilnehmer.add(teilnehmerkreis);
          this.setReadyOnly.add(new PruefungDTOBuilder(pruefungInTimeZone).build());

          test = true;
        }
      }
    }
    return test;
  }

  private LocalDateTime startDay(LocalDateTime time){
    return LocalDateTime.of(time.getYear(),time.getMonth(),time.getDayOfMonth(),0,0);
  }
  private LocalDateTime endDay(LocalDateTime time){
    return LocalDateTime.of(time.getYear(),time.getMonth(),time.getDayOfMonth(),23,59);
  }

}
