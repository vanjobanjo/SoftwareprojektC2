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

public class PruefungDTO implements ReadOnlyPruefung {
  private final String name;
  private final String pruefungsNr;
  private final Set<String> pruefer;
  private final Duration duration;
  private final Set<Teilnehmerkreis> teilnehmerkreise;
  private final Integer teilnehmerSchatzung;

  public PruefungDTO(String name, String pruefungsNr,
                     String pruefer, Duration duration, Set<Teilnehmerkreis> teilnehmerkreise, Integer teilnehmerSchatzung) {
    this.name = name;
    this.pruefungsNr = pruefungsNr;
    this.pruefer = new HashSet<>();
    this.pruefer.add(pruefer);
    this.duration = duration;
    this.teilnehmerkreise = new HashSet<>(teilnehmerkreise);
    this.teilnehmerSchatzung = teilnehmerSchatzung;
  }

  public PruefungDTO(String name, String pruefungsNr,
                     Set<String> pruefer, Duration duration, Set<Teilnehmerkreis> teilnehmerkreise, Integer teilnehmerSchatzung) {
    this.name = name;
    this.pruefungsNr = pruefungsNr;
    this.pruefer = new HashSet<>();
    this.pruefer.addAll(pruefer);
    this.duration = duration;
    this.teilnehmerkreise = new HashSet<>(teilnehmerkreise);
    this.teilnehmerSchatzung = teilnehmerSchatzung;
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
    return new HashMap<>();  // TODO is the interface correct in returning a map? It should only return one integer
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





}
