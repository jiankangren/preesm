/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.plugin.mapper;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.task.PreesmException;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.impl.latency.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.abc.taskscheduling.SimpleTaskSched;
import org.ietr.preesm.plugin.abc.taskscheduling.TopologicalTaskSched;
import org.ietr.preesm.plugin.mapper.algo.fast.FastAlgorithm;
import org.ietr.preesm.plugin.mapper.algo.list.InitialLists;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.graphtransfo.TagDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.params.AbcParameters;
import org.ietr.preesm.plugin.mapper.params.FastAlgoParameters;
import org.sdf4j.model.parameters.InvalidExpressionException;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * FAST Kwok algorithm
 * 
 * @author pmenuet
 * @author mpelcat
 */
public class FASTTransformation extends AbstractMapping {

	/**
	 * 
	 */
	public FASTTransformation() {
	}

	/**
	 * Function called while running the plugin
	 * @throws PreesmException 
	 */
	@Override
	public TaskResult transform(SDFGraph algorithm,
			MultiCoreArchitecture architecture, TextParameters textParameters,
			PreesmScenario scenario, IProgressMonitor monitor) throws PreesmException {

		super.transform(algorithm,architecture,textParameters,scenario,monitor);
		TaskResult result = new TaskResult();

		FastAlgoParameters fastParams = new FastAlgoParameters(textParameters);
		AbcParameters abcParams = new AbcParameters(textParameters);

		MapperDAG dag = SdfToDagConverter.convert(algorithm, architecture,
				scenario, false);
		
		if(dag == null){
			throw(new PreesmException(" graph can't be scheduled, check console messages"));
		}
		
		// calculates the DAG span length on the architecture main operator (the tasks that can
		// not be executed by the main operator are deported without transfer time to other operator
		calculateSpan(dag,architecture,scenario,abcParams);

		IAbc simu = new InfiniteHomogeneousAbc(abcParams,
				dag, architecture, abcParams.getSimulatorType().getTaskSchedType(), scenario);
		
		InitialLists initialLists = new InitialLists();
		if (!initialLists.constructInitialLists(dag, simu)){
			return result;
		}

		TopologicalTaskSched taskSched = new TopologicalTaskSched(simu.getTotalOrder());
		simu.resetDAG();
		

		FastAlgorithm fastAlgorithm = new FastAlgorithm(initialLists, scenario);

		WorkflowLogger.getLogger().log(Level.INFO,"Mapping");
		
		dag = fastAlgorithm.map("test", abcParams, fastParams, dag, architecture, false, false
				, fastParams.isDisplaySolutions(), monitor, taskSched);

		WorkflowLogger.getLogger().log(Level.INFO,"Mapping finished");

		IAbc simu2 = AbstractAbc.getInstance(abcParams, dag, architecture, scenario);
		// Transfer vertices are automatically regenerated
		simu2.setDAG(dag);

		// The transfers are reordered using the best found order during
		// scheduling
		simu2.reschedule(fastAlgorithm.getBestTotalOrder());
		TagDAG tagSDF = new TagDAG();

		// The mapper dag properties are put in the property bean to be transfered to code generation
		try {
			tagSDF.tag(dag, architecture, scenario, simu2, abcParams
					.getEdgeSchedType());
		} catch (InvalidExpressionException e) {
			throw(new PreesmException(e.getMessage()));
		}
		
		result.setDAG(dag); 
		// A simple task scheduler avoids new task swaps and ensures reuse of previous order.
		simu2.setTaskScheduler(new SimpleTaskSched());
		result.setAbc(simu2);

		super.clean(architecture,scenario);
		return result;
	}

	@Override
	public void transform(SDFGraph algorithm, SDFGraph transformedAlgorithm) {
		// TODO Auto-generated method stub

	}

}
