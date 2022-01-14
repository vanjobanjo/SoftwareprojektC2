package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH;

import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AnzahlPruefungenGleichzeitigRestriktion extends AtSameTimeRestriction {

  protected static final int DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME = 6;

  private final int maxPruefungenAtATime;

  public AnzahlPruefungenGleichzeitigRestriktion() {
    this(ServiceProvider.getDataAccessService());
  }

  protected AnzahlPruefungenGleichzeitigRestriktion(@NotNull DataAccessService dataAccessService) {
    this(dataAccessService, DEFAULT_MAX_PRUEFUNGEN_AT_A_TIME,
        DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN);
  }

  protected AnzahlPruefungenGleichzeitigRestriktion(@NotNull DataAccessService dataAccessService,
      int maxPruefungenAtATime, @NotNull Duration puffer) {
    super(dataAccessService, ANZAHL_PRUEFUNGEN_GLEICHZEITIG_ZU_HOCH, puffer);
    this.maxPruefungenAtATime = maxPruefungenAtATime;
  }

  protected AnzahlPruefungenGleichzeitigRestriktion(@NotNull DataAccessService dataAccessService,
      int maxPruefungenAtATime) {
    this(dataAccessService, maxPruefungenAtATime, DEFAULT_BUFFER_BETWEEN_PLANUNGSEINHEITEN);
  }

  @Override
  protected void ignorePruefungenOf(@NotNull List<Planungseinheit> planungseinheiten,
      @NotNull Pruefung toFilterFor) throws NoPruefungsPeriodeDefinedException {
    Pruefung pruefung = toFilterFor.asPruefung();
    Optional<Block> block = dataAccessService.getBlockTo(pruefung);
    if (block.isPresent()) {
      planungseinheiten.removeAll(block.get().getPruefungen());
      planungseinheiten.remove(block.get());
      planungseinheiten.add(toFilterFor);
    }
  }

  @Override
  protected boolean violatesRestriction(Collection<Planungseinheit> planungseinheiten) {
    return planungseinheiten.size() > maxPruefungenAtATime;
  }

  @Override
  protected Set<Teilnehmerkreis> getAffectedTeilnehmerkreiseFrom(
      Set<Planungseinheit> violatingPlanungseinheiten) {
    Set<Teilnehmerkreis> teilnehmerkreise = new HashSet<>();
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      teilnehmerkreise.addAll(planungseinheit.getTeilnehmerkreise());
    }
    return teilnehmerkreise;
  }

  @Override
  protected int getAffectedStudentsFrom(Set<Planungseinheit> violatingPlanungseinheiten) {
    HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis = new HashMap<>();
    for (Planungseinheit planungseinheit : violatingPlanungseinheiten) {
      collectMaxAmountOfStudentsInFor(maxTeilnehmerPerTeilnehmerkreis, planungseinheit);
    }
    return getSumm(maxTeilnehmerPerTeilnehmerkreis.values());
  }

  @Override
  protected int calcScoringFor(Set<Planungseinheit> violatingPlanungseinheiten) {
    int scoring = 0;
    if (violatingPlanungseinheiten.size() > maxPruefungenAtATime) {
      scoring = violatingPlanungseinheiten.size() - maxPruefungenAtATime;
      scoring *= this.kriterium.getWert();
    }
    return scoring;
  }

  private void collectMaxAmountOfStudentsInFor(
      HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis,
      Planungseinheit planungseinheit) {
    for (Map.Entry<Teilnehmerkreis, Integer> entry : planungseinheit.getSchaetzungen().entrySet()) {
      if (isNotContainedWithHigherValueIn(maxTeilnehmerPerTeilnehmerkreis, entry)) {
        maxTeilnehmerPerTeilnehmerkreis.put(entry.getKey(), entry.getValue());
      }
    }
  }

  private int getSumm(@NotNull Iterable<Integer> values) {
    int result = 0;
    for (Integer value : values) {
      result += value;
    }
    return result;
  }

  private boolean isNotContainedWithHigherValueIn(
      HashMap<Teilnehmerkreis, Integer> maxTeilnehmerPerTeilnehmerkreis,
      Entry<Teilnehmerkreis, Integer> entry) {
    return !maxTeilnehmerPerTeilnehmerkreis.containsKey(entry.getKey())
        || maxTeilnehmerPerTeilnehmerkreis.get(entry.getKey()) <= entry.getValue();
  }
}
