/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.correlation.AttributeCorrelator;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.utils.TestHelper;

/**
 * @author micha
 * 
 */
public class CorrelationWithAttributesTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void testCorrelator() {
		assertNumberOfDataSets(0, 0, 0, 0);

		// EventTyp anlegen
		List<EapEventType> eventTypes = TestHelper.createEventTypes();
		EapEventType kinoEventType = null;
		for (EapEventType eventType : eventTypes) {
			eventType.save();
			if (eventType.getTypeName().equals("Kino")) {
				kinoEventType = eventType;
			}
		}
		assertNotNull(kinoEventType);
		assertNumberOfDataSets(0, 2, 0, 0);

		// Events reinladen
		List<EapEvent> events = TestHelper.createEvents(kinoEventType);
		for (EapEvent event : events) {
			event.save();
		}

		Set<EapEventType> correlationEventTypes = new HashSet<EapEventType>();
		correlationEventTypes.add(kinoEventType);

		assertNumberOfDataSets(3, 2, 0, 0);

		// Process anlegen
		CorrelationProcess process = TestHelper.createProcess(new HashSet<EapEventType>(Arrays.asList(kinoEventType)));
		process.save();
		assertNumberOfDataSets(3, 2, 1, 0);

		// Korrelieren
		TypeTreeNode correlationAttribute = new TypeTreeNode("Location", AttributeTypeEnum.INTEGER);
		AttributeCorrelator.correlate(correlationEventTypes, Arrays.asList(correlationAttribute), process, null);

		// Prüfen, gleiche Anzahl Events, EventTypen, Prozesse und richtige
		// Anzahl Prozessinstanzen
		assertNumberOfDataSets(3, 2, 1, 3);
	}

	@Test
	public void testCorrelatorWithOutEvents() {
		assertNumberOfDataSets(0, 0, 0, 0);

		// EventTyp anlegen
		List<EapEventType> eventTypes = TestHelper.createEventTypes();
		EapEventType kinoEventType = null;
		for (EapEventType eventType : eventTypes) {
			eventType.save();
			if (eventType.getTypeName().equals("Kino")) {
				kinoEventType = eventType;
			}
		}
		assertNotNull(kinoEventType);
		assertNumberOfDataSets(0, 2, 0, 0);

		Set<EapEventType> correlationEventTypes = new HashSet<EapEventType>();
		correlationEventTypes.add(kinoEventType);

		// Process anlegen
		CorrelationProcess process = TestHelper.createProcess(new HashSet<EapEventType>(Arrays.asList(kinoEventType)));
		process.save();
		assertNumberOfDataSets(0, 2, 1, 0);

		assertTrue(CorrelationProcess.findByName(process.getName()).size() == 1);
		CorrelationProcess processFromDataBase = CorrelationProcess.findByName(process.getName()).get(0);

		processFromDataBase.save();
		processFromDataBase.save();
		assertNumberOfDataSets(0, 2, 1, 0);

		// Korrelieren
		TypeTreeNode correlationAttribute = new TypeTreeNode("Location", AttributeTypeEnum.INTEGER);
		AttributeCorrelator.correlate(correlationEventTypes, Arrays.asList(correlationAttribute), processFromDataBase,
				null);

		// Prüfen, gleiche Anzahl Events, EventTypen, Prozesse und richtige
		// Anzahl Prozessinstanzen
		assertNumberOfDataSets(0, 2, 1, 0);
	}

	private void assertNumberOfDataSets(int events, int eventTypes, int processes, int processInstances) {
		assertTrue("Number of events must be " + events + " but was " + EapEvent.findAll().size(), EapEvent.findAll()
				.size() == events);
		assertTrue("Number of event types must be " + eventTypes + " but was " + EapEventType.findAll().size(),
				EapEventType.findAll().size() == eventTypes);
		assertTrue("Number of processes must be " + processes + " but was " + CorrelationProcess.findAll().size(),
				CorrelationProcess.findAll().size() == processes);
		assertTrue("Number of process instances must be " + processInstances + " but was "
				+ CorrelationProcessInstance.findAll().size(),
				CorrelationProcessInstance.findAll().size() == processInstances);
	}

}
