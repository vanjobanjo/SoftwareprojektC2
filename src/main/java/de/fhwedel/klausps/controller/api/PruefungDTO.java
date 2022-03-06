package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;

/**
 * Data transfer Object representing {@link ReadOnlyPruefung}.
 */
public class PruefungDTO extends ReadOnlyPruefung {

  /**
   * Create a data transfer object for {@link Pruefung Pruefungen}.
   *
   * @param pruefungsnummer           The unique id to identify a pruefung by.
   * @param name                      The name of the pruefung.
   * @param termin                    The moment the pruefung is scheduled at.
   * @param dauer                     The duration of the pruefung.
   * @param teilnehmerkreisSchaetzung The participants of the pruefung with their amounts.
   * @param pruefer                   The professors conducting the pruefung.
   * @param scoring                   The scoring describing the quality of the pruefungs
   *                                  positioning.
   */
  public PruefungDTO(@NotNull String pruefungsnummer, @NotNull String name,
      @NotNull LocalDateTime termin, @NotNull Duration dauer,
      @NotNull Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung,
      @NotNull Set<String> pruefer, int scoring) {
    super(pruefungsnummer, name, termin, dauer, teilnehmerkreisSchaetzung, pruefer, scoring);
  }

  /**
   * Create a data transfer object for {@link Pruefung Pruefungen}.
   *
   * @param pruefungsnummer           The unique id to identify a pruefung by.
   * @param name                      The name of the pruefung.
   * @param dauer                     The duration of the pruefung.
   * @param teilnehmerkreisSchaetzung The participants of the pruefung with their amounts.
   * @param pruefer                   The professors conducting the pruefung.
   * @param scoring                   The scoring describing the quality of the pruefungs
   *                                  positioning.
   */
  public PruefungDTO(@NotNull String pruefungsnummer, @NotNull String name, @NotNull Duration dauer,
      @NotNull Map<Teilnehmerkreis, Integer> teilnehmerkreisSchaetzung,
      @NotNull Set<String> pruefer, int scoring) {
    super(pruefungsnummer, name, null, dauer, teilnehmerkreisSchaetzung, pruefer, scoring);
  }

  @Override
  public String toString() {
    return new StringJoiner(":", "[", "]")
        .add(this.getName())
        .add(this.getPruefungsnummer())
        .toString();
  }
}
