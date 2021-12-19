package de.fhwedel.klausps.controller.restriction.soft;

import static org.junit.jupiter.api.Assertions.*;

import de.fhwedel.klausps.controller.services.DataAccessService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

class AnzahlPruefungenGleichzeitigTest {

  @Mock
  public DataAccessService dataAccessService;

  @InjectMocks
  public AnzahlPruefungenGleichzeitig deviceUnderTest;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void worksTest() {
    deviceUnderTest.toString();
    System.out.println("coolio");
  }

}
