/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2018)
 * Hamza Deroui <hamza.deroui@insa-rennes.fr> (2017 - 2018)
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
package org.ietr.preesm.throughput.tools.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map.Entry;
import org.ietr.dftools.algorithm.model.AbstractVertex;
import org.ietr.dftools.algorithm.model.IInterface;
import org.ietr.dftools.algorithm.model.parameters.InvalidExpressionException;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.algorithm.model.sdf.SDFInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFVertex;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSinkInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSourceInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.types.SDFIntEdgePropertyType;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.throughput.tools.parsers.Identifier;

/**
 * @author hderoui
 *
 *         A structure helper to simplify the manipulation of the structure of a SDF graph
 *
 */
public abstract class GraphStructureHelper {

  /**
   * Adds a new edge to an SDF graph.
   *
   * @param graph
   *          SDF graph
   * @param srcActorName
   *          the name of the source actor
   * @param srcPortName
   *          the name of the source port
   * @param trgActorName
   *          the name of the target actor
   * @param trgPortName
   *          the name of the target port
   * @param prod_rate
   *          the production rate of the edge
   * @param cons_rate
   *          the consumption rate of the edge
   * @param delay
   *          the delay of the edge
   * @param base
   *          the parent edge
   * @return the created SDF edge
   */

  public static SDFEdge addEdge(final SDFGraph graph, final String srcActorName, final String srcPortName, final String trgActorName, final String trgPortName,
      final int prod_rate, final int cons_rate, final int delay, final SDFEdge base) {
    // get the source actor if not exists create one
    SDFAbstractVertex srcActor = graph.getVertex(srcActorName);
    if (srcActor == null) {
      srcActor = GraphStructureHelper.addActor(graph, srcActorName, null, null, null, null, null);
    }
    // get the source port if not create one
    SDFInterfaceVertex srcPort;
    if (srcPortName != null) {
      srcPort = srcActor.getInterface(srcPortName);
      if (srcPort == null) {
        srcPort = GraphStructureHelper.addSinkPort(srcActor, srcPortName, prod_rate);
      }
    } else {
      srcPort = GraphStructureHelper.addSinkPort(srcActor, Identifier.generateOutputPortId() + "_to_" + trgActorName, prod_rate);
    }

    // get the target actor if not exists create one
    SDFAbstractVertex tgtActor = graph.getVertex(trgActorName);
    if (tgtActor == null) {
      tgtActor = GraphStructureHelper.addActor(graph, trgActorName, null, null, null, null, null);
    }
    // get the target port if not exists create one
    SDFInterfaceVertex tgtPort;
    if (trgPortName != null) {
      tgtPort = tgtActor.getInterface(trgPortName);
      if (tgtPort == null) {
        tgtPort = GraphStructureHelper.addSrcPort(tgtActor, trgPortName, cons_rate);
      }
    } else {
      tgtPort = GraphStructureHelper.addSrcPort(tgtActor, Identifier.generateInputPortId() + "_from_" + srcActorName, cons_rate);
    }

    // add the edge to the srSDF graph
    final SDFEdge edge = graph.addEdge(srcActor, srcPort, tgtActor, tgtPort);
    edge.setPropertyValue("edgeName", "from_" + srcActorName + "_" + srcPortName + "_to_" + trgActorName + "_" + trgPortName);
    edge.setPropertyValue("edgeId", Identifier.generateEdgeId());
    edge.setProd(new SDFIntEdgePropertyType(prod_rate));
    edge.setCons(new SDFIntEdgePropertyType(cons_rate));
    edge.setDelay(new SDFIntEdgePropertyType(delay));
    edge.setPropertyValue("baseEdge", base);

    return edge;
  }

