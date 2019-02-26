/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018) :
 *
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
/**
 *
 */
package org.preesm.model.pisdf.statictools.optims;

import org.preesm.model.pisdf.BroadcastActor;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.pisdf.RoundBufferActor;
import org.preesm.model.pisdf.util.PiMMSwitch;

/**
 * @author farresti
 *
 */
public class BroadcastRoundBufferOptimization extends PiMMSwitch<Boolean> implements PiMMOptimization {

  boolean keepGoing = false;
  boolean full      = true;

  /*
   * (non-Javadoc)
   *
   * @see
   * org.ietr.preesm.pimm.algorithm.pimmoptims.PiMMOptimization#optimize(org.ietr.preesm.experiment.model.pimm.PiGraph)
   */
  @Override
  public boolean optimize(PiGraph graph) {
    return optimize(graph, true);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.ietr.preesm.pimm.algorithm.pimmoptims.PiMMOptimization#optimize(org.ietr.preesm.experiment.model.pimm.PiGraph)
   */
  @Override
  public boolean optimize(PiGraph graph, boolean full) {
    do {
      this.keepGoing = false;
      this.full = full;
      final Boolean doSwitch = doSwitch(graph);
      this.keepGoing = (doSwitch == null);
    } while (this.keepGoing);
    return true;
  }

  @Override
  public Boolean casePiGraph(PiGraph graph) {
    graph.getActors().forEach(this::doSwitch);
    return true;
  }

  @Override
  public Boolean caseBroadcastActor(BroadcastActor actor) {
    final ForkOptimization forkOptimization = new ForkOptimization();
    if (full) {
      this.keepGoing |= forkOptimization.remove(actor.getContainingPiGraph(), actor);
    } else {
      this.keepGoing |= forkOptimization.removeUnused(actor.getContainingPiGraph(), actor);
    }
    return true;
  }

  @Override
  public Boolean caseRoundBufferActor(RoundBufferActor actor) {
    final JoinOptimization joinOptimization = new JoinOptimization();
    if (full) {
      this.keepGoing |= joinOptimization.remove(actor.getContainingPiGraph(), actor);
    } else {
      this.keepGoing |= joinOptimization.removeUnused(actor.getContainingPiGraph(), actor);
    }
    return true;
  }
}
