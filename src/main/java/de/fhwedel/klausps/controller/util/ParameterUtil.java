package de.fhwedel.klausps.controller.util;

import static java.util.Objects.requireNonNull;

public class ParameterUtil {
  private ParameterUtil() {}
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
}