  /**
   * Adds an actor to an SDF graph
   *
   * @param graph
   *          SDF graph
   * @param actorName
   *          name of the actor
   * @param subgraph
   *          subgraph of the actor if is hierarchical
   * @param rv
   *          repetition factor of the actor
   * @param l
   *          the execution duration of the actor
   * @param z
   *          the normalized consumption/production rate of the actor
   * @param Base
   *          the actor base
   * @return the created actor
   */
  public static SDFAbstractVertex addActor(final SDFGraph graph, final String actorName, final SDFGraph subgraph, final Integer rv, final Double l,
      final Double z, final SDFAbstractVertex Base) {
    final SDFAbstractVertex actor = new SDFVertex(graph);
    // set the name
    actor.setId(actorName);
    actor.setName(actorName);

    // add the subgraph if the actor is hierarchical
    if (subgraph != null) {
      actor.setGraphDescription(subgraph);
    }

    // set the repetition factor
    if (rv != null) {
      actor.setNbRepeat(rv);
    }

    // set the execution duration
    if (l != null) {
      actor.setPropertyValue("duration", l);
    }

    // set the normalized consumption/production rate
    if (z != null) {
      actor.setPropertyValue("normalizedRate", z);
    }

    // set the base actor
    if (Base != null) {
      actor.setPropertyValue("baseActor", Base);
    } else {
      actor.setPropertyValue("baseActor", actor);
    }

    graph.addVertex(actor);
    return actor;
  }

  /**
   * add an input interface to a subgraph
   *
   * @param graph
   *          the SDF subgraph
   * @param interfaceName
   *          the name of the input interface (the same name of its associated port in its hierarchical parent actor)
   * @param rv
   *          the repetition factor of the interface
   * @param l
   *          the execution duration of the interface
   * @param z
   *          the normalized rate
   * @param Base
   *          the base interface of the input interface
   * @return the created input interface
   */
  public static SDFSourceInterfaceVertex addInputInterface(final SDFGraph graph, final String interfaceName, final Integer rv, final Double l, final Double z,
      final SDFAbstractVertex Base) {
    // an interface is an independent actor from its hierarchical parent actor but with the same name of its associated port in the hierarchical parent actor

    // create an input interface and set the parent graph
    final SDFSourceInterfaceVertex in = new SDFSourceInterfaceVertex();
    in.setPropertyValue(AbstractVertex.BASE, graph);

    // set the name
    in.setId(interfaceName);
    in.setName(interfaceName);

    // set the repetition factor
    if (rv != null) {
      in.setNbRepeat(rv);
    }

    // set the execution duration
    if (l != null) {
      in.setPropertyValue("duration", l);
    }

    // set the normalized consumption/production rate
    if (z != null) {
      in.setPropertyValue("normalizedRate", z);
    }

    // set the base actor
    if (Base != null) {
      in.setPropertyValue("baseActor", Base);
    } else {
      in.setPropertyValue("baseActor", in);
    }

    graph.addVertex(in);
    return in;

  }

  /**
   * add an output interface to a subgraph
   *
   * @param graph
   *          the SDF subgraph
   * @param interfaceName
   *          the name of the output interface (the same name of its associated port in its hierarchical parent actor)
   * @param rv
   *          the repetition factor of the interface
   * @param l
   *          the execution duration of the interface
   * @param z
   *          the normalized rate
   * @param Base
   *          the base interface of the output interface
   * @return the created output interface
   */
  public static SDFSinkInterfaceVertex addOutputInterface(final SDFGraph graph, final String interfaceName, final Integer rv, final Double l, final Double z,
      final SDFAbstractVertex Base) {
    // an interface is an independent actor from its hierarchical parent actor but with the same name of its associated port in the hierarchical actor

    // create an output interface and set the parent graph
    final SDFSinkInterfaceVertex out = new SDFSinkInterfaceVertex();
    out.setPropertyValue(AbstractVertex.BASE, graph);

    // set the name
    out.setId(interfaceName);
    out.setName(interfaceName);

    // set the repetition factor
    if (rv != null) {
      out.setNbRepeat(rv);
    }

    // set the execution duration
    if (l != null) {
      out.setPropertyValue("duration", l);
    }

    // set the normalized consumption/production rate
    if (z != null) {
      out.setPropertyValue("normalizedRate", z);
    }

    // set the base actor
    if (Base != null) {
      out.setPropertyValue("baseActor", Base);
    } else {
      out.setPropertyValue("baseActor", out);
    }

    graph.addVertex(out);
    return out;
  }

