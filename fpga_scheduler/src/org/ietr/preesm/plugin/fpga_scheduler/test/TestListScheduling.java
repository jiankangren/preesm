package org.ietr.preesm.plugin.fpga_scheduler.test;

import org.ietr.preesm.plugin.fpga_scheduler.scheduler.*;
import org.ietr.preesm.plugin.fpga_scheduler.descriptor.*;
import org.ietr.preesm.plugin.fpga_scheduler.parser.*;
import org.ietr.preesm.plugin.fpga_scheduler.plotter.GanttPlotter;
import org.jfree.ui.RefineryUtilities;
import org.sdf4j.factories.DAGEdgeFactory;

public class TestListScheduling {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String algorithmFileName = "src\\org\\ietr\\preesm\\plugin\\fpga_scheduler\\test\\algorithm.xml";
		String architectureFileName = "src\\org\\ietr\\preesm\\plugin\\fpga_scheduler\\test\\architecture.xml";
		String parameterFileName = "src\\org\\ietr\\preesm\\plugin\\fpga_scheduler\\test\\parameter.xml";

		int argc = Integer.parseInt(args[0]);
		System.out.println("Number of argument: " + argc);

		switch (argc) {
		case 1: {
			algorithmFileName = args[1];
			System.out.println("algorithm: " + algorithmFileName);
			break;
		}
		case 2: {
			algorithmFileName = args[1];
			System.out.println("algorithm: " + algorithmFileName);
			architectureFileName = args[2];
			System.out.println("architecture: " + architectureFileName);
			break;
		}
		case 3: {
			algorithmFileName = args[1];
			System.out.println("algorithm: " + algorithmFileName);
			architectureFileName = args[2];
			System.out.println("architecture: " + architectureFileName);
			parameterFileName = args[3];
			System.out.println("parameter: " + parameterFileName);
			break;
		}
		default: {
			System.out.println("Invalid command! Use default arguments!");
			System.out.println("algorithm: " + algorithmFileName);
			System.out.println("architecture: " + architectureFileName);
			System.out.println("parameter: " + parameterFileName);
		}
		}
		AlgorithmDescriptor algorithm = new AlgorithmDescriptor(
				new DAGEdgeFactory());
		ArchitectureDescriptor architecture = new ArchitectureDescriptor();
		// Parse the design algorithm document
		new AlgorithmParser(algorithmFileName, algorithm).parse();
		// Parse the design parameter document
		new ParameterParser(parameterFileName, architecture, algorithm).parse();
		// Parse the architecture document
		new ArchitectureParser(architectureFileName, architecture).parse();

		OperatorDescriptor defaultOperator = null;
		NetworkDescriptor defaultNetwork = null;
		for (ComponentDescriptor indexComponent : architecture.getComponents()
				.values()) {
			if ((indexComponent.getType() == ComponentType.Ip || indexComponent
					.getType() == ComponentType.Processor)
					&& indexComponent.getId().equalsIgnoreCase(
							indexComponent.getName())) {
				defaultOperator = (OperatorDescriptor) indexComponent;
			} else if (indexComponent.getType() == ComponentType.Network
					&& indexComponent.getId().equalsIgnoreCase(
							indexComponent.getName())) {
				defaultNetwork = (NetworkDescriptor) indexComponent;
			}
		}

