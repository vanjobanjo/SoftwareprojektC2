package de.fhwedel.klausps.controller.util;

import java.time.Duration;
import java.util.Random;

public class TestUtils {

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