  /**
   * Adds a source port to an actor
   *
   * @param actor
   *          SDF actor
   * @param portName
   *          name of the port
   * @param portRate
   *          rate of the port
   * @return created port
   */
  public static SDFSourceInterfaceVertex addSrcPort(final SDFAbstractVertex actor, final String portName, final Integer portRate) {
    final SDFSourceInterfaceVertex port = new SDFSourceInterfaceVertex();
    port.setId(portName);
    port.setName(portName);
    port.setPropertyValue("port_rate", portRate);
    actor.addInterface(port);
    return port;
  }

  /**
   * Adds a sink port to an actor
   *
   * @param actor
   *          SDF actor
   * @param portName
   *          name of the actor
   * @param portRate
   *          rate of the actor
   * @return created port
   */
  public static SDFSinkInterfaceVertex addSinkPort(final SDFAbstractVertex actor, final String portName, final Integer portRate) {
    final SDFSinkInterfaceVertex port = new SDFSinkInterfaceVertex();
    port.setId(portName);
    port.setName(portName);
    port.setPropertyValue("port_rate", portRate);
    actor.addInterface(port);
    return port;
  }

  /**
   * returns a list of all the hierarchical actors in the hierarchy
   *
   * @param graph
   *          IBSDF graph
   * @return the list of hierarchical actors
   */
  public static Hashtable<String, SDFAbstractVertex> getAllHierarchicalActors(final SDFGraph graph) {
    // list of hierarchical actors
    final Hashtable<String, SDFAbstractVertex> hierarchicalActorsList = new Hashtable<>();
    final ArrayList<SDFGraph> subgraphsToCheck = new ArrayList<>();

    // add the hierarchical actors of the top graph
    for (final SDFAbstractVertex a : graph.vertexSet()) {
      if (a.getGraphDescription() != null) {
        hierarchicalActorsList.put(a.getName(), a);
        subgraphsToCheck.add((SDFGraph) a.getGraphDescription());
      }
    }

    // process all subgraphs in the hierarchy
    while (!subgraphsToCheck.isEmpty()) {
      for (final SDFAbstractVertex a : subgraphsToCheck.get(0).vertexSet()) {
        if (a.getGraphDescription() != null) {
          hierarchicalActorsList.put(a.getName(), a);
          subgraphsToCheck.add((SDFGraph) a.getGraphDescription());
        }
      }
      subgraphsToCheck.remove(0);
    }

    return hierarchicalActorsList;
  }

  /**
   * returns the hierarchical actors of an SDF graph
   *
   * @param graph
   *          SDF graph
   * @return list of hierarchical actors
   */
  public static Hashtable<String, SDFAbstractVertex> getHierarchicalActors(final SDFGraph graph) {
    // list of hierarchical actors
    final Hashtable<String, SDFAbstractVertex> hierarchicalActorsList = new Hashtable<>();
    for (final SDFAbstractVertex a : graph.vertexSet()) {
      if (a.getGraphDescription() != null) {
        hierarchicalActorsList.put(a.getName(), a);
      }
    }
    return hierarchicalActorsList;
  }

