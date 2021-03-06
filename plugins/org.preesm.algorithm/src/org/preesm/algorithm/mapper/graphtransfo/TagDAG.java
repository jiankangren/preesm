/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Florian Arrestier <florian.arrestier@insa-rennes.fr> (2018)
 * Jonathan Piat <jpiat@laas.fr> (2008 - 2011)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2013)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2014)
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
package org.preesm.algorithm.mapper.graphtransfo;

import java.util.Iterator;
import org.preesm.algorithm.mapper.abc.edgescheduling.AbstractEdgeSched;
import org.preesm.algorithm.mapper.abc.edgescheduling.EdgeSchedType;
import org.preesm.algorithm.mapper.abc.edgescheduling.IEdgeSched;
import org.preesm.algorithm.mapper.abc.impl.latency.LatencyAbc;
import org.preesm.algorithm.mapper.abc.order.OrderManager;
import org.preesm.algorithm.mapper.abc.route.CommunicationRouter;
import org.preesm.algorithm.mapper.model.MapperDAG;
import org.preesm.algorithm.mapper.model.MapperDAGEdge;
import org.preesm.algorithm.mapper.model.MapperDAGVertex;
import org.preesm.algorithm.mapper.model.special.ReceiveVertex;
import org.preesm.algorithm.mapper.model.special.SendVertex;
import org.preesm.algorithm.model.AbstractEdge;
import org.preesm.algorithm.model.PropertyBean;
import org.preesm.algorithm.model.dag.DAGEdge;
import org.preesm.algorithm.model.dag.DAGVertex;
import org.preesm.algorithm.model.sdf.SDFEdge;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.scenario.types.BufferAggregate;
import org.preesm.model.scenario.types.BufferProperties;
import org.preesm.model.scenario.types.DataType;
import org.preesm.model.scenario.types.ImplementationPropertyNames;
import org.preesm.model.scenario.types.VertexType;
import org.preesm.model.slam.ComponentInstance;
import org.preesm.model.slam.Design;
import org.preesm.model.slam.route.AbstractRouteStep;
import org.preesm.model.slam.utils.DesignTools;

/**
 * Tags an SDF with the implementation information necessary for code generation, and DAG exporting.
 *
 * @author pmenuet
 * @author mpelcat
 */
public class TagDAG {

  /**
   * Constructor.
   */
  public TagDAG() {
    super();
  }

  /**
   * tag adds the send and receive operations necessary to the code generation. It also adds the necessary properies.
   *
   * @param dag
   *          the dag
   * @param architecture
   *          the architecture
   * @param scenario
   *          the scenario
   * @param simu
   *          the simu
   * @param edgeSchedType
   *          the edge sched type
   */
  public void tag(final MapperDAG dag, final Design architecture, final PreesmScenario scenario, final LatencyAbc simu,
      final EdgeSchedType edgeSchedType) {

    final PropertyBean bean = dag.getPropertyBean();
    bean.setValue(ImplementationPropertyNames.Graph_AbcReferenceType, simu.getType());
    bean.setValue(ImplementationPropertyNames.Graph_EdgeSchedReferenceType, edgeSchedType);

    addSendReceive(dag, architecture, scenario);
    addProperties(dag, simu);
    addAllAggregates(dag, scenario);
  }

  /**
   * Adds send and receive without scheduling them.
   *
   * @param dag
   *          the dag
   * @param architecture
   *          the architecture
   * @param scenario
   *          the scenario
   */
  private void addSendReceive(final MapperDAG dag, final Design architecture, final PreesmScenario scenario) {

    final OrderManager orderMgr = new OrderManager(architecture);
    orderMgr.reconstructTotalOrderFromDAG(dag); // could be avoided?

    final IEdgeSched instance = AbstractEdgeSched.getInstance(EdgeSchedType.Simple, orderMgr);
    final CommunicationRouter comRouter = new CommunicationRouter(architecture, scenario, dag, instance, orderMgr);
    comRouter.routeAll(CommunicationRouter.SEND_RECEIVE_TYPE); // takes a lot of time, should be optimized
    orderMgr.tagDAG(dag);
  }

