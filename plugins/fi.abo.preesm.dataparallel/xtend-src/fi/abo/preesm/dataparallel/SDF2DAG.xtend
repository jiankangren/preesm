/**
 * Copyright or © or Copr. Åbo Akademi University (2017 - 2019),
 * IETR/INSA - Rennes (2017 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2019)
 * Sudeep Kanur <skanur@abo.fi> (2017 - 2018)
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
package fi.abo.preesm.dataparallel

import fi.abo.preesm.dataparallel.operations.DAGOperations
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.Set
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import org.eclipse.xtend.lib.annotations.Accessors
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.AbstractGraph
import org.jgrapht.graph.AsSubgraph
import org.preesm.algorithm.model.sdf.SDFAbstractVertex
import org.preesm.algorithm.model.sdf.SDFEdge
import org.preesm.algorithm.model.sdf.SDFGraph
import org.preesm.algorithm.model.sdf.SDFInterfaceVertex
import org.preesm.algorithm.model.sdf.esdf.SDFBroadcastVertex
import org.preesm.algorithm.model.sdf.esdf.SDFForkVertex
import org.preesm.algorithm.model.sdf.esdf.SDFJoinVertex
import org.preesm.algorithm.model.sdf.esdf.SDFRoundBufferVertex
import org.preesm.algorithm.model.sdf.transformations.SpecialActorPortsIndexer
import org.preesm.algorithm.model.types.LongEdgePropertyType
import org.preesm.algorithm.model.types.StringEdgePropertyType
import org.preesm.commons.exceptions.PreesmRuntimeException

/**
 * Construct DAG from a SDF Graph
 *
 * @author Sudeep Kanur
 */
final class SDF2DAG extends AbstractDAGConstructor implements PureDAGConstructor {

	/**
	 * Hold the cloned version of original SDF graph
	 */
	@Accessors(PUBLIC_GETTER, PRIVATE_SETTER)
	val AbstractGraph<SDFAbstractVertex, SDFEdge> inputGraph;

	/**
	 * Holds constructed DAG
	 */
	@Accessors(PUBLIC_GETTER, PRIVATE_SETTER)
	var SDFGraph outputGraph

	/**
	 * True if the original graph is SDFGraph
	 */
	@Accessors(PUBLIC_GETTER, PRIVATE_SETTER)
	var Boolean isInputSDFGraph

	/**
	 * True if the original graph is a Subgraph
	 */
	@Accessors(PUBLIC_GETTER, PRIVATE_SETTER)
	var Boolean isInputSubgraph

	/**
	 * Map of all actors with instance. Does not contain implodes and explodes
	 * This is used as intermediate variable while creating linked edges
	 */
	protected val Map<SDFAbstractVertex, List<SDFAbstractVertex>> actor2InstancesLocal;

	/**
	 * List of all the actors that form the part of the cycles in the original SDFG
	 */
	@Accessors(PUBLIC_GETTER, PRIVATE_SETTER)
	val List<SDFAbstractVertex> cycleActors

	/**
	 * Protected constructor used for both SDF and subgraph of SDF. It cannot be used for anything
	 * else. Hence the constructor is protected.
	 *
	 * @param graph SDF or Subgraph
	 * @param logger Logger instance
	 * @param isSDF True if the passed graph is SDF. False if passed graph is a subgraph of SDFGraph
	 */
	protected new(AbstractGraph<SDFAbstractVertex, SDFEdge> graph, Logger logger, Boolean isSDF) {
		super(logger)

		if(isSDF) {
			this.isInputSDFGraph = true
			this.isInputSubgraph = false
		} else {
			this.isInputSDFGraph = false
			this.isInputSubgraph = true
		}

		inputGraph = graph
		actor2InstancesLocal = newLinkedHashMap
		cycleActors = newArrayList

		if(isSDF) { // Its an SDF graph
			val sdfGraph = graph as SDFGraph
			val cycleDetector = new CycleDetector(sdfGraph)
			cycleActors.addAll(cycleDetector.findCycles)

			sdfGraph.vertexSet.forEach[vertex |
				if(sdfGraph.incomingEdgesOf(vertex).size == 0) {
					sourceActors.add(vertex)
				}
				if(sdfGraph.outgoingEdgesOf(vertex).size == 0) {
					sinkActors.add(vertex)
				}
			]

			sdfGraph.vertexSet.forEach[vertex |
				val predecessorList = newArrayList
				sdfGraph.incomingEdgesOf(vertex).forEach[edge |
					predecessorList.add(edge.source)
				]
				actorPredecessor.put(vertex, predecessorList)
			]
		} else { // Its a subgraph of SDFGraph
			val subgraph = graph as AsSubgraph<SDFAbstractVertex, SDFEdge>
			val cycleDetector = new CycleDetector(subgraph)
			cycleActors.addAll(cycleDetector.findCycles)

			subgraph.vertexSet.forEach[vertex |
				if(subgraph.incomingEdgesOf(vertex).size == 0) {
					sourceActors.add(vertex)
				}
				if(subgraph.outgoingEdgesOf(vertex).size == 0) {
					sinkActors.add(vertex)
				}
			]

			subgraph.vertexSet.forEach[vertex |
				val predecessorList = newArrayList
				subgraph.incomingEdgesOf(vertex).forEach[edge |
					predecessorList.add(edge.source)
				]
				actorPredecessor.put(vertex, predecessorList)
			]
		}

		this.outputGraph = new SDFGraph()
		createInstances()
		linkEdges()
		outputGraph.propertyBean.setValue("schedulable", true)
	}

