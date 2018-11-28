/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018) :
 *
 * Alexandre Honorat <ahonorat@insa-rennes.fr> (2018)
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2018)
 * Florian Arrestier <florian.arrestier@insa-rennes.fr> (2018)
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
package org.preesm.algorithm.pisdf.pimm2srdag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang3.time.StopWatch;
import org.preesm.algorithm.mapper.model.MapperDAG;
import org.preesm.algorithm.model.dag.DAGEdge;
import org.preesm.algorithm.model.dag.DAGVertex;
import org.preesm.algorithm.model.types.LongEdgePropertyType;
import org.preesm.algorithm.pisdf.pimm2srdag.visitor.StaticPiMM2ASrPiMMVisitor;
import org.preesm.algorithm.pisdf.pimm2srdag.visitor.StaticPiMM2MapperDAGVisitor;
import org.preesm.algorithm.pisdf.pimmoptims.BroadcastRoundBufferOptimization;
import org.preesm.algorithm.pisdf.pimmoptims.ForkJoinOptimization;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.pisdf.AbstractVertex;
import org.preesm.model.pisdf.Parameter;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.pisdf.brv.BRVMethod;
import org.preesm.model.pisdf.brv.PiBRV;
import org.preesm.model.pisdf.statictools.PiMMHandler;
import org.preesm.model.pisdf.util.PiMMSwitch;
import org.preesm.model.scenario.PreesmScenario;

/**
 * The Class StaticPiMM2SDFLauncher.
 */
public class StaticPiMM2SrDAGLauncher extends PiMMSwitch<Boolean> {

  /** The scenario. */
  private final PreesmScenario scenario;

  /** The graph. */
  private final PiGraph graph;

  /** Map from Pi actors to their Repetition Vector value. */
  protected Map<AbstractVertex, Long> graphBRV = new LinkedHashMap<>();

  /** Map of all parametersValues */
  protected Map<Parameter, Integer> parametersValues;

  /**
   * Instantiates a new static pi MM 2 SDF launcher.
   *
   * @param scenario
   *          the scenario
   * @param graph
   *          the graph
   */
  public StaticPiMM2SrDAGLauncher(final PreesmScenario scenario, final PiGraph graph) {
    this.scenario = scenario;
    this.graph = graph;
  }

  /**
   * Precondition: All.
   *
   * @return the SDFGraph obtained by visiting graph
   */
  public PiGraph launch(final BRVMethod method) {
    // 1. First we resolve all parameters.
    // It must be done first because, when removing persistence, local parameters have to be known at upper level
    PiMMHandler.resolveAllParameters(this.graph);
    // 2. We perform the delay transformation step that deals with persistence
    PiMMHandler.removePersistence(this.graph);
    // 3. Compute BRV following the chosen method
    this.graphBRV = PiBRV.compute(graph, method);
    // 4. Print the RV values
    // 4.5 Check periods with BRV
    PiMMHandler.checkPeriodicity(this.graphBRV);
    // 5. Convert to SR-DAG
    return convert2SRDAG();
  }

  /**
   * Convert the PiSDF graph to SRDAG
   *
   * @return the resulting SR DAG
   */
  private PiGraph convert2SRDAG() {

    // 1- Convert to acyclic single rate
    final StaticPiMM2ASrPiMMVisitor visitorPiMM2ASRPiMM = new StaticPiMM2ASrPiMMVisitor(this.graph, this.graphBRV,
        this.scenario);
    visitorPiMM2ASRPiMM.doSwitch(this.graph);
    final PiGraph acyclicSRPiMM = visitorPiMM2ASRPiMM.getResult();

    // 2- do some optimization on the graph
    final ForkJoinOptimization forkJoinOptimization = new ForkJoinOptimization();
    forkJoinOptimization.optimize(acyclicSRPiMM);
    final BroadcastRoundBufferOptimization brRbOptimization = new BroadcastRoundBufferOptimization();
    brRbOptimization.optimize(acyclicSRPiMM);

    return acyclicSRPiMM;
  }

  /**
   * Creates edge aggregate for all multi connection between two vertices.
   *
   * @param dag
   *          the dag on which to perform
   */
  private void aggregateEdges(final MapperDAG dag) {
    for (final DAGVertex vertex : dag.vertexSet()) {
      // List of extra edges to remove
      final ArrayList<DAGEdge> toRemove = new ArrayList<>();
      for (final DAGEdge edge : vertex.incomingEdges()) {
        final DAGVertex source = edge.getSource();
        // Maybe doing the copy is not optimal
        final ArrayList<DAGEdge> allEdges = new ArrayList<>(dag.getAllEdges(source, vertex));
        // if there is only one connection no need to modify anything
        if (allEdges.size() == 1 || toRemove.contains(allEdges.get(1))) {
          continue;
        }
        // Get the first edge
        final DAGEdge firstEdge = allEdges.remove(0);
        for (final DAGEdge extraEdge : allEdges) {
          // Update the weight
          firstEdge.setWeight(
              new LongEdgePropertyType(firstEdge.getWeight().longValue() + extraEdge.getWeight().longValue()));
          // Add the aggregate edge
          firstEdge.getAggregate().add(extraEdge.getAggregate().get(0));
          toRemove.add(extraEdge);
        }
      }
      // Removes the extra edges
      toRemove.forEach(dag::removeEdge);
    }
  }

  /**
   * Converts the single rate acyclic PiSDF to {@link MapperDAG} and aggregate edges
   */
  public MapperDAG covnertToMapperDAG(PiGraph resultPi) {
    // Convert the PiMM vertices to DAG vertices
    final StaticPiMM2MapperDAGVisitor visitor = new StaticPiMM2MapperDAGVisitor(resultPi, this.scenario);
    visitor.doSwitch(resultPi);
    MapperDAG result = visitor.getResult();

    // 6. Aggregate edges
    final StopWatch timer = new StopWatch();
    timer.start();
    // This is needed as the memory allocator does not yet handle multiple edges
    // There is a potential TODO for someone with a brave heart here
    // if you're doing this, remember to check for addAggregate in TAGDag.java and for createEdge in
    // SRVerticesLinker.java.
    // also in ScriptRunner.xtend, there is a part where the aggregate list is flatten, check that also
    aggregateEdges(result);
    timer.stop();
    final String msg = "Edge aggregation: " + timer + "s.";
    PreesmLogger.getLogger().log(Level.INFO, msg);
    return result;
  }

}