  /**
   * Adds the properties.
   *
   * @param dag
   *          the dag
   * @param simu
   *          the simu
   */
  private void addProperties(final MapperDAG dag, final LatencyAbc simu) {

    MapperDAGVertex currentVertex;

    final Iterator<DAGVertex> iter = dag.vertexSet().iterator();

    // Updates graph time
    if (simu instanceof LatencyAbc) {
      simu.updateTimings();
    }

    // Tagging the vertices with informations for code generation
    while (iter.hasNext()) {
      currentVertex = (MapperDAGVertex) iter.next();
      final PropertyBean bean = currentVertex.getPropertyBean();

      if (currentVertex instanceof SendVertex) {

        final MapperDAGEdge incomingEdge = (MapperDAGEdge) ((SendVertex) currentVertex).incomingEdges().toArray()[0];

        // Setting the vertex type
        bean.setValue(ImplementationPropertyNames.Vertex_vertexType, VertexType.SEND);

        // Setting the operator on which vertex is executed
        bean.setValue(ImplementationPropertyNames.Vertex_Operator,
            ((SendVertex) currentVertex).getRouteStep().getSender());

        // Setting the medium transmitting the current data
        final AbstractRouteStep sendRs = ((SendVertex) currentVertex).getRouteStep();
        bean.setValue(ImplementationPropertyNames.SendReceive_routeStep, sendRs);

        // Setting the size of the transmitted data
        bean.setValue(ImplementationPropertyNames.SendReceive_dataSize, incomingEdge.getInit().getDataSize());

        // Setting the name of the data sending vertex
        bean.setValue(ImplementationPropertyNames.Send_senderGraphName, incomingEdge.getSource().getName());

        // Setting the address of the operator on which vertex is
        // executed
        final String baseAddress = DesignTools.getParameter(((SendVertex) currentVertex).getRouteStep().getSender(),
            DesignTools.OPERATOR_BASE_ADDRESS);

        if (baseAddress != null) {
          bean.setValue(ImplementationPropertyNames.SendReceive_Operator_address, baseAddress);
        }

      } else if (currentVertex instanceof ReceiveVertex) {

        final MapperDAGEdge outgoingEdge = (MapperDAGEdge) ((ReceiveVertex) currentVertex).outgoingEdges().toArray()[0];

        // Setting the vertex type
        bean.setValue(ImplementationPropertyNames.Vertex_vertexType, VertexType.RECEIVE);

        // Setting the operator on which vertex is executed
        bean.setValue(ImplementationPropertyNames.Vertex_Operator,
            ((ReceiveVertex) currentVertex).getRouteStep().getReceiver());

        // Setting the medium transmitting the current data
        final AbstractRouteStep rcvRs = ((ReceiveVertex) currentVertex).getRouteStep();
        bean.setValue(ImplementationPropertyNames.SendReceive_routeStep, rcvRs);

        // Setting the size of the transmitted data
        bean.setValue(ImplementationPropertyNames.SendReceive_dataSize, outgoingEdge.getInit().getDataSize());

        // Setting the name of the data receiving vertex
        bean.setValue(ImplementationPropertyNames.Receive_receiverGraphName, outgoingEdge.getTarget().getName());

        // Setting the address of the operator on which vertex is
        // executed
        final String baseAddress = DesignTools
            .getParameter(((ReceiveVertex) currentVertex).getRouteStep().getReceiver(), "BaseAddress");

        if (baseAddress != null) {
          bean.setValue(ImplementationPropertyNames.SendReceive_Operator_address, baseAddress);
        }
      } else {

        // Setting the operator on which vertex is executed
        bean.setValue(ImplementationPropertyNames.Vertex_Operator, currentVertex.getEffectiveOperator());

        // Setting the vertex type
        bean.setValue(ImplementationPropertyNames.Vertex_vertexType, VertexType.TASK);

        bean.setValue(ImplementationPropertyNames.Vertex_originalVertexId, currentVertex.getId());

        // Setting the task duration
        final ComponentInstance effectiveOperator = currentVertex

            .getEffectiveOperator();
        final long singleRepeatTime = currentVertex.getInit().getTime(effectiveOperator);
        final long nbRepeat = currentVertex.getInit().getNbRepeat();
        final long totalTime = nbRepeat * singleRepeatTime;
        bean.setValue(ImplementationPropertyNames.Task_duration, totalTime);

        // Tags the DAG with vertex starttime when possible
        if (simu instanceof LatencyAbc) {
          final long startTime = (simu).getTLevel(currentVertex, false);
          bean.setValue(ImplementationPropertyNames.Start_time, startTime);
        }
      }

      // Setting the scheduling total order
      bean.setValue(ImplementationPropertyNames.Vertex_schedulingOrder, currentVertex.getTotalOrder());
    }
  }

  /**
   * Loop on the edges to add aggregates.
   *
   * @param dag
   *          the dag
   * @param scenario
   *          the scenario
   */
  private void addAllAggregates(final MapperDAG dag, final PreesmScenario scenario) {

    MapperDAGEdge edge;

    final Iterator<DAGEdge> iter = dag.edgeSet().iterator();

    // Tagging the vertices with informations for code generation
    while (iter.hasNext()) {
      edge = (MapperDAGEdge) iter.next();
      addAggregate(edge, scenario);
    }
  }

  /**
   * Aggregate is imported from the SDF edge. An aggregate in SDF is a set of sdf edges that were merged into one DAG
   * edge.
   *
   * @param edge
   *          the edge
   * @param scenario
   *          the scenario
   */
  private void addAggregate(final MapperDAGEdge edge, final PreesmScenario scenario) {
    final BufferAggregate agg = new BufferAggregate();
    for (final AbstractEdge<?, ?> aggMember : edge.getAggregate()) {
      final DAGEdge dagEdge = (DAGEdge) aggMember;
      final String value = (String) dagEdge.getPropertyBean().getValue(SDFEdge.DATA_TYPE);
      final DataType dataType = scenario.getSimulationManager().getDataType(value);
      final BufferProperties props = new BufferProperties(dataType, dagEdge.getSourceLabel(), dagEdge.getTargetLabel(),
          (int) dagEdge.getWeight().longValue());
      agg.add(props);
    }
    edge.getPropertyBean().setValue(BufferAggregate.propertyBeanName, agg);
  }

}
