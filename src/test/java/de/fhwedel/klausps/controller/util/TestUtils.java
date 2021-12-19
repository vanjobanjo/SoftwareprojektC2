package de.fhwedel.klausps.controller.util;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Pruefung;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

  public static List<Pruefung> getRandomPruefungen(long seed, int amount) {
    Random random = new Random(seed);
    List<Pruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(new PruefungImpl(getRandomString(random, 5), getRandomString(random, 5),
          getRandomString(random, 5), getRandomDuration(random, 120)));
    }
    return randomPruefungen;
  }

  public static List<Pruefung> getRandomPlannedPruefungen(long seed, int amount) {
    Random random = new Random(seed);
    List<Pruefung> randomPruefungen = new ArrayList<>(amount);
    for (int index = 0; index < amount; index++) {
      randomPruefungen.add(new PruefungImpl(getRandomString(random, 5), getRandomString(random, 5),
          getRandomString(random, 5), getRandomDuration(random, 120), LocalDateTime.now()));
    }
    return randomPruefungen;
  }

  public static Pruefung getRandomPruefung(long seed) {
    return getRandomPruefungen(seed, 1).get(0);
  }

  public static Pruefung getRandomPlannedPruefung(long seed) {
    return getRandomPlannedPruefungen(seed, 1).get(0);
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
}
