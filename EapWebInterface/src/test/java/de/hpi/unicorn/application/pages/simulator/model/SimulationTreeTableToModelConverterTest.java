/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.bpmn.decomposition.ANDComponent;
import de.hpi.unicorn.bpmn.decomposition.LoopComponent;
import de.hpi.unicorn.bpmn.decomposition.SequenceComponent;
import de.hpi.unicorn.bpmn.decomposition.XORComponent;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.utils.SetUtil;

public class SimulationTreeTableToModelConverterTest {

	private EventTree<Object> tree;
	private EapEventType e1;
	private EapEventType e2;

	@Before
	public void setup() {
		this.tree = new EventTree<Object>();
		final SequenceComponent sequence1 = new SequenceComponent(null, null, null, null);
		this.tree.add(sequence1);
		final XORComponent xor1 = new XORComponent(null, null, null, null);
		this.tree.addChild(sequence1, xor1);
		this.e1 = new EapEventType("E1");
		this.e2 = new EapEventType("E2");
		this.tree.addChild(xor1, this.e1);
		this.tree.addChild(xor1, this.e2);
		final ANDComponent and1 = new ANDComponent(null, null, null, null);
		this.tree.addChild(sequence1, and1);
		final LoopComponent loop1 = new LoopComponent(null, null, null, null);
		this.tree.addChild(and1, loop1);
		this.tree.addChild(loop1, this.e2);
		this.tree.addChild(loop1, this.e1);
		final SequenceComponent sequence2 = new SequenceComponent(null, null, null, null);
		this.tree.addChild(and1, sequence2);
		this.tree.addChild(sequence2, this.e1);
		this.tree.addChild(sequence2, this.e2);
	}

	@Test
	public void testConversion() {
		final SimulationTreeTableToModelConverter converter = new SimulationTreeTableToModelConverter();
		final BPMNProcess process = converter.convertTreeToModel(this.tree);
		Assert.assertTrue("Should be 14, but was " + process.getBPMNElementsWithOutSequenceFlows().size(), process
				.getBPMNElementsWithOutSequenceFlows().size() == 14);
		Assert.assertNotNull(process.getStartEvent());
		final BPMNStartEvent startEvent = process.getStartEvent();
		Assert.assertTrue(startEvent.getSuccessors().size() == 1);
		Assert.assertTrue(startEvent.getSuccessors().iterator().next() instanceof BPMNXORGateway);
		final BPMNXORGateway xor1 = (BPMNXORGateway) startEvent.getSuccessors().iterator().next();
		List<AbstractBPMNElement> successors = SetUtil.asList(xor1.getSuccessors());
		Assert.assertTrue(successors.size() == 2);
		Assert.assertTrue(successors.get(0) instanceof BPMNTask);
		Assert.assertTrue(successors.get(1) instanceof BPMNTask);
		Assert.assertTrue(successors.get(0).getSuccessors().iterator().next() instanceof BPMNXORGateway);
		final BPMNXORGateway xor2 = (BPMNXORGateway) successors.get(0).getSuccessors().iterator().next();

		Assert.assertTrue(xor2.getSuccessors().size() == 1);
		Assert.assertTrue("Should be BPMNAndGateway, but was " + xor2.getSuccessors().iterator().next(), xor2
				.getSuccessors().iterator().next() instanceof BPMNAndGateway);
		final BPMNAndGateway and1 = (BPMNAndGateway) xor2.getSuccessors().iterator().next();
		Assert.assertTrue(and1.getSuccessors().size() == 2);
		successors = SetUtil.asList(and1.getSuccessors());
		for (final AbstractBPMNElement successor : successors) {
			if (successor instanceof BPMNTask) {
				final BPMNTask task = (BPMNTask) successor;
				if (task.getMonitoringPoints().get(0).getEventType().equals(this.e1)) {
					Assert.assertTrue(task.getSuccessors().iterator().next() instanceof BPMNTask);
				}
			} else {
				Assert.assertTrue(successor instanceof BPMNXORGateway);
				final BPMNXORGateway loopEntry = (BPMNXORGateway) successor;
				Assert.assertTrue(loopEntry.getPredecessors().size() == 2);
				Assert.assertTrue(loopEntry.getSuccessors().size() == 1);
				Assert.assertTrue(loopEntry.getSuccessors().iterator().next() instanceof BPMNTask);
				final BPMNTask loopTask1 = (BPMNTask) loopEntry.getSuccessors().iterator().next();
				Assert.assertTrue(loopTask1.getSuccessors().size() == 1);
				Assert.assertTrue(loopTask1.getSuccessors().iterator().next() instanceof BPMNTask);
				final BPMNTask loopTask2 = (BPMNTask) loopTask1.getSuccessors().iterator().next();
				Assert.assertTrue(loopTask2.getSuccessors().size() == 1);
				Assert.assertTrue(loopTask2.getSuccessors().iterator().next() instanceof BPMNXORGateway);
				final BPMNXORGateway loopExit = (BPMNXORGateway) loopTask2.getSuccessors().iterator().next();
				Assert.assertTrue(loopExit.getSuccessors().size() == 2);
				for (final AbstractBPMNElement loopExitSuccessor : loopExit.getSuccessors()) {
					if (loopExitSuccessor instanceof BPMNAndGateway) {
						Assert.assertTrue(loopExitSuccessor.getPredecessors().size() == 2);
						Assert.assertTrue(loopExitSuccessor.getSuccessors().size() == 1);
						Assert.assertTrue(loopExitSuccessor.getSuccessors().contains(process.getEndEvent()));
					}
				}
			}
		}
	}

}
