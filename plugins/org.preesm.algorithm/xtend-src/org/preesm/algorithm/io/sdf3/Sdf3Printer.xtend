/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2014 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2014)
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
package org.preesm.algorithm.io.sdf3

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Collection
import org.eclipse.xtend.lib.annotations.Accessors
import org.preesm.algorithm.model.AbstractEdge
import org.preesm.algorithm.model.IInterface
import org.preesm.algorithm.model.sdf.SDFAbstractVertex
import org.preesm.algorithm.model.sdf.SDFEdge
import org.preesm.algorithm.model.sdf.SDFGraph
import org.preesm.algorithm.model.sdf.esdf.SDFSourceInterfaceVertex
import org.preesm.commons.math.MathFunctionsHelper
import org.preesm.model.slam.Design

/**
 * This class is used to print an {@link SDFGraph} in the SDF For Free (SDF3)
 * XML file format. Beside printing the graph actors and edge.
 *
 * @author kdesnos
 * @date 2014.03.20
 */
class Sdf3Printer {

	/**
	 * The {@link SDFGraph} printed by the current instance of {@link
	 * SDFPrinter}.
	 */
	@Accessors
	val SDFGraph sdf

	/**
	 * The {@link Design architecture model} on which the printed {@link
	 * SDFGraph} is mapped.
	 */
	@Accessors
	val Design archi

	/**
	 * Constructor of the {@link Sdf3Printer}.
	 *
	 * @param sdf
	 * 	 the exported {@link SDFGraph}.
	 * @param archi
	 *   the {@link Design architecture} model referenced in the scenario.
	 *
	 */
	new(SDFGraph sdf, Design archi) {
		this.sdf = sdf
		this.archi = archi
	}

	/**
	 * Main method to print the {@link SDFGraph} in the SDF3 graph XML format.
	 *
	 * @return the {@link CharSequence} containing the XML representation of
	 * the graph.
	 */
	def String print() '''
		<sdf3 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" type="sdf"
		    			xsi:noNamespaceSchemaLocation="http://www.es.ele.tue.nl/sdf3/xsd/sdf3-sdf.xsd">
			<applicationGraph>
				<sdf name="«sdf.name.toFirstLower»" type="«sdf.name.toFirstUpper»">
					«FOR actor : sdf.vertexSet»
						«actor.print»
					«ENDFOR»
					«FOR edge : sdf.edgeSet»
						«edge.print»
					«ENDFOR»
				</sdf>
			</applicationGraph>
		</sdf3>
	'''

	/**
	 * Print a port of an {@link SDFAbstractVertex} of the graph.
	 *
	 * @param port
	 *   the printed port
	 *
	 * @param edge
	 *   the {@link SDFEdge} connected to the port.
	 *
	 * @return the {@link CharSequence} containing the XML code for the
	 * declaration of an actor port in the SDF3 format.
	 */
	def String print(IInterface port, SDFEdge edge) {
		val tokenSize = MathFunctionsHelper.gcd(edge.cons.longValue, edge.prod.longValue)
		var rate = if (port instanceof SDFSourceInterfaceVertex)
				edge.cons.longValue / tokenSize
			else
				edge.prod.longValue / tokenSize

		return '''
			<port name="«port.name»" type="«if(port instanceof SDFSourceInterfaceVertex) "in" else "out"»" rate="«rate»"/>
		'''
	}

	/**
	 * Print an {@link SDFAbstractVertex} of the graph.
	 *
	 * @param actor
	 * 	the printed {@link SDFAbstractVertex}.
	 *
	 * @return the {@link CharSequence} containing the XML code for the
	 * declaration of an actor and its ports in the SDF3 format.
	 */
	@SuppressWarnings("unchecked")
	def String print(SDFAbstractVertex actor) '''
		<actor name="«actor.name»" type="«actor.name»">
			«val Collection<IInterface> interfaces = actor.interfaces»
			«FOR port : interfaces»
				«val AbstractEdge<SDFGraph, SDFAbstractVertex> associatedEdge = actor.getAssociatedEdge(port)»
				«val sdfEdge = associatedEdge as SDFEdge»
				«print(port , sdfEdge)»
			«ENDFOR»
		</actor>
	'''

	/**
	 * Print an {@link SDFEdge} of the graph.
	 *
	 * @param edge
	 *    the printed {@link SDFEdge}.
	 *
	 * @return the {@link CharSequence} containing the XML code for the
	 * declaration of an edge in the SDF3 format.
	 */
	def String print(SDFEdge edge) {
		val tokenSize = MathFunctionsHelper.gcd(edge.cons.longValue, edge.prod.
			longValue)
		return '''
			<channel name="«edge.printName»" srcActor="«edge.source»" srcPort="«edge.sourceLabel»" dstActor="«edge.target»" dstPort="«edge.
				targetLabel»" initialTokens="«edge.delay.longValue/tokenSize»"/>
		'''
	}

	/**
	 * Print the name of an {@link SDFEdge}.<br>
	 * The name is composed as follows:<br>
	 * <code>[SourceName]_[SourcePort]__[TargetName]_[TargetPort]</code>.
	 *
	 * @param edge
	 *    the {@link SDFEdge} whose name is printed.
	 *
	 * @return the printed {@link CharSequence}.
	 *
	 */
	def String printName(SDFEdge edge) '''«edge.source».«edge.sourceLabel»__«edge.target».«edge.targetLabel»'''

	def void write(File file) {
		try {
			val writer = new FileWriter(file);
			writer.write(this.print().toString);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
