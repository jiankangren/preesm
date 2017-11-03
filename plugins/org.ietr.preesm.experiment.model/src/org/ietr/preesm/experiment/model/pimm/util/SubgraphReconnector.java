/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
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
package org.ietr.preesm.experiment.model.pimm.util;

import org.ietr.preesm.experiment.model.PiGraphException;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.ConfigInputPort;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputPort;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * This class provides a single method to connect a hierarchical actor to its underlying subgraph. Synthetically, it takes edges from the super graph (the graph
 * containing the hierarchical actor), and connect them to subgraph (which is also an actor).
 *
 * @author anmorvan
 *
 */
public class SubgraphReconnector {

  private SubgraphReconnector() {
  }

  /**
   * Reconnect pi graph.
   *
   * @param hierarchicalActor
   *          the actor
   * @param subGraph
   *          the subgraph linked to the hierarchical actor
   */
  public static void reconnectPiGraph(final Actor hierarchicalActor, final PiGraph subGraph) {
    SubgraphReconnector.reconnectDataInputPorts(hierarchicalActor, subGraph);
    SubgraphReconnector.reconnectDataOutputPorts(hierarchicalActor, subGraph);
    SubgraphReconnector.reconnectConfigInputPorts(hierarchicalActor, subGraph);
    SubgraphReconnector.reconnectConfigOutputPorts(hierarchicalActor, subGraph);
  }

  private static void reconnectConfigOutputPorts(final Actor hierarchicalActor, final PiGraph subGraph) {
    boolean found;
    for (final ConfigOutputPort cop1 : hierarchicalActor.getConfigOutputPorts()) {
      found = false;
      for (final ConfigOutputPort cop2 : subGraph.getConfigOutputPorts()) {
        if (cop1.getName().equals(cop2.getName())) {
          for (final Dependency dep : cop1.getOutgoingDependencies()) {
            cop2.getOutgoingDependencies().add(dep);
            dep.setSetter(cop2);
          }
          found = true;
          break;
        }
      }
      if (!found) {
        SubgraphReconnector.error(hierarchicalActor, subGraph, cop1);
      }
    }
  }

  private static void reconnectConfigInputPorts(final Actor hierarchicalActor, final PiGraph subGraph) {
    boolean found;
    for (final ConfigInputPort cip1 : hierarchicalActor.getConfigInputPorts()) {
      found = false;
      for (final ConfigInputPort cip2 : subGraph.getConfigInputPorts()) {
        if (cip1.getName().equals(cip2.getName())) {
          found = true;
          final Dependency dep = cip1.getIncomingDependency();
          if (dep != null) {
            cip2.setIncomingDependency(dep);
            dep.setGetter(cip2);
          }
          break;
        }
      }
      if (!found) {
        SubgraphReconnector.error(hierarchicalActor, subGraph, cip1);
      }
    }
  }

  private static void reconnectDataOutputPorts(final Actor hierarchicalActor, final PiGraph subGraph) {
    boolean found;
    for (final DataOutputPort dop1 : hierarchicalActor.getDataOutputPorts()) {
      found = false;
      for (final DataOutputPort dop2 : subGraph.getDataOutputPorts()) {
        if (dop1.getName().equals(dop2.getName())) {
          final Fifo fifo = dop1.getOutgoingFifo();
          if (fifo != null) {
            dop2.setOutgoingFifo(fifo);
            fifo.setSourcePort(dop2);

            dop2.setExpression(dop1.getExpression());
            dop2.setAnnotation(dop1.getAnnotation());
          }
          found = true;
          break;
        }
      }
      if (!found) {
        SubgraphReconnector.error(hierarchicalActor, subGraph, dop1);
      }
    }
  }

  private static void reconnectDataInputPorts(final Actor hierarchicalActor, final PiGraph subGraph) {
    boolean found;
    for (final DataInputPort dip1 : hierarchicalActor.getDataInputPorts()) {
      found = false;
      for (final DataInputPort dip2 : subGraph.getDataInputPorts()) {
        if (dip1.getName().equals(dip2.getName())) {
          final Fifo fifo = dip1.getIncomingFifo();
          if (fifo != null) {
            dip2.setIncomingFifo(fifo);
            fifo.setTargetPort(dip2);

            dip2.setExpression(dip1.getExpression());
            dip2.setAnnotation(dip1.getAnnotation());
          }
          found = true;
          break;
        }
      }
      if (!found) {
        SubgraphReconnector.error(hierarchicalActor, subGraph, dip1);
      }
    }
  }

  private static void error(final Actor hierarchicalActor, final PiGraph subGraph, final Port port) {
    throw new PiGraphException("PiGraph '" + subGraph.getName() + "' does not have a corresponding " + port.getClass().getSimpleName() + " named '"
        + port.getName() + "' for Actor " + hierarchicalActor.getName());
  }

}
