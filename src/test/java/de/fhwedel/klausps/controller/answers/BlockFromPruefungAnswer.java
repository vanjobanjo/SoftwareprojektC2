package de.fhwedel.klausps.controller.answers;

import de.fhwedel.klausps.model.api.Block;
import de.fhwedel.klausps.model.api.Pruefung;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class BlockFromPruefungAnswer implements Answer<Optional<Block>> {

  private final Set<Block> bloecke;

  public BlockFromPruefungAnswer(Collection<Block> bloecke) {
    this.bloecke = Set.copyOf(bloecke);
  }

  @Override
  public Optional<Block> answer(InvocationOnMock invocation) {
    Pruefung pruefung = invocation.getArgument(0);
    return bloecke.stream().filter(x -> x.getPruefungen().contains(pruefung)).findFirst();
  }
}
