package org.preesm.commons.model;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 *
 */
public interface PreesmUserFactory {

  /**
   * Copy an existing Preesm object.
   */
  public default <T extends EObject> T copy(final T eObject) {
    @SuppressWarnings("unchecked")
    final T copy = (T) new EcoreUtil.Copier(false).copy(eObject);
    return copy;
  }

  /**
   * Copy an existing Preesm object.
   */
  public default <T extends EObject> T copyWithHistory(final T eObject) {
    final T copy = copy(eObject);
    PreesmCopyTracker.trackCopy(eObject, copy);
    return copy;
  }
}
