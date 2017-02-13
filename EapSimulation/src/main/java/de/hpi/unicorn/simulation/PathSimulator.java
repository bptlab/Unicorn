/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.simulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.event.EapEvent;

/**
 * Represents one active path during the execution of a process
 */
public class PathSimulator {

	private AbstractBPMNElement currentElement;
	private InstanceSimulator instanceSimulator;
	private Date currentSimulationDate;
	private Boolean currentElementIsTraversed;
	private final List<EapEvent> newEvents;

	public PathSimulator(final AbstractBPMNElement startElement, final InstanceSimulator parentSimulator,
			final Date currentSimulationDate) {
		this.setCurrentElement(startElement);
		this.setInstanceSimulator(parentSimulator);
		this.setCurrentSimulationDate(currentSimulationDate);
		this.setCurrentElementIsTraversed(false);
		this.newEvents = new ArrayList<EapEvent>();
	}

	/**
	 * Traverses the current element or if it is allready traversed continues to
	 * the next - produces events
	 */
	public List<EapEvent> continueSimulation() {
		this.newEvents.clear();
		if (this.currentElementIsTraversed) {
			this.addEventFromMonitorinPointTerminate();

			final AbstractBPMNElement nextElement = this.getNextElement();

			if (this.getCurrentElement() instanceof BPMNXORGateway) {
				this.skipUnreachableElements(nextElement);
			}

			this.setCurrentElement(nextElement);
			this.setCurrentElementIsTraversed(false);
			if (this.getCurrentElement() == null) {
				this.getInstanceSimulator().unsubscribe(this);
			} else {
				// Terminate eines Elements bewirkt u.U. Enable des nächsten
				this.addEventFromMonitorinPointEnable();
			}
		} else {
			this.addEventFromMonitorinPointBegin();
			this.setCurrentSimulationDate(new Date(this.getCurrentSimulationDate().getTime()
					+ this.getInstanceSimulator().getSimulator().getDurationForBPMNElement(this.getCurrentElement())));
			this.setCurrentElementIsTraversed(true);
		}
		return this.newEvents;
	}

	/**
	 * skips unreachable elements after an XOR-Split by marking all indirect
	 * predecessors of the XOR and demarking all indirect predecessors of any
	 * actualElement of a PathSimulator from the same instance
	 */
	private void skipUnreachableElements(final AbstractBPMNElement nextElement) {
		final Set<AbstractBPMNElement> unreachableElements = this.getCurrentElement().getIndirectSuccessors();
		if (nextElement != null) {
			unreachableElements.remove(nextElement);
			this.setCurrentElement(nextElement);
			for (final PathSimulator pathSimulator : this.getInstanceSimulator().pathSimulators) {
				unreachableElements.removeAll(pathSimulator.getCurrentElement().getIndirectSuccessors());
			}
		}
		this.skipElements(unreachableElements);
	}

	/**
	 * Returns true if there is exactly one path to go - creates new
	 * PathSimulators if 2 or more paths should be simulated at once
	 */
	private AbstractBPMNElement getNextElement() {
		if (this.getCurrentElement().hasSuccessors()) {
			// Bei mehreren ausgehenden Kanten...
			if (this.getCurrentElement().getSuccessors().size() > 1) {
				// Beim XOR-Split eine Kante verfolgen
				// TODO: deferred choice, OR, complex?
				if (this.getCurrentElement() instanceof BPMNXORGateway) {
					return this.getInstanceSimulator().getSimulator().choosePath(this.getCurrentElement());
				}
				// ansonsten alle Kanten verfolgen
				else {
					PathSimulator pathSimulator;
					for (final AbstractBPMNElement successor : this.getCurrentElement().getSuccessors()) {
						// ein Simulator für jede Kante erzeugen
						if (successor instanceof BPMNAndGateway) {
							this.getInstanceSimulator().addJoinPredecessorToGateway(this.getCurrentElement(),
									(BPMNAndGateway) successor);
							if ((this.getInstanceSimulator()
									.allPredecessorsOfGatewayVisited((BPMNAndGateway) successor))) {
								this.getInstanceSimulator().resetGateway((BPMNAndGateway) successor);
								pathSimulator = new PathSimulator(successor, this.getInstanceSimulator(),
										this.getCurrentSimulationDate());
								this.getInstanceSimulator().pathSimulators.add(pathSimulator);
							}
						}
						pathSimulator = new PathSimulator(successor, this.getInstanceSimulator(),
								this.getCurrentSimulationDate());
						this.getInstanceSimulator().pathSimulators.add(pathSimulator);
					}
				}
			}
			// Nachfolger durchlaufen (eine ausgehende Kante)
			else {
				final AbstractBPMNElement successor = this.getCurrentElement().getSuccessors().iterator().next();
				if (successor instanceof BPMNAndGateway) {
					this.getInstanceSimulator().addJoinPredecessorToGateway(this.getCurrentElement(),
							(BPMNAndGateway) successor);
					if ((this.getInstanceSimulator().allPredecessorsOfGatewayVisited((BPMNAndGateway) successor))) {
						this.getInstanceSimulator().resetGateway((BPMNAndGateway) successor);

					} else {
						return null;
					}
				}
				return successor;
			}
		}
		return null;
	}

	private InstanceSimulator getInstanceSimulator() {
		return this.instanceSimulator;
	}

	private void setInstanceSimulator(final InstanceSimulator instanceSimulator) {
		this.instanceSimulator = instanceSimulator;
	}

	public Date getCurrentSimulationDate() {
		return this.currentSimulationDate;
	}

	private void setCurrentSimulationDate(final Date currentSimulationDate) {
		this.currentSimulationDate = currentSimulationDate;
	}

	private void addEventFromMonitorinPointEnable() {
		this.addEventFromMonitorinPoint(MonitoringPointStateTransition.enable);
	}

	private void addEventFromMonitorinPointBegin() {
		this.addEventFromMonitorinPoint(MonitoringPointStateTransition.begin);
	}

	private void addEventFromMonitorinPointTerminate() {
		this.addEventFromMonitorinPoint(MonitoringPointStateTransition.terminate);
	}

	private void addEventFromMonitorinPointSkip() {
		this.addEventFromMonitorinPoint(MonitoringPointStateTransition.skip);
	}

	private void addEventFromMonitorinPoint(final MonitoringPointStateTransition stateTransition) {
		final MonitoringPoint monitoringPoint = this.getCurrentElement().getMonitoringPointByStateTransitionType(
				stateTransition);
		if (monitoringPoint != null) {
			this.newEvents.add(new EapEvent(monitoringPoint.getEventType(), this.getCurrentSimulationDate()));
		}
	}

	private void setCurrentElementIsTraversed(final Boolean currentElementIsTraversed) {
		this.currentElementIsTraversed = currentElementIsTraversed;
	}

	private void skipElements(final Set<AbstractBPMNElement> unreachableElements) {
		final Iterator<AbstractBPMNElement> iterator = unreachableElements.iterator();
		while (iterator.hasNext()) {
			this.setCurrentElement(iterator.next());
			this.addEventFromMonitorinPointSkip();
		}
	}

	public AbstractBPMNElement getCurrentElement() {
		return this.currentElement;
	}

	private void setCurrentElement(final AbstractBPMNElement currentElement) {
		this.currentElement = currentElement;
	}

}
