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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This class tests the saving, finding and removing of
 * {@link CorrelationProcessInstance}.
 * 
 * @author micha
 */
public class ProcessInstancePersistorTest implements PersistenceTest {

	private EapEvent michaEvent;
	private EapEvent tsunEvent;
	private EapEventType michaEventType;
	private EapEventType tsunEventType;
	private CorrelationProcess firstProcess;
	private CorrelationProcessInstance firstProcessInstance;
	private int processID;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();

		Map<String, Serializable> hm = new HashMap<String, Serializable>();
		hm.put("kuchen", "kaese");
		hm.put("kuchen2", "kirsch");
		hm.put("kuchen3", "apfel");
		tsunEventType = new EapEventType("Tsun");
		tsunEventType.save();
		tsunEvent = new EapEvent(tsunEventType, new Date(), hm);
		tsunEvent.save();

		Map<String, Serializable> hm2 = new HashMap<String, Serializable>();
		hm2.put("getraenk1", "cola");
		hm2.put("getraenk2", "apfelsaft");
		hm2.put("getraenk3", "fanta");
		Date oldDate = null;
		try {
			oldDate = new SimpleDateFormat("dd/MM/yyyy").parse("18/05/2011");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		michaEventType = new EapEventType("Micha");
		michaEventType.save();
		michaEvent = new EapEvent(michaEventType, oldDate, hm2);
		michaEvent.save();
	}

	@Override
	@Test
	public void testStoreAndRetrieve() {
		storeExampleProcessInstances();
		assertTrue("Value should be 2, but was " + CorrelationProcessInstance.findAll().size(),
				CorrelationProcessInstance.findAll().size() == 2);
		CorrelationProcessInstance.removeAll();
		assertTrue("Value should be 0, but was " + CorrelationProcessInstance.findAll().size(),
				CorrelationProcessInstance.findAll().size() == 0);
	}

	private void storeExampleProcessInstances() {
		TypeTreeNode correlationAttribute = new TypeTreeNode("location", AttributeTypeEnum.INTEGER);

		Map<String, Serializable> correlation1 = new HashMap<String, Serializable>();
		correlation1.put(correlationAttribute.getAttributeExpression(), "1");

		Map<String, Serializable> correlation2 = new HashMap<String, Serializable>();
		correlation2.put(correlationAttribute.getAttributeExpression(), "2");

		firstProcessInstance = new CorrelationProcessInstance(correlation1);
		firstProcessInstance.addEvent(tsunEvent);
		firstProcessInstance.save();
		tsunEvent.addProcessInstance(firstProcessInstance);
		tsunEvent.save();

		CorrelationProcessInstance secondProcessInstance = new CorrelationProcessInstance(correlation2);
		secondProcessInstance.addEvent(michaEvent);
		secondProcessInstance.save();
		michaEvent.addProcessInstance(secondProcessInstance);
		michaEvent.save();

		ArrayList<CorrelationProcessInstance> processInstances = new ArrayList<CorrelationProcessInstance>(
				Arrays.asList(firstProcessInstance, secondProcessInstance));
		CorrelationProcessInstance.save(processInstances);
		assertTrue(CorrelationProcessInstance.findAll().size() == 2);

		CorrelationProcess process = new CorrelationProcess();
		process.addProcessInstance(firstProcessInstance);
		process.addProcessInstance(secondProcessInstance);
		process.save();
		processID = process.getID();
	}

	private void storeExampleProcess() {
		firstProcess = new CorrelationProcess("Kino");
		firstProcess.addEventType(new EapEventType("KinoEvent"));
		firstProcess.addProcessInstance(firstProcessInstance);
		firstProcess.save();
	}

