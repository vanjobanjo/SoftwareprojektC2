package de.fhwedel.klausps.controller.matchers;

import java.util.Collection;
import org.mockito.ArgumentMatcher;

public class IsOneOfMatcher<T> implements ArgumentMatcher<T> {

  private final Collection<T> expected;

  public IsOneOfMatcher(Collection<T> expected) {
    this.expected = expected;
  }

  @Override
  public boolean matches(T actual) {
    return expected.contains(actual);
  }
}
