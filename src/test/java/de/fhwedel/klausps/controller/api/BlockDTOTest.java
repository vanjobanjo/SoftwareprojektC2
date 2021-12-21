package de.fhwedel.klausps.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.api.Ausbildungsgrad;
import de.fhwedel.klausps.model.api.Blocktyp;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BlockDTOTest {

  @Test
  void teilnehmerKreisSchaetzung() {
    TeilnehmerkreisImpl bwl = new TeilnehmerkreisImpl("BWL", "Bla", 10, Ausbildungsgrad.BACHELOR);
    TeilnehmerkreisImpl inf = new TeilnehmerkreisImpl("inf", "Bla", 10, Ausbildungsgrad.BACHELOR);
    PruefungDTO analysis =
        new PruefungDTOBuilder().withAdditionalTeilnehmerkreisSchaetzung(bwl, 10).build();

    PruefungDTO dm =
        new PruefungDTOBuilder()
            .withAdditionalTeilnehmerkreisSchaetzung(inf, 10)
            .withAdditionalTeilnehmerkreisSchaetzung(bwl, 10)
            .build();
    Set<ReadOnlyPruefung> dmana = new HashSet<>(Arrays.asList(dm, analysis));

    BlockDTO tester =
        new BlockDTO("Hallo", LocalDateTime.now(), Duration.ofMinutes(90), dmana, 1,
            Blocktyp.PARALLEL);
    assertThat(tester.getTeilnehmerKreisSchaetzung()).containsKey(bwl);
    assertThat(tester.getTeilnehmerKreisSchaetzung()).containsKey(inf);
    assertThat(tester.getTeilnehmerKreisSchaetzung().values()).hasSize(2);
    assertThat(tester.getTeilnehmerkreise()).containsOnly(bwl, inf);
    assertThat(tester.getGesamtschaetzung()).isEqualTo(20);
  }
}
