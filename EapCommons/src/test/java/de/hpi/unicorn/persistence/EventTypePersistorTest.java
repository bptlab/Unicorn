/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.utils.TestHelper;

/**
 * This class tests the saving, finding and removing of {@link EapEventType}.
 * 
 * @author micha
 */
@FixMethodOrder(MethodSorters.JVM)
public class EventTypePersistorTest implements PersistenceTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Override
	@Test
	public void testStoreAndRetrieve() {
		storeExampleEventType();
		assertTrue("Value should be 2, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 2);
		EapEventType.removeAll();
		assertTrue("Value should be 0, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 0);
	}

	private void storeExampleEventType() {
		List<EapEventType> eventTypes = TestHelper.createEventTypes();
		EapEventType firstEventType = eventTypes.get(0);
		EapEventType secondEventType = eventTypes.get(1);

		Set<EapEventType> eventTypes1 = new HashSet<EapEventType>();
		eventTypes1.add(firstEventType);

		Set<EapEventType> eventTypes2 = new HashSet<EapEventType>();
		eventTypes1.add(secondEventType);

		assertTrue(EapEventType.save(eventTypes));

		CorrelationProcess process1 = new CorrelationProcess("Process1", eventTypes1);
		process1.save();

		CorrelationProcess process2 = new CorrelationProcess("Process2", eventTypes2);
		process2.save();

	}

	private void storeExampleEvents() {
		EapEventType eventType = EapEventType.findByTypeName("Kino");

		Map<String, Serializable> hm = new HashMap<String, Serializable>();
		hm.put("Location", 1);
		hm.put("Movie", "Event");

		EapEvent event = new EapEvent(eventType, new Date(), hm);
		event.save();
	}

	@Override
	@Test
	public void testFind() {
		storeExampleEventType();
		assertTrue(EapEventType.findAll().size() == 2);
		EapEventType eventType = EapEventType.findByAttribute("TypeName", "Kino").get(0);
		assertTrue(eventType.getTypeName().equals("Kino"));
		// assertTrue(eventType.getValueTypes().get("Location") == false);
		// assertTrue(eventType.getValueTypes().get("SecondaryEvent") == true);
	}

	@Override
	@Test
	public void testRemove() {
		storeExampleEventType();
		List<EapEventType> eventTypes;
		eventTypes = EapEventType.findAll();
		assertTrue("Value should be 2, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 2);

		EapEventType deleteEventType = eventTypes.get(0);
		deleteEventType.remove();

		eventTypes = EapEventType.findAll();
		assertTrue(eventTypes.size() == 1);

		assertTrue(eventTypes.get(0).getID() != deleteEventType.getID());
	}

	@Test
	public void testRemoveEventTypeWithEvents() {
		storeExampleEventType();
		storeExampleEvents();
		List<EapEventType> eventTypes;
		List<EapEvent> events;
		eventTypes = EapEventType.findAll();
		assertTrue("Value should be 2, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 2);

		EapEventType deleteEventType = EapEventType.findByTypeName("Kino");

		events = EapEvent.findByEventType(deleteEventType);
		assertTrue("Value should be 1, but was " + events.size(), events.size() == 1);

		// assertTrue("should contain 1 event, but contains " +
		// eventTypes.get(0).getChilds().size(),
		// eventTypes.get(0).getChilds().size() == 1);

		deleteEventType.remove();

		events = EapEvent.findByEventType(deleteEventType);
		assertTrue("Value should be 0, but was " + events.size(), events.size() == 0);

		eventTypes = EapEventType.findAll();
		assertTrue(eventTypes.size() == 1);

		assertTrue(eventTypes.get(0).getID() != deleteEventType.getID());
	}
}
