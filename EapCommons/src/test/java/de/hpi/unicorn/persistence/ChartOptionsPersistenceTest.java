/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.visualisation.ChartConfiguration;
import de.hpi.unicorn.visualisation.ChartTypeEnum;

/**
 * This class tests the saving, finding and removing of
 * {@link ChartConfiguration}.
 */
public class ChartOptionsPersistenceTest implements PersistenceTest {

	private EapEventType eventType;
	private ChartConfiguration options;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	private void storeExampleOptions() {
		eventType = new EapEventType("Tsun");
		eventType.save();
		options = new ChartConfiguration(eventType, "attribute", AttributeTypeEnum.STRING, "chartTitle",
				ChartTypeEnum.COLUMN, 1);
		options.save();
	}

	@Override
	@Test
	public void testStoreAndRetrieve() {
		storeExampleOptions();
		assertTrue("Value should be 1, but was " + ChartConfiguration.findAll().size(), ChartConfiguration.findAll()
				.size() == 1);
		ChartConfiguration.removeAll();
		assertTrue("Value should be 0, but was " + ChartConfiguration.findAll().size(), ChartConfiguration.findAll()
				.size() == 0);
	}

	@Override
	@Test
	public void testFind() {
		storeExampleOptions();
		assertTrue(ChartConfiguration.findByID(options.getID()) == options);

	}

	@Override
	@Test
	public void testRemove() {
		storeExampleOptions();
		options.remove();
		assertTrue(ChartConfiguration.findByID(options.getID()) != options);

	}

	@Test
	public void testDeleteEventType() {
		storeExampleOptions();
		eventType.remove();
		assertTrue(EapEventType.findByID(eventType.getID()) == null
				|| !EapEventType.findByID(eventType.getID()).getTypeName().equals(eventType.getTypeName()));
	}

}
