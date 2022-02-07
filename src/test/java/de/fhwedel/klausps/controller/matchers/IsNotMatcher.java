package de.fhwedel.klausps.controller.matchers;

import org.mockito.ArgumentMatcher;

public class IsNotMatcher<T> implements ArgumentMatcher<T> {

  private final T unexpected;

  public IsNotMatcher(T unexpected) {
    this.unexpected = unexpected;
  }

  @Override
  public boolean matches(T actual) {
    return !unexpected.equals(actual);
  }
}
