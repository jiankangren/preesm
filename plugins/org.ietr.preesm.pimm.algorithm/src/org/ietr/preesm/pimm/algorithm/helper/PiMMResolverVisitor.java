/**
 *
 */
package org.ietr.preesm.pimm.algorithm.helper;

import java.util.LinkedHashMap;
import java.util.logging.Level;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.experiment.model.expression.ExpressionEvaluator;
import org.ietr.preesm.experiment.model.factory.PiMMUserFactory;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.ConfigInputInterface;
import org.ietr.preesm.experiment.model.pimm.ConfigInputPort;
import org.ietr.preesm.experiment.model.pimm.DataPort;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.DelayActor;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.ExecutableActor;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.ISetter;
import org.ietr.preesm.experiment.model.pimm.InterfaceActor;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.Port;
import org.ietr.preesm.experiment.model.pimm.util.PiMMSwitch;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

/**
 * @author farresti
 *
 */
public class PiMMResolverVisitor extends PiMMSwitch<Boolean> {

  /*
   * Map used to rapidly check if a parameter value has allready been resolved
   */
  final LinkedHashMap<Parameter, Long> parameterValues;

  public PiMMResolverVisitor(final LinkedHashMap<Parameter, Long> parameterValues) {
    this.parameterValues = parameterValues;
  }

  /**
   * Set the value of parameters of a PiGraph when possible (i.e., if we have currently only one available value, or if we can compute the value)
   *
   * @param graph
   *          the PiGraph in which we want to set the values of parameters
   */
  private static void computeDerivedParameterValues(final PiGraph graph, final LinkedHashMap<Parameter, Long> parameterValues) {
    // If there is no value or list of values for one Parameter, the value
    // of the parameter is derived (i.e., computed from other parameters
    // values), we can evaluate it (after the values of other parameters
    // have been fixed)
    for (final Parameter p : graph.getParameters()) {
      if (!parameterValues.containsKey(p)) {
        // Evaluate the expression wrt. the current values of the
        // parameters and set the result as new expression
        final Expression pExp = PiMMUserFactory.instance.createExpression();
        final Expression valueExpression = p.getValueExpression();
        final long evaluate = ExpressionEvaluator.evaluate(valueExpression);
        pExp.setExpressionString(Long.toString(evaluate));
        p.setExpression(pExp);
        try {
          final long value = Long.parseLong(p.getExpression().getExpressionString());
          parameterValues.put(p, value);
        } catch (final NumberFormatException e) {
          WorkflowLogger.getLogger().log(Level.INFO, "TROLOLOLOLOLOLOLO.");
          break;
        }
      }
    }
  }

  private static JEP initJep(final LinkedHashMap<String, Long> portValues) {
    final JEP jep = new JEP();
    if (portValues != null) {
      portValues.forEach(jep::addVariable);
    }
    // TODO move to JEP 3 and get rid of these
    jep.addStandardConstants();
    jep.addStandardFunctions();
    return jep;
  }

  private static long parsePortExpression(final JEP jep, final String expressionString) throws ParseException {
    final Node parse = jep.parse(expressionString);
    final Object result = jep.evaluate(parse);
    if (result instanceof Long) {
      return (Long) result;
    } else if (result instanceof Double) {
      return Math.round((Double) result);
    } else {
      throw new ParseException("Unsupported result type " + result.getClass().getSimpleName());
    }
  }

  private static void parseJEP(final AbstractActor actor, final LinkedHashMap<String, Long> portValues) {
    // Init the JEP parser associated with the actor
    final JEP jepParser = PiMMResolverVisitor.initJep(portValues);
    // Iterate over all data ports of the actor and resolve their rates
    for (final DataPort dp : actor.getAllDataPorts()) {
      try {
        PiMMResolverVisitor.resolveExpression(dp.getPortRateExpression(), jepParser);
      } catch (final ParseException eparse) {
        throw new RuntimeException("Failed to parse rate for [" + dp.getId() + "] port: " + eparse.getMessage());
      }
    }

    // Parse delays as well
    if (actor instanceof PiGraph) {
      for (final Delay d : ((PiGraph) actor).getDelays()) {
        try {
          PiMMResolverVisitor.resolveExpression(d.getSizeExpression(), jepParser);
        } catch (final ParseException eparse) {
          throw new RuntimeException("Failed to parse expression for delay [" + d.getName() + "]: " + eparse.getMessage());
        }
      }
    }
  }

  /**
   * Fast evaluator for data port rate expression.<br>
   * If rate expression is a constant, then parsing is ignored since it is already done. <br>
   * This implementation uses benefit of the fact that the parser is initialized once for a given actor.
   *
   * @param expression
   *          the expression to evaluate
   * @param actorParser
   *          parser of the actor containing the port
   * @throws PiMMHelperException
   *           the PiMMHandlerException exception
   */
  private static void resolveExpression(final Expression expression, final JEP actorParser) throws ParseException {
    try {
      // If we can parse it, then it is constant
      Long.parseLong(expression.getExpressionString());
    } catch (final NumberFormatException e) {
      // Now, we deal with expression
      final long rate = PiMMResolverVisitor.parsePortExpression(actorParser, expression.getExpressionString());
      expression.setExpressionString(Long.toString(rate));
    }
  }

