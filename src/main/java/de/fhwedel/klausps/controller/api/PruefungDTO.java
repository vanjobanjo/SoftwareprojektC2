package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;

public class PruefungDTO extends ReadOnlyPruefung {

  public PruefungDTO(@NotNull String pruefungsnummer, @NotNull String name,
      @NotNull LocalDateTime termin, @NotNull Duration dauer,
      @NotNull Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung,
      @NotNull Set<String> pruefer, int scoring) {
    super(pruefungsnummer, name, termin, dauer, teilnehmerkreisSchaetzung, pruefer, scoring);
  }

  public PruefungDTO(@NotNull String pruefungsnummer, @NotNull String name, @NotNull Duration dauer,
      @NotNull Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung,
      @NotNull Set<String> pruefer, int scoring) {
    super(pruefungsnummer, name, null, dauer, teilnehmerkreisSchaetzung, pruefer, scoring);
  }

  public PruefungDTO(@NotNull String pruefungsnummer, @NotNull String name,
      @NotNull LocalDateTime termin, @NotNull Duration dauer,
      @NotNull Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung, @NotNull String pruefer,
      int scoring) {
    super(pruefungsnummer, name, termin, dauer, teilnehmerkreisSchaetzung, Set.of(pruefer),
        scoring);
  }

  public PruefungDTO(@NotNull String pruefungsnummer, @NotNull String name, @NotNull Duration dauer,
      @NotNull Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung, @NotNull String pruefer,
      int scoring) {
    super(pruefungsnummer, name, null, dauer, teilnehmerkreisSchaetzung, Set.of(pruefer), scoring);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PruefungDTO.class.getSimpleName() + "[" + this.getName(), "]")
        .toString();
  }
}