  /**
   * replace a hierarchical actor by a sdf/srSDF graph
   *
   * @param parentGraph
   *          the graph containing the hierarchical actor
   * @param h
   *          the hierarchical actor
   * @param replacementGraph
   *          the replacement graph
   */
  public static void replaceHierarchicalActor(final SDFGraph parentGraph, final SDFAbstractVertex h, final SDFGraph replacementGraph) {

    // Step 1: add the replacement subgraph into the parent graph
    for (final SDFAbstractVertex a : replacementGraph.vertexSet()) {
      GraphStructureHelper.addActor(parentGraph, h.getName() + "_" + a.getName(), (SDFGraph) a.getGraphDescription(), 1,
          (Double) a.getPropertyBean().getValue("duration"), null, (SDFAbstractVertex) a.getPropertyBean().getValue("baseActor"));
    }
    for (final SDFEdge e : replacementGraph.edgeSet()) {
      final SDFEdge e_clone = GraphStructureHelper.addEdge(parentGraph, h.getName() + "_" + e.getSource().getName(), null,
          h.getName() + "_" + e.getTarget().getName(), null, e.getProd().intValue(), e.getCons().intValue(), e.getDelay().intValue(),
          (SDFEdge) e.getPropertyBean().getValue("baseedge"));

      // copy properties
      if (e.getPropertyBean().getValue("weight_LP") != null) {
        e_clone.setPropertyValue("weight_LP", (double) e.getPropertyBean().getValue("weight_LP"));
      }
    }

    // Step 2: connect the interfaces of the added graph with actors of the parent graph
    // disconnect the edges from the hierarchical actor and connect them to their associated interface in the subgraph
    ArrayList<SDFEdge> edges = new ArrayList<>();
    for (final SDFInterfaceVertex input : h.getSources()) {
      edges.add(h.getAssociatedEdge(input));
    }
    for (final SDFEdge edge : edges) {
      final String hierarchicalPortName = ((SDFEdge) edge.getPropertyBean().getValue("baseEdge")).getTargetInterface().getName();
      final String subgraphInterfaceName = h.getName() + "_" + hierarchicalPortName + "_1";
      GraphStructureHelper.replaceEdgeTargetActor(parentGraph, edge, subgraphInterfaceName, null);
    }

    // disconnect the edges from the hierarchical actor and connect them to their associated interface in the subgraph
    edges = new ArrayList<>();
    for (final SDFInterfaceVertex output : h.getSinks()) {
      edges.add(h.getAssociatedEdge(output));
    }
    for (final SDFEdge edge : edges) {
      final String hierarchicalPortName = ((SDFEdge) edge.getPropertyBean().getValue("baseEdge")).getSourceInterface().getName();
      final String subgraphInterfaceName = h.getName() + "_" + hierarchicalPortName + "_1";
      GraphStructureHelper.replaceEdgeSourceActor(parentGraph, edge, subgraphInterfaceName, null);
    }

    // remove the hierarchical actor from the parent graph
    parentGraph.removeVertex(h);
  }

  /**
   * replaces the source actor of an edge by a new actor and a new source port
   *
   * @param graph
   *          SDF graph
   * @param edge
   *          SDF Edge
   * @param newSourceActor
   *          new source actor
   * @param newSourcePort
   *          new source port
   */
  public static SDFEdge replaceEdgeSourceActor(final SDFGraph graph, final SDFEdge edge, final String newSourceActor, final String newSourcePort) {
    // create the new edge
    final SDFEdge nweEdge = GraphStructureHelper.addEdge(graph, newSourceActor, newSourcePort, edge.getTarget().getName(), edge.getTargetInterface().getName(),
        edge.getProd().intValue(), edge.getCons().intValue(), edge.getDelay().intValue(), (SDFEdge) edge.getPropertyBean().getValue("baseEdge"));
    // remove the old edge
    graph.removeEdge(edge);
    return nweEdge;
  }

  /**
   * replaces the target actor of an edge by a new actor and a new target port
   *
   * @param graph
   *          SDF graph
   * @param edge
   *          SDF Edge
   * @param newTargetActor
   *          new target Actor
   * @param newTargetPort
   *          new target port
   */
  public static SDFEdge replaceEdgeTargetActor(final SDFGraph graph, final SDFEdge edge, final String newTargetActor, final String newTargetPort) {
    // create the new edge
    final SDFEdge nweEdge = GraphStructureHelper.addEdge(graph, edge.getSource().getName(), edge.getSourceInterface().getName(), newTargetActor, newTargetPort,
        edge.getProd().intValue(), edge.getCons().intValue(), edge.getDelay().intValue(), (SDFEdge) edge.getPropertyBean().getValue("baseEdge"));
    // remove the old edge
    graph.removeEdge(edge);
    return nweEdge;
  }

