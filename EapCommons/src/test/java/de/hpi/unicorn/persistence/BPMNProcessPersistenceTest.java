/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class tests the saving, finding and removing of {@link BPMNProcess}.
 * 
 * @author micha
 */
public class BPMNProcessPersistenceTest implements PersistenceTest {

	private BPMNProcess process;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		process = new BPMNProcess("1", "SimpleProcess", null);
		BPMNStartEvent startEvent = new BPMNStartEvent("2", "StartEvent", null);
		BPMNTask firstTask = new BPMNTask("3", "firstTask", null);

		AbstractBPMNElement.connectElements(startEvent, firstTask);

		BPMNTask secondTask = new BPMNTask("4", "secondTask", null);

		AbstractBPMNElement.connectElements(firstTask, secondTask);

		BPMNEndEvent endEvent = new BPMNEndEvent("5", "endEvent", null);

		AbstractBPMNElement.connectElements(secondTask, endEvent);

		process.addBPMNElement(startEvent);
		process.addBPMNElement(firstTask);
		process.addBPMNElement(secondTask);
		process.addBPMNElement(endEvent);
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		process.save();
		assertTrue("Value should be 1, but was " + BPMNProcess.findAll().size(), BPMNProcess.findAll().size() == 1);
		BPMNStartEvent startEvent = (BPMNStartEvent) process.getStartEvent();
		assertNotNull(startEvent);
		assertTrue(startEvent.getName().equals("StartEvent"));
		assertTrue(startEvent.getSuccessors().size() == 1);
		assertTrue(startEvent.getSuccessors().iterator().next() instanceof BPMNTask);
		assertTrue(startEvent.getSuccessors().iterator().next().getBPMN_ID().equals("3"));
		BPMNProcess.removeAll();
		assertTrue("Value should be 0, but was " + BPMNProcess.findAll().size(), BPMNProcess.findAll().size() == 0);
	}

	// Test läuft in jUnit, aber in Maven nicht
	// "Exception Description: Missing class indicator field from database row "
	// @Test
	@Override
	public void testFind() {
		// process.save();
		// assertTrue("Value should be 1, but was " +
		// BPMNProcess.findAll().size(), BPMNProcess.findAll().size()==1);
		// assertTrue(BPMNProcess.findByID(2).equals(process));
	}

	@Test
	@Override
	public void testRemove() {
		process.save();
		assertTrue("Value should be 1, but was " + BPMNProcess.findAll().size(), BPMNProcess.findAll().size() == 1);
		process.remove();
		assertTrue("Value should be 0, but was " + BPMNProcess.findAll().size(), BPMNProcess.findAll().size() == 0);

	}

}
