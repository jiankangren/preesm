package org.ietr.preesm.plugin.fpga_scheduler.test;

import org.ietr.preesm.plugin.fpga_scheduler.descriptor.ComputationDescriptor;
import org.ietr.preesm.plugin.fpga_scheduler.plotter.GanttPlotter;
import org.ietr.preesm.plugin.fpga_scheduler.scheduler.ListSchedulingCcCdClassicWithStaticOrderByBottomLevel;
import org.ietr.preesm.plugin.fpga_scheduler.scheduler.ListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation;
import org.ietr.preesm.plugin.fpga_scheduler.scheduler.ListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn;
import org.ietr.preesm.plugin.fpga_scheduler.scheduler.ListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut;
import org.ietr.preesm.plugin.fpga_scheduler.scheduler.ListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut;
import org.ietr.preesm.plugin.fpga_scheduler.scheduler.ListSchedulingClassic;
import org.jfree.ui.RefineryUtilities;

public class RandomComparisonListSchedulingCcCdClassic extends
		RandomComparisonListScheduling {

	private static String algorithmFileName = "src\\org\\ietr\\preesm\\plugin\\fpga_scheduler\\test\\algorithm.xml";

	private static String architectureFileName = "src\\org\\ietr\\preesm\\plugin\\fpga_scheduler\\test\\architecture1.xml";

	private static String parameterFileName = "src\\org\\ietr\\preesm\\plugin\\fpga_scheduler\\test\\parameter.xml";

	ListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation scheduler1 = null;

	ListSchedulingCcCdClassicWithStaticOrderByBottomLevel scheduler2 = null;

	ListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn scheduler3 = null;

	ListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut scheduler4 = null;

	ListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut scheduler5 = null;

	ListSchedulingClassic schedulerClassic = null;

	public RandomComparisonListSchedulingCcCdClassic(double ccr) {
		super(algorithmFileName, parameterFileName, architectureFileName);
		dagCreator = new DAGCreator();
		// Generating random sdf dag
		int nbVertex = 100, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;
		graph = dagCreator
				.randomDAG(nbVertex, minInDegree, maxInDegree, minOutDegree,
						maxOutDegree,
						(int) (500 * ccr / ((minInDegree + maxInDegree
								+ minOutDegree + maxOutDegree) / 4)),
						(int) (1000 * ccr / ((minInDegree + maxInDegree
								+ minOutDegree + maxOutDegree) / 4)));
		generateRandomNodeWeight(graph, 500, 1000);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out
				.println("\n***** Random Comparison List Scheduling CcCdClassic Begins! *****");
		RandomComparisonListSchedulingCcCdClassic randomCompare = new RandomComparisonListSchedulingCcCdClassic(
				1);
		randomCompare.drawDAG();
		randomCompare.compare();

		// Gantt graph
		GanttPlotter plot1 = new GanttPlotter(
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation()
						.getName()
						+ " -> Schedule Length="
						+ randomCompare
								.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation()
								.getScheduleLength(),
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation());
		plot1.pack();
		RefineryUtilities.centerFrameOnScreen(plot1);
		plot1.setVisible(true);

		GanttPlotter plot2 = new GanttPlotter(
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevel()
						.getName()
						+ " -> Schedule Length="
						+ randomCompare
								.getListSchedulingCcCdClassicWithStaticOrderByBottomLevel()
								.getScheduleLength(),
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevel());
		plot2.pack();
		RefineryUtilities.centerFrameOnScreen(plot2);
		plot2.setVisible(true);

		GanttPlotter plot3 = new GanttPlotter(
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn()
						.getName()
						+ " -> Schedule Length="
						+ randomCompare
								.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn()
								.getScheduleLength(),
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn());
		plot3.pack();
		RefineryUtilities.centerFrameOnScreen(plot3);
		plot3.setVisible(true);

		GanttPlotter plot4 = new GanttPlotter(
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut()
						.getName()
						+ " -> Schedule Length="
						+ randomCompare
								.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut()
								.getScheduleLength(),
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut());
		plot4.pack();
		RefineryUtilities.centerFrameOnScreen(plot4);
		plot4.setVisible(true);

		GanttPlotter plot5 = new GanttPlotter(
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut()
						.getName()
						+ " -> Schedule Length="
						+ randomCompare
								.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut()
								.getScheduleLength(),
				randomCompare
						.getListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut());
		plot5.pack();
		RefineryUtilities.centerFrameOnScreen(plot5);
		plot5.setVisible(true);

		GanttPlotter plot6 = new GanttPlotter(randomCompare
				.getListSchedulingClassic().getName()
				+ " -> Schedule Length="
				+ randomCompare.getListSchedulingClassic().getScheduleLength(),
				randomCompare.getListSchedulingClassic());
		plot6.pack();
		RefineryUtilities.centerFrameOnScreen(plot6);
		plot6.setVisible(true);

		System.out
				.println("\n\n***** Random Comparison List Scheduling CcCdClassic Finishes!*****");
	}

	public int compare() {
		algorithm = dagCreator.sdf2dag(graph);
		parse(graph, architectureFileName, parameterFileName);

		scheduler1 = new ListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation(
				algorithm, architecture);
		testScheduler(scheduler1, algorithm, architecture);

		algorithm = dagCreator.sdf2dag(graph);
		parse(graph, architectureFileName, parameterFileName);
		scheduler2 = new ListSchedulingCcCdClassicWithStaticOrderByBottomLevel(
				algorithm, architecture);
		testScheduler(scheduler2, algorithm, architecture);

		algorithm = dagCreator.sdf2dag(graph);
		parse(graph, architectureFileName, parameterFileName);
		scheduler3 = new ListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn(
				algorithm, architecture);
		testScheduler(scheduler3, algorithm, architecture);

		algorithm = dagCreator.sdf2dag(graph);
		parse(graph, architectureFileName, parameterFileName);
		scheduler4 = new ListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut(
				algorithm, architecture);
		testScheduler(scheduler4, algorithm, architecture);

		algorithm = dagCreator.sdf2dag(graph);
		parse(graph, architectureFileName, parameterFileName);
		scheduler5 = new ListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut(
				algorithm, architecture);
		testScheduler(scheduler5, algorithm, architecture);

		algorithm = dagCreator.sdf2dag(graph);
		parse(graph, architectureFileName, parameterFileName);
		schedulerClassic = new ListSchedulingClassic(algorithm, architecture);
		testScheduler(schedulerClassic, algorithm, architecture);

		System.out.println("***Compared Results***");

		System.out
				.print("No.\tScheduling Method\t\t\t\t\t\t\t\tSchedule Length\t\tUsed Operators\t\tScheduling Order");

		System.out.print("\n1\t" + scheduler1.getName() + "\t"
				+ scheduler1.getScheduleLength() + "\t\t\t"
				+ scheduler1.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler1
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n2\t" + scheduler2.getName() + "\t\t\t"
				+ scheduler2.getScheduleLength() + "\t\t\t"
				+ scheduler2.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler2
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n3\t" + scheduler3.getName() + "\t\t"
				+ scheduler3.getScheduleLength() + "\t\t\t"
				+ scheduler3.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler3
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n4\t" + scheduler4.getName() + "\t\t"
				+ scheduler4.getScheduleLength() + "\t\t\t"
				+ scheduler4.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler4
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n5\t" + scheduler5.getName() + "\t\t"
				+ scheduler5.getScheduleLength() + "\t\t\t"
				+ scheduler5.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler5
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n\t" + schedulerClassic.getName() + "\t\t\t\t\t\t\t\t"
				+ schedulerClassic.getScheduleLength() + "\t\t\t"
				+ schedulerClassic.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : schedulerClassic
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.println("\n********************");
		int minScheduleLength = Integer.MAX_VALUE;
		minScheduleLength = minScheduleLength > scheduler1.getScheduleLength() ? scheduler1
				.getScheduleLength()
				: minScheduleLength;
		minScheduleLength = minScheduleLength > scheduler2.getScheduleLength() ? scheduler2
				.getScheduleLength()
				: minScheduleLength;
		minScheduleLength = minScheduleLength > scheduler3.getScheduleLength() ? scheduler3
				.getScheduleLength()
				: minScheduleLength;
		minScheduleLength = minScheduleLength > scheduler4.getScheduleLength() ? scheduler4
				.getScheduleLength()
				: minScheduleLength;
		minScheduleLength = minScheduleLength > scheduler5.getScheduleLength() ? scheduler5
				.getScheduleLength()
				: minScheduleLength;
		minScheduleLength = minScheduleLength > schedulerClassic
				.getScheduleLength() ? schedulerClassic.getScheduleLength()
				: minScheduleLength;
		if (minScheduleLength == scheduler1.getScheduleLength()
				|| minScheduleLength == scheduler2.getScheduleLength()
				|| minScheduleLength == scheduler3.getScheduleLength()
				|| minScheduleLength == scheduler4.getScheduleLength()
				|| minScheduleLength == scheduler5.getScheduleLength()) {
			return 1;
		} else {
			return 0;
		}
	}

	public ListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation getListSchedulingCcCdClassicWithStaticOrderByBottomLevelComputation() {
		return scheduler1;
	}

	public ListSchedulingCcCdClassicWithStaticOrderByBottomLevel getListSchedulingCcCdClassicWithStaticOrderByBottomLevel() {
		return scheduler2;
	}

	public ListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn getListSchedulingCcCdClassicWithStaticOrderByBottomLevelIn() {
		return scheduler3;
	}

	public ListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut getListSchedulingCcCdClassicWithStaticOrderByBottomLevelOut() {
		return scheduler4;
	}

	public ListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut getListSchedulingCcCdClassicWithStaticOrderByBottomLevelInOut() {
		return scheduler5;
	}

	public ListSchedulingClassic getListSchedulingClassic() {
		return schedulerClassic;
	}

}