  /**
   * re-time the IBSDF graph to reveal the hidden delays
   *
   * @param graph
   *          IBSDF graph
   */
  public static void retime(final SDFGraph graph) {
    // reveal the hidden delays of each hierarchical actor in the top graph
    for (final SDFAbstractVertex actor : graph.vertexSet()) {
      if (actor.getGraphDescription() != null) {
        GraphStructureHelper.revealHiddenDelays(actor);
      }
    }
  }

  /***
   * reveal the hidden delays of the hierarchical actor subgraph
   *
   * @param h
   *          hierarchical actor
   */
  public static void revealHiddenDelays(final SDFAbstractVertex h) {
    // get the subgraph of the hierarchical actor
    final SDFGraph subgraph = (SDFGraph) h.getGraphDescription();

    // reveal the hidden delays of each hierarchical actor in the subgraph
    for (final SDFAbstractVertex actor : subgraph.vertexSet()) {
      if (actor.getGraphDescription() != null) {
        GraphStructureHelper.revealHiddenDelays(actor);
      }
    }

    // execute the subgraph to reveal the hidden delays
    // Step 1: add an empty loop edge for each actor with no inputs (including inputInterfaces)
    final ArrayList<SDFEdge> emptyLoopsList = new ArrayList<>();
    for (final SDFAbstractVertex actor : subgraph.vertexSet()) {
      if (actor.getSources().isEmpty()) {
        final SDFEdge e = GraphStructureHelper.addEdge(subgraph, actor.getName(), null, actor.getName(), null, 1, 1, 0, null);
        emptyLoopsList.add(e);
      }
    }

    // Step 2: execute actor as long as they are ready
    final GraphSimulationHelper simulator = new GraphSimulationHelper(subgraph);
    Hashtable<SDFAbstractVertex, Integer> readyActors = simulator.getReadyActorsNbExecutions();
    while (!readyActors.isEmpty()) {
      for (final Entry<SDFAbstractVertex, Integer> e : readyActors.entrySet()) {
        simulator.execute(e.getKey(), e.getValue());
      }
      readyActors = simulator.getReadyActorsNbExecutions();
    }

    // Step3: remove the empty loops
    for (final SDFEdge e : emptyLoopsList) {
      subgraph.removeEdge(e);
    }

    // for each output interface add RV(h)*n*prod delays on its output edge
    for (final SDFAbstractVertex actor : subgraph.vertexSet()) {
      if (actor instanceof SDFSinkInterfaceVertex) {
        final SDFEdge e = h.getAssociatedEdge(h.getInterface(actor.getName()));
        final int oldMarking = e.getDelay().intValue();
        final int newMarking = oldMarking + (h.getNbRepeatAsInteger() * (simulator.getExecutionCounter(actor) * e.getProd().intValue()));
        e.setDelay(new SDFIntEdgePropertyType(newMarking));
      }
    }

    // save the current state as the initial state
    simulator.resetExecutionCounter();
  }

  /**
   * computes a topological sorting of the complete DAG
   *
   * @param DAG
   *          graph
   * @return list of sorted actors
   */
  public static ArrayList<SDFAbstractVertex> topologicalSorting(final SDFGraph DAG) {
    // the sorted list (inverse order of topological sorting)
    // TODO uses stack instead
    final ArrayList<SDFAbstractVertex> sortedActors = new ArrayList<>();

    // Mark all the actors as not visited
    final Hashtable<String, Boolean> visited = new Hashtable<>();
    for (final SDFAbstractVertex actor : DAG.vertexSet()) {
      // System.out.println("processing actor: " + actor.getName());
      visited.put(actor.getName(), false);
    }

    // Topological Sort starting from all actors one by on
    for (final SDFAbstractVertex actor : DAG.vertexSet()) {
      if (!visited.get(actor.getName())) {
        GraphStructureHelper.recurciveTopologicalSorting(actor, visited, sortedActors);
      }
    }

    // inverse the order to get the final list
    Collections.reverse(sortedActors);
    return sortedActors;
  }

