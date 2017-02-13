/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.simulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;

public class SimulationTest {

	private EapEventType eventType1, eventType2, eventType3, eventType4, eventType5;
	private BPMNProcess simpleBPMNProcess, complexBPMNProcess;
	private Map<TypeTreeNode, List<Serializable>> attributes;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();

		this.createEventTypes();

	}

	private void createEventTypes() {
		final AttributeTypeTree values = new AttributeTypeTree();

		this.attributes = new HashMap<TypeTreeNode, List<Serializable>>();

		this.eventType1 = new EapEventType("EventType1", values);
		Broker.getInstance().importEventType(this.eventType1);
		this.eventType2 = new EapEventType("EventType2", values);
		Broker.getInstance().importEventType(this.eventType2);
		this.eventType3 = new EapEventType("EventType3", values);
		Broker.getInstance().importEventType(this.eventType3);
		this.eventType4 = new EapEventType("EventType4", values);
		Broker.getInstance().importEventType(this.eventType4);
		this.eventType5 = new EapEventType("EventType5", values);
		Broker.getInstance().importEventType(this.eventType5);
	}

	private CorrelationProcess createSimpleCorrelatedProcess() {

		this.simpleBPMNProcess = new BPMNProcess("1", "SimpleProcess", null);
		final BPMNStartEvent startEvent = new BPMNStartEvent("2", "StartEvent", null);
		final BPMNTask task1 = new BPMNTask("3", "Task1", Arrays.asList(new MonitoringPoint(this.eventType1,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNTask task2 = new BPMNTask("4", "Task2", Arrays.asList(new MonitoringPoint(this.eventType2,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNEndEvent endEvent = new BPMNEndEvent("5", "EndEvent", null);

		AbstractBPMNElement.connectElements(startEvent, task1);
		AbstractBPMNElement.connectElements(task1, task2);
		AbstractBPMNElement.connectElements(task2, endEvent);

		final Set<EapEventType> eventTypes = new HashSet<EapEventType>(Arrays.asList(this.eventType1, this.eventType2));
		final CorrelationProcess process = new CorrelationProcess("SimpleProcess", eventTypes);
		process.save();

		this.simpleBPMNProcess.addBPMNElements(Arrays.asList(startEvent, task1, task2, endEvent));
		this.simpleBPMNProcess.save();
		process.setBpmnProcess(this.simpleBPMNProcess);
		process.merge();
		return process;
	}

	private CorrelationProcess createComplexProcess() {
		this.complexBPMNProcess = new BPMNProcess("6", "ComplexProcess", null);
		final BPMNStartEvent startEvent = new BPMNStartEvent("7", "StartEvent", null);
		final BPMNTask task1 = new BPMNTask("8", "Task1", Arrays.asList(new MonitoringPoint(this.eventType1,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNAndGateway and1 = new BPMNAndGateway("9", "XOR1", null);
		final BPMNTask task2 = new BPMNTask("10", "Task2", Arrays.asList(new MonitoringPoint(this.eventType2,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNTask task3 = new BPMNTask("11", "Task3", Arrays.asList(new MonitoringPoint(this.eventType3,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNTask task4 = new BPMNTask("12", "Task4", Arrays.asList(new MonitoringPoint(this.eventType4,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNTask task5 = new BPMNTask("13", "Task5", Arrays.asList(new MonitoringPoint(this.eventType5,
				MonitoringPointStateTransition.terminate, "")));
		final BPMNAndGateway and2 = new BPMNAndGateway("14", "XOR2", null);
		final BPMNEndEvent endEvent = new BPMNEndEvent("15", "EndEvent", null);

		AbstractBPMNElement.connectElements(startEvent, task1);
		AbstractBPMNElement.connectElements(task1, and1);
		AbstractBPMNElement.connectElements(and1, task2);
		AbstractBPMNElement.connectElements(task2, task4);
		AbstractBPMNElement.connectElements(and1, task3);
		AbstractBPMNElement.connectElements(task3, task5);
		AbstractBPMNElement.connectElements(task4, and2);
		AbstractBPMNElement.connectElements(task5, and2);
		AbstractBPMNElement.connectElements(and2, endEvent);

		this.complexBPMNProcess.addBPMNElements(Arrays.asList(startEvent, task1, task2, task3, task4, task5, and1,
				and2, endEvent));
		this.complexBPMNProcess.save();
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>(Arrays.asList(this.eventType1, this.eventType2,
				this.eventType3, this.eventType4, this.eventType5));
		final CorrelationProcess process = new CorrelationProcess("ComplexProcess", eventTypes);
		process.save();
		process.setBpmnProcess(this.simpleBPMNProcess);
		process.merge();
		return process;
	}

	@Test
	public void simpleProcessTest() {
		final CorrelationProcess process = this.createSimpleCorrelatedProcess();

		final Map<AbstractBPMNElement, String> elementDurations = new HashMap<AbstractBPMNElement, String>();
		final Map<AbstractBPMNElement, DerivationType> elementDerivation = new HashMap<AbstractBPMNElement, DerivationType>();
		for (final AbstractBPMNElement bpmnElement : this.simpleBPMNProcess.getBPMNElementsWithOutSequenceFlows()) {
			elementDerivation.put(bpmnElement, DerivationType.FIXED);
			elementDurations.put(bpmnElement, "1");
		}
		final Simulator simulator = new Simulator(process, this.simpleBPMNProcess, this.attributes, elementDurations,
				new HashMap<AbstractBPMNElement, String>(), elementDerivation, null);
		simulator.simulate(1);

		final List<EapEvent> events = EapEvent.findAll();
		Assert.assertTrue(events.size() == 2);
		Assert.assertEquals(this.eventType1, events.get(0).getEventType());
		Assert.assertEquals(this.eventType2, events.get(1).getEventType());
	}

	@Test
	public void complexProcessTest() {
		final CorrelationProcess process = this.createComplexProcess();

		final Map<AbstractBPMNElement, String> elementDurations = new HashMap<AbstractBPMNElement, String>();
		final Map<AbstractBPMNElement, DerivationType> elementDerivation = new HashMap<AbstractBPMNElement, DerivationType>();
		for (final AbstractBPMNElement bpmnElement : this.complexBPMNProcess.getBPMNElementsWithOutSequenceFlows()) {
			elementDerivation.put(bpmnElement, DerivationType.FIXED);
			elementDurations.put(bpmnElement, "1");
		}

		final Simulator simulator = new Simulator(process, this.complexBPMNProcess, this.attributes, elementDurations,
				new HashMap<AbstractBPMNElement, String>(), elementDerivation, null);
		simulator.simulate(1);

		final List<EapEvent> events = EapEvent.findAll();
		Assert.assertTrue(events.size() == 5);
		Assert.assertEquals(this.eventType1, events.get(0).getEventType());
		Assert.assertTrue(this.eventType2.equals(events.get(1).getEventType())
				|| this.eventType3.equals(events.get(1).getEventType()));
		Assert.assertTrue(this.eventType4.equals(events.get(2).getEventType())
				|| this.eventType5.equals(events.get(2).getEventType())
				|| this.eventType2.equals(events.get(1).getEventType())
				|| this.eventType3.equals(events.get(1).getEventType()));
	}
}
