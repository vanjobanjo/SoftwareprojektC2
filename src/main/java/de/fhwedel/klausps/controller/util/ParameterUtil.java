package de.fhwedel.klausps.controller.util;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public class ParameterUtil {

  private ParameterUtil() {
  }

  /**
   * In case any parameter is null, immediately throw a NullPointer exception
   *
   * @param objects The parameters to check.
   * @throws NullPointerException In case any of the parameters is null.
   */
  public static void noNullParameters(Object... objects) throws NullPointerException {
    for (Object parameter : objects) {
      requireNonNull(parameter);
    }
  }

  /**
   * Checks for empty strings
   * @param strings to check
   */
  public static void noEmptyStrings(String... strings) {
    for (String parameter : strings) {
      if (Objects.equals(parameter, "")) {
        String message = String.format("%s was illegally called with an empty string.",
            Thread.currentThread().getStackTrace()[1].getMethodName());
        throw new IllegalArgumentException(message);
      }
    }
  }

}