  /**
   * computes a topological sorting of the DAG starting from a source actor
   *
   * @param source
   *          actor to start from
   * @param visited
   *          list of visited actors for a complete topological sorting
   * @param sortedActors
   *          the resulted list
   */
  private static void recurciveTopologicalSorting(final SDFAbstractVertex source, final Hashtable<String, Boolean> visited,
      final ArrayList<SDFAbstractVertex> sortedActors) {
    // Mark the current actor as visited
    visited.put(source.getName(), true);

    // explore all the target sink actors of the current source
    for (final SDFInterfaceVertex output : source.getSinks()) {
      // get the target actor
      final SDFAbstractVertex target = source.getAssociatedEdge(output).getTarget();
      if (!visited.containsKey(target.getName()) || !visited.get(target.getName())) {
        GraphStructureHelper.recurciveTopologicalSorting(target, visited, sortedActors);
      }
    }

    // add the current actor to the sorted list
    sortedActors.add(source);

  }

  /**
   * computes a topological sorting of the DAG starting from a source actor
   *
   * @param source
   *          actor to start from
   * @return list of sorted actors
   */
  public static ArrayList<SDFAbstractVertex> partialTopologicalSorting(final SDFAbstractVertex source) {
    // prepare the lists
    final Hashtable<String, Boolean> visited = new Hashtable<>();
    final ArrayList<SDFAbstractVertex> sortedActors = new ArrayList<>();

    // do a partial topological sorting
    GraphStructureHelper.recurciveTopologicalSorting(source, visited, sortedActors);

    // return the final list
    Collections.reverse(sortedActors);
    return sortedActors;
  }

  /**
   * Computes the longest path of a DAG
   *
   * @param source
   *          source actor
   * @return list of distances
   */
  public static Hashtable<String, Double> getLongestPathToAllTargets(final SDFAbstractVertex source, final PreesmScenario scenario,
      ArrayList<SDFAbstractVertex> topoSortList) {

    // get the topological sorting of the graph
    if (topoSortList == null) {
      topoSortList = GraphStructureHelper.partialTopologicalSorting(source);
    }

    // table of distances
    final Hashtable<String, Double> distance = new Hashtable<>();
    for (final SDFAbstractVertex actor : topoSortList) {
      distance.put(actor.getName(), Double.NEGATIVE_INFINITY);
    }
    distance.replace(source.getName(), 0.);

    for (int i = 0; i < topoSortList.size(); i++) {
      // get the current source actor
      final SDFAbstractVertex current_source = topoSortList.get(i);

      // define the edge weight as the duration of the current source actor
      double actorDuration;
      if (scenario != null) {
        actorDuration = scenario.getTimingManager().getTimingOrDefault(current_source.getId(), "x86").getTime();
      } else {
        actorDuration = (double) current_source.getPropertyBean().getValue("duration");
      }

      // update the distances
      for (final SDFInterfaceVertex output : current_source.getSinks()) {
        double edgeWeight = actorDuration;
        // get the associated output edge and its weight if defined
        final SDFEdge outputEdge = current_source.getAssociatedEdge(output);
        if (outputEdge.getPropertyBean().getValue("weight_LP") != null) {
          edgeWeight = (double) outputEdge.getPropertyBean().getValue("weight_LP");
        }

        // get the target actor of the associated output edge
        // SDFAbstractVertex current_target = current_source.getAssociatedEdge(output).getTarget();
        final SDFAbstractVertex current_target = outputEdge.getTarget();

        // update the distance of the current target
        if (distance.get(current_target.getName()) < (distance.get(current_source.getName()) + edgeWeight)) {
          distance.replace(current_target.getName(), distance.get(current_source.getName()) + edgeWeight);
        }
      }
    }

    return distance;
  }

