package de.fhwedel.klausps.controller.restriction.soft;

import static de.fhwedel.klausps.controller.kriterium.WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.exceptions.NoPruefungsPeriodeDefinedException;
import de.fhwedel.klausps.controller.services.DataAccessService;
import de.fhwedel.klausps.controller.services.ServiceProvider;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class MehrePruefungenAmTag extends WeicheRestriktion {

  static final int START_ZEIT = 8;
  static final int END_ZEIT = 18;
  Set<ReadOnlyPruefung> setReadyOnly = new HashSet<>();
  Set<Pruefung> setPruefung = new HashSet<>();
  Set<Teilnehmerkreis> setTeilnehmer = new HashSet<>();
  int countStudents = 0;
  int scoring = 0;

  protected MehrePruefungenAmTag(DataAccessService dataAccessService) {
    super(dataAccessService, MEHRERE_PRUEFUNGEN_AM_TAG);
  }

  public MehrePruefungenAmTag() {
    this(ServiceProvider.getDataAccessService());
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung)
      throws NoPruefungsPeriodeDefinedException {
    //TODO schön machen
    setReadyOnly = new HashSet<>();
    setPruefung = new HashSet<>();
    setTeilnehmer = new HashSet<>();
    countStudents = 0;
    scoring = 0;

    boolean weichesKrierium = false;
    if (pruefung != null && pruefung.isGeplant()) {

      LocalDateTime start = startDay(pruefung.getStartzeitpunkt());
      LocalDateTime end = endDay(pruefung.getStartzeitpunkt());

      List<Planungseinheit> testList = null;
      try {
        testList = dataAccessService.getAllPlanungseinheitenBetween(start, end);
      } catch (IllegalTimeSpanException e) {
        //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
        e.printStackTrace();
      }
      Set<Pruefung> pruefungenFromBlock;
      for (Planungseinheit planungseinheit : testList) {
        //Unterscheidung auf Block
        if (planungseinheit.isBlock()) {
          pruefungenFromBlock = planungseinheit.asBlock().getPruefungen();
          //Wenn der Block die Pruefung nicht beinhaltet, muss dieser nicht angeguckt werden
          if (!pruefungenFromBlock.contains(pruefung)) {
            // jede Pruefung im Block überprüfen
            for (Pruefung pruefungBlock : pruefungenFromBlock) {
              weichesKrierium =
                  getTeilnehmerkreisFromPruefung(pruefung, pruefungBlock) || weichesKrierium;
            }
          }
        } else {
          weichesKrierium = getTeilnehmerkreisFromPruefung(pruefung, planungseinheit.asPruefung())
              || weichesKrierium;
        }
      }
    }

    return getWeichesKriteriumAnalyse(pruefung, weichesKrierium);
  }

  @NotNull
  private Optional<WeichesKriteriumAnalyse> getWeichesKriteriumAnalyse(Pruefung pruefung,
      boolean conflicted) {
    if (conflicted) {
      this.setReadyOnly.add(new PruefungDTOBuilder(pruefung).build());
      this.setPruefung.add(pruefung);

      scoring +=
          MEHRERE_PRUEFUNGEN_AM_TAG.getWert() * (setPruefung.size() - 2 + 1);

      WeichesKriteriumAnalyse wKA = new WeichesKriteriumAnalyse(this.setPruefung,
          MEHRERE_PRUEFUNGEN_AM_TAG, setTeilnehmer, countStudents);
      return Optional.of(wKA);
    } else {
      return Optional.empty();
    }
  }


  private boolean getTeilnehmerkreisFromPruefung(Pruefung pruefung, Pruefung toCheck) {
    boolean retBool = false;
    Set<Teilnehmerkreis> teilnehmer = pruefung.getTeilnehmerkreise();
    for (Teilnehmerkreis teilnehmerkreis : toCheck.getTeilnehmerkreise()) {
      if (teilnehmer.contains(teilnehmerkreis)) {
        if (!setTeilnehmer.contains(teilnehmerkreis)) {
          //hier sollte ein Teilnehmerkreis nur einmal dazu addiert werden.
          this.countStudents += toCheck.getSchaetzungen().get(teilnehmerkreis);
        }
        //Hier ist es egal, da es ein Set ist und es nur einmal vorkommen darf
        this.setTeilnehmer.add(teilnehmerkreis);
        this.setReadyOnly.add(new PruefungDTOBuilder(toCheck).build());
        this.setPruefung.add(toCheck);
        retBool = true;
      }
    }
    return retBool;
  }

  private LocalDateTime startDay(LocalDateTime time) {
    return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), START_ZEIT, 0);
  }

  private LocalDateTime endDay(LocalDateTime time) {
    return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), END_ZEIT, 0);
  }

  @Override
  protected int addDeltaScoring(Set<Pruefung> affectedPruefungen) {
    throw new UnsupportedOperationException("not implemented");
  }

}
