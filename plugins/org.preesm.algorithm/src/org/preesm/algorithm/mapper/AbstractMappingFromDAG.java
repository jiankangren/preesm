/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2019)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Julien Hascoet <jhascoet@kalray.eu> (2017)
 * Jonathan Piat <jpiat@laas.fr> (2009)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2017 - 2018)
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
package org.preesm.algorithm.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.preesm.algorithm.mapper.abc.impl.latency.InfiniteHomogeneousAbc;
import org.preesm.algorithm.mapper.abc.impl.latency.LatencyAbc;
import org.preesm.algorithm.mapper.abc.impl.latency.SpanLengthCalculator;
import org.preesm.algorithm.mapper.abc.route.RouteCalculator;
import org.preesm.algorithm.mapper.abc.taskscheduling.AbstractTaskSched;
import org.preesm.algorithm.mapper.abc.taskscheduling.TaskSchedType;
import org.preesm.algorithm.mapper.abc.taskscheduling.TopologicalTaskSched;
import org.preesm.algorithm.mapper.algo.InitialLists;
import org.preesm.algorithm.mapper.graphtransfo.TagDAG;
import org.preesm.algorithm.mapper.model.MapperDAG;
import org.preesm.algorithm.mapper.optimizer.RedundantSynchronizationCleaner;
import org.preesm.algorithm.mapper.params.AbcParameters;
import org.preesm.algorithm.mapper.tools.CommunicationOrderChecker;
import org.preesm.algorithm.model.dag.DirectedAcyclicGraph;
import org.preesm.commons.exceptions.PreesmException;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.slam.Design;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;
import org.preesm.workflow.implement.AbstractWorkflowNodeImplementation;

/**
 * Generic class representing the scheduling algorithm behaviour.
 *
 * @author pmenuet
 * @author mpelcat
 * @author kdesnos
 */
public abstract class AbstractMappingFromDAG extends AbstractTaskImplementation {

  /** The Constant PARAM_CHECK. */
  private static final String PARAM_CHECK = "Check";

  /** The Constant PARAM_OPTIMIZE. */
  private static final String PARAM_OPTIMIZE = "Optimize synchronization";

  /** The Constant VALUE_CHECK_FALSE. */
  private static final String VALUE_FALSE = "False";