	/**
	 * Constructor for a {@link SDFGraph} instance. Mainly used in the plugin
	 *
	 * @param sdf the sdf graph for which DAG is created
	 * @param logger log messages to console
	 */
	new(SDFGraph sdf, Logger logger) {
		this(sdf, logger, true)
	}

	/**
	 * Constructor for a  subgraphs instance. Mainly used in the plugin
	 *
	 * @param subgraph A {@link DirectedSubgraph<SDFAbstractVertex, SDFEdge>} instance from which
	 * DAG is created
	 * @param logger log messages to console
	 */
	new(AsSubgraph<SDFAbstractVertex, SDFEdge> subgraph, Logger logger) {
		this(subgraph, logger, false)
	}

	/**
	 * Constructor without logging information for SDFGraphs. Mainly used for test
	 * setup
	 *
	 * @param sdf the sdf graph for which DAG is created
	 */
	new(SDFGraph sdf) {
		this(sdf as AbstractGraph<SDFAbstractVertex, SDFEdge>, null, true)
	}

	/**
	 * Constructor without logging information for subgraphs of SDFGraph. Mainly used for test
	 * setup
	 *
	 * @param subgraph The {@link DirectedSubgraph<SDFAbstractVertex, SDFEdge>} instance of SDFGraph
	 */
	new(AsSubgraph<SDFAbstractVertex, SDFEdge> subgraph) {
		this(subgraph as AbstractGraph<SDFAbstractVertex, SDFEdge>, null, false)
	}

	/**
	 * Accept method for DAG operations
	 *
	 * @param A {@link DAGOperations} instance
	 */
	override accept(DAGOperations visitor) {
		visitor.visit(this)
	}

	/**
	 * Create instances according to the repetition count. Also rename the
	 * instances
	 *
	 * @param isHSDF True if the input graph is HSDF
	 */
	 protected def void createInstances() {
	 	// Create instances repetition vector times
	 	for(actor: inputGraph.vertexSet) {
	 		log(Level.FINE, "Actor " + actor + " has " + actor.nbRepeatAsLong + " instances.")
	 		val instances = newArrayList

	 		for(var ii = 0; ii < actor.nbRepeatAsLong; ii++) {
	 			// Clone and set properties
	 			val instance = actor.copy
	 			if(actor.nbRepeatAsLong == 1) {
	 				instance.name = actor.name
	 			} else {
	 				instance.name = actor.name + "_" + ii;
	 			}
	 			instance.nbRepeat = 1L

	 			// Add to maps
	 			outputGraph.addVertex(instance)
	 			instances.add(instance)
	 			instance2Actor.put(instance, actor)
	 		}
	 		// Add to reverse map
	 		actor2InstancesLocal.put(actor, instances)
	 		actor2Instances.put(actor, new ArrayList(instances))
	 	}
	 }

