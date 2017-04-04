/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2012 - 2014)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
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
 *******************************************************************************/

package org.ietr.preesm.mapper.abc.edgescheduling;

import java.util.Random;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.preesm.mapper.abc.order.OrderManager;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.special.TransferVertex;

/**
 * An advanced edge scheduler. It looks for the largest free interval in
 * scheduling and schedules the new communication in this slot.
 * 
 * @author mpelcat
 */
public class SwitcherEdgeSched extends AbstractEdgeSched {

	private IntervalFinder intervalFinder = null;

	public SwitcherEdgeSched(OrderManager orderManager) {
		super(orderManager);

		intervalFinder = new IntervalFinder(orderManager);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	public void schedule(TransferVertex vertex, MapperDAGVertex source,
			MapperDAGVertex target) {

		ComponentInstance component = vertex
				.getEffectiveComponent();
		// intervalFinder.displayCurrentSchedule(vertex, source);
		Interval largestInterval = intervalFinder.findLargestFreeInterval(
				component, source, target);

		if (largestInterval.getDuration() > 0) {
			orderManager.insertAtIndex(largestInterval.getTotalOrderIndex(),
					vertex);
		} else {
			int sourceIndex = intervalFinder.getOrderManager().totalIndexOf(
					source) + 1;
			int targetIndex = intervalFinder.getOrderManager().totalIndexOf(
					target);

			if (targetIndex - sourceIndex > 0) {
				Random r = new Random();
				int randomVal = Math.abs(r.nextInt());
				randomVal = randomVal % (targetIndex - sourceIndex);
				orderManager.insertAtIndex(sourceIndex + randomVal, vertex);
			} else {
				orderManager.insertAfter(source, vertex);
			}
		}
	}

	@Override
	public EdgeSchedType getEdgeSchedType() {
		return EdgeSchedType.Switcher;
	}
}
