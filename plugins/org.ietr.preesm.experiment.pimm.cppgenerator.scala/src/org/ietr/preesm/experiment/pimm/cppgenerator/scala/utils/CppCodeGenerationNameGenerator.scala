/**
 * *****************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot, Clément Guy, Yaset Oliva Venegas
 *
 * [mpelcat,jnezan,kdesnos,jheulot,cguy,yoliva]@insa-rennes.fr
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
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
 * knowledge of the CeCILL-C license and that you accept its terms.
 * ****************************************************************************
 */
package org.ietr.preesm.experiment.pimm.cppgenerator.scala.utils

import org.ietr.preesm.experiment.model.pimm._
import java.util.Map
import java.util.HashMap
import collection.JavaConversions._

trait CppCodeGenerationNameGenerator {
  /**
   * Returns the name of the subgraph pg
   */
  protected def getSubraphName(pg: PiGraph): String = pg.getName() + "_subGraph"

  /**
   * Returns the name of the variable pointing to the C++ object corresponding to AbstractActor aa
   */
  protected def getVertexName(aa: AbstractActor): String = "vx" + aa.getName()

  /**
   * Returns the name of the building method for the PiGraph pg
   */
  protected def getMethodName(pg: PiGraph): String = pg.getName()
  
  /**
   * Returns the name of the parameter
   */
  protected def getParameterName(p: Parameter): String = "param_" + p.getName();

  /**
   * Returns the position of the C++ edge corresponding to Fifo f in the collection of edges of graph pg
   */
  private var counterMap: Map[AbstractActor, ActorEdgesCounters] = new HashMap[AbstractActor, ActorEdgesCounters]
  private var edgeMap :Map[Fifo, Integer] = new HashMap[Fifo, Integer]
  protected def getEdgeNumber(aa : AbstractActor, f: Fifo, kind: EdgeKind.EdgeKind): Integer = {
    if (!counterMap.containsKey(aa)) {
      counterMap.put(aa, new ActorEdgesCounters)
    }
    if (!edgeMap.containsKey(f)) {
      var edgeCounter = 0
      kind match {
        case EdgeKind.in => {
          edgeCounter = counterMap.get(aa).inputEdgesCounter
          counterMap.get(aa).inputEdgesCounter = edgeCounter+1
        }
        case EdgeKind.out => {
          edgeCounter = counterMap.get(aa).outputEdgesCounter
          counterMap.get(aa).outputEdgesCounter = edgeCounter+1
        }
      }
      edgeMap.put(f, edgeCounter)
    }
    edgeMap.get(f)
  }
}

private class ActorEdgesCounters {
  var inputEdgesCounter = 0
  var outputEdgesCounter = 0
}
