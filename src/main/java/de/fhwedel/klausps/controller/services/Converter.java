package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.analysis.HartesKriteriumAnalyse;
import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.BlockDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyBlock;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPlanungseinheit;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.HartesKriteriumException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

  public ReadOnlyBlock convertToROBlock(Block block) {
    Set<ReadOnlyPruefung> pruefungen = new HashSet<>(
        convertToROPruefungCollection(block.getPruefungen()));

    return new BlockDTO(block.getName(),
        block.getStartzeitpunkt(),
        block.getDauer(),
        pruefungen,
        block.getId(),
        block.getTyp());
  }

  public ReadOnlyPruefung convertToReadOnlyPruefung(Pruefung pruefung) {
    return new PruefungDTOBuilder(pruefung).withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }

  public ReadOnlyPlanungseinheit convertToReadOnlyPlanungseinheit(
      Planungseinheit planungseinheit) {
    if (planungseinheit.isBlock()) {
      return convertToROBlock(planungseinheit.asBlock());
    } else {
      return convertToReadOnlyPruefung(planungseinheit.asPruefung());
    }
  }

  public Collection<ReadOnlyPruefung> convertToROPruefungCollection(
      Collection<Pruefung> collection) {
    Collection<ReadOnlyPruefung> result = new HashSet<>();
    for (Pruefung pruefung : collection) {
      result.add(convertToReadOnlyPruefung(pruefung));
    }
    return result;
  }

  public Collection<ReadOnlyBlock> convertToROBlockCollection(
      Collection<Block> collection) {
    Collection<ReadOnlyBlock> result = new HashSet<>();
    for (Block block : collection) {
      result.add(convertToROBlock(block));
    }
    return result;
  }

  public Collection<ReadOnlyPlanungseinheit> convertToROPlanungseinheitCollection(
      Collection<Planungseinheit> collection) {
    Collection<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : collection) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  public Collection<ReadOnlyPlanungseinheit> convertToROPlanungseinheitCollection(
      Planungseinheit... planungseinheiten) {
    Collection<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  public List<KriteriumsAnalyse> convertAnalyseScoring(
      List<WeichesKriteriumAnalyse> analysen) {

    List<KriteriumsAnalyse> list = new LinkedList<>();
    for (WeichesKriteriumAnalyse a : analysen) {
      list.add(convertAnalyse(a));
    }
    return list;
  }

  public KriteriumsAnalyse convertAnalyse(WeichesKriteriumAnalyse analyse) {
    return new KriteriumsAnalyse(
        new HashSet<>(convertToROPruefungCollection(analyse.getCausingPruefungen())),
        analyse.getKriterium(), analyse.getAffectedTeilnehmerKreise(),
        analyse.getAmountAffectedStudents());
  }

  public HartesKriteriumException convertHardException(List<HartesKriteriumAnalyse> hard) {

    Set<ReadOnlyPruefung> conflictPruefung = new HashSet<>();
    Set<Teilnehmerkreis> conflictTeilnehmer = new HashSet<>();
    int amountStudents = 0;

    for (HartesKriteriumAnalyse hKA : hard) {

      for (Pruefung pruefung : hKA.getCausingPruefungen()) {
        conflictPruefung.add(convertToReadOnlyPruefung(pruefung));
      }
      for (Teilnehmerkreis teilnehmerkreis : hKA.getAffectedTeilnehmerkreise()) {
        if (!conflictTeilnehmer.contains(teilnehmerkreis)) {
          conflictTeilnehmer.add(teilnehmerkreis);
          amountStudents += hKA.getAmountAffectedStudents();
        }
      }
    }

    return new HartesKriteriumException(conflictPruefung, conflictTeilnehmer, amountStudents);
  }
}
