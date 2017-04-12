/*******************************************************************************
 * Copyright or © or Copr. 2015 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2015)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ietr.preesm.ui.pimm.diagram;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.ui.MarkerHelper;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;

// TODO: Auto-generated Javadoc
/**
 * The purpose of this custom {@link MarkerHelper} is to associate a new attribute to the {@link IMarker} created in the {@link PiMMDiagramEditor}. This
 * attribute contains a {@link String} corresponding to the uriFragment that can be used to access the pictogram element to which the marker is associated.
 *
 * @author kdesnos
 *
 */
public class PiMMMarkerHelper extends EditUIMarkerHelper {

  /**
   * This {@link IMarker} attribute contains a {@link String} corresponding to the uriFragment that can be used to access the pictogram element to which the
   * marker is associated.
   */
  public static final String DIAGRAM_URI = "Diagram_URI";

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.emf.edit.ui.util.EditUIMarkerHelper#adjustMarker(org.eclipse.core.resources.IMarker, org.eclipse.emf.common.util.Diagnostic,
   * org.eclipse.emf.common.util.Diagnostic)
   */
  @Override
  protected void adjustMarker(final IMarker marker, final Diagnostic diagnostic, final Diagnostic parentDiagnostic) throws CoreException {

    super.adjustMarker(marker, diagnostic, parentDiagnostic);
    if (diagnostic.getData().size() == 2) {
      marker.setAttribute(PiMMMarkerHelper.DIAGRAM_URI, (diagnostic.getData().get(1)));
    }
  }
}
