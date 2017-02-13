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
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.utils.Tuple;

/**
 * The central class for simulation
 */
public class Simulator {

	private int numberOfInstances;
	private final AbstractBPMNElement startEvent;
	private final List<InstanceSimulator> instanceSimulators;
	private final Map<TypeTreeNode, List<Serializable>> attributesAndValues;
	private Map<TypeTreeNode, List<Serializable>> correlationAttributesMap;
	private Date currentSimulationDate;
	private final Map<AbstractBPMNElement, Long> elementExecutionDurations;
	private final Map<AbstractBPMNElement, DerivationType> elementTimeDerivationTypes;
	private final Random random = new Random();
	private final Map<AbstractBPMNElement, Long> elementDerivations;
	private final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>> xorSplitsWithSuccessorProbabilities;
	private final List<TypeTreeNode> identicalAttributes;
	private final List<TypeTreeNode> differingAttributes;

	public Simulator(final CorrelationProcess process, final BPMNProcess bpmnProcess,
			final Map<TypeTreeNode, List<Serializable>> attributesAndValues,
			final Map<AbstractBPMNElement, String> tasksDurationString,
			final Map<AbstractBPMNElement, String> tasksDerivationString,
			final Map<AbstractBPMNElement, DerivationType> tasksDerivationTypes,
			final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>> xorSplitsWithSuccessorProbabilities) {
		this.startEvent = bpmnProcess.getStartEvent();
		this.instanceSimulators = new ArrayList<InstanceSimulator>();
		this.attributesAndValues = attributesAndValues;
		this.correlationAttributesMap = this.getCorrelationAttributesMap(process);
		this.numberOfInstances = 0;
		this.currentSimulationDate = new Date();
		this.elementExecutionDurations = SimulationUtils.getDurationsFromMap(tasksDurationString);
		this.elementDerivations = SimulationUtils.getDurationsFromMap(tasksDerivationString);
		this.elementTimeDerivationTypes = tasksDerivationTypes;
		this.xorSplitsWithSuccessorProbabilities = xorSplitsWithSuccessorProbabilities;
		this.identicalAttributes = new ArrayList<TypeTreeNode>();
		this.differingAttributes = new ArrayList<TypeTreeNode>();
	}

	private Map<TypeTreeNode, List<Serializable>> getCorrelationAttributesMap(final CorrelationProcess process) {
		this.correlationAttributesMap = new HashMap<TypeTreeNode, List<Serializable>>();
		for (final TypeTreeNode correlationAttribute : process.getCorrelationAttributes()) {
			for (final TypeTreeNode attribute : this.attributesAndValues.keySet()) {
				if (correlationAttribute.equals(attribute)) {
					this.correlationAttributesMap.put(correlationAttribute, this.attributesAndValues.get(attribute));
					break;
				}
			}

		}
		return this.correlationAttributesMap;
	}

	/**
	 * Simulates the process numberOfInstances over 1 Day
	 * 
	 * @param numberOfInstances
	 */
	public void simulate(final int numberOfInstances) {
		this.simulate(numberOfInstances, 1);
	}

	/**
	 * Simulates the process numberOfInstances over a numberOfDays
	 * 
	 * @param numberOfInstances
	 * @param numberOfDays
	 */
	public void simulate(final int numberOfInstances, final int numberOfDays) {
		final int timeDifferenceInMs = this.timeDifferenceInMs(numberOfInstances, numberOfDays);
		for (int i = 0; i < numberOfInstances; i++) {
			final Map<TypeTreeNode, List<Serializable>> instanceAttributes = this.createNewInstanceAttributesMap();
			final InstanceSimulator instanceSimulator = new InstanceSimulator(this.startEvent, this,
					instanceAttributes, this.currentSimulationDate, this.differingAttributes);
			this.currentSimulationDate = new Date(this.currentSimulationDate.getTime() + timeDifferenceInMs);
			this.instanceSimulators.add(instanceSimulator);
		}
		this.startSimulation();
	}

	/**
	 * starts the simulation bei repating to
	 */
	private void startSimulation() {
		while (!this.instanceSimulators.isEmpty()) {
			this.getEarliestInstanceSimulator().simulateStep();
		}
	}

	private Map<TypeTreeNode, List<Serializable>> createNewInstanceAttributesMap() {
		final Map<TypeTreeNode, List<Serializable>> instanceCorrelationAttributes = this.getNextCorrelationAttributes();
		final Map<TypeTreeNode, List<Serializable>> instanceAttributes = new HashMap<TypeTreeNode, List<Serializable>>();
		for (final TypeTreeNode attribute : this.attributesAndValues.keySet()) {
			instanceAttributes.put(attribute, new ArrayList<Serializable>(this.attributesAndValues.get(attribute)));
		}
		this.addIdenticalAttributes(instanceAttributes);
		this.addCorrelationAttributes(instanceCorrelationAttributes, instanceAttributes);
		return instanceAttributes;
	}

	private void addIdenticalAttributes(final Map<TypeTreeNode, List<Serializable>> instanceAttributes) {
		final Random random = new Random();
		int index;
		List<Serializable> valueList;
		for (final TypeTreeNode identicalAttribute : this.identicalAttributes) {
			for (final TypeTreeNode existingAttribute : instanceAttributes.keySet()) {
				if (identicalAttribute.equals(existingAttribute)) {
					valueList = new ArrayList<Serializable>();
					index = random.nextInt(instanceAttributes.get(existingAttribute).size());
					final Serializable chosenObject = this.attributesAndValues.get(existingAttribute).get(index);
					valueList.add(chosenObject);
					instanceAttributes.put(existingAttribute, valueList);
					break;
				}

			}
		}
	}

