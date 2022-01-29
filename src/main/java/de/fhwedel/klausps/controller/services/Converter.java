package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.controller.util.TeilnehmerkreisUtil;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <ul>
 * <li>Pruefung &rarr; ReadOnlyPruefung</li>
 * <li>Block &rarr; ReadOnlyBlock</li>
 * <li>Collection&lt;Pruefung&gt; &rarr; Collection&lt;ReadOnlyPruefung&gt;</li>
 * <li>Collection&lt;Block&gt; &rarr; Collection&lt;ReadOnlyBlock&gt;</li>
 * </ul>
 */
public class Converter {

  private ScheduleService scheduleService;

  public void setScheduleService(ScheduleService service) {
    scheduleService = service;
  }

  /**
   * Converts the passed Collection of model blocks to Set of DTO ReadOnlyBlock
   * @param collection models blocks
   * @return dto  ro blocks
   * @throws NoPruefungsPeriodeDefinedException when no period is defined
   */
  public Set<ReadOnlyBlock> convertToROBlockSet(
      Collection<Block> collection) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyBlock> result = new HashSet<>();
    for (Block block : collection) {
      result.add(convertToROBlock(block));
    }
    return result;
  }

  /**
   * Converts single model block to DTO ReadOnlyBlock
   * @param block from model
   * @return DTO block RO
   * @throws NoPruefungsPeriodeDefinedException when no period is defined
   */
  public ReadOnlyBlock convertToROBlock(Block block) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>(
        convertToROPruefungSet(block.getPruefungen()));

    return new BlockDTO(block.getName(),
        block.getStartzeitpunkt(),
        block.getDauer(),
        pruefungen,
        block.getId(),
        block.getTyp());
  }

  public Set<ReadOnlyPruefung> convertToROPruefungSet(
      Collection<Pruefung> collection) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> result = new HashSet<>();
    for (Pruefung pruefung : collection) {
      result.add(convertToReadOnlyPruefung(pruefung));
    }
    return result;
  }
 public List<ReadOnlyPruefung> convertToROPruefungList(
      Collection<Pruefung> collection) throws NoPruefungsPeriodeDefinedException {
    List<ReadOnlyPruefung> result = new ArrayList<>(collection.size());
    for (Pruefung pruefung : collection) {
      result.add(convertToReadOnlyPruefung(pruefung));
    }
    return result;
 }

  public Set<ReadOnlyPlanungseinheit> convertToROPlanungseinheitSet(
      Collection<Planungseinheit> collection) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : collection) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  public List<ReadOnlyPlanungseinheit> convertToROPlanungseinheitList(
      Collection<Planungseinheit> collection) throws NoPruefungsPeriodeDefinedException {
    List<ReadOnlyPlanungseinheit> result = new ArrayList<>(collection.size());
    for (Planungseinheit planungseinheit : collection) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  public ReadOnlyPlanungseinheit convertToReadOnlyPlanungseinheit(
      Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    if (planungseinheit.isBlock()) {
      return convertToROBlock(planungseinheit.asBlock());
    } else {
      return convertToReadOnlyPruefung(planungseinheit.asPruefung());
    }
  }

  public ReadOnlyPruefung convertToReadOnlyPruefung(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    return new PruefungDTOBuilder(pruefung).withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }

  public Set<ReadOnlyPlanungseinheit> convertToROPlanungseinheitSet(
      Planungseinheit... planungseinheiten) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  public List<KriteriumsAnalyse> convertAnalyseList(
      List<WeichesKriteriumAnalyse> analysen) throws NoPruefungsPeriodeDefinedException {

    List<KriteriumsAnalyse> result = new LinkedList<>();
    for (WeichesKriteriumAnalyse a : analysen) {
      result.add(convertAnalyse(a));
    }
    return result;
  }

  public KriteriumsAnalyse convertAnalyse(WeichesKriteriumAnalyse analyse)
      throws NoPruefungsPeriodeDefinedException {
    return new KriteriumsAnalyse(
        new HashSet<>(convertToROPruefungSet(analyse.getCausingPruefungen())),
        analyse.getKriterium(), analyse.getAffectedTeilnehmerKreise(),
        analyse.getAmountAffectedStudents());
  }

  public HartesKriteriumException convertHardException(List<HartesKriteriumAnalyse> hard)
      throws NoPruefungsPeriodeDefinedException {

    Set<ReadOnlyPruefung> conflictPruefung = new HashSet<>();
    Set<Teilnehmerkreis> conflictTeilnehmer = new HashSet<>();
    int amountStudents = 0;
    Map<Teilnehmerkreis, Integer> teilnehmercount = new HashMap<>();

    for (HartesKriteriumAnalyse hKA : hard) {

      for (Pruefung pruefung : hKA.getCausingPruefungen()) {
        conflictPruefung.add(convertToReadOnlyPruefung(pruefung));
      }

      TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(teilnehmercount, hKA.getTeilnehmercount());
    }

    for (Integer count : teilnehmercount.values()) {
      amountStudents += count;
    }

    return new HartesKriteriumException(conflictPruefung, conflictTeilnehmer, amountStudents);
  }
}
