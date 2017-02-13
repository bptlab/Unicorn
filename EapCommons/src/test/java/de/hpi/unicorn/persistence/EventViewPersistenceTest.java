/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.visualisation.EventView;

/**
 * This class tests the saving, finding and removing of {@link EventView}.
 */
public class EventViewPersistenceTest implements PersistenceTest {

	private EapEventType eventType;
	private EventView view;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	private void storeExampleView() {
		eventType = new EapEventType("Tsun");
		eventType.save();
		ArrayList<EapEventType> types = new ArrayList<EapEventType>();
		types.add(eventType);
		view = new EventView(null, types, null);
		;
		view.save();
	}

	@Override
	@Test
	public void testStoreAndRetrieve() {
		storeExampleView();
		assertTrue("Value should be 1, but was " + EventView.findAll().size(), EventView.findAll().size() == 1);
		EventView.removeAll();
		assertTrue("Value should be 0, but was " + EventView.findAll().size(), EventView.findAll().size() == 0);
	}

	@Override
	@Test
	public void testFind() {
		storeExampleView();
		assertTrue(EventView.findByID(view.getID()) == view);
	}

	@Test
	public void testFindByEventType() {
		storeExampleView();
		assertTrue("should have been 1, but was " + EventView.findByEventType(eventType).size(),
				(EventView.findByEventType(eventType)).size() == 1);
		assertTrue(EventView.findByEventType(eventType).toString(), (EventView.findByEventType(eventType)).get(0)
				.getID() == view.getID());
	}

	@Override
	@Test
	public void testRemove() {
		storeExampleView();
		view.remove();
		assertTrue(EventView.findByID(view.getID()) != view);
	}

	@Test
	public void testDeleteEventType() {
		storeExampleView();
		eventType.remove();
		assertTrue(EapEventType.findByID(eventType.getID()) == null
				|| !EapEventType.findByID(eventType.getID()).getTypeName().equals(eventType.getTypeName()));
	}

}
