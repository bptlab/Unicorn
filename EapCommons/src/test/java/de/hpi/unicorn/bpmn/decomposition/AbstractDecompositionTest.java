/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import java.util.Arrays;

import org.junit.Before;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;

/**
 * Abstract class to centralize some multiple times used test methods. This
 * class provides a example {@link BPMNProcess} with contained
 * {@link AbstractBPMNElement}s.
 * 
 * @author micha
 */
public class AbstractDecompositionTest {

	protected BPMNProcess process;
	protected BPMNTask task11, task2111, task2112, task22, task31, task32, task212;
	protected BPMNAndGateway and1, and2, and211, and212;
	protected BPMNXORGateway xor1, xor2, xor21, xor22, xor211, xor212;
	protected BPMNEndEvent endEvent;
	protected BPMNStartEvent startEvent;

	@Before
	public void setup() {
		startEvent = new BPMNStartEvent("1", "Start", null);
		task11 = new BPMNTask("2", "Task 1.1", null);
		xor1 = new BPMNXORGateway("3", "XOR 1", null);
		xor21 = new BPMNXORGateway("4", "XOR 2.1", null);
		task2111 = new BPMNTask("5", "Task 2.1.1.1", null);
		xor22 = new BPMNXORGateway("6", "XOR 2.2", null);
		xor211 = new BPMNXORGateway("7", "XOR 2.1.1", null);
		task212 = new BPMNTask("8", "Task 2.1.2", null);
		xor212 = new BPMNXORGateway("9", "XOR 2.1.2", null);
		task22 = new BPMNTask("10", "Task 2.2", null);
		and1 = new BPMNAndGateway("11", "And 1", null);
		task31 = new BPMNTask("12", "Task 3.1", null);
		task32 = new BPMNTask("13", "Task 3.2", null);
		and2 = new BPMNAndGateway("14", "And 2", null);
		xor2 = new BPMNXORGateway("15", "XOR 2", null);
		endEvent = new BPMNEndEvent("16", "End", null);
		and211 = new BPMNAndGateway("17", "And 2.1.1", null);
		and212 = new BPMNAndGateway("18", "And 2.1.2", null);
		task2112 = new BPMNTask("19", "Task 2.1.1.2", null);

		AbstractBPMNElement.connectElements(startEvent, task11);
		AbstractBPMNElement.connectElements(task11, xor1);
		AbstractBPMNElement.connectElements(xor1, xor21);
		AbstractBPMNElement.connectElements(xor1, task22);
		AbstractBPMNElement.connectElements(xor1, and1);
		AbstractBPMNElement.connectElements(xor21, and211);
		AbstractBPMNElement.connectElements(and211, task2111);
		AbstractBPMNElement.connectElements(and211, task2112);
		AbstractBPMNElement.connectElements(task2111, and212);
		AbstractBPMNElement.connectElements(task2112, and212);
		AbstractBPMNElement.connectElements(and212, xor22);

		// Repeat-Pattern --> Schleife
		AbstractBPMNElement.connectElements(xor22, xor211);

		// Schleife in der Schleife
		AbstractBPMNElement.connectElements(xor211, task212);

		AbstractBPMNElement.connectElements(task212, xor212);

		AbstractBPMNElement.connectElements(xor212, xor211);

		AbstractBPMNElement.connectElements(xor212, xor21);

		AbstractBPMNElement.connectElements(xor22, xor2);
		AbstractBPMNElement.connectElements(task22, xor2);
		AbstractBPMNElement.connectElements(and1, task31);
		AbstractBPMNElement.connectElements(and1, task32);
		AbstractBPMNElement.connectElements(task31, and2);
		AbstractBPMNElement.connectElements(task32, and2);
		AbstractBPMNElement.connectElements(and2, xor2);
		AbstractBPMNElement.connectElements(xor2, endEvent);

		process = new BPMNProcess("12", "Process", null);
		process.addBPMNElements(Arrays.asList(startEvent, task11, xor1, xor21, task2111, task2112, xor22, xor211,
				task212, xor212, task22, and1, task31, task32, and2, xor2, endEvent, and211, and212));
	}

}
