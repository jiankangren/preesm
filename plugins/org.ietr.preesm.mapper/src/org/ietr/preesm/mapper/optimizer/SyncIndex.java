package org.ietr.preesm.mapper.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ietr.dftools.architecture.slam.ComponentInstance;

/**
 * Utility class used to store per component index of the last sync group executed.
 *
 * @author kdesnos
 *
 */
class SyncIndex {
  Map<ComponentInstance, Integer> syncIndexPerComponent;

  public SyncIndex(Set<ComponentInstance> components) {
    syncIndexPerComponent = new HashMap<>();
    for (ComponentInstance comp : components) {
      syncIndexPerComponent.put(comp, -1);
    }
  }

  public boolean strictlySmallerOrEqual(SyncIndex syncIndex) {
    boolean result = true;
    for (ComponentInstance comp : syncIndex.syncIndexPerComponent.keySet()) {
      result &= this.syncIndexPerComponent.get(comp) <= syncIndex.syncIndexPerComponent.get(comp);
    }
    return result;
  }

  public void increment(ComponentInstance component) {
    this.syncIndexPerComponent.put(component, this.syncIndexPerComponent.get(component) + 1);
  }

  @Override
  public String toString() {
    String result = "";
    List<ComponentInstance> components = new ArrayList<>(syncIndexPerComponent.keySet());
    components.sort((a, b) -> {
      return a.getInstanceName().compareTo(b.getInstanceName());
    });
    for (ComponentInstance c : components) {
      result += c.getInstanceName() + ": " + syncIndexPerComponent.get(c) + '\n';
    }

    return result;
  }

  public SyncIndex clone() {
    SyncIndex res = new SyncIndex(this.syncIndexPerComponent.keySet());
    this.syncIndexPerComponent.forEach((comp, index) -> {
      res.syncIndexPerComponent.put(comp, index);
    });
    return res;
  }

  /**
   * Update this with the max index of itself versus the one of the {@link SyncIndex} passed as a paramater for each
   * {@link ComponentInstance}.
   *
   * @param a
   *          the {@link SyncIndex} used to update the max value
   */
  public void max(SyncIndex a) {
    for (ComponentInstance c : a.syncIndexPerComponent.keySet()) {
      this.syncIndexPerComponent.put(c, Math.max(this.syncIndexPerComponent.get(c), a.syncIndexPerComponent.get(c)));
    }
  }
}