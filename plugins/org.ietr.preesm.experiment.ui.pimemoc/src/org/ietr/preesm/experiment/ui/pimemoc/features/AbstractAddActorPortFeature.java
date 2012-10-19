package org.ietr.preesm.experiment.ui.pimemoc.features;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.graphiti.util.IColorConstant;
import org.ietr.preesm.experiment.model.pimemoc.Actor;
import org.ietr.preesm.experiment.ui.pimemoc.util.PimemocUtil;

/**
 * Feature called to create and add a port to an actor
 * 
 * @author kdesnos
 * 
 */
public abstract class AbstractAddActorPortFeature extends AbstractCustomFeature {

	/**
	 * Position of the port
	 * 
	 * @author kdesnos
	 * 
	 */
	public enum PortPosition {
		LEFT, RIGHT
	}

	boolean hasDoneChanges = false;

	@Override
	public boolean hasDoneChanges() {
		return this.hasDoneChanges;
	}

	public static final IColorConstant PORT_BACKGROUND = IColorConstant.BLACK;

	public static final IColorConstant PORT_TEXT_FOREGROUND = IColorConstant.BLACK;
	
	/**
	 * Size of the space between the label of a port and the GA
	 */
	public static final int PORT_LABEL_GA_SPACE = 2;

	/**
	 * Size of the GA of the anchor
	 */
	public static final int PORT_ANCHOR_GA_SIZE = 8;

	public AbstractAddActorPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		// allow if exactly one pictogram element
		// representing an Actor is selected
		boolean ret = false;
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Actor) {
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public void execute(ICustomContext context) {
		// Re-check if only one element is selected
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			// Retrieve the container shape
			ContainerShape containerShape = (ContainerShape) pes[0];
			// Retrieve the rectangle graphic algorithm
			GraphicsAlgorithm gaRectangle = pes[0].getGraphicsAlgorithm();
			// Get the PeCreateService
			IPeCreateService peCreateService = Graphiti.getPeCreateService();
			// Get the GaService
			IGaService gaService = Graphiti.getGaService();

			// create an box relative anchor
			final BoxRelativeAnchor boxAnchor = peCreateService
					.createBoxRelativeAnchor(containerShape);
			if (this.getPosition() == PortPosition.LEFT) {
				boxAnchor.setRelativeWidth(0.0);
			} else {
				boxAnchor.setRelativeWidth(1.0);
			}
			boxAnchor.setRelativeHeight(1.0); // The height will be fixed by the
												// layout feature
			boxAnchor.setReferencedGraphicsAlgorithm(gaRectangle);

			// Ask the name of the new port
			String portName = PimemocUtil.askString(this.getName(),
					this.getDescription(), "NewPort", null);
			if (portName == null) {
				this.hasDoneChanges = false;
				return;
			}

			// Retrieve the size of the text
			IDimension size = GraphitiUi.getUiLayoutService()
					.calculateTextSize(portName, this.getPortFont());

			// create invisible rectangle
			Rectangle invisibleRectangle = gaService
					.createInvisibleRectangle(boxAnchor);

			// Layout the invisible rectangle
			if (this.getPosition() == PortPosition.LEFT) {
				gaService.setLocationAndSize(invisibleRectangle, 0, 0,
						size.getWidth() + PORT_ANCHOR_GA_SIZE + PORT_LABEL_GA_SPACE,
						size.getHeight());
			} else {
				gaService.setLocationAndSize(invisibleRectangle,
						-size.getWidth() - PORT_ANCHOR_GA_SIZE - PORT_LABEL_GA_SPACE, 0,
						size.getWidth() + PORT_ANCHOR_GA_SIZE + PORT_LABEL_GA_SPACE,
						size.getHeight());
			}

			// Add a text label for the box relative anchor
			this.addPortLabel(invisibleRectangle, portName);

			// add a graphics algorithm for the box relative anchor
			this.addPortGA(invisibleRectangle);

			// Call the layout feature
			layoutPictogramElement(containerShape);

			// Update the Pictogram element
			updatePictogramElement(containerShape);

			this.hasDoneChanges = true;
		}
	}

	/**
	 * Retrieve the {@link PortPosition} of the port
	 * 
	 * @return the PortPosition
	 */
	public abstract PortPosition getPosition();

	/**
	 * Add a GraphicAlgorithm of the port.
	 * 
	 * @param containerShape
	 *            the shape containing the port {@link GraphicsAlgorithm}.
	 * 
	 * @return the graphic algorithm
	 */
	public abstract GraphicsAlgorithm addPortGA(GraphicsAlgorithm containerShape);

	/**
	 * Add a label to the port
	 * 
	 * @param containerShape
	 *            the shape containing the port
	 * @param label
	 *            the name of the port
	 * @return
	 */
	public abstract GraphicsAlgorithm addPortLabel(
			GraphicsAlgorithm containerShape, String portName);

	/**
	 * Get the font of the port
	 * 
	 * @return the font
	 */
	public abstract Font getPortFont();

}