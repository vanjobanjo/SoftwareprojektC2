package de.fhwedel.klausps.controller.services;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Planungseinheit;
import de.fhwedel.klausps.model.api.Pruefungsperiode;
import de.fhwedel.klausps.model.api.Teilnehmerkreis;
import de.fhwedel.klausps.model.impl.PruefungImpl;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataAccessServiceTest {

  String pruefungsName = "Computergrafik";
  String pruefungsNummer = "b123";
  Duration pruefungsDauer = Duration.ofMinutes(120);
  Map<Teilnehmerkreis, Integer> teilnehmerKreise = new HashMap<>();

  @Test
  public void createPruefungSuccessTest() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    DataAccessService service = new DataAccessService(pruefungsperiode);
    ReadOnlyPruefung pruefung = service.createPruefung("Analysis",
        "b123", "pruefer 1", Duration.ofMinutes(90),
        new HashMap<>());
    Assert.assertNotNull("Pruefung ist nicht null", pruefung);
  }


  @Test
  public void createPruefungSuccessRightAttributesTest() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    DataAccessService service = new DataAccessService(pruefungsperiode);

    Map<Teilnehmerkreis, Integer> teilnehmerkreise = new HashMap<>();
    ReadOnlyPruefung pruefung = service.createPruefung("abc",
        "b123", "pruefer 1", Duration.ofMinutes(90),
        teilnehmerkreise);

    assertThat(pruefung.getName()).isEqualTo("abc");
    assertThat(pruefung.getPruefungsnummer()).isEqualTo("b123");
    assertThat(pruefung.getPruefer()).containsExactly("pruefer 1");
    assertThat(pruefung.getDauer()).hasMinutes(90);
    assertThat(pruefung.getTeilnehmerkreise()).isEqualTo(teilnehmerkreise.keySet());
  }

  @Test
  public void createPruefungSaveInModelTest() {
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    DataAccessService service = spy(new DataAccessService(pruefungsperiode));

    ReadOnlyPruefung pruefung = service.createPruefung("asdf", "222b", "def",
        Duration.ofMinutes(120), new HashMap<>());
    // TODO test whether or not the correct data is passed
    verify(pruefungsperiode, times(1)).addPlanungseinheit(notNull());
    assertThat(pruefung).isNotNull();
  }

  @Test
  public void createPruefung_existsAllready() {
    // given
    Pruefungsperiode pruefungsperiode = mock(Pruefungsperiode.class);
    DataAccessService dataAccessService = new DataAccessService(pruefungsperiode);
    Set<Planungseinheit> geplant = new HashSet<>();
    geplant.add(new PruefungImpl(pruefungsNummer, pruefungsName, "", 20, pruefungsDauer));
    // when
    when(pruefungsperiode.filteredPlanungseinheiten(any())).thenReturn(geplant);
    // then
    assertThat(dataAccessService.createPruefung(pruefungsName, pruefungsNummer, "", pruefungsDauer,
        teilnehmerKreise))
        .isNull();
  }

}
