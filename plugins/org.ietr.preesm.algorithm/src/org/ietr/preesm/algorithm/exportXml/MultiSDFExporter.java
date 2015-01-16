package org.ietr.preesm.algorithm.exportXml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.preesm.core.Activator;
import org.ietr.preesm.core.tools.PathTools;
import org.ietr.preesm.utils.files.ContainersManager;

public class MultiSDFExporter extends AbstractTaskImplementation {

	private static final String PATH_KEY = "path";

	@Override
	public Map<String, Object> execute(Map<String, Object> inputs,
			Map<String, String> parameters, IProgressMonitor monitor,
			String nodeName, Workflow workflow) throws WorkflowException {

		String sXmlPath;
		IPath xmlPath;

		@SuppressWarnings("unchecked")
		Set<SDFGraph> algorithms = (Set<SDFGraph>) inputs
				.get(KEY_SDF_GRAPHS_SET);
		SDF2GraphmlExporter exporter = new SDF2GraphmlExporter();

		for (SDFGraph algorithm : algorithms) {

			sXmlPath = PathTools.getAbsolutePath(parameters.get(PATH_KEY) + "/"
					+ algorithm.getName() + ".graphml",
					workflow.getProjectName());
			xmlPath = new Path(sXmlPath);
			// Get a complete valid path with all folders existing
			try {
				if (xmlPath.getFileExtension() != null)
					ContainersManager.createMissingFolders(xmlPath
							.removeFileExtension().removeLastSegments(1));
				else {
					ContainersManager.createMissingFolders(xmlPath);
					xmlPath = xmlPath.removeFileExtension()
							.removeLastSegments(1)
							.append(algorithm.getName() + ".graphml");
				}
			} catch (CoreException e) {
				throw new WorkflowException("Path " + sXmlPath
						+ " is not a valid path for export.");
			}

			exporter.export(algorithm, xmlPath);
		}

		Activator.updateWorkspace();

		return new HashMap<String, Object>();
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put(PATH_KEY, "");
		return parameters;
	}

	@Override
	public String monitorMessage() {
		return "Exporting algorithms graphs";
	}

}