	 /**
	  * Link the instances created by {@link SDF2DAG#createInstances}
	  */
	protected def void linkEdges() {
		// Edges that have delay tokens greater than buffer size need not have any
		// links in the DAG
		val filteredEdges = inputGraph.edgeSet.filter[edge |
			edge.delay.longValue < edge.cons.longValue * edge.target.nbRepeatAsLong
		]

		for(edge: filteredEdges) {
			// Legacy code. Usage debatable. Check the comment below (search the usage of inputVertex)
			// var SDFInterfaceVertex inputVertex = null
			// var SDFInterfaceVertex outputVertex = null

			// Total number of tokens that must be stored in the buffer
			val bufferSize = edge.cons.longValue * edge.target.nbRepeatAsLong

			val sourceInstances = actor2InstancesLocal.get(edge.source)
			val targetInstances = actor2InstancesLocal.get(edge.target)
			val originalSourceInstances = new ArrayList(sourceInstances)
			val originalTargetInstances = new ArrayList(targetInstances)

			var absoluteTarget = edge.delay.longValue
			var absoluteSource = 0L
			var currentBufferSize = edge.delay.longValue

			val newEdges = newArrayList
			while(currentBufferSize < bufferSize) {
				// Index of currently processed source instance among the instances of source actor.
				// Similarly for target actor
				val int sourceIndex = ((absoluteSource/edge.prod.longValue) % sourceInstances.size) as int
				val int targetIndex = ((absoluteTarget/edge.cons.longValue) % targetInstances.size) as int

				// Number of tokens already consumed and produced by currently indexed instances
				val sourceProd = absoluteSource % edge.prod.longValue
				val targetCons = absoluteTarget % edge.cons.longValue

				// Remaining tokens that needs to be handled in the next iteration
				val restSource = edge.prod.longValue - sourceProd
				val restTarget = edge.cons.longValue - targetCons
				val rest = Math.min(restSource, restTarget)

				// The below part of the code is from ToHSDFVisitor. It should not be relevant here as
		        // iterationDiff is never greater than 1. But I have included here for legacy reasons
		        // and added an exception when it occurs

		        // This int represent the number of iteration separating the
		        // currently indexed source and target (between which an edge is
		        // added)
		        // If this int is > to 0, this means that the added edge must
		        // have
		        // delays (with delay=prod=cons of the added edge).
		        // With the previous example:
		        // A_1 will target B_(1+targetIndex%3) = B_0 (with a delay of 1)
		        // A_2 will target B_(2+targetIndex%3) = B_1 (with a delay of 1)
		        // Warning, this integer division is not factorable
		        val iterationDiff = (absoluteTarget / bufferSize) - (absoluteSource / bufferSize);
		        if (iterationDiff > 0) {
		        	throw new PreesmRuntimeException("iterationDiff is greater than 0.")
		        }

		        // For inserting explode and implode when boolean is true
		        val explode = rest < edge.prod.longValue
		        val implode = rest < edge.cons.longValue

		        // Add explode instance for non-broadcast, non-roundbuffer, non-fork/join actors
				if(explode && !(sourceInstances.get(sourceIndex) instanceof SDFForkVertex)
					&& (!(sourceInstances.get(sourceIndex) instanceof SDFBroadcastVertex) || !(sourceInstances.get(sourceIndex) instanceof SDFRoundBufferVertex))) {
						val explodeInstance = new SDFForkVertex()
						explodeInstance.name = "explode_" + sourceInstances.get(sourceIndex).name + "_" + edge.sourceInterface.name
						outputGraph.addVertex(explodeInstance)
						val originalInstance = sourceInstances.get(sourceIndex)
						// Replace the source vertex by explode in the sourceInstances list
						sourceInstances.set(sourceIndex, explodeInstance)
						// Add to maps
						updateMaps(originalInstance, explodeInstance)
						// Add an edge between the explode and its instance
						val newEdge = outputGraph.addEdge(originalInstance, explodeInstance)
						newEdge.delay = new LongEdgePropertyType(0)
						newEdge.prod = new LongEdgePropertyType(edge.prod.longValue)
						newEdge.cons = new LongEdgePropertyType(edge.prod.longValue)
						newEdge.dataType = edge.dataType
						// Name the ports and set its attributes
						explodeInstance.addInterface(edge.targetInterface)
						newEdge.sourceInterface = edge.sourceInterface
						newEdge.targetInterface = edge.targetInterface
						newEdge.sourcePortModifier = edge.sourcePortModifier
						// As its explode, target port modifier is read-only
						newEdge.targetPortModifier = new StringEdgePropertyType(SDFEdge.MODIFIER_READ_ONLY)
				}

				// Add implode instance for non-fork/join and non-roundbuffer
				if(implode && !(targetInstances.get(targetIndex) instanceof SDFJoinVertex) && !(targetInstances.get(targetIndex) instanceof SDFRoundBufferVertex)) {
					val implodeInstance = new SDFJoinVertex()
					implodeInstance.name = "implode_" + targetInstances.get(targetIndex).name + "_" + edge.targetInterface.name
					val originalInstance = targetInstances.get(targetIndex)
					outputGraph.addVertex(implodeInstance)
					// Replace the target vertex by implode in the targetInstances list
					targetInstances.set(targetIndex, implodeInstance)
					// Add to maps
					updateMaps(originalInstance, implodeInstance)
					// Add an edge between the implode and its instance
					val newEdge = outputGraph.addEdge(implodeInstance, originalInstance)
					newEdge.delay = new LongEdgePropertyType(0)
					newEdge.prod = new LongEdgePropertyType(edge.cons.longValue)
					newEdge.cons = new LongEdgePropertyType(edge.cons.longValue)
					newEdge.dataType = edge.dataType
					// Name the ports and set its attributes
					implodeInstance.addInterface(edge.sourceInterface)
					newEdge.sourceInterface = edge.sourceInterface
					newEdge.targetInterface = edge.targetInterface
					newEdge.targetPortModifier = edge.targetPortModifier
					// As its implode, source port modifier is write-only
					newEdge.sourcePortModifier = new StringEdgePropertyType(SDFEdge.MODIFIER_WRITE_ONLY)
				}

				// Create the new edge for the output graph
				val newEdge = outputGraph.addEdge(sourceInstances.get(sourceIndex), targetInstances.get(targetIndex))
				newEdges.add(newEdge)

				// Set the source interface of the new edge
				// If the source is a newly added fork/broadcast (or extra output added to existing fork/broadcast)
				// We rename the output ports. Contrary to the ports of join/roundbuffer, no special processing
				// is needed to order the edges
				if( (sourceInstances.get(sourceIndex) == originalSourceInstances.get(sourceIndex))
					&& (!explode && !((originalSourceInstances.get(sourceIndex) instanceof SDFBroadcastVertex)
						|| (originalSourceInstances.get(sourceIndex) instanceof SDFForkVertex)))) {
					// if the source does not need new ports
					if(sourceInstances.get(sourceIndex).getSink(edge.sourceInterface.name) !== null) {
						// Source already has an interface. Just assign the correct name
						newEdge.sourceInterface = sourceInstances.get(sourceIndex).getSink(edge.sourceInterface.name)
					} else {
						// source does not have an interface
						newEdge.sourceInterface = edge.sourceInterface.copy
						sourceInstances.get(sourceIndex).addInterface(newEdge.sourceInterface)
					}
					// Copy the source port modifier of the original source
					newEdge.sourcePortModifier = edge.sourcePortModifier
				} else {
					// if the source is a fork (new or not) or a broadcast with a new port
					val sourceInterface = edge.sourceInterface.copy
					var newInterfaceName = sourceInterface.name + "_" + sourceProd

					// Get the current index of the port (if any) and update it
					if(sourceInterface.name.matches(SpecialActorPortsIndexer.INDEX_REGEX)) {
						val pattern = Pattern.compile(SpecialActorPortsIndexer.INDEX_REGEX)
						val matcher = pattern.matcher(sourceInterface.name)
						matcher.find
						val existingIdx = Integer.decode(matcher.group(SpecialActorPortsIndexer.GROUP_XX))
						val newIdx = existingIdx + sourceProd
						newInterfaceName = sourceInterface.name.substring(0, matcher.start(SpecialActorPortsIndexer.GROUP_XX)) + newIdx
					}

					sourceInterface.name = newInterfaceName
					newEdge.sourceInterface = sourceInterface
					newEdge.source.addInterface(sourceInterface)
					// Add a source port modifier
					newEdge.sourcePortModifier = new StringEdgePropertyType(SDFEdge.MODIFIER_WRITE_ONLY)
				}

				// Set the target interface of the new edge
				// If the target is a newly added join/roundbuffer we need to take extra care to
				// make sure the incoming edges are in the right order (which might be a little
				// bit complex when playing with delays)

				// If the target is not an instance with new ports (because of an explosion)
				if((targetInstances.get(targetIndex) == originalTargetInstances.get(targetIndex))
					&& (!implode || !((originalTargetInstances.get(targetIndex) instanceof SDFRoundBufferVertex)
						|| (originalTargetInstances.get(targetIndex) instanceof SDFJoinVertex)))) {
					// if the target already has appropriate interface
					if(targetInstances.get(targetIndex).getSource(edge.targetInterface.name) !== null) {
						newEdge.targetInterface = targetInstances.get(targetIndex).getSource(edge.targetInterface.name)
					} else {
						// if the target does not have the interface
						newEdge.targetInterface = edge.targetInterface.copy
						targetInstances.get(targetIndex).addInterface(newEdge.targetInterface)
					}
					// Copy the target port modifier of the original source, except roundbuffers
					if(!(newEdge.target instanceof SDFRoundBufferVertex)) {
						newEdge.targetPortModifier = edge.targetPortModifier
					} else {
						// The processing of round buffer is done after the while loop
					}
				} else {
					// if the target is join (new or not)/ roundbuffer with new ports
					val targetInterface = edge.targetInterface.copy
					var newInterfaceName = targetInterface.name + "_" + targetCons

					// Get the current index of the port (if any) and update it
					if(targetInterface.name.matches(SpecialActorPortsIndexer.INDEX_REGEX)) {
						val pattern = Pattern.compile(SpecialActorPortsIndexer.INDEX_REGEX)
						val matcher = pattern.matcher(targetInterface.name)
						matcher.find
						val existingIdx = Integer.decode(matcher.group(SpecialActorPortsIndexer.GROUP_XX))
						val newIdx = existingIdx + targetCons
						newInterfaceName = targetInterface.name.substring(0, matcher.start(SpecialActorPortsIndexer.GROUP_XX)) + newIdx
					}

					targetInterface.name = newInterfaceName
					newEdge.targetInterface = targetInterface
					newEdge.target.addInterface(targetInterface)
					// Add a target port modifier
					newEdge.targetPortModifier = new StringEdgePropertyType(SDFEdge.MODIFIER_READ_ONLY)
				}

				// Associate the interfaces to the new edge
		        // TODO Not sure what it does. Adding the edge and its associated interface was done in previous if-else block
		        // Why do it again? And setInterfaceVertexExternalLink purpose is ambiguous. Git blame shows this is a legacy code
		        // not touched by kdesnos
		        // I'm going to comment this part now and see if this changes anything

		        // if (targetInstances.get(targetIndex) instanceof SDFVertex) {
		        // if (((SDFVertex) targetInstances.get(targetIndex)).getSource(edge.getTargetInterface().getName()) != null) {
		        // inputVertex = ((SDFVertex) targetInstances.get(targetIndex)).getSource(edge.getTargetInterface().getName());
		        // ((SDFVertex) targetInstances.get(targetIndex)).setInterfaceVertexExternalLink(newEdge, inputVertex);
		        // }
		        // }
		        // if (sourceInstances.get(sourceIndex) instanceof SDFVertex) {
		        // if (((SDFVertex) sourceInstances.get(sourceIndex)).getSink(edge.getSourceInterface().getName()) != null) {
		        // outputVertex = ((SDFVertex) sourceInstances.get(sourceIndex)).getSink(edge.getSourceInterface().getName());
		        // ((SDFVertex) sourceInstances.get(sourceIndex)).setInterfaceVertexExternalLink(newEdge, outputVertex);
		        // }
		        // }

		        // Set the properties of the new edge
		        newEdge.prod = new LongEdgePropertyType(rest)
		        newEdge.cons = new LongEdgePropertyType(rest)
		        newEdge.dataType = edge.dataType
		        newEdge.delay = new LongEdgePropertyType(0)

		        absoluteTarget += rest
		        absoluteSource += rest
		        currentBufferSize += rest

		        // Below code although in ToHSDFVisitor class, does not occur in my case
		        if ((currentBufferSize == bufferSize) && (targetInstances.get(0) instanceof SDFInterfaceVertex)
		            && ((absoluteSource / edge.getProd().longValue()) < sourceInstances.size())) {
		          throw new PreesmRuntimeException("CurrentBuffersize never needs to be reset. There is a bug, so contact" + " Sudeep Kanur (skanur@abo.fi) with the SDF graph that caused this.")
		          // currentBufferSize = 0;
		        }
			}

			// if the edge target was a round buffer we set the port modifiers here
			if(edge.target instanceof SDFRoundBufferVertex) {
				// Set all the target modifiers as unused and sort list of input
				SpecialActorPortsIndexer.sortFifoList(newEdges, false);
				val iter = newEdges.listIterator()
				while(iter.hasNext()) {
					iter.next().targetPortModifier = new StringEdgePropertyType(SDFEdge.MODIFIER_UNUSED)
				}
				val portModifier = edge.targetPortModifier
				if( (portModifier !== null) && !portModifier.toString.equals(SDFEdge.MODIFIER_UNUSED)) {
					// If the target is unused, set last edges targetModifier as read-only
					var tokensToProduce = (edge.target.base.outgoingEdgesOf(edge.target) as Set<SDFEdge>).iterator.next.prod.longValue

					// Scan the edges in reverse order
					while ((tokensToProduce > 0) && iter.hasPrevious()) {
						val SDFEdge newEdge = iter.previous()
						newEdge.targetPortModifier = new StringEdgePropertyType(SDFEdge.MODIFIER_READ_ONLY)
						tokensToProduce -= newEdge.cons.longValue
					}
				}
			}
			// If fork/join vertices were added during the function call, put back the true source/target in the
			// instances copies map
			// TODO Check this is needed in my case
			// TODO Also add reverse map
			for(var ii = 0; ii < sourceInstances.size(); ii++) {
				if((sourceInstances.get(ii) instanceof SDFForkVertex)&& !originalSourceInstances.get(ii).equals(sourceInstances.get(ii))) {
					var SDFAbstractVertex trueSource = null
					// TODO Again, this code does not make sense. The size of incomingEdges will always be one!
					if (outputGraph.incomingEdgesOf(sourceInstances.get(ii)).size > 1) {
					    throw new PreesmRuntimeException("Explode instance has more than one edge from its source instance! "
							+ "Something is wrong in understanding and could be a potential bug! Please contact "
							+ "Sudeep Kanur (skanur@abo.fi) with the SDF that caused this." + "Number of outgoing edges for explode instance ("
							+ sourceInstances.get(ii).name + ") is: " + outputGraph.incomingEdgesOf(sourceInstances.get(ii)).size);
					}
					for(inEdge: outputGraph.incomingEdgesOf(sourceInstances.get(ii))) {
						trueSource = inEdge.source
					}
					sourceInstances.set(ii, trueSource)
				}
			}

			for(var ii = 0; ii < targetInstances.size(); ii++) {
				if((targetInstances.get(ii) instanceof SDFJoinVertex) && !originalTargetInstances.get(ii).equals(targetInstances.get(ii))) {
					var SDFAbstractVertex trueTarget = null
					// TODO Again, this code does not make sense. The size of incomingEdges will always be one!
					if (outputGraph.outgoingEdgesOf(targetInstances.get(ii)).size > 1) {
					    throw new PreesmRuntimeException("Implode instance has more than one edge from its target instance! "
							+ "Something is wrong in understanding and could be a potential bug! Please contact "
							+ "Sudeep Kanur (skanur@abo.fi) with the SDF that caused this." + "Number of incoming edges for implode instance ("
							+ targetInstances.get(ii).name + ") is: " + outputGraph.outgoingEdgesOf(targetInstances.get(ii)).size);
					}
					for(inEdge: outputGraph.outgoingEdgesOf(targetInstances.get(ii))) {
						trueTarget = inEdge.target
					}
					targetInstances.set(ii, trueTarget)
				}
			}
		}

		// Remove any unconnected implode and explode actors and related interfaces
		val removableVertices = newArrayList // Mark the vertices to be removed
		outputGraph.vertexSet.forEach[vertex |
			// Remove only those explode/implode instances that are not added by the user
			if(explodeImplodeOrigInstances.keySet.contains(vertex)) {
				if(outputGraph.incomingEdgesOf(vertex).empty) {
					if(vertex instanceof SDFJoinVertex) {
						removableVertices.add(vertex)
					}
				}
				if(outputGraph.outgoingEdgesOf(vertex).empty) {
					if(vertex instanceof SDFForkVertex) {
						removableVertices.add(vertex)
					}
				}
			}
		]
		removableVertices.forEach[vertex| // Remove the actual vertex
			outputGraph.removeVertex(vertex)
		]

		// Make sure all the ports are in order
		if(isInputSDFGraph && !SpecialActorPortsIndexer.checkIndexes(outputGraph)) {
			throw new PreesmRuntimeException("There are still special actors with non-indexed ports. Contact PREESM developers")
		}
		SpecialActorPortsIndexer.sortIndexedPorts(outputGraph)
	}

	/**
	 * Update the relevant data-structures associated with the class.
	 */
	protected def void updateMaps(SDFAbstractVertex sourceInstance, SDFAbstractVertex exImInstance) {
		explodeImplodeOrigInstances.put(exImInstance, sourceInstance)
		val actor = instance2Actor.get(sourceInstance)
		val updatedInstances = actor2Instances.get(actor)
		updatedInstances.add(exImInstance)
		actor2Instances.put(actor, updatedInstances)
		instance2Actor.put(exImInstance, actor)
	}
}