  /** The Constant VALUE_CHECK_TRUE. */
  public static final String VALUE_TRUE = "True";

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#execute(java.util.Map, java.util.Map,
   * org.eclipse.core.runtime.IProgressMonitor, java.lang.String, org.ietr.dftools.workflow.elements.Workflow)
   */
  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters,
      final IProgressMonitor monitor, final String nodeName, final Workflow workflow) {

    final Map<String, Object> outputs = new LinkedHashMap<>();
    final Design architecture = (Design) inputs.get(AbstractWorkflowNodeImplementation.KEY_ARCHITECTURE);
    final MapperDAG dag = (MapperDAG) inputs.get(AbstractWorkflowNodeImplementation.KEY_SDF_DAG);
    final PreesmScenario scenario = (PreesmScenario) inputs.get(AbstractWorkflowNodeImplementation.KEY_SCENARIO);

    // Asking to recalculate routes
    RouteCalculator.recalculate(architecture, scenario);

    if (dag == null) {
      throw new PreesmRuntimeException(" graph can't be scheduled, check console messages");
    }

    final AbcParameters abcParams = new AbcParameters(parameters);

    final MapperDAG clonedDag = dag.copy();

    // calculates the DAG span length on the architecture main operator (the
    // tasks that can not be executed by the main operator are deported
    // without transfer time to other operator)
    calculateSpan(clonedDag, architecture, scenario, abcParams);

    final LatencyAbc simu = new InfiniteHomogeneousAbc(abcParams, dag, architecture,
        abcParams.getSimulatorType().getTaskSchedType(), scenario);

    final InitialLists initial = new InitialLists();
    final boolean couldConstructInitialLists = initial.constructInitialLists(dag, simu);
    if (!couldConstructInitialLists) {
      final String msg = "Error in scheduling";
      throw new PreesmRuntimeException(msg);
    } else {

      // Using topological task scheduling in list scheduling: the t-level
      // order of the infinite homogeneous simulation
      final TopologicalTaskSched taskSched = new TopologicalTaskSched(simu.getTotalOrder());
      simu.resetDAG();

      PreesmLogger.getLogger().log(Level.INFO, "Mapping");
      final LatencyAbc resSimu = schedule(outputs, parameters, initial, scenario, abcParams, dag, architecture,
          taskSched);
      PreesmLogger.getLogger().log(Level.INFO, "Mapping finished, now add communications tasks.");

      final MapperDAG resDag = resSimu.getDAG();
      final TagDAG tagSDF = new TagDAG();
      tagSDF.tag(dag, architecture, scenario, resSimu, abcParams.getEdgeSchedType());
      outputs.put(AbstractWorkflowNodeImplementation.KEY_SDF_ABC, resSimu);
      outputs.put(AbstractWorkflowNodeImplementation.KEY_SDF_DAG, dag);

      clean(architecture);
      PreesmLogger.getLogger().log(Level.INFO, "DAG fully mapped, now removes useless sync and check schedules.");
      removeRedundantSynchronization(parameters, dag);
      checkSchedulingResult(parameters, resDag);
    }
    return outputs;
  }

  protected abstract LatencyAbc schedule(final Map<String, Object> outputs, final Map<String, String> parameters,
      final InitialLists initial, final PreesmScenario scenario, final AbcParameters abcParams, final MapperDAG dag,
      final Design architecture, final AbstractTaskSched taskSched);

  /**
   * Generic mapping message.
   *
   * @return the string
   */
  @Override
  public String monitorMessage() {
    return "Mapping/Scheduling";
  }

  /**
   * Returns parameters common to all mappers.
   *
   * @return the default parameters
   */
  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new LinkedHashMap<>();

    parameters.put("simulatorType", "LooselyTimed");
    parameters.put("edgeSchedType", "Simple");
    parameters.put("balanceLoads", "false");
    parameters.put(AbstractMappingFromDAG.PARAM_CHECK, AbstractMappingFromDAG.VALUE_TRUE);
    parameters.put(AbstractMappingFromDAG.PARAM_OPTIMIZE, AbstractMappingFromDAG.VALUE_FALSE);
    return parameters;
  }

  /**
   * Clean.
   *
   * @param architecture
   *          the architecture
   * @param scenario
   *          the scenario
   */
  private void clean(final Design architecture) {
    // Asking to delete route
    RouteCalculator.deleteRoutes(architecture);
  }

  /**
   * This method performs optional checks to verify the integrity of a schedule. It should be call at the end of all
   * scheduler implementations.
   *
   * @param parameters
   *          {@link Map} of parameters values that were given to the mapper workflow task.
   * @param dag
   *          Scheduled {@link DirectedAcyclicGraph}.
   */
  private void checkSchedulingResult(final Map<String, String> parameters, final DirectedAcyclicGraph dag) {
    if (parameters.get(AbstractMappingFromDAG.PARAM_CHECK).equals(AbstractMappingFromDAG.VALUE_TRUE)) {
      CommunicationOrderChecker.checkCommunicationOrder(dag);
      CommunicationOrderChecker.checkMultiStepSendReceiveValidity(dag);
    }
  }

  /**
   * This method performs optional optimization of the communications generated in the schedule.
   *
   * @param parameters
   *          {@link Map} of parameters values that were given to the mapper workflow task.
   * @param dag
   *          Scheduled {@link DirectedAcyclicGraph}.
   */
  private void removeRedundantSynchronization(final Map<String, String> parameters, final DirectedAcyclicGraph dag) {
    final String paramValue = parameters.get(AbstractMappingFromDAG.PARAM_OPTIMIZE);
    if (paramValue.equals(AbstractMappingFromDAG.VALUE_TRUE)) {
      RedundantSynchronizationCleaner.cleanRedundantSynchronization(dag);
    }
  }

  /**
   * Calculates the DAG span length on the architecture main operator (the tasks that cannot be executed by the main
   * operator are deported without transfer time to other operator).
   *
   * @param dag
   *          the dag
   * @param archi
   *          the archi
   * @param scenario
   *          the scenario
   * @param parameters
   *          the parameters
   * @throws PreesmException
   *           the workflow exception
   */
  private void calculateSpan(final MapperDAG dag, final Design archi, final PreesmScenario scenario,
      final AbcParameters parameters) {
    final TaskSchedType taskSchedType = parameters.getSimulatorType().getTaskSchedType();
    final SpanLengthCalculator spanCalc = new SpanLengthCalculator(parameters, dag, archi, taskSchedType, scenario);
    spanCalc.resetDAG();
  }
}