	@Override
	@Test
	public void testFind() {
		storeExampleProcessInstances();
		storeExampleProcess();
		assertTrue(CorrelationProcessInstance.findAll().size() == 2);
		assertTrue("Value should be 2, but was "
				+ CorrelationProcessInstance.findByCorrelationAttribute("location").size(), CorrelationProcessInstance
				.findByCorrelationAttribute("location").size() == 2);
		List<CorrelationProcessInstance> processInstances = CorrelationProcessInstance
				.findByCorrelationAttribute("location");
		testFirstProcessInstance(processInstances.get(0));
		testSecondProcessInstance(processInstances.get(1));
		processInstances = CorrelationProcessInstance.findByCorrelationAttributeAndValue("location", "1");
		testFirstProcessInstance(processInstances.get(0));
		processInstances = CorrelationProcessInstance.findByCorrelationAttributeAndValue("location", "2");
		testSecondProcessInstance(processInstances.get(0));
		processInstances = CorrelationProcessInstance.findByContainedEvent(tsunEvent);
		testFirstProcessInstance(processInstances.get(0));
		processInstances = CorrelationProcessInstance.findByContainedEvent(michaEvent);
		testSecondProcessInstance(processInstances.get(0));
		processInstances = CorrelationProcessInstance.findByContainedEventType(tsunEventType);
		testFirstProcessInstance(processInstances.get(0));
		processInstances = CorrelationProcessInstance.findByContainedEventType(michaEventType);
		testSecondProcessInstance(processInstances.get(0));
		processInstances = CorrelationProcessInstance.findByProcess(firstProcess);
		testFirstProcessInstance(processInstances.get(0));
	}

	private void testFirstProcessInstance(CorrelationProcessInstance processInstance) {
		assertTrue(processInstance.getEvents().size() == 1);
		assertTrue(processInstance.getEvents().get(0).getEventType().getTypeName().equals("Tsun"));
	}

	private void testSecondProcessInstance(CorrelationProcessInstance processInstance) {
		assertTrue(processInstance.getEvents().size() == 1);
		assertTrue(processInstance.getEvents().get(0).getEventType().getTypeName().equals("Micha"));
	}

	@Override
	@Test
	public void testRemove() {
		storeExampleProcessInstances();
		List<CorrelationProcessInstance> processInstances;
		processInstances = CorrelationProcessInstance.findAll();
		assertTrue("Value should be 2, but was " + CorrelationProcessInstance.findAll().size(),
				CorrelationProcessInstance.findAll().size() == 2);

		CorrelationProcessInstance deleteProcessInstance = processInstances.get(0);

		// Add a timerEvent to process instance
		EapEvent timerEvent = new EapEvent(new Date(), new HashMap<String, Serializable>());
		timerEvent.save();
		deleteProcessInstance.setTimerEvent(timerEvent);
		deleteProcessInstance.save();
		assertTrue("Value should be 2, but was " + CorrelationProcessInstance.findAll().size(),
				CorrelationProcessInstance.findAll().size() == 2);

		List<EapEvent> eventsOfDeletedProcessInstance = deleteProcessInstance.getEvents();
		assertTrue(eventsOfDeletedProcessInstance.size() == 1);
		deleteProcessInstance.remove();
		EapEvent eventWithOutProcessInstance = eventsOfDeletedProcessInstance.get(0);
		assertTrue(eventWithOutProcessInstance.getProcessInstances().size() == 0);
		CorrelationProcess process = CorrelationProcess.findByID(processID);
		assertNotNull("There should be a process", process);
		assertTrue("Number of process instances was " + process.getProcessInstances().size() + " but should be 1.",
				process.getProcessInstances().size() == 1);

		processInstances = CorrelationProcessInstance.findAll();
		assertTrue(processInstances.size() == 1);

		assertTrue(processInstances.get(0).getID() != deleteProcessInstance.getID());

		CorrelationProcessInstance.removeAll();
		processInstances = CorrelationProcessInstance.findAll();
		assertTrue(processInstances.size() == 0);
		process = CorrelationProcess.findByID(processID);
		assertNotNull(process);
		assertTrue(process.getProcessInstances().size() == 0);
	}

}
