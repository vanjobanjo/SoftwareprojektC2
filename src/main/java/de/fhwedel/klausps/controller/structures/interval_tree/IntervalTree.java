package de.fhwedel.klausps.controller.structures.interval_tree;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class IntervalTree {

  private int size = 0;

  private IntervalTreeNode root = null;

  public void add(Interval interval, Planungseinheit... planungseinheiten) {
    root = IntervalTreeNode.addTo(root, interval, planungseinheiten);
    size++;
  }

  public int getSize() {
    return size;
  }

  public Set<Planungseinheit> getAllPlanungseinheitenThatOverlapAtLeastWith(int amount) {
    if (root == null) {
      return Collections.emptySet();
    }
    return this.root.getPlanungseinheitenThat(
        (Collection<Planungseinheit> planungseinheiten) -> planungseinheiten.size() > amount);
  }
}
