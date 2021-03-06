/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2013 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2019)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2013)
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
package org.preesm.algorithm.io.sdf3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.preesm.algorithm.model.sdf.SDFGraph;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.files.PathTools;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.slam.Design;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;
import org.preesm.workflow.implement.AbstractWorkflowNodeImplementation;

/**
 * This class is a {@link Workflow} task that parse a SDF in the SDF3 XML format and output its corresponding
 * {@link SDFGraph}.
 *
 * @author kdesnos
 *
 */
public class Sdf3Importer extends AbstractTaskImplementation {

  /** The Constant PARAM_PATH. */
  public static final String PARAM_PATH = "path";

  /** The Constant VALUE_PATH_DEFAULT. */
  public static final String VALUE_PATH_DEFAULT = "./Code/SDF3/graph.xml";

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#execute(java.util.Map, java.util.Map,
   * org.eclipse.core.runtime.IProgressMonitor, java.lang.String, org.ietr.dftools.workflow.elements.Workflow)
   */
  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters,
      final IProgressMonitor monitor, final String nodeName, final Workflow workflow) {

    // Rem: Logger is used to display messages in the console
    final Logger logger = PreesmLogger.getLogger();

    // Retrieve the inputs
    final Design architecture = (Design) inputs.get(AbstractWorkflowNodeImplementation.KEY_ARCHITECTURE);
    final PreesmScenario scenario = (PreesmScenario) inputs.get(AbstractWorkflowNodeImplementation.KEY_SCENARIO);

    if (architecture == null) {
      throw new PreesmRuntimeException("Architecture is not properly initialized, cannot execute SDF3 import task");
    }

    // Locate the intput file
    final String sPath = PathTools.getAbsolutePath(parameters.get("path"), workflow.getProjectName());
    final IPath path = new Path(sPath);

    final SDF3ImporterEngine engine = new SDF3ImporterEngine();

    final SDFGraph graph = engine.importFrom(path, scenario, architecture, logger);

    Map<String, Object> outputs = null;

    // If there was no problem while parsing the graph
    if (graph != null) {
      // put it in the outputs
      outputs = new LinkedHashMap<>();
      outputs.put(AbstractWorkflowNodeImplementation.KEY_SDF_GRAPH, graph);
      outputs.put(AbstractWorkflowNodeImplementation.KEY_SCENARIO, scenario);
    }

    return outputs;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#getDefaultParameters()
   */
  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put(Sdf3Importer.PARAM_PATH, Sdf3Importer.VALUE_PATH_DEFAULT);
    return parameters;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractWorkflowNodeImplementation#monitorMessage()
   */
  @Override
  public String monitorMessage() {
    return "Importing SDF3 Xml File";
  }

}
