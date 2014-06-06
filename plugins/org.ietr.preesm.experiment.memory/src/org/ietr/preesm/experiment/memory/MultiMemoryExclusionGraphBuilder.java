package org.ietr.preesm.experiment.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.dftools.algorithm.model.dag.DirectedAcyclicGraph;
import org.ietr.dftools.algorithm.model.parameters.InvalidExpressionException;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.algorithm.transforms.ForkJoinRemover;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.types.DataType;
import org.ietr.preesm.memory.exclusiongraph.MemoryExclusionGraph;
import org.ietr.preesm.memory.exclusiongraph.MemoryExclusionVertex;

public class MultiMemoryExclusionGraphBuilder extends
		AbstractTaskImplementation {

	static final public String PARAM_VERBOSE = "Verbose";
	static final public String VALUE_TRUE_FALSE_DEFAULT = "? C {True, False}";
	static final public String VALUE_TRUE = "True";
	static final public String VALUE_FALSE = "False";

	static final public String PARAM_SUPPR_FORK_JOIN = "Suppr Fork/Join";

	static final public String OUTPUT_KEY_MEM_EX = "MemEx";
	static final public String OUTPUT_KEY_DAG = "DAG";

	@Override
	public Map<String, Object> execute(Map<String, Object> inputs,
			Map<String, String> parameters, IProgressMonitor monitor,
			String nodeName, Workflow workflow) throws WorkflowException {

		// Rem: Logger is used to display messages in the console
		Logger logger = WorkflowLogger.getLogger();

		// Check Workflow element parameters
		String valueVerbose = parameters.get(PARAM_VERBOSE);
		boolean verbose;
		verbose = valueVerbose.equals(VALUE_TRUE);

		String valueSupprForkJoin = parameters.get(PARAM_SUPPR_FORK_JOIN);
		boolean supprForkJoin;
		supprForkJoin = valueSupprForkJoin.equals(VALUE_TRUE);

		// Retrieve list of types and associated sizes in the scenario
		PreesmScenario scenario = (PreesmScenario) inputs.get(KEY_SCENARIO);
		Map<String, DataType> dataTypes = scenario.getSimulationManager()
				.getDataTypes();
		MemoryExclusionVertex.setDataTypes(dataTypes);

		// Make a copy of the Input DAG for treatment
		// The DAG is altered when building the exclusion graph.
		Set<DirectedAcyclicGraph> dags = (Set<DirectedAcyclicGraph>) inputs
				.get("DAGs");

		Map<DirectedAcyclicGraph, MemoryExclusionGraph> dagsAndMemExs = new HashMap<DirectedAcyclicGraph, MemoryExclusionGraph>();

		for (DirectedAcyclicGraph dag : dags) {
			// Clone is deep copy i.e. vertices are thus copied too.
			DirectedAcyclicGraph localDAG = (DirectedAcyclicGraph) dag.clone();
			if (localDAG == null) {
				localDAG = dag;
			}

			// Remove Fork/Join vertices
			if (supprForkJoin) {
				ForkJoinRemover.supprImplodeExplode(localDAG);
			}

			// Build the exclusion graph
			if (verbose)
				logger.log(Level.INFO,
						"Memory exclusion graph : start building");
			MemoryExclusionGraph memEx = new MemoryExclusionGraph();
			try {
				memEx.buildGraph(localDAG);
			} catch (InvalidExpressionException e) {
				throw new WorkflowException(e.getLocalizedMessage());
			}
			double density = memEx.edgeSet().size()
					/ (memEx.vertexSet().size()
							* (memEx.vertexSet().size() - 1) / 2.0);
			if (verbose)
				logger.log(Level.INFO, "Memory exclusion graph built with "
						+ memEx.vertexSet().size() + " vertices and density = "
						+ density);

			dagsAndMemExs.put(localDAG, memEx);
		}

		// Generate output
		Map<String, Object> output = new HashMap<String, Object>();
		output.put(KEY_DAG_AND_MEM_EX_MAP, dagsAndMemExs);
		return output;
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(PARAM_VERBOSE, VALUE_TRUE_FALSE_DEFAULT);
		parameters.put(PARAM_SUPPR_FORK_JOIN, VALUE_TRUE_FALSE_DEFAULT);
		return parameters;
	}

	@Override
	public String monitorMessage() {
		return "Building MemEx Graph";
	}

}
