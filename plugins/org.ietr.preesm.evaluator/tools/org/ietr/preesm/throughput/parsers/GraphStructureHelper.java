package org.ietr.preesm.throughput.parsers;

import java.util.ArrayList;
import java.util.Hashtable;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.algorithm.model.sdf.SDFInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFVertex;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSinkInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSourceInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.types.SDFIntEdgePropertyType;

/**
 * @author hderoui
 *
 *         A structure helper to simplify the manipulation of an SDF graph structure
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
   * @param prod
   *          the production rate of the edge
   * @param cons
   *          the consumption of the edge
   * @param d
   *          the delay of the edge
   * @param base
   *          the base edge
   * @return the created edge
   */
  public static SDFEdge addEdge(SDFGraph graph, String srcActorName, String srcPortName, String trgActorName, String trgPortName, int prod_rate, int cons_rate,
      int delay, SDFEdge base) {
    // get the source actor if not exists create one
    SDFAbstractVertex srcActor = graph.getVertex(srcActorName);
    if (srcActor == null) {
      srcActor = addActor(graph, srcActorName, null, null, null, null, null);
    }
    // get the source port if not create one
    SDFInterfaceVertex srcPort;
    if (srcPortName != null) {
      srcPort = srcActor.getInterface(srcPortName);
      if (srcPort == null) {
        srcPort = addSinkPort(srcActor, srcPortName, prod_rate);
      }
    } else {
      srcPort = addSinkPort(srcActor, Identifier.generateOutputPortId() + "_to_" + trgActorName, prod_rate);
    }

    // get the target actor if not exists create one
    SDFAbstractVertex tgtActor = graph.getVertex(trgActorName);
    if (tgtActor == null) {
      tgtActor = addActor(graph, trgActorName, null, null, null, null, null);
    }
    // get the target port if not exists create one
    SDFInterfaceVertex tgtPort;
    if (trgPortName != null) {
      tgtPort = tgtActor.getInterface(trgPortName);
      if (tgtPort == null) {
        tgtPort = addSrcPort(tgtActor, trgPortName, cons_rate);
      }
    } else {
      tgtPort = addSrcPort(tgtActor, Identifier.generateInputPortId() + "_from_" + srcActorName, cons_rate);
    }

    // add the edge to the srSDF graph
    SDFEdge edge = graph.addEdge(srcActor, srcPort, tgtActor, tgtPort);
    // edge.setPropertyValue("edgeName", "from_" + e.getSource().getName() + "_" + i + "_to_" + e.getTarget().getName() + "_" + j);
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
  public static SDFVertex addActor(SDFGraph graph, String actorName, SDFGraph subgraph, Integer rv, Double l, Double z, SDFAbstractVertex Base) {
    SDFVertex actor = new SDFVertex(graph);
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

  public static void addInputInterface(SDFGraph graph, String actorName, SDFGraph subgraph, Integer rv, Double l, Double z, SDFAbstractVertex Base) {

  }

  public static void addOutputInterface(SDFGraph graph, String actorName, SDFGraph subgraph, Integer rv, Double l, Double z, SDFAbstractVertex Base) {

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
  public static SDFSourceInterfaceVertex addSrcPort(SDFAbstractVertex actor, String portName, Integer portRate) {
    SDFSourceInterfaceVertex port = new SDFSourceInterfaceVertex();
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
  public static SDFSinkInterfaceVertex addSinkPort(SDFAbstractVertex actor, String portName, Integer portRate) {
    SDFSinkInterfaceVertex port = new SDFSinkInterfaceVertex();
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
  public static Hashtable<String, SDFAbstractVertex> getAllHierarchicalActors(SDFGraph graph) {
    // list of hierarchical actors
    Hashtable<String, SDFAbstractVertex> hierarchicalActorsList = new Hashtable<String, SDFAbstractVertex>();
    ArrayList<SDFGraph> subgraphsToCheck = new ArrayList<SDFGraph>();

    // add the hierarchical actors of the top graph
    for (SDFAbstractVertex a : graph.vertexSet()) {
      if (a.getGraphDescription() != null) {
        hierarchicalActorsList.put(a.getName(), a);
        subgraphsToCheck.add((SDFGraph) a.getGraphDescription());
      }
    }

    // process all subgraphs in the hierarchy
    while (!subgraphsToCheck.isEmpty()) {
      for (SDFAbstractVertex a : subgraphsToCheck.get(0).vertexSet()) {
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
  public static Hashtable<String, SDFAbstractVertex> getHierarchicalActors(SDFGraph graph) {
    // list of hierarchical actors
    Hashtable<String, SDFAbstractVertex> hierarchicalActorsList = new Hashtable<String, SDFAbstractVertex>();
    for (SDFAbstractVertex a : graph.vertexSet()) {
      if (a.getGraphDescription() != null) {
        hierarchicalActorsList.put(a.getName(), a);
      }
    }
    return hierarchicalActorsList;
  }

  public static void replaceHierarchicalActor(SDFGraph parentGraph, SDFAbstractVertex h, SDFGraph replacementGraph) {

    // Step 1: add the replacement subgraph into the parent graph
    for (SDFAbstractVertex a : replacementGraph.vertexSet()) {
      GraphStructureHelper.addActor(parentGraph, h.getName() + "_" + a.getName(), (SDFGraph) a.getGraphDescription(), 1,
          (double) a.getPropertyBean().getValue("duration"), null, (SDFAbstractVertex) a.getPropertyBean().getValue("baseActor"));
    }
    for (SDFEdge e : replacementGraph.edgeSet()) {
      GraphStructureHelper.addEdge(parentGraph, h.getName() + "_" + e.getSource().getName(), null, h.getName() + "_" + e.getTarget().getName(), null,
          e.getProd().intValue(), e.getCons().intValue(), e.getDelay().intValue(), (SDFEdge) e.getPropertyBean().getValue("baseedge"));
    }

    // Step 2: connect the interfaces of the added graph with actors of the parent graph
    // disconnect the edges from the hierarchical actor and connect them to their associated interface in the subgraph
    ArrayList<SDFEdge> edges = new ArrayList<SDFEdge>();
    for (SDFInterfaceVertex input : h.getSources()) {
      edges.add(h.getAssociatedEdge(input));
    }
    for (SDFEdge edge : edges) {
      String hierarchicalPortName = ((SDFEdge) edge.getPropertyBean().getValue("baseEdge")).getTargetInterface().getName();
      String subgraphInterfaceName = h.getName() + "_" + hierarchicalPortName + "_1";
      replaceEdgeTargetActor(parentGraph, edge, subgraphInterfaceName, null);
    }

    // disconnect the edges from the hierarchical actor and connect them to their associated interface in the subgraph
    edges = new ArrayList<SDFEdge>();
    for (SDFInterfaceVertex output : h.getSinks()) {
      edges.add(h.getAssociatedEdge(output));
    }
    for (SDFEdge edge : edges) {
      String hierarchicalPortName = ((SDFEdge) edge.getPropertyBean().getValue("baseEdge")).getSourceInterface().getName();
      String subgraphInterfaceName = h.getName() + "_" + hierarchicalPortName + "_1";
      replaceEdgeSourceActor(parentGraph, edge, subgraphInterfaceName, null);
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
  public static void replaceEdgeSourceActor(SDFGraph graph, SDFEdge edge, String newSourceActor, String newSourcePort) {
    // create the new edge
    GraphStructureHelper.addEdge(graph, newSourceActor, newSourcePort, edge.getTarget().getName(), edge.getTargetInterface().getName(),
        edge.getProd().intValue(), edge.getCons().intValue(), edge.getDelay().intValue(), (SDFEdge) edge.getPropertyBean().getValue("baseEdge"));
    // remove the old edge
    graph.removeEdge(edge);
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
  public static void replaceEdgeTargetActor(SDFGraph graph, SDFEdge edge, String newTargetActor, String newTargetPort) {
    // create the new edge
    GraphStructureHelper.addEdge(graph, edge.getSource().getName(), edge.getSourceInterface().getName(), newTargetActor, newTargetPort,
        edge.getProd().intValue(), edge.getCons().intValue(), edge.getDelay().intValue(), (SDFEdge) edge.getPropertyBean().getValue("baseEdge"));
    // remove the old edge
    graph.removeEdge(edge);
  }

}
