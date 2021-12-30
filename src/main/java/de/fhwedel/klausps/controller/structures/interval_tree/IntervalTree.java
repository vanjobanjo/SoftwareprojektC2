package de.fhwedel.klausps.controller.structures.interval_tree;

import de.fhwedel.klausps.model.api.Planungseinheit;
import java.util.Collections;
import java.util.Set;

public class IntervalTree {

  private int size = 0;

  private IntervalTreeNode root = null;

  public void add(Interval interval, Planungseinheit... planungseinheiten) {
    this.root = new IntervalTreeNode(interval, planungseinheiten);
    size++;
  }

  public int getSize() {
    return size;
  }

  public Set<Planungseinheit> getAllPlanungseinheitenThatOverlapAtLeastWith(int amount) {
    if (root == null) {
      return Collections.emptySet();
    }
    if (root.getPlanungseinheiten().size() > amount) {
      return root.getPlanungseinheiten();
    } else {
      return Collections.emptySet();
    }
  }
}