		System.out.println(" default operator: Id=" + defaultOperator.getId()
				+ "; Name=" + defaultOperator.getName());
		System.out.println(" default network: Id=" + defaultNetwork.getId()
				+ "; Name=" + defaultNetwork.getName());
		System.out.println("Computations in the algorithm:");
		for (ComputationDescriptor indexComputation : algorithm
				.getComputations().values()) {
			if (!indexComputation.getComputationDurations().containsKey(
					defaultOperator)) {
				indexComputation.addComputationDuration(defaultOperator,
						indexComputation.getTime());
				System.out.println(" Name="
						+ indexComputation.getName()
						+ "; default computationDuration="
						+ indexComputation
								.getComputationDuration(defaultOperator));
			}
		}
		System.out.println("Communications in the algorithm:");
		for (CommunicationDescriptor indexCommunication : algorithm
				.getCommunications().values()) {
			if (!indexCommunication.getCommunicationDurations().containsKey(
					defaultNetwork)) {
				indexCommunication.addCommunicationDuration(defaultNetwork,
						indexCommunication.getWeight());
				System.out.println(" Name="
						+ indexCommunication.getName()
						+ "; default communicationDuration="
						+ indexCommunication
								.getCommunicationDuration(defaultNetwork));
			}
		}
		System.out.println("Operators in the architecture:");
		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			System.out.println(" Id=" + indexOperator.getId() + "; Name="
					+ indexOperator.getName());
		}
		System.out.println("\n***** TestListScheduling begins! *****");
		ListSchedulingAdvanced scheduler = new ListSchedulingAdvanced(
				algorithm, architecture);
		scheduler.schedule();
		System.out.println("\n***** Schedule results *****");
		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			System.out.println("\n Operator: Id=" + indexOperator.getId()
					+ "; Name=" + indexOperator.getName());
			for (OperationDescriptor indexOperation : indexOperator
					.getOperations()) {
				if (indexOperation != scheduler.getTopCommunication()
						&& indexOperation != scheduler.getBottomCommunication()) {
					if (indexOperator.getComputations()
							.contains(indexOperation)) {
						System.out.println("  computation: Name="
								+ indexOperation.getName()
								+ "\n   1> startTime="
								+ indexOperator.getOccupiedTimeInterval(
										indexOperation.getName())
										.getStartTime()
								+ "\n   2> finishTime="
								+ indexOperator.getOccupiedTimeInterval(
										indexOperation.getName())
										.getFinishTime());
					} else {
						if (indexOperator.getSendCommunications().contains(
								indexOperation)) {
							System.out
									.println("  sendCommunication: Name="
											+ indexOperation.getName()
											+ "\n   1> startTimeOnSendOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getStartTime()
											+ "\n   2> finishTimeOnSendOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getFinishTime()
											+ "\n   3> startTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getStartTimeOnLink()
											+ "\n   4> finishTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getFinishTimeOnLink());
						} else {
							System.out
									.println("  receiveCommunication: Name="
											+ indexOperation.getName()
											+ "\n   1> startTimeOnReceiveOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getStartTime()
											+ "\n   2> finishTimeOnReceiveOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getFinishTime()
											+ "\n   3> startTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getStartTimeOnLink()
											+ "\n   4> finishTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getFinishTimeOnLink());
						}
					}
				}
			}
			for (LinkDescriptor indexLink : indexOperator.getOutputLinks()) {
				System.out.println(" outputLink: Id=" + indexLink.getId()
						+ "; Name=" + indexLink.getName());
				for (CommunicationDescriptor indexCommunication : indexLink
						.getCommunications()) {
					if (indexCommunication.getSendLink() == indexLink) {
						System.out.println("  sendCommunication: Name="
								+ indexCommunication.getName()
								+ "\n   1> startTimeOnLink="
								+ indexCommunication.getStartTimeOnLink()
								+ "\n   2> finishTimeOnLink="
								+ indexCommunication.getFinishTimeOnLink());
					}
				}
			}
			for (LinkDescriptor indexLink : indexOperator.getInputLinks()) {
				System.out.println(" inputLink: Id=" + indexLink.getId()
						+ "; Name=" + indexLink.getName());
				for (CommunicationDescriptor indexCommunication : indexLink
						.getCommunications()) {
					if (indexCommunication.getReceiveLink() == indexLink) {
						System.out.println("  receiveCommunication: Name="
								+ indexCommunication.getName()
								+ "\n   1> startTimeOnLink="
								+ indexCommunication.getStartTimeOnLink()
								+ "\n   2> finishTimeOnLink="
								+ indexCommunication.getFinishTimeOnLink());
					}
				}
			}
		}
		System.out.println("\n***** Schedule Length="
				+ scheduler.getScheduleLength() + " *****");

		GanttPlotter plot = new GanttPlotter(scheduler.getName()
				+ " -> Schedule Length=" + scheduler.getScheduleLength(),
				scheduler);
		plot.pack();
		RefineryUtilities.centerFrameOnScreen(plot);
		plot.setVisible(true);
		System.out.println("\n*****TestListScheduling ends!*****\n");
	}
}
