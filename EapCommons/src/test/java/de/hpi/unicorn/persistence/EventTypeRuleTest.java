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
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EventTypeRule;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.notification.EventCondition;
import de.hpi.unicorn.persistence.Persistor;

public class EventTypeRuleTest implements PersistenceTest {

	private EapEventType eventType;
	private EapEventType createdEventType;
	private EventTypeRule eventTypeRule;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		eventTypeRule = createEventTypeRule();
		eventTypeRule.save();
		assertNotNull(EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType));
		EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType).equals(eventTypeRule);
	}

	@Test
	@Override
	public void testFind() {
		eventTypeRule = createEventTypeRule();
		eventTypeRule.save();
		assertNotNull(EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType));
		EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType).equals(eventTypeRule);
	}

	@Test
	@Override
	public void testRemove() {
		eventTypeRule = createEventTypeRule();
		eventTypeRule.save();
		assertNotNull(EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType));
		eventTypeRule.remove();
		assertNull(EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType));
	}

	@Test
	public void testRemoveOfContainedEventType() {
		eventTypeRule = createEventTypeRule();
		eventTypeRule.save();
		assertNotNull(EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType));
		eventType.remove();
		assertNull(EapEventType.findByID(eventType.getID()));
	}

	private EventTypeRule createEventTypeRule() {
		TypeTreeNode rootAttribute1 = new TypeTreeNode("Timestamp", AttributeTypeEnum.DATE);
		TypeTreeNode rootAttribute2 = new TypeTreeNode("Location", AttributeTypeEnum.INTEGER);
		List<TypeTreeNode> attributes1 = Arrays.asList(rootAttribute1, rootAttribute2);
		eventType = new EapEventType("testEventType1", attributes1);
		eventType.save();
		TypeTreeNode rootAttribute3 = new TypeTreeNode("Timestamp", AttributeTypeEnum.DATE);
		TypeTreeNode rootAttribute4 = new TypeTreeNode("Location", AttributeTypeEnum.INTEGER);
		List<TypeTreeNode> attributes2 = Arrays.asList(rootAttribute3, rootAttribute4);
		createdEventType = new EapEventType("testEventType2", attributes2);
		createdEventType.save();
		eventTypeRule = new EventTypeRule(new ArrayList<EapEventType>(Arrays.asList(eventType)), new EventCondition(),
				createdEventType);
		return eventTypeRule;
	}

	private void storeExampleEventType() {
		AttributeTypeTree values = new AttributeTypeTree();
		values.addRoot(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER));
		values.addRoot(new TypeTreeNode("SecondaryEvent", AttributeTypeEnum.STRING));
		EapEventType firstEventType = new EapEventType("Kino", values);

		EapEventType secondEventType = new EapEventType("GET-Transport");

		ArrayList<EapEventType> eventTypes = new ArrayList<EapEventType>(Arrays.asList(firstEventType, secondEventType));
		assertTrue(EapEventType.save(eventTypes));
	}

	@Test
	public void testRemoveEventTypeWithEventTypeRuleForCreation() {
		storeExampleEventType();
		ArrayList<EapEventType> usedEventTypes = new ArrayList<EapEventType>();
		usedEventTypes.addAll(EapEventType.findAll());
		EapEventType createdEventType = EapEventType.findByTypeName("Kino");
		EventTypeRule rule = new EventTypeRule(usedEventTypes, new EventCondition(), createdEventType);
		rule.save();

		assertTrue("Value should be 2, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 2);
		assertTrue("rule not saved", EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType) != null);
		EapEventType deleteEventType = EapEventType.findByTypeName("Kino");
		deleteEventType.remove();
		assertTrue(
				"should not find eventtyperule, but found "
						+ EventTypeRule.findEventTypeRuleForCreatedEventType(deleteEventType),
				EventTypeRule.findEventTypeRuleForCreatedEventType(deleteEventType) == null);

		List<EapEventType> eventTypes;
		eventTypes = EapEventType.findAll();
		assertTrue(eventTypes.size() == 1);

		assertTrue(eventTypes.get(0).getID() != deleteEventType.getID());
	}

	@Test
	public void testRemoveEventTypeWithEventTypeRuleAsSource() {
		storeExampleEventType();
		ArrayList<EapEventType> usedEventTypes = new ArrayList<EapEventType>();
		usedEventTypes.addAll(EapEventType.findAll());
		EapEventType createdEventType = EapEventType.findByTypeName("Kino");
		EventTypeRule rule = new EventTypeRule(usedEventTypes, new EventCondition(), createdEventType);
		rule.save();
		List<EapEventType> eventTypes;
		eventTypes = EapEventType.findAll();
		// System.out.println(eventTypes);
		assertTrue("Value should be 2, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 2);
		EapEventType deleteEventType = EapEventType.findByTypeName("GET-Transport");
		deleteEventType.remove();

		assertTrue("eventtyperule was deleted, but should not have been ",
				EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType) != null);

		eventTypes = EapEventType.findAll();
		assertTrue("there were " + eventTypes.size() + "eventtypes instead of 1", eventTypes.size() == 1);

		assertTrue(eventTypes.get(0).getID() != deleteEventType.getID());
	}

	@Test
	public void testRemoveEventTypeWithEventTypeRuleAsOnlySource() {
		assertTrue("Value should be 0, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 0);
		storeExampleEventType();
		ArrayList<EapEventType> usedEventTypes = new ArrayList<EapEventType>();
		usedEventTypes.add(EapEventType.findByTypeName("GET-Transport"));
		EapEventType createdEventType = EapEventType.findByTypeName("Kino");
		EventTypeRule rule = new EventTypeRule(usedEventTypes, new EventCondition(), createdEventType);
		rule.save();
		List<EapEventType> eventTypes;
		eventTypes = EapEventType.findAll();
		assertTrue("Value should be 2, but was " + EapEventType.findAll().size(), EapEventType.findAll().size() == 2);
		EapEventType deleteEventType = EapEventType.findByTypeName("GET-Transport");
		deleteEventType.remove();

		assertTrue("eventtyperule was not deleted ",
				EventTypeRule.findEventTypeRuleForCreatedEventType(createdEventType) == null);

		eventTypes = EapEventType.findAll();
		assertTrue(eventTypes.size() == 1);

		assertTrue(eventTypes.get(0).getID() != deleteEventType.getID());
	}

}