  /**
   * Static parameters. There should be no problem of order with ConfigInputInterface as the interfaces depending on local parameters are located in lower
   * levels of hierarchy.
   *
   * @param p
   *          the p
   */
  @Override
  public Boolean caseParameter(final Parameter p) {
    if (!p.isLocallyStatic()) {
      throw new RuntimeException("Parameter " + p.getName() + " is depends on a configuration actor. It is thus impossible to use the"
          + " Static PiMM 2 SDF transformation. Try instead the Dynamic PiMM 2 SDF" + " transformation (id: org.ietr.preesm.experiment.pimm2sdf.PiMM2SDFTask)");
    }
    if (!this.parameterValues.containsKey(p)) {
      // Evaluate the expression wrt. the current values of the
      // parameters and set the result as new expression
      final Expression valueExpression = p.getValueExpression();
      final long value = ExpressionEvaluator.evaluate(valueExpression);
      valueExpression.setExpressionString(Long.toString(value));
      this.parameterValues.put(p, value);
    }
    return true;
  }

  /*
   * ConfigInputInterface parameters resolver.
   *
   */
  @Override
  public Boolean caseConfigInputInterface(final ConfigInputInterface cii) {
    final ConfigInputPort graphPort = cii.getGraphPort();
    final Dependency incomingDependency = graphPort.getIncomingDependency();
    final ISetter setter = incomingDependency.getSetter();
    // Setter of an incoming dependency into a ConfigInputInterface must be
    // a parameter
    if (setter instanceof Parameter) {
      final Expression pExp = PiMMUserFactory.instance.createExpression();
      // When we arrive here all upper graphs have been processed.
      // We can then directly evaluate parameter expression here.
      final Expression valueExpression = ((Parameter) setter).getValueExpression();
      final String expressionString = valueExpression.getExpressionString();
      pExp.setExpressionString(expressionString);
      cii.setExpression(pExp);
      this.parameterValues.put(cii, Long.parseLong(expressionString));
    } else {
      throw new UnsupportedOperationException("In a static PiMM graph, setter of an incomming dependency must be a parameter.");
    }
    return true;
  }

  @Override
  public Boolean caseExecutableActor(final ExecutableActor actor) {
    // Map that associate to every parameter of an acotr the corresponding value in the graph
    final LinkedHashMap<String, Long> portValues = new LinkedHashMap<>();
    // We have to fetch the corresponding parameter port for normal actors
    for (final Parameter p : actor.getInputParameters()) {
      final Port lookupPort = actor.lookupConfigInputPortConnectedWithParameter(p);
      portValues.put(lookupPort.getName(), this.parameterValues.get(p));
    }
    PiMMResolverVisitor.parseJEP(actor, portValues);
    return true;
  }

  @Override
  public Boolean caseInterfaceActor(final InterfaceActor actor) {
    // Map that associate to every parameter of an acotr the corresponding value in the graph
    final LinkedHashMap<String, Long> portValues = new LinkedHashMap<>();
    // Data interface actors do not have parameter ports, thus expression is directly graph parameter
    for (final Parameter p : actor.getInputParameters()) {
      portValues.put(p.getName(), this.parameterValues.get(p));
    }
    PiMMResolverVisitor.parseJEP(actor, portValues);
    return true;
  }

  @Override
  public Boolean caseDelayActor(final DelayActor actor) {
    // Map that associate to every parameter of an actor the corresponding value in the graph
    final LinkedHashMap<String, Long> portValues = new LinkedHashMap<>();
    // Delay actors do not have parameter ports, they use the parameters of the linked delay
    // Thus, since delays do not have parameter ports either, the expression is directly the graph parameter
    for (final Parameter p : actor.getInputParameters()) {
      portValues.put(p.getName(), this.parameterValues.get(p));
    }
    PiMMResolverVisitor.parseJEP(actor, portValues);
    return true;
  }

  @Override
  public Boolean casePiGraph(final PiGraph graph) {
    // Resolve input interfaces
    for (final ConfigInputInterface p : graph.getConfigInputInterfaces()) {
      doSwitch(p);
    }
    // Resolve locally static parameters
    for (final Parameter p : graph.getOnlyParameters()) {
      doSwitch(p);
    }

    // Finally, we derive parameter values that have not already been processed
    PiMMResolverVisitor.computeDerivedParameterValues(graph, this.parameterValues);

    // We can now resolve data port rates for this graph
    for (final AbstractActor actor : graph.getOnlyActors()) {
      doSwitch(actor);
    }

    // Deals with data ports of the graph
    // Map that associate to every parameter of an acotr the corresponding value in the graph
    final LinkedHashMap<String, Long> portValues = new LinkedHashMap<>();
    // We have to fetch the corresponding parameter port for normal actors
    // Port of a parameter may have a dependency to higher level parameter
    for (final Parameter p : graph.getInputParameters()) {
      final Port lookupPort = graph.lookupConfigInputPortConnectedWithParameter(p);
      portValues.put(lookupPort.getName(), this.parameterValues.get(p));
    }
    PiMMResolverVisitor.parseJEP(graph, portValues);

    // Switch on child subgraphs
    for (final PiGraph g : graph.getChildrenGraphs()) {
      final PiMMResolverVisitor piMMResolverVisitor = new PiMMResolverVisitor(this.parameterValues);
      piMMResolverVisitor.doSwitch(g);
    }

    return true;
  }

}