  /**
   * @param source
   *          actor
   * @param target
   *          actor
   * @return value of the longest path between the source and the target
   */
  public static double getLongestPathToTarget(final SDFAbstractVertex source, final SDFAbstractVertex target, final PreesmScenario scenario,
      ArrayList<SDFAbstractVertex> topoSortList) {

    // get the topological sorting of the graph
    if (topoSortList == null) {
      topoSortList = GraphStructureHelper.partialTopologicalSorting(source);
    }

    // table of distances
    final Hashtable<String, Double> distance = new Hashtable<>();
    for (final SDFAbstractVertex actor : topoSortList) {
      distance.put(actor.getName(), Double.NEGATIVE_INFINITY);
    }
    distance.replace(source.getName(), 0.);

    for (int i = 0; i < topoSortList.size(); i++) {
      // get the current source actor
      final SDFAbstractVertex current_source = topoSortList.get(i);

      // check if the target actor is reached
      if (current_source.getName().equals(target.getName())) {
        return distance.get(target.getName());
      } else {

        // define the edge weight as the duration of the current source actor
        double actorDuration;
        if (scenario != null) {
          actorDuration = scenario.getTimingManager().getTimingOrDefault(current_source.getId(), "x86").getTime();
        } else {
          actorDuration = (double) current_source.getPropertyBean().getValue("duration");
        }

        // update the distances
        for (final SDFInterfaceVertex output : current_source.getSinks()) {
          double edgeWeight = actorDuration;
          // get the associated output edge and its weight if defined
          final SDFEdge outputEdge = current_source.getAssociatedEdge(output);
          if (outputEdge.getPropertyBean().getValue("weight_LP") != null) {
            edgeWeight = (double) outputEdge.getPropertyBean().getValue("weight_LP");
          }

          // get the target actor of the associated output edge
          // SDFAbstractVertex current_target = current_source.getAssociatedEdge(output).getTarget();
          final SDFAbstractVertex current_target = outputEdge.getTarget();

          // update the distance of the current target
          if (distance.get(current_target.getName()) < (distance.get(current_source.getName()) + edgeWeight)) {
            distance.replace(current_target.getName(), distance.get(current_source.getName()) + edgeWeight);
          }
        }
      }
    }

    // return the distance from the source to the target
    return distance.get(target.getName());
  }

  /**
   * @param DAG
   *          graph
   * @return value of the longest path in the graph
   */
  public static double getLongestPath(final SDFGraph DAG, final PreesmScenario scenario, ArrayList<SDFAbstractVertex> topoSortList) {
    // add a source and a target actor
    final SDFAbstractVertex source = GraphStructureHelper.addActor(DAG, "S_LongestPath", null, 1, 0., null, null);
    final SDFAbstractVertex target = GraphStructureHelper.addActor(DAG, "T_LongestPath", null, 1, 0., null, null);

    // connect the source to all input actors and the target to all output actors
    for (final SDFAbstractVertex actor : DAG.vertexSet()) {
      // check if the actor has no input edges
      if (actor.getSources().isEmpty()) {
        // add an edge between the source and the current actor
        GraphStructureHelper.addEdge(DAG, source.getName(), null, actor.getName(), null, 1, 1, 0, null);
      }
      // check if the actor has no output edges
      if (actor.getSinks().isEmpty()) {
        // add an edge between the current actor and the target
        GraphStructureHelper.addEdge(DAG, actor.getName(), null, target.getName(), null, 1, 1, 0, null);
      }
    }

    // get the topological sorting of the graph
    if (topoSortList == null) {
      topoSortList = GraphStructureHelper.topologicalSorting(DAG);
    } else {
      topoSortList.add(0, source);
      topoSortList.add(topoSortList.size(), target);
    }

    // compute the longest path from the source
    final Hashtable<String, Double> distance = GraphStructureHelper.getLongestPathToAllTargets(source, scenario, topoSortList);
    final double lp = distance.get(target.getName());

    // remove the source and the target from the graph
    DAG.removeVertex(source);
    DAG.removeVertex(target);

    return lp;
  }

