package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.PlanungseinheitUtil.getAllPruefungen;
import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.WeichesKriterium;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AnzahlPruefungenGleichzeitigRestriktion extends WeicheRestriktion {

  private static final WeichesKriterium KRITERIUM = ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

  private static final int DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME = 6;

  private final int maxPruefungenAtATime;

  protected AnzahlPruefungenGleichzeitigRestriktion(DataAccessService dataAccessService) {
    this(dataAccessService, DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME);
  }

  protected AnzahlPruefungenGleichzeitigRestriktion(DataAccessService dataAccessService,
      int maxPruefungenAtATime) {
    super(dataAccessService, KRITERIUM);
    this.maxPruefungenAtATime = maxPruefungenAtATime;
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(@NotNull Pruefung pruefung) {
    if (pruefung.isGeplant()) {
      LocalDateTime pruefungsEnde = pruefung.getStartzeitpunkt().plus(pruefung.getDauer());
      // TODO result is not nessesary simultaneous, just at some time during the specified exam
      List<Planungseinheit> simultaneousPlanungseinheiten = tryToGetSimultaneousPlanungseinheiten(
          pruefung.getStartzeitpunkt(), pruefungsEnde);

      if (simultaneousPlanungseinheiten.size() > maxPruefungenAtATime) {

        return Optional.of(
            new WeichesKriteriumAnalyse(getAllPruefungen(simultaneousPlanungseinheiten), KRITERIUM,
                getAllTeilnehmerkreiseFrom(simultaneousPlanungseinheiten),
                getAmountAffectedStudents(simultaneousPlanungseinheiten)));
      }
    }
    return Optional.empty();
  }

  private List<Planungseinheit> tryToGetSimultaneousPlanungseinheiten(LocalDateTime from,
      LocalDateTime to) {
    try {
      return dataAccessService.getAllPruefungenBetween(from, to);
    } catch (IllegalTimeSpanException e) {
      // can never happen, as the duration of a pruefung is checked to be > 0
      throw new IllegalStateException("A Pruefung with a negative duration can not exist.", e);
    }
  }

  private Set<Teilnehmerkreis> getAllTeilnehmerkreiseFrom(
      Iterable<Planungseinheit> planungseinheiten) {
    Set<Teilnehmerkreis> teilnehmerkreise = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      teilnehmerkreise.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return teilnehmerkreise;
  }

  private int getAmountAffectedStudents(Iterable<Planungseinheit> planungseinheiten) {
    // TODO calculate affected students
    return 0;
  }

}
