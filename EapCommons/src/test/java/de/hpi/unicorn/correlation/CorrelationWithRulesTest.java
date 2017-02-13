/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.correlation.Correlator;
import de.hpi.unicorn.correlation.RuleCorrelator;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.utils.TestHelper;

public class CorrelationWithRulesTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void testCorrelationWithOneRule() {
		assertNumberOfDataSets(0, 0, 0, 0);

		List<EapEventType> correlationEventTypes = new ArrayList<EapEventType>();

		TypeTreeNode kinoRatingAttribute1 = new TypeTreeNode("Location", AttributeTypeEnum.INTEGER);
		TypeTreeNode kinoRatingAttribute2 = new TypeTreeNode("Rating", AttributeTypeEnum.STRING);
		List<TypeTreeNode> kinoRatingAttributes = Arrays.asList(kinoRatingAttribute1, kinoRatingAttribute2);
		EapEventType kinoRatingEventType = new EapEventType("KinoRating", kinoRatingAttributes);
		correlationEventTypes.add(kinoRatingEventType.save());

		TypeTreeNode kinoFilmeAttribute1 = new TypeTreeNode("Location", AttributeTypeEnum.INTEGER);
		TypeTreeNode kinoFilmeAttribute2 = new TypeTreeNode("Movie", AttributeTypeEnum.STRING);
		TypeTreeNode kinoFilmeAttribute3 = new TypeTreeNode("Action", AttributeTypeEnum.STRING);
		List<TypeTreeNode> kinoFilmeAttributes = Arrays.asList(kinoFilmeAttribute1, kinoFilmeAttribute2,
				kinoFilmeAttribute3);
		EapEventType kinoFilmeEventType = new EapEventType("KinoFilme", kinoFilmeAttributes);
		correlationEventTypes.add(kinoFilmeEventType.save());

		assertNumberOfDataSets(0, 2, 0, 0);

		for (EapEventType eventType : correlationEventTypes) {
			List<EapEvent> events = TestHelper.createEvents(eventType);
			for (EapEvent event : events) {
				event.save();
			}
		}

		assertNumberOfDataSets(6, 2, 0, 0);

		CorrelationProcess process = new CorrelationProcess("Kino");
		process.save();
		assertNumberOfDataSets(6, 2, 1, 0);

		RuleCorrelator.correlate(
				new HashSet<CorrelationRule>(Arrays.asList(new CorrelationRule(kinoRatingAttribute1,
						kinoFilmeAttribute1))), process, null);

		// Prüfen, gleiche Anzahl Events, EventTypen, Prozesse und richtige
		// Anzahl Prozessinstanzen
		assertNumberOfDataSets(6, 2, 1, 3);
	}

	@Test
	public void testCorrelationWithMultipleRules() {
		assertNumberOfDataSets(0, 0, 0, 0);

		List<EapEventType> correlationEventTypes = new ArrayList<EapEventType>();

		TypeTreeNode firstEventAttribute1 = new TypeTreeNode("FirstEventAttributeOne", AttributeTypeEnum.INTEGER);
		TypeTreeNode firstEventAttribute2 = new TypeTreeNode("FirstEventAttributeTwo", AttributeTypeEnum.STRING);
		List<TypeTreeNode> firstEventAttributes = Arrays.asList(firstEventAttribute1, firstEventAttribute2);
		EapEventType firstEventType = (new EapEventType("FirstEvent", firstEventAttributes)).save();
		correlationEventTypes.add(firstEventType);

		TypeTreeNode secondEventAttribute1 = new TypeTreeNode("SecondEventAttributeOne", AttributeTypeEnum.STRING);
		TypeTreeNode secondEventAttribute2 = new TypeTreeNode("SecondEventAttributeTwo", AttributeTypeEnum.DATE);
		TypeTreeNode secondEventAttribute3 = new TypeTreeNode("SecondEventAttributeThree");
		new TypeTreeNode(secondEventAttribute3, "SecondEventAttributeThreeOne", AttributeTypeEnum.INTEGER);
		TypeTreeNode secondEventAttribute4 = new TypeTreeNode("SecondEventAttributeFour", AttributeTypeEnum.INTEGER);
		AttributeTypeTree secondEventAttributeTree = new AttributeTypeTree(Arrays.asList(secondEventAttribute1,
				secondEventAttribute2, secondEventAttribute3, secondEventAttribute4));
		EapEventType secondEventType = (new EapEventType("SecondEvent", secondEventAttributeTree)).save();
		correlationEventTypes.add(secondEventType);

		TypeTreeNode thirdEventAttribute1 = new TypeTreeNode("ThirdEventAttributeOne", AttributeTypeEnum.INTEGER);
		List<TypeTreeNode> thirdEventAttributes = Arrays.asList(thirdEventAttribute1);
		EapEventType thirdEventType = (new EapEventType("ThirdEvent", thirdEventAttributes)).save();
		correlationEventTypes.add(thirdEventType);

		assertNumberOfDataSets(0, 3, 0, 0);

		Set<CorrelationRule> correlationRules = new HashSet<CorrelationRule>();
		correlationRules.add(new CorrelationRule(firstEventType.getValueTypeTree().getAttributeByExpression(
				"FirstEventAttributeTwo"), secondEventType.getValueTypeTree().getAttributeByExpression(
				"SecondEventAttributeOne")));
		correlationRules.add(new CorrelationRule(secondEventType.getValueTypeTree().getAttributeByExpression(
				"SecondEventAttributeThree.SecondEventAttributeThreeOne"), thirdEventType.getValueTypeTree()
				.getAttributeByExpression("ThirdEventAttributeOne")));

		CorrelationProcess process = new CorrelationProcess("SomeProcess");
		process.save();

		assertNumberOfDataSets(0, 3, 1, 0);

		RuleCorrelator.correlate(correlationRules, process, null);

		assertNumberOfDataSets(0, 3, 1, 0);

		for (EapEventType eventType : correlationEventTypes) {
			List<EapEvent> eventsToCorrelate = createEventsForCorrelation(eventType);
			Correlator.correlate(EapEvent.save(eventsToCorrelate));
		}

		assertNumberOfDataSets(9, 3, 1, 3);
	}

	@Test(expected = RuntimeException.class)
	public void testCorrelationRuleWithAttributesOfDifferentTypes() {
		TypeTreeNode attribute1 = new TypeTreeNode("One", AttributeTypeEnum.INTEGER);
		TypeTreeNode attribute2 = new TypeTreeNode("Two", AttributeTypeEnum.STRING);
		new CorrelationRule(attribute1, attribute2);
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

	private List<EapEvent> createEventsForCorrelation(EapEventType eventType) {
		List<EapEvent> events = new ArrayList<EapEvent>();
		for (int i = 1; i < 4; i++) {
			Map<String, Serializable> values = new HashMap<String, Serializable>();
			for (TypeTreeNode valueType : eventType.getValueTypes()) {
				String attributeName = valueType.getAttributeExpression();
				if (valueType.getType() == AttributeTypeEnum.STRING) {
					// values: A, B, C ...
					values.put(attributeName, String.valueOf((char) (64 + i)));
				} else if (valueType.getType() == AttributeTypeEnum.INTEGER) {
					values.put(attributeName, i);
				} else if (valueType.getType() == AttributeTypeEnum.DATE) {
					values.put(attributeName, new Date());
				}
			}
			EapEvent event = new EapEvent(eventType, new Date(), values);
			events.add(event);
		}
		return events;
	}
}
