/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2012)
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
package org.preesm.algorithm.memory.exclusiongraph;

// TODO: Auto-generated Javadoc
/**
 * This interface must be implemented by weighted Vertex classes. A weighted vertex class is mandatory when solving the
 * Maximum-Weight Clique problem
 *
 * @author kdesnos
 *
 * @param <W>
 *          is the type of the weight
 */
public interface IWeightedVertex<W> {

  /**
   * Accessor to the weight of the vertex.
   *
   * @return the weight of the vertex
   */
  public abstract W getWeight();

  /**
   * Set the weight of the vertex.
   *
   * @param weight
   *          the weight to set
   */
  public abstract void setWeight(W weight);

  /**
   * Get the unique identifier of the vertex. Each vertex must have a unique identifier. If two vertices have the same
   * identifier, they might be confused in some function, list, ...
   *
   * @return the unique identifier of the vertex
   */
  public abstract long getIdentifier();

  /**
   * Set the unique identifier of the vertex. Each vertex must have a unique identifier. If two vertices have the same
   * identifier, they might be confused in some function, list, ...
   *
   * @param id
   *          the new identifier of the vertex
   */
  public abstract void setIdentifier(long id);

  /**
   * Get a deep copy of the vertex.
   *
   * @return the deep copy of the vertex
   */
  public abstract IWeightedVertex<W> getClone();
}