	/**
	 * overwrites the correlation attributes to make same identical per instance
	 * and unique between instances
	 */
	private void addCorrelationAttributes(final Map<TypeTreeNode, List<Serializable>> instanceCorrelationAttributes,
			final Map<TypeTreeNode, List<Serializable>> instanceAttributes) {
		// Korrelationsattribute überschreiben andere ausgewählte
		for (final TypeTreeNode correlationAttribute : instanceCorrelationAttributes.keySet()) {
			for (final TypeTreeNode existingAttribute : instanceAttributes.keySet()) {
				if (correlationAttribute.equals(existingAttribute)) {
					instanceAttributes.put(existingAttribute, instanceCorrelationAttributes.get(correlationAttribute));
					break;
				}
			}
		}
	}

	/**
	 * creates new correlation attributes for each instance by counting and
	 * stepping throught the possible values
	 */
	private Map<TypeTreeNode, List<Serializable>> getNextCorrelationAttributes() {
		final HashMap<TypeTreeNode, List<Serializable>> instanceCorrelationAttributes = new HashMap<TypeTreeNode, List<Serializable>>();
		// die Zahl wird jedes mal inkrementiert, um neue Werte zu erzeugen
		int index = this.numberOfInstances;
		int nextIndex;
		for (final TypeTreeNode correlationAttributeKey : this.correlationAttributesMap.keySet()) {
			// TODO: werte aus event... holen
			final List<Serializable> correlationAttribute = this.correlationAttributesMap.get(correlationAttributeKey);
			nextIndex = 0;
			final int mapsize = correlationAttribute.size();
			while (index >= mapsize) {
				nextIndex++;
				index = index - mapsize;
			}
			final Serializable value = correlationAttribute.get(index);
			final List<Serializable> valueList = new ArrayList<Serializable>();
			valueList.add(value);
			instanceCorrelationAttributes.put(correlationAttributeKey, valueList);
			index = nextIndex;
		}
		this.numberOfInstances++;
		return instanceCorrelationAttributes;
	}

	public void unsubscribe(final InstanceSimulator simulator) {
		this.instanceSimulators.remove(simulator);
	}

	public int timeDifferenceInMs(final int numberOfInstances, final int numberOfDays) {

		final int totalSimulationPeriodInMs = numberOfDays * 24 * 60 * 60 * 1000;
		return totalSimulationPeriodInMs / numberOfInstances;
	}

	public long getMeanDurationForBPMNElement(final AbstractBPMNElement element) {
		long meanDuration;
		if (this.elementExecutionDurations != null && this.elementExecutionDurations.containsKey(element)) {
			meanDuration = this.elementExecutionDurations.get(element);
		} else {
			meanDuration = 0;
		}
		return meanDuration;
	}

	public long getDerivationForBPMNElement(final AbstractBPMNElement element) {
		long derivation;
		if (this.elementDerivations != null && this.elementDerivations.containsKey(element)) {
			derivation = this.elementDerivations.get(element);
		} else {
			derivation = 0;
		}
		return derivation;
	}

	/**
	 * returns the duration for executing a bpmn-element, calculation depends on
	 * the derivationType
	 */
	public long getDurationForBPMNElement(final AbstractBPMNElement element) {
		final long mean = this.getMeanDurationForBPMNElement(element);
		final long derivation = this.getDerivationForBPMNElement(element);
		if (this.elementTimeDerivationTypes == null || (this.elementTimeDerivationTypes.get(element) == null)) {
			return mean;
		}
		if (this.elementTimeDerivationTypes.get(element).equals(DerivationType.NORMAL)) {
			final double dur = mean + (this.random.nextGaussian() * derivation);
			return (long) dur;
		} else {
			return mean;
		}

	}

	/**
	 * randomly chooses a path with the given probabilities
	 */
	public AbstractBPMNElement choosePath(final AbstractBPMNElement currentElement) {
		final List<Tuple<AbstractBPMNElement, Integer>> successorsWithProbability = this.xorSplitsWithSuccessorProbabilities
				.get(currentElement);
		final Random random = new Random();
		final int index = random.nextInt(100);
		for (final Tuple<AbstractBPMNElement, Integer> tuple : successorsWithProbability) {
			if (index < tuple.y) {
				return tuple.x;
			}
		}
		return null;
	}

	private InstanceSimulator getEarliestInstanceSimulator() {
		InstanceSimulator earliestInstanceSimulator = this.instanceSimulators.get(0);
		for (final InstanceSimulator instanceSimulator : this.instanceSimulators) {
			if (instanceSimulator.getEarliestDate().before(earliestInstanceSimulator.getEarliestDate())) {
				earliestInstanceSimulator = instanceSimulator;
			}
		}
		return earliestInstanceSimulator;
	}

	public void addAdvancedValueRules(final List<ValueRule> valueRules) {
		for (final ValueRule valueRule : valueRules) {
			if (valueRule.getRuleType().equals(ValueRuleType.EQUAL)) {
				this.identicalAttributes.add(valueRule.getAttribute());
			} else {
				this.differingAttributes.add(valueRule.getAttribute());
			}
		}
	}
}
