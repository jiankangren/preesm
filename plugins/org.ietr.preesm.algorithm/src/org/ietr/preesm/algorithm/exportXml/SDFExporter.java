/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Jonathan Piat <jpiat@laas.fr> (2008 - 2011)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2016)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2012)
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
package org.ietr.preesm.algorithm.exportXml;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.preesm.utils.files.ContainersManager;
import org.ietr.preesm.utils.files.WorkspaceUtils;
import org.ietr.preesm.utils.paths.PathTools;

/**
 * The Class SDFExporter.
 */
public class SDFExporter extends AbstractTaskImplementation {

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#execute(java.util.Map, java.util.Map, org.eclipse.core.runtime.IProgressMonitor,
   * java.lang.String, org.ietr.dftools.workflow.elements.Workflow)
   */
  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters, final IProgressMonitor monitor,
      final String nodeName, final Workflow workflow) throws WorkflowException {

    final SDFGraph algorithm = (SDFGraph) inputs.get("SDF");
    final String sXmlPath = PathTools.getAbsolutePath(parameters.get("path"), workflow.getProjectName());
    IPath xmlPath = new Path(sXmlPath);
    // Get a complete valid path with all folders existing
    try {
      if (xmlPath.getFileExtension() != null) {
        ContainersManager.createMissingFolders(xmlPath.removeFileExtension().removeLastSegments(1));
      } else {
        ContainersManager.createMissingFolders(xmlPath);
        xmlPath = xmlPath.append(algorithm.getName() + ".graphml");
      }
    } catch (CoreException | IllegalArgumentException e) {
      throw new WorkflowException("Path " + sXmlPath + " is not a valid path for export.\n" + e.getMessage());
    }

    final SDF2GraphmlExporter exporter = new SDF2GraphmlExporter();
    exporter.export(algorithm, xmlPath);

    WorkspaceUtils.updateWorkspace();

    return new LinkedHashMap<>();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#getDefaultParameters()
   */
  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new LinkedHashMap<>();

    parameters.put("path", "");
    return parameters;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractWorkflowNodeImplementation#monitorMessage()
   */
  @Override
  public String monitorMessage() {
    return "Exporting algorithm graph";
  }

}
