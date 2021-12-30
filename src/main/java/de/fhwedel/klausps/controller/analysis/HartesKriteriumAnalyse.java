package de.fhwedel.klausps.controller.analysis;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.HashSet;
import java.util.Set;

public class HartesKriteriumAnalyse {

  Set<Pruefung> causingPruefungen;

  Set<Teilnehmerkreis> affectedTeilnehmerkreise;

  Integer amountAffectedStudents;

  public HartesKriteriumAnalyse(
      Set<Pruefung> causingPruefungen,
      Set<Teilnehmerkreis> affectedTeilnehmerkreise, Integer amountAffectedStudents) {
    this.causingPruefungen = causingPruefungen;
    this.affectedTeilnehmerkreise = affectedTeilnehmerkreise;
    this.amountAffectedStudents = amountAffectedStudents;
  }

  public Set<ReadOnlyPruefung> getCausingPruefungen() {
   //TODO hier nochmal drauf achten ob wirklich ReadOnlyPruefungenzurück gegeben werden sollen
    Set<ReadOnlyPruefung> returnSet = new HashSet<>();
    for(Pruefung p: this.causingPruefungen){
      returnSet.add(new PruefungDTOBuilder(p).build());
    }
    return returnSet;
  }

  public Set<Teilnehmerkreis> getAffectedTeilnehmerkreise() {
    return affectedTeilnehmerkreise;
  }

  public Integer getAmountAffectedStudents() {
    return amountAffectedStudents;
  }
}
