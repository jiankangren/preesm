/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
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
package org.ietr.dftools.algorithm.test;

import org.junit.Assert;
import org.junit.Test;
import org.preesm.algorithm.DFToolsAlgoException;
import org.preesm.algorithm.model.parameters.ExpressionValue;
import org.preesm.algorithm.model.parameters.InvalidExpressionException;
import org.preesm.algorithm.model.parameters.NoIntegerValueException;

/**
 * The Class JepTransitionTest.
 */
public class JepTransitionTest {

  /**
   * Test expression value.
   */
  @Test
  public void testExpressionValue() {
    final double expected = 2 + (((5 * 7) / 1.2) * 7.00002);
    final ExpressionValue value = new ExpressionValue("2 + 5 * 7 / 1.2 * 7.00002");
    try {
      final long intValue = value.longValue();
      Assert.assertEquals((int) expected, intValue);
    } catch (final InvalidExpressionException e) {
      final String message = "Expression should be valid, but failed with " + e.getMessage();
      throw new DFToolsAlgoException(message, e);
    } catch (final NoIntegerValueException e) {
      final String message = "Result should be integer, but failed with " + e.getMessage();
      throw new DFToolsAlgoException(message, e);
    }
  }
}