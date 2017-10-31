/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2012 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Julien Heulot <julien.heulot@insa-rennes.fr> (2013)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012 - 2013)
 *
 * This software is a computer program whose purpose is to help prototyping
 * parallel applications using dataflow formalism.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */
package org.ietr.preesm.ui.pimm.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.util.IColorConstant;
import org.ietr.preesm.experiment.model.pimm.DataInputInterface;
import org.ietr.preesm.experiment.model.pimm.PiGraph;

/**
 * Add feature to add a new {@link DataInputInterface} to the {@link PiGraph}.
 *
 * @author kdesnos
 */
public class AddDataInputInterfaceFeature extends AbstractAddDataInterfacefeature {

  /** The Constant DATA_INPUT_TEXT_FOREGROUND. */
  public static final IColorConstant DATA_INPUT_TEXT_FOREGROUND = IColorConstant.BLACK;

  /** The Constant DATA_INPUT_FOREGROUND. */
  public static final IColorConstant DATA_INPUT_FOREGROUND = AddDataInputPortFeature.DATA_INPUT_PORT_FOREGROUND;

  /** The Constant DATA_INPUT_BACKGROUND. */
  public static final IColorConstant DATA_INPUT_BACKGROUND = AddDataInputPortFeature.DATA_INPUT_PORT_BACKGROUND;

  @Override
  protected IColorConstant getTextForegroundColor() {
    return AddDataInputInterfaceFeature.DATA_INPUT_TEXT_FOREGROUND;
  }

  @Override
  protected IColorConstant getBackgroundColor() {
    return AddDataInputInterfaceFeature.DATA_INPUT_BACKGROUND;
  }

  @Override
  protected IColorConstant getForegroundColor() {
    return AddDataInputInterfaceFeature.DATA_INPUT_FOREGROUND;
  }

  @Override
  protected double getRelativeWidth() {
    return 1.0;
  }

  @Override
  protected int getX() {
    return -AbstractAddDataInterfacefeature.WIDTH;
  }

  /**
   * The default constructor of {@link AddDataInputInterfaceFeature}.
   *
   * @param fp
   *          the feature provider
   */
  public AddDataInputInterfaceFeature(final IFeatureProvider fp) {
    super(fp);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.graphiti.func.IAdd#canAdd(org.eclipse.graphiti.features.context.IAddContext)
   */
  @Override
  public boolean canAdd(final IAddContext context) {
    // Check that the user wants to add an SourceInterface to the Diagram
    return ((context != null) && (context.getNewObject() instanceof DataInputInterface)) && (context.getTargetContainer() instanceof Diagram);
  }

}
