package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.controller.api.PruefungDTO;
import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class TestUtils {

  public static List<ReadOnlyPruefung> getRandomPruefungenReadOnly(long seed, int amount) {
    Random random = new Random(seed);
    List<ReadOnlyPruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(new PruefungDTOBuilder().withPruefungsName(getRandomString(random, 5))
          .withDauer(getRandomDuration(random, 120)).withPruefungsNummer(getRandomString(random, 4))
          .build());
    }
    return randomPruefungen;
  }

  public static String getRandomString(Random random, int length) {
    // code from https://www.baeldung.com/java-random-string modified
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
      buffer.append((char) randomLimitedInt);
    }
    return buffer.toString();
  }

  public static Duration getRandomDuration(Random random, int maxMinutes) {
    return Duration.ofMinutes(random.nextInt(1, maxMinutes));
  }

  public static Pruefung getRandomUnplannedPruefung(long seed) {
    return getRandomPruefungen(seed, 1).get(0);
  }

  public static List<Pruefung> getRandomPruefungen(long seed, int amount) {
    Random random = new Random(seed);
    List<Pruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(new PruefungImpl(getRandomString(random, 5), getRandomString(random, 5),
          getRandomString(random, 5), getRandomDuration(random, 120)));
    }
    return randomPruefungen;
  }

  public static Pruefung getRandomPruefung(LocalDateTime from, LocalDateTime to, long seed) {
    Random random = new Random(seed);
    return new PruefungImpl(getRandomString(random, 5), getRandomString(random, 5),
        getRandomString(random, 5), Duration.between(from, to), from);
  }

  public static List<Pruefung> getRandomPruefungenAt(long seed, LocalDateTime... schedules) {
    Random random = new Random(seed);
    List<Pruefung> randomPruefungen = new ArrayList<>(schedules.length);
    for (LocalDateTime schedule : schedules) {
      randomPruefungen.add(new PruefungImpl(getRandomString(random, 5), getRandomString(random, 5),
          getRandomString(random, 5), getRandomDuration(random, 120), schedule));
    }
    return randomPruefungen;
  }

  public static List<Planungseinheit> convertPruefungenToPlanungseinheiten(
      List<Pruefung> pruefungen) {
    return new ArrayList<>(pruefungen);
  }

  public static Set<String> getPruefungsnummernFromDTO(Collection<ReadOnlyPruefung> pruefungen) {
    return pruefungen.stream().map(ReadOnlyPruefung::getPruefungsnummer)
        .collect(Collectors.toUnmodifiableSet());
  }

  public static Set<String> getPruefungsnummernFromModel(Collection<Pruefung> pruefungen) {
    return pruefungen.stream().map(Pruefung::getPruefungsnummer)
        .collect(Collectors.toUnmodifiableSet());
  }

  public static Teilnehmerkreis getRandomTeilnehmerkreis(long seed) {
    Random random = new Random(seed);
    return new TeilnehmerkreisImpl(getRandomString(random, 10), getRandomString(random, 5),
        random.nextInt(11),
        Ausbildungsgrad.values()[random.nextInt(Ausbildungsgrad.values().length)]);
  }

  public static List<Teilnehmerkreis> getRandomTeilnehmerkreise(long seed, int amount) {
    List<Teilnehmerkreis> result = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      result.add(getRandomTeilnehmerkreis(seed++));
    }
    return result;
  }

  public static Pruefung getRandomPruefungWith(long seed, Teilnehmerkreis... teilnehmerkreise) {
    Random random = new Random(seed);
    Pruefung result = getRandomPlannedPruefung(seed);
    for (Teilnehmerkreis teilnehmerkreis : teilnehmerkreise) {
      result.addTeilnehmerkreis(teilnehmerkreis, random.nextInt(1, Integer.MAX_VALUE));
    }
    return result;
  }

  public static Pruefung getRandomPruefungWith(long seed,
      Collection<Teilnehmerkreis> teilnehmerkreise) {
    Random random = new Random(seed);
    Pruefung result = getRandomPlannedPruefung(seed);
    for (Teilnehmerkreis teilnehmerkreis : teilnehmerkreise) {
      result.addTeilnehmerkreis(teilnehmerkreis, random.nextInt(1, Integer.MAX_VALUE));
    }
    return result;
  }

  public static Pruefung getRandomPlannedPruefung(long seed) {
    return getRandomPlannedPruefungen(seed, 1).get(0);
  }

  public static List<Pruefung> getRandomPlannedPruefungen(long seed, int amount) {
    Random random = new Random(seed);
    List<Pruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(new PruefungImpl(getRandomString(random, 5), getRandomString(random, 5),
          getRandomString(random, 5), getRandomDuration(random, 120),
          LocalDateTime.of(2021, 12, 29, 10, 0)));
    }
    return randomPruefungen;
  }

  public static PruefungDTO getRandomPlannedROPruefung(long seed) {
    Random random = new Random(seed);
    return new PruefungDTOBuilder().withPruefungsName(getRandomString(random, 8))
        .withPruefungsNummer(getRandomString(random, 20))
        .withDauer(Duration.ofMinutes(random.nextInt(60, 150)))
        .withStartZeitpunkt(getRandomTime(seed)).withAdditionalPruefer(getRandomString(random, 5))
        .withScoring(random.nextInt(0, Integer.MAX_VALUE)).build();
  }

  public static LocalDateTime getRandomTime(long seed) {
    Random random = new Random(seed);
    return LocalDateTime.of(random.nextInt(2000, 2030), random.nextInt(1, 12),
        random.nextInt(1, 28), random.nextInt(0, 20), random.nextInt(0, 60));
  }

  public static LocalDate getRandomDate(long seed) {
    Random random = new Random(seed);
    return LocalDate.of(random.nextInt(2000, 2030), random.nextInt(1, 12),
        random.nextInt(1, 28));
  }

  public static ReadOnlyPruefung getRandomUnplannedROPruefung(long seed) {
    return getRandomUnplannedROPruefungen(seed, 1).get(0);
  }

  public static List<ReadOnlyPruefung> getRandomUnplannedROPruefungen(long seed, int amount) {
    Random random = new Random(seed);
    List<ReadOnlyPruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(new PruefungDTOBuilder().withPruefungsName(getRandomString(random, 5))
          .withDauer(getRandomDuration(random, 120)).withPruefungsNummer(getRandomString(random, 4))
          .build());
    }
    return randomPruefungen;
  }

}
