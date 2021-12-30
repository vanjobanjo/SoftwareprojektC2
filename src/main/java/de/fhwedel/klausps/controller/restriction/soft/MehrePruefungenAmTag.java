package de.fhwedel.klausps.controller.restriction.soft;

import de.fhwedel.klausps.controller.analysis.WeichesKriteriumAnalyse;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.controller.exceptions.IllegalTimeSpanException;
import de.fhwedel.klausps.controller.kriterium.KriteriumsAnalyse;
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
import java.util.function.Predicate;

public class MehrePruefungenAmTag extends WeicheRestriktion implements Predicate<Pruefung> {

  static final int START_ZEIT = 8;
  static final int END_ZEIT = 18;
  Set<ReadOnlyPruefung> setReadyOnly = new HashSet<>();
  Set<Pruefung> setPruefung = new HashSet<>();
  Set<Teilnehmerkreis> setTeilnehmer = new HashSet<>();
  int countStudents = 0;
  int scoring = 0;
  KriteriumsAnalyse kA = new KriteriumsAnalyse(setReadyOnly,
      WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG, setTeilnehmer, countStudents);

  protected MehrePruefungenAmTag(DataAccessService dataAccessService, WeichesKriterium kriterium) {
    super(dataAccessService, kriterium);
  }

  @Override
  public Optional<WeichesKriteriumAnalyse> evaluate(Pruefung pruefung) {
    boolean weichesKrierium = false;

    LocalDateTime start = startDay(pruefung.getStartzeitpunkt());
    LocalDateTime end = endDay(pruefung.getStartzeitpunkt());

    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPruefungenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
      e.printStackTrace();
    }
    Set<Pruefung> pruefungenFromBlock;
    // TODO wieso wird überprüft, ob ein Ergebnis von "getAllPruefungenBetween" ein Block ist,
    //  die Methode sorgt ganz explizit dafür, dass die Klausuren in den Blöcken statt der Blöcke
    //  selbst returned werden
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

    if (weichesKrierium) {
      this.setReadyOnly.add(new PruefungDTOBuilder(pruefung).build());
      this.setPruefung.add(pruefung);

      scoring +=
          WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG.getWert() * (setPruefung.size() - 2 + 1);
      WeichesKriteriumAnalyse wKA = new WeichesKriteriumAnalyse(this.setPruefung,
          WeichesKriterium.MEHRERE_PRUEFUNGEN_AM_TAG, setTeilnehmer, countStudents);
      return Optional.of(wKA);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean test(Pruefung pruefung) {
    boolean weichesKrierium = false;

    LocalDateTime start = startDay(pruefung.getStartzeitpunkt());
    LocalDateTime end = endDay(pruefung.getStartzeitpunkt());

    List<Planungseinheit> testList = null;
    try {
      testList = dataAccessService.getAllPruefungenBetween(start, end);
    } catch (IllegalTimeSpanException e) {
      //Kann nicht davor liegen, da ich den Morgen und den Abend nehme
      e.printStackTrace();
    }
    Set<Pruefung> pruefungenFromBlock;
    // TODO wieso wird überprüft, ob ein Ergebnis von "getAllPruefungenBetween" ein Block ist,
    //  die Methode sorgt ganz explizit dafür, dass die Klausuren in den Blöcken statt der Blöcke
    //  selbst returned werden
    for (Planungseinheit planungseinheit : testList) {
      if (planungseinheit.isBlock()) {
        pruefungenFromBlock = planungseinheit.asBlock().getPruefungen();
        if (!pruefungenFromBlock.contains(pruefung)) {
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
    if (weichesKrierium) {
      scoring += 10;
      this.setPruefung.add(pruefung);
      this.setReadyOnly.add(new PruefungDTOBuilder(pruefung).build());
    }

    return weichesKrierium;
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

}
