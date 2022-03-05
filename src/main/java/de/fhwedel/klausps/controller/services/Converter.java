package de.fhwedel.klausps.controller.services;


import de.fhwedel.klausps.controller.analysis.HardRestrictionAnalysis;
import de.fhwedel.klausps.controller.analysis.SoftRestrictionAnalysis;
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
 * This class handles all conversion Operations from Controller-internal and model data types to
 * read-only data types intended for view. In case of {@link Pruefung Pruefungen} a scoring will be
 * calculated before conversion, because this information is not stored in model.
 */
public class Converter {

  /**
   * ScheduleService used to calculate the scoring of a {@link Pruefung}
   */
  private ScheduleService scheduleService;

  public void setScheduleService(ScheduleService service) {
    scheduleService = service;
  }

  /**
   * Converts the passed Collection of model blocks to Set of DTO ReadOnlyBlock
   *
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
   *
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

  /**
   * Convert a collection of {@link Pruefung} into a Set of {@link ReadOnlyPruefung}. Adds scoring
   * information for each Pruefung
   *
   * @param collection the collection to convert
   * @return a Set of ReadOnlyPruefungen
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public Set<ReadOnlyPruefung> convertToROPruefungSet(
      Collection<Pruefung> collection) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPruefung> result = new HashSet<>();
    for (Pruefung pruefung : collection) {
      result.add(convertToReadOnlyPruefung(pruefung));
    }
    return result;
  }

  /**
   * Convert a collection of {@link Pruefung} into a List of {@link ReadOnlyPruefung}. Adds scoring
   * information for each Pruefung
   *
   * @param collection the collection to convert
   * @return a List of ReadOnlyPruefungen
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public List<ReadOnlyPruefung> convertToROPruefungList(
      Collection<Pruefung> collection) throws NoPruefungsPeriodeDefinedException {
    List<ReadOnlyPruefung> result = new ArrayList<>(collection.size());
    for (Pruefung pruefung : collection) {
      result.add(convertToReadOnlyPruefung(pruefung));
    }
    return result;
  }

  /**
   * Convert a collection of {@link Planungseinheit} into a Set of {@link ReadOnlyPlanungseinheit}.
   * Adds scoring information for each Pruefung.
   *
   * @param collection the collection to convert
   * @return a Set of ReadOnlyPlanungseinheit
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public Set<ReadOnlyPlanungseinheit> convertToROPlanungseinheitSet(
      Collection<Planungseinheit> collection) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : collection) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  /**
   * Convert a collection of {@link Planungseinheit} into a List of {@link ReadOnlyPlanungseinheit}.
   * Adds scoring information for each Pruefung.
   *
   * @param collection the collection to convert
   * @return a List of ReadOnlyPlanungseinheit
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public List<ReadOnlyPlanungseinheit> convertToROPlanungseinheitList(
      Collection<Planungseinheit> collection) throws NoPruefungsPeriodeDefinedException {
    List<ReadOnlyPlanungseinheit> result = new ArrayList<>(collection.size());
    for (Planungseinheit planungseinheit : collection) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  /**
   * convert a single {@link Planungseinheit} to {@link ReadOnlyPlanungseinheit}. Scoring
   * information is added, if Planungseinheit is a {@link Pruefung}.
   *
   * @param planungseinheit the Planungseinheit to convert
   * @return a read only equivalent of the given planungseinheit
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public ReadOnlyPlanungseinheit convertToReadOnlyPlanungseinheit(
      Planungseinheit planungseinheit) throws NoPruefungsPeriodeDefinedException {
    if (planungseinheit.isBlock()) {
      return convertToROBlock(planungseinheit.asBlock());
    } else {
      return convertToReadOnlyPruefung(planungseinheit.asPruefung());
    }
  }

  /**
   * convert a single {@link Pruefung} to {@link ReadOnlyPruefung}. Scoring information is added.
   *
   * @param pruefung the Pruefung to convert
   * @return a read only equivalent of the given Pruefung
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public ReadOnlyPruefung convertToReadOnlyPruefung(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    return new PruefungDTOBuilder(pruefung)
        .withScoring(scheduleService.scoringOfPruefung(pruefung))
        .build();
  }


  /**
   * Convert an array of {@link Planungseinheit} into a Set of {@link ReadOnlyPlanungseinheit}. Adds
   * scoring information for each Pruefung.
   *
   * @param planungseinheiten the Planungseinheiten to convert
   * @return a Set of ReadOnlyPlanungseinheit
   * @throws NoPruefungsPeriodeDefinedException when no {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public Set<ReadOnlyPlanungseinheit> convertToROPlanungseinheitSet(
      Planungseinheit... planungseinheiten) throws NoPruefungsPeriodeDefinedException {
    Set<ReadOnlyPlanungseinheit> result = new HashSet<>();
    for (Planungseinheit planungseinheit : planungseinheiten) {
      result.add(convertToReadOnlyPlanungseinheit(planungseinheit));
    }
    return result;
  }

  /**
   * Converts a List of internally used {@link SoftRestrictionAnalysis} into a List of {@link
   * KriteriumsAnalyse}.
   *
   * @param analysen the List to convert
   * @return a list of KriteriumsAnalyse
   * @throws NoPruefungsPeriodeDefinedException {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public List<KriteriumsAnalyse> convertAnalyseList(
      List<SoftRestrictionAnalysis> analysen) throws NoPruefungsPeriodeDefinedException {

    List<KriteriumsAnalyse> result = new LinkedList<>();
    for (SoftRestrictionAnalysis a : analysen) {
      result.add(convertAnalyse(a));
    }
    return result;
  }

  /**
   * Converts a single internally used {@link SoftRestrictionAnalysis} into a {@link
   * KriteriumsAnalyse}.
   *
   * @param analyse the analysis to convert
   * @return a corresponding KriteriumsAnalyse
   * @throws NoPruefungsPeriodeDefinedException {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public KriteriumsAnalyse convertAnalyse(SoftRestrictionAnalysis analyse)
      throws NoPruefungsPeriodeDefinedException {
    return new KriteriumsAnalyse(
        new HashSet<>(convertToROPruefungSet(analyse.getAffectedPruefungen())),
        analyse.getKriterium(), analyse.getAffectedTeilnehmerKreise(),
        analyse.getAmountAffectedStudents());
  }

  /**
   * Converts a List of internally used {@link HardRestrictionAnalysis} into an externally used
   * {@link HartesKriteriumException}.
   *
   * @param hard List of HardRestrictionAnalysis to convert
   * @return a HartesKriteriumException
   * @throws NoPruefungsPeriodeDefinedException {@link de.fhwedel.klausps.model.api.Pruefungsperiode
   *                                            Pruefungsperiode} is defined
   */
  public HartesKriteriumException convertHardException(List<HardRestrictionAnalysis> hard)
      throws NoPruefungsPeriodeDefinedException {

    Set<ReadOnlyPruefung> conflictPruefung = new HashSet<>();
    Set<Teilnehmerkreis> conflictTeilnehmer = new HashSet<>();
    int amountStudents = 0;
    Map<Teilnehmerkreis, Integer> teilnehmerCount = new HashMap<>();

    for (HardRestrictionAnalysis hKA : hard) {

      for (Pruefung pruefung : hKA.getConflictingPruefungen()) {
        conflictPruefung.add(convertToReadOnlyPruefung(pruefung));
      }
      conflictTeilnehmer.addAll(hKA.getParticipants().keySet());

      TeilnehmerkreisUtil.compareAndPutBiggerSchaetzung(teilnehmerCount, hKA.getParticipants());
    }

    for (Integer count : teilnehmerCount.values()) {
      amountStudents += count;

    }

    return new HartesKriteriumException(conflictPruefung, conflictTeilnehmer, amountStudents);
  }
}
