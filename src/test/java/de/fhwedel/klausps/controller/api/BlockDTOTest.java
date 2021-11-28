package de.fhwedel.klausps.controller.api;

import de.fhwedel.klausps.controller.api.builders.PruefungDTOBuilder;
import de.fhwedel.klausps.controller.api.view_dto.ReadOnlyPruefung;
import de.fhwedel.klausps.model.impl.TeilnehmerkreisImpl;
import org.junit.jupiter.api.Test;

import javax.naming.RefAddr;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BlockDTOTest {

    @Test
    public void teilnehmerKreisSchaetzung(){
        TeilnehmerkreisImpl bwl = new TeilnehmerkreisImpl("BWL", "Bla", 10);
        TeilnehmerkreisImpl inf = new TeilnehmerkreisImpl("inf", "Bla", 10);
        PruefungDTO analysis = new PruefungDTOBuilder()
                .withAdditionalTeilnehmerkreisSchaetzung(bwl, 10)
                .build();

        PruefungDTO dm = new PruefungDTOBuilder()
                .withAdditionalTeilnehmerkreisSchaetzung(inf, 10)
                .withAdditionalTeilnehmerkreisSchaetzung(bwl, 10)
                .build();
        Set<ReadOnlyPruefung> dmana = new HashSet<>(Arrays.asList(dm, analysis));

        BlockDTO tester = new BlockDTO("Hallo",
                LocalDateTime.now(),
                Duration.ofMinutes(90),
                false, dmana);
        assertThat(tester.getTeilnehmerKreisSchaetzung().containsKey(bwl)).isTrue();
        assertThat(tester.getTeilnehmerKreisSchaetzung().containsKey(inf)).isTrue();
        assertThat(tester.getTeilnehmerKreisSchaetzung().values().size()).isEqualTo(2);
        assertThat(tester.getTeilnehmerkreise()).containsOnly(bwl, inf);
        assertThat(tester.getGesamtschaetzung()).isEqualTo(20);

    }


}