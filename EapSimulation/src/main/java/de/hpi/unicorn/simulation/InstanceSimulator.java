/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;

/**
 * Represents the simulation of one process instance
 */
public class InstanceSimulator {

	public List<PathSimulator> pathSimulators;
	private Simulator simulator;
	private final Map<BPMNAndGateway, List<AbstractBPMNElement>> andJoinsVisitedPredecessors;
	private final Map<TypeTreeNode, List<Serializable>> attributesAndValues;
	private final List<TypeTreeNode> differingAttributes;

	public InstanceSimulator(final AbstractBPMNElement startElement, final Simulator simulator,
			final Map<TypeTreeNode, List<Serializable>> attributesAndValues, final Date currentSimulationDate,
			final List<TypeTreeNode> differingAttributes) {
		this.setSimulator(simulator);
		this.attributesAndValues = attributesAndValues;
		this.differingAttributes = differingAttributes;
		final PathSimulator initialPathSimulator = new PathSimulator(startElement, this, currentSimulationDate);
		this.pathSimulators = new ArrayList<PathSimulator>();
		this.pathSimulators.add(initialPathSimulator);
		this.andJoinsVisitedPredecessors = new HashMap<BPMNAndGateway, List<AbstractBPMNElement>>();
	}

	/**
	 * gets the earliest PathSimulator and starts it
	 */
	public void simulateStep() {

		final List<EapEvent> newEvents = this.getEarliestSubSimulator().continueSimulation();
		final Random random = new Random();
		int index;
		for (final EapEvent event : newEvents) {
			final EapEventType eventType = event.getEventType();
			for (final TypeTreeNode attribute : eventType.getValueTypes()) {
				for (final TypeTreeNode key : this.attributesAndValues.keySet()) {
					if (attribute.equals(key)) {
						final List<Serializable> values = this.attributesAndValues.get(key);
						index = random.nextInt(values.size());
						event.getValues().put(attribute.getAttributeExpression(), values.get(index));
						for (final TypeTreeNode differingAttribute : this.differingAttributes) {
							if (differingAttribute.equals(attribute)) {
								if (values.size() > 1) {
									this.attributesAndValues.get(key).remove(index);
								}
								break;
							}
						}
						break;
					}
				}
			}
			Broker.getEventImporter().importEvent(event);
		}
		if (this.pathSimulators.isEmpty()) {
			this.getSimulator().unsubscribe(this);
		}
	}

	public void unsubscribe(final PathSimulator simulator) {
		this.pathSimulators.remove(simulator);
	}

	public Simulator getSimulator() {
		return this.simulator;
	}

	private void setSimulator(final Simulator parentSimulator) {
		this.simulator = parentSimulator;
	}

	public PathSimulator getEarliestSubSimulator() {
		PathSimulator earliestSubSimulator = this.pathSimulators.get(0);
		for (final PathSimulator subSimulator : this.pathSimulators) {
			if (subSimulator.getCurrentSimulationDate().before(earliestSubSimulator.getCurrentSimulationDate())) {
				earliestSubSimulator = subSimulator;
			}
		}
		return earliestSubSimulator;
	}

	public Date getEarliestDate() {
		return this.getEarliestSubSimulator().getCurrentSimulationDate();
	}

	public void addJoinPredecessorToGateway(final AbstractBPMNElement predecessor, final BPMNAndGateway gateway) {
		if (this.andJoinsVisitedPredecessors.containsKey(gateway)) {
			this.andJoinsVisitedPredecessors.get(gateway).add(predecessor);
		} else {
			final List<AbstractBPMNElement> predecessorList = new ArrayList<AbstractBPMNElement>();
			predecessorList.add(predecessor);
			this.andJoinsVisitedPredecessors.put(gateway, predecessorList);
		}
	}

	public Boolean allPredecessorsOfGatewayVisited(final BPMNAndGateway gateway) {
		return this.andJoinsVisitedPredecessors.containsKey(gateway)
				&& this.andJoinsVisitedPredecessors.get(gateway).containsAll(gateway.getPredecessors());
	}

	public void resetGateway(final BPMNAndGateway gateway) {
		for (final AbstractBPMNElement predecessor : gateway.getPredecessors()) {
			this.andJoinsVisitedPredecessors.get(gateway).remove(predecessor);
		}
	}
}
