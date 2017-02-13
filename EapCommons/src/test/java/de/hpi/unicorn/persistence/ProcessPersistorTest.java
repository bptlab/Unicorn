/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.hpi.unicorn.correlation.TimeCondition;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This class tests the saving, finding and removing of
 * {@link CorrelationProcess}.
 * 
 * @author micha
 */
@FixMethodOrder(MethodSorters.JVM)
public class ProcessPersistorTest implements PersistenceTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Override
	@Test
	public void testStoreAndRetrieve() {
		storeExampleProcesses();
		assertTrue("Value should be 2, but was " + CorrelationProcess.findAll().size(), CorrelationProcess.findAll()
				.size() == 2);
		CorrelationProcess.removeAll();
		assertTrue("Value should be 0, but was " + CorrelationProcess.findAll().size(), CorrelationProcess.findAll()
				.size() == 0);
	}

	private void storeExampleProcesses() {
		CorrelationProcess firstProcess = new CorrelationProcess("Kino");
		firstProcess.addEventType(new EapEventType("KinoEvent"));

		CorrelationProcess secondProcess = new CorrelationProcess("GET-Transport");
		secondProcess.addEventType(new EapEventType("GET-Transport"));

		ArrayList<CorrelationProcess> processes = new ArrayList<CorrelationProcess>(Arrays.asList(firstProcess,
				secondProcess));
		assertTrue(CorrelationProcess.save(processes));
	}

	@Override
	@Test
	public void testFind() {
		storeExampleProcesses();
		assertTrue(CorrelationProcess.findAll().size() == 2);
		EapEventType kino = EapEventType.findByTypeName("KinoEvent");
		CorrelationProcess process = CorrelationProcess.findByEventType(kino).get(0);
		assertTrue(process.getEventTypes().size() == 1);
		assertTrue(process.getEventTypes().get(0).getTypeName().equals("KinoEvent"));
		assertTrue(process.getName().equals("Kino"));
		assertTrue(CorrelationProcess.findByName("GET-Transport").size() == 1);
	}

	@Override
	@Test
	public void testRemove() {
		storeExampleProcesses();
		List<CorrelationProcess> processes;
		processes = CorrelationProcess.findAll();
		assertTrue("Value should be 2, but was " + CorrelationProcess.findAll().size(), CorrelationProcess.findAll()
				.size() == 2);

		CorrelationProcess deleteProcess = processes.get(0);
		deleteProcess.remove();

		processes = CorrelationProcess.findAll();
		assertTrue(processes.size() == 1);

		assertTrue(processes.get(0).getID() != deleteProcess.getID());
	}

	@Test
	public void testProcessWithTimeCondition() {
		EapEventType testEventType = new EapEventType("KinoEventType");
		testEventType.save();
		TimeCondition timeCondition = new TimeCondition(testEventType, 1000, true, "Test=KinoTest");
		timeCondition.save();
		CorrelationProcess process = new CorrelationProcess("ProcessWithTimeCondition");
		process.addEventType(testEventType);
		process.setTimeCondition(timeCondition);
		process.save();
		assertTrue("Value should be 1, but was " + CorrelationProcess.findByName("ProcessWithTimeCondition").size(),
				CorrelationProcess.findByName("ProcessWithTimeCondition").size() == 1);
		CorrelationProcess processFromDataBase = CorrelationProcess.findByName("ProcessWithTimeCondition").get(0);
		assertNotNull(processFromDataBase.getTimeCondition());
		TimeCondition timeConditionFromDatabase = processFromDataBase.getTimeCondition();
		assertTrue(timeCondition == timeConditionFromDatabase);

		timeConditionFromDatabase.remove();
		processFromDataBase = CorrelationProcess.findByName("ProcessWithTimeCondition").get(0);
		assertNull(processFromDataBase.getTimeCondition());
	}

}
