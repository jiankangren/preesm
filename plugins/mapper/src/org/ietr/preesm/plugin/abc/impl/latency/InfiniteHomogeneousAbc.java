/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

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

package org.ietr.preesm.plugin.abc.impl.latency;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.abc.taskscheduling.TaskSchedType;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;

/**
 * Simulates an architecture having as many cores as necessary to
 * execute one operation on one core. All core have the main operator
 * definition. These cores are all interconnected with media
 * corresponding to the main medium definition.
 *         
 * @author mpelcat   
 */
public class InfiniteHomogeneousAbc extends
		LatencyAbc {

	/**
	 * Constructor 
	 */
	public InfiniteHomogeneousAbc(EdgeSchedType edgeSchedType, MapperDAG dag,
			MultiCoreArchitecture archi) {
		this(edgeSchedType,dag,archi,TaskSchedType.Simple);
	}
	
	/**
	 * Constructor of the simulator from a "blank" implementation where every
	 * vertex has not been implanted yet.
	 */
	public InfiniteHomogeneousAbc(EdgeSchedType edgeSchedType, MapperDAG dag,
			MultiCoreArchitecture archi, TaskSchedType taskSchedType) {
		super(null, dag, archi, AbcType.InfiniteHomogeneous);
		this.getType().setTaskSchedType(taskSchedType);

		// The InfiniteHomogeneousArchitectureSimulator is specifically done
		// to implant all vertices on the main operator definition but consider
		// as many cores as there are tasks.
		implantAllVerticesOnOperator(archi.getMainOperator());
	}

	@Override
	protected void fireNewMappedVertex(MapperDAGVertex vertex, boolean updateRank) {

		Operator effectiveOp = vertex.getImplementationVertexProperty()
				.getEffectiveOperator();

		/*
		 * implanting a vertex sets the cost of the current vertex and its edges
		 * 
		 * As we have an infinite homogeneous architecture, each communication
		 * is done through the unique type of medium
		 */
		if (effectiveOp == Operator.NO_COMPONENT) {
			PreesmLogger.getLogger().severe(
					"implementation of " + vertex.getName() + " failed");

			vertex.getTimingVertexProperty().setCost(0);
			
		} else {

			if (updateRank) {
				taskScheduler.insertVertex(vertex);
			} else {
				orderManager.insertVertexInTotalOrder(vertex);
			}
			
			// Setting vertex time
			long vertextime = vertex.getInitialVertexProperty().getTime(
					effectiveOp);
			vertex.getTimingVertexProperty().setCost(vertextime);

			// Setting edges times
			
			setEdgesCosts(vertex.incomingEdges());
			setEdgesCosts(vertex.outgoingEdges());
		}
	}

	@Override
	protected void fireNewUnmappedVertex(MapperDAGVertex vertex) {

		// unimplanting a vertex resets the cost of the current vertex
		// and its edges
		
		vertex.getTimingVertexProperty().resetCost();

		resetCost(vertex.incomingEdges());
		resetCost(vertex.outgoingEdges());

	}

	/**
	 * Asks the time keeper to update timings. Crucial and costly operation.
	 * Depending on the king of timings we want, calls the necessary updates.
	 */
	@Override
	protected final void updateTimings() {
		timeKeeper.updateTandBLevels();
	}

	@Override
	protected final void setEdgeCost(MapperDAGEdge edge) {

		long edgesize = edge.getInitialEdgeProperty().getDataSize();

		/**
		 * In a Infinite Homogeneous Architecture, each communication is
		 * supposed to be done on the main medium. The communication
		 * cost is simply calculated from the main medium speed.
		 */
		if (archi.getMainMedium() != null) {
			MediumDefinition def = (MediumDefinition) archi
					.getMainMedium().getDefinition();
			Float speed = def.getInvSpeed();
			speed = edgesize * speed;
			edge.getTimingEdgeProperty().setCost(speed.intValue());
		} else {

			PreesmLogger
					.getLogger()
					.info(
							"current architecture has no main medium. infinite homogeneous simulator will use default speed");

			Float speed = 1f;
			speed = edgesize * speed;
			edge.getTimingEdgeProperty().setCost(speed.intValue());
		}
	}
	
	public EdgeSchedType getEdgeSchedType() {
		return null;
	}
}