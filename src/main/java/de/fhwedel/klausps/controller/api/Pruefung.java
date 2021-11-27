package de.fhwedel.klausps.controller.api;


import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Pruefung implements ReadOnlyPruefung {
  private final String name;
  private final String pruefungsNr;
  private final Set<String> pruefer;
  private final Duration duration;
  private final Map<Teilnehmerkreis, Integer> teilnehmerkreis;


  public Pruefung(String name, String pruefungsNr,
      String pruefer, Duration duration, Map<Teilnehmerkreis, Integer> teilnehmerkreis) {
    this.name = name;
    this.pruefungsNr = pruefungsNr;
    this.pruefer = new HashSet<>();
    this.pruefer.add(pruefer);
    this.duration = duration;
    this.teilnehmerkreis = new HashMap<>(teilnehmerkreis);
  }

  @Override
  public String getPruefungsnummer() {
    return this.pruefungsNr;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean geplant() {
    return false;
  }

  @Override
  public boolean ungeplant() {
    return false;
  }

  @Override
  public Optional<ReadOnlyBlock> getBlock() {
    return Optional.empty();
  }

  @Override
  public Optional<LocalDateTime> getTermin() {
    return Optional.empty();
  }

  @Override
  public Duration getDauer() {
    return this.duration;
  }

  @Override
  public int getGesamtSchaetzung() {
    return 0;
  }

  @Override
  public Map<Teilnehmerkreis, Integer> getTeilnehmerKreisSchaetzung() {
    return null;
  }


  public int getSchaetzung() {
    return 0;
  }

  @Override
  public Set<String> getPruefer() {
    return new HashSet<>(pruefer);
  }

  @Override
  public Set<Teilnehmerkreis> getTeilnehmerkreise() {
    return new HashSet<>(teilnehmerkreis.keySet());
  }

  @Override
  public boolean isBlock() {
    return false;
  }

  /************************************************************/
  public void addTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis, Integer schaetzung){
    //TODO 0 noch in ein richtigen Wert ändern
    this.teilnehmerkreis.put(teilnehmerkreis,schaetzung);
  }
  public void removeTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis){
    this.teilnehmerkreis.remove(teilnehmerkreis);
  }
}

