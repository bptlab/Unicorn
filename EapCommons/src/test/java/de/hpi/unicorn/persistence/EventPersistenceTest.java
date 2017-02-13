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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class tests the saving, finding and removing of {@link EapEvent}.
 * 
 * @author micha
 */
@FixMethodOrder(MethodSorters.JVM)
public class EventPersistenceTest implements PersistenceTest {

	private HashMap<String, Serializable> michaAttributes;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Override
	@Test
	public void testStoreAndRetrieve() {
		storeExampleEvents();
		assertTrue("Value should be 2, but was " + EapEvent.findAll().size(), EapEvent.findAll().size() == 2);
		EapEvent.removeAll();
		assertTrue("Value should be 0, but was " + EapEvent.findAll().size(), EapEvent.findAll().size() == 0);
	}

	private void storeExampleEvents() {
		Map<String, Serializable> tsunAttributes = new HashMap<String, Serializable>();
		tsunAttributes.put("kuchen", "kaese");
		tsunAttributes.put("kuchen2", "kirsch");
		tsunAttributes.put("kuchen3", "apfel");
		tsunAttributes.put("preis", "34.56");

		EapEventType firstEventType = new EapEventType("Tsun");
		firstEventType.getValueTypeTree().addRoot("kuchen", AttributeTypeEnum.STRING);
		firstEventType.getValueTypeTree().addRoot("kuchen2", AttributeTypeEnum.STRING);
		firstEventType.getValueTypeTree().addRoot("kuchen3", AttributeTypeEnum.STRING);
		firstEventType.getValueTypeTree().addRoot("preis", AttributeTypeEnum.FLOAT);

		firstEventType.save();
		EapEvent event1 = new EapEvent(firstEventType, new Date(), tsunAttributes);

		michaAttributes = new HashMap<String, Serializable>();
		michaAttributes.put("getraenk1", "cola");
		michaAttributes.put("getraenk2", "apfelsaft");
		michaAttributes.put("getraenk3", "fanta");
		Date oldDate = null;
		try {
			oldDate = new SimpleDateFormat("dd/MM/yyyy").parse("18/05/2011");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		EapEventType secondEventType = new EapEventType("Micha");
		secondEventType.getValueTypeTree().addRoot("getraenk1", AttributeTypeEnum.STRING);
		secondEventType.getValueTypeTree().addRoot("getraenk1", AttributeTypeEnum.STRING);
		secondEventType.getValueTypeTree().addRoot("getraenk1", AttributeTypeEnum.STRING);
		secondEventType.save();
		EapEvent event2 = new EapEvent(secondEventType, oldDate, michaAttributes);
		ArrayList<EapEvent> events = new ArrayList<EapEvent>(Arrays.asList(event1, event2));
		EapEvent.save(events);
	}

	@Override
	@Test
	public void testFind() {
		storeExampleEvents();
		assertTrue(EapEvent.findAll().size() == 2);
		EapEventType tsun = EapEventType.findByTypeName("Tsun");
		EapEvent event = EapEvent.findByEventType(tsun).get(0);
		assertTrue(event.getValues().get("kuchen").equals("kaese"));
		assertTrue(event.getValues().get("kuchen2").equals("kirsch"));
		assertTrue(event.getValues().get("preis").equals("34.56"));
		assertTrue(EapEvent.findByValue("getraenk1", "cola").size() == 1);
		List<EapEvent> events = EapEvent.findByValues(michaAttributes);
		assertTrue(events.size() == 1);
		event = events.get(0);
		assertTrue(event.getEventType().getTypeName().equals("Micha"));
	}

	@Test
	public void testFindBetween() {
		storeExampleEvents();
		EapEventType tsun = EapEventType.findByTypeName("Tsun");
		EapEventType micha = EapEventType.findByTypeName("Micha");
		Date oldDate = null;
		try {
			oldDate = new SimpleDateFormat("dd/MM/yyyy").parse("17/05/2011");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(EapEvent.findBetween(oldDate, new Date()).size() == 2);

		assertTrue(EapEvent.findBetween(oldDate, new Date(), tsun).size() == 1);
		try {
			oldDate = new SimpleDateFormat("dd/MM/yyyy").parse("17/05/2012");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(EapEvent.findBetween(oldDate, new Date()).size() == 1);
		assertTrue(EapEvent.findBetween(oldDate, new Date(), tsun).size() == 1);
		assertTrue(EapEvent.findBetween(oldDate, new Date(), micha).size() == 0);
	}

	@Override
	@Test
	public void testRemove() {
		storeExampleEvents();
		List<EapEvent> events;
		events = EapEvent.findAll();
		assertTrue(events.size() == 2);

		EapEvent deleteEvent = events.get(0);
		deleteEvent.remove();

		events = EapEvent.findAll();
		assertTrue(events.size() == 1);

		assertTrue(events.get(0).getID() != deleteEvent.getID());
	}

	@Test
	public void testFindEventWithEventType() {
		AttributeTypeTree eventTypeTree = new AttributeTypeTree();
		TypeTreeNode firstRootAttribute = new TypeTreeNode("vehicle_information");
		new TypeTreeNode(firstRootAttribute, "ETA", AttributeTypeEnum.DATE);
		TypeTreeNode secondRootAttribute = new TypeTreeNode("sender", AttributeTypeEnum.STRING);
		eventTypeTree.addRoot(firstRootAttribute);
		eventTypeTree.addRoot(secondRootAttribute);
		EapEventType testEventType = new EapEventType("Event", eventTypeTree);
		testEventType.setXMLName("EventTaxonomy");
		testEventType.setXMLEvent(true);
		testEventType.setTimestampName("Current timestamp");
		testEventType.save();

		// TransformationTree<String, Serializable> eventValueTree = new
		// TransformationTree<String, Serializable>();
		// eventValueTree.addRootElement("sender", "DHL");
		// eventValueTree.addChild("vehicle_information", "ETA",
		// "24.12.2013 20:25");
		// EapEvent testEvent = new EapEvent(testEventType, new Date(),
		// eventValueTree);
		Map<String, Serializable> eventValues = new HashMap<String, Serializable>();
		eventValues.put("sender", "DHL");
		eventValues.put("vehicle_information.ETA", "24.12.2013 20:25");
		EapEvent testEvent = new EapEvent(testEventType, new Date(), eventValues);
		testEvent.save();

		for (EapEvent eventFromDatabase : EapEvent.findAll()) {
			// System.out.println(eventFromDatabase);
		}

	}

	@Test
	public void testGetNumberOfEvents() {
		storeExampleEvents();
		EapEventType tsun = EapEventType.findByTypeName("Tsun");
		assertTrue(EapEvent.getNumberOfEventsByEventType(tsun) == 1);
	}

	@Test
	public void testGetDistinctValuesOfAttributes() {
		storeExampleEvents();
		EapEventType tsun = EapEventType.findByTypeName("Tsun");

		Map<String, Serializable> hm2 = new HashMap<String, Serializable>();
		hm2.put("kuchen", "kaese");
		hm2.put("kuchen2", "kirschkirsch");
		hm2.put("kuchen3", "apfel");
		EapEvent event2 = new EapEvent(tsun, new Date(), hm2);
		event2.save();

		List<String> values = EapEvent.findDistinctValuesOfAttributeOfType("kuchen", tsun);
		assertTrue(values.contains("kaese"));
		assertTrue(values.size() == 1);

		List<String> values2 = EapEvent.findDistinctValuesOfAttributeOfType("kuchen2", tsun);
		assertTrue(values2.contains("kirschkirsch"));
		assertTrue(values2.size() == 2);

		long appearancesOfKaese = EapEvent.findNumberOfAppearancesByAttributeValue("kuchen", "kaese", tsun);
		long appearancesOfKirschkirsch = EapEvent.findNumberOfAppearancesByAttributeValue("kuchen2", "kirschkirsch",
				tsun);
		assertTrue("should be 2, but was " + appearancesOfKaese, appearancesOfKaese == 2);
		assertTrue("should be 1, but was " + appearancesOfKirschkirsch, appearancesOfKirschkirsch == 1);
	}
}
