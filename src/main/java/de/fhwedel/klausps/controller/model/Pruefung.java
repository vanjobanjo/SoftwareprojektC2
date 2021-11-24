package de.fhwedel.klausps.controller.model;

import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Pruefung implements de.fhwedel.klausps.model.api.Pruefung {

  private String pruefungsnummer;
  private String name;
  private Duration dauer;
  private int schaetzung;
  private final Set<String> pruefer;
  private final Set<Teilnehmerkreis> teilnehmerkreise;
  private LocalDateTime start;
  private LocalDateTime end;

  public Pruefung(String pruefungsnummer, String name, Duration dauer,
      int schaetzung, Set<String> pruefer,
      Set<Teilnehmerkreis> teilnehmerkreis, LocalDateTime start) {
    this.pruefungsnummer = pruefungsnummer;
    this.name = name;
    this.dauer = dauer;
    this.schaetzung = schaetzung;
    this.start = start;
    this.end = start.plus(dauer);
    this.pruefer = new HashSet<>(pruefer);
    this.teilnehmerkreise = new HashSet<>(teilnehmerkreis);
  }


  @Override
  public String getPruefungsnummer() {
    return pruefungsnummer;
  }

  @Override
  public void setPruefungsnummer(String s) {
    pruefungsnummer = s;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String s) {
    this.name = s;
  }

  @Override
  public String getReferenzVerwaltungsystem() {
    return null;
  }

  @Override
  public void setDauer(Duration duration) {
    this.dauer = duration;
  }

  @Override
  public void setSchaetzung(int i) {
    this.schaetzung = i;
  }

  @Override
  public void addPruefer(String s) {
    pruefer.add(s);
  }

  @Override
  public void removePruefer(String s) {
    pruefer.remove(s);
  }

  @Override
  public void addTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis) {
    teilnehmerkreise.add(teilnehmerkreis);
  }

  @Override
  public void removeTeilnehmerkreis(Teilnehmerkreis teilnehmerkreis) {
    teilnehmerkreise.remove(teilnehmerkreis);
  }

  @Override
  public LocalDateTime getStartzeitpunkt() {
    return start;
  }

  @Override
  public void setStartzeitpunkt(LocalDateTime localDateTime) {
    start = localDateTime;
    end = start.plus(dauer);
  }

  @Override
  public Duration getDauer() {
    return dauer;
  }

  @Override
  public int getSchaetzung() {
    return schaetzung;
  }

  @Override
  public Set<String> getPruefer() {
    return new HashSet<>(pruefer);
  }

  @Override
  public Set<Teilnehmerkreis> getTeilnehmerkreise() {
    return new HashSet<>(teilnehmerkreise);
  }

  @Override
  public boolean isBlock() {
    return false;
  }

  @Override
  public boolean isGeplant() {
    return start != null;
  }

  @Override
  public LocalDateTime endzeitpunkt() {
    return end;
  }
}