  /**
   * Clone an IBSDF graph
   *
   * @param ibsdf
   *          input IBSDF graph
   * @return IBSDF graph
   */
  public static SDFGraph cloneIBSDF(final SDFGraph ibsdf) {
    // list of hierarchical actor to be cloned
    final ArrayList<SDFAbstractVertex> hierarchicalActorsToBeCloned = new ArrayList<>();

    // clone the tograph
    final SDFGraph clonedIBSDF = ibsdf.clone();

    // add the hierarchical actors of the new top-graph to the list
    Hashtable<String, SDFAbstractVertex> hActors = GraphStructureHelper.getHierarchicalActors(clonedIBSDF);
    for (final SDFAbstractVertex a : hActors.values()) {
      hierarchicalActorsToBeCloned.add(a);
    }

    // for each hierarchical actor clone its subgraph and set the new graph as its original subgraph
    while (!hierarchicalActorsToBeCloned.isEmpty()) {
      // get a hierarchical actor
      final SDFAbstractVertex h = hierarchicalActorsToBeCloned.get(0);
      // clone its subgraph
      final SDFGraph cloneSubgraph = ((SDFGraph) h.getGraphDescription()).clone();
      // set the subgraph cloned
      h.setGraphDescription(cloneSubgraph);

      // add the hierarchical actors of the new subgraph to the list
      hActors = GraphStructureHelper.getHierarchicalActors(cloneSubgraph);
      for (final SDFAbstractVertex a : hActors.values()) {
        hierarchicalActorsToBeCloned.add(a);
      }

      // remove the hierarchical actor proceeded
      hierarchicalActorsToBeCloned.remove(0);
    }

    return clonedIBSDF;
  }

  /**
   * Print a IBSDF subgraph
   *
   * @param graph
   *          IBSDF subgraph
   */
  public static void printSDF(final SDFGraph graph) {
    System.out.println("=> Liste des vertex :");
    for (final SDFAbstractVertex actor : graph.vertexSet()) {
      try {
        System.out.println(actor.getKind() + "  " + actor.getName() + ", rv= " + actor.getPropertyBean().getValue(SDFAbstractVertex.NB_REPEAT) + ", dur="
            + actor.getPropertyBean().getValue("duration"));

        if (actor.getGraphDescription() != null) {
          // System.out.println("Hierarchical duration = " + scenario.getTimingManager().generateVertexTimingFromHierarchy(actor, "x86"));
        }

      } catch (final InvalidExpressionException e) {
        e.printStackTrace();
      }

      System.out.println("\t interfaces : ");
      for (final IInterface inter : actor.getInterfaces()) {
        final SDFInterfaceVertex _interface = (SDFInterfaceVertex) inter;
        System.out.println("\t\t interface port name " + _interface.getName() + " : " + actor.getAssociatedEdge(_interface).toString());
      }

      System.out.println("\t inputs : ");
      for (final SDFInterfaceVertex input : actor.getSources()) {
        System.out.println("\t\t input port name " + input.getName() + " : " + actor.getAssociatedEdge(input).toString());
      }

      System.out.println("\t outputs : ");
      for (final SDFInterfaceVertex output : actor.getSinks()) {
        System.out.println("\t\t output port name " + output.getName() + " : " + actor.getAssociatedEdge(output).toString());
      }
    }

    System.out.println("\n=> Liste des edges :");
    for (final SDFEdge edge : graph.edgeSet()) {
      System.out.println("name: " + edge.toString());
      System.out.println("e(" + edge.getSource().getName() + "," + edge.getTarget().getName() + "), p(" + edge.getSourceInterface().getName() + ","
          + edge.getTargetInterface().getName() + "), prod=" + edge.getProd() + " cons= " + edge.getCons() + " M0= " + edge.getDelay());
    }

    System.out.println("---------------------------");
  }

}