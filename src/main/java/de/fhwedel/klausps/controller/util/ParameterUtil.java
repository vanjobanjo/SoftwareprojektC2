package de.fhwedel.klausps.controller.util;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * This class contains utility concerning the {@link de.fhwedel.klausps.controller.Controller
 * Controller Interface} conformity of method parameters.
 */
public class ParameterUtil {

  /**
   * Changes visibility of the default constructor to private because utility classes containing
   * only static methods should not be instantiated.
   */
  private ParameterUtil() {
  }

  /**
   * In case any parameter is null, immediately throw a {@link IllegalArgumentException}
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
   * In case a String parameter is an empty String, immediately throw an {@link
   * IllegalArgumentException}
   *
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
