/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This class collects some often used methods for testing purposes.
 * 
 * @author micha
 */
public class TestHelper {

	/**
	 * Creates attributes from the given attribute names which are the column
	 * titles of the excel files.
	 * 
	 * @return list of Attributes
	 */
	public static List<TypeTreeNode> createAttributes(final List<String> attributeNames) {
		final List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
		for (final String attributeName : attributeNames) {
			if (attributeName.startsWith("Time")) {
				attributes.add(new TypeTreeNode(attributeName, AttributeTypeEnum.DATE));
			} else if (attributeName.equals("Location") || attributeName.equals("Duration")) {
				attributes.add(new TypeTreeNode(attributeName, AttributeTypeEnum.INTEGER));
			} else {
				attributes.add(new TypeTreeNode(attributeName, AttributeTypeEnum.STRING));
			}
		}
		return attributes;
	}

	/**
	 * Creates two event types for testing. The event types have to be saved in
	 * the test at first!
	 * 
	 * @return list of two EapEventTypes
	 */
	public static List<EapEventType> createEventTypes() {
		final List<EapEventType> eventTypes = new ArrayList<EapEventType>();
		final AttributeTypeTree values = new AttributeTypeTree();
		values.addRoot(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER));
		values.addRoot(new TypeTreeNode("Movie", AttributeTypeEnum.STRING));

		eventTypes.add(new EapEventType("Kino", values));
		eventTypes.add(new EapEventType("GET-Transport"));
		return eventTypes;
	}

	/**
	 * Creates three events for the given EventType. The events have to be saved
	 * in the test at first!
	 * 
	 * @return list of three EapEvent
	 */
	public static List<EapEvent> createEvents(final EapEventType eventType) {
		final List<EapEvent> events = new ArrayList<EapEvent>();
		for (int i = 1; i < 4; i++) {
			final Map<String, Serializable> values = new HashMap<String, Serializable>();
			for (final TypeTreeNode valueType : eventType.getValueTypes()) {
				final String attributeName = valueType.getAttributeExpression();
				if (valueType.getType() == AttributeTypeEnum.STRING) {
					values.put(attributeName, attributeName + i);
				} else if (valueType.getType() == AttributeTypeEnum.INTEGER) {
					values.put(attributeName, valueType.getName().hashCode() + i);
				} else if (valueType.getType() == AttributeTypeEnum.DATE) {
					values.put(attributeName, new Date());
				}
			}
			final EapEvent event = new EapEvent(eventType, new Date(), values);
			events.add(event);
		}
		return events;
	}

	public static List<EapEvent> createDummyEvents(final EapEventType eventType, final int count) {
		final List<EapEvent> events = new ArrayList<EapEvent>();
		for (int i = 1; i <= count; i++) {
			final Map<String, Serializable> values = new HashMap<String, Serializable>();
			values.put("Location", 1);
			values.put("Movie", "Movie Name");
			new EapEvent(eventType, new Date(), values);
		}
		return events;
	}

	/**
	 * Creates a new process. The process has to be saved in the test at first!
	 * 
	 * @return a CorrelationProcess
	 */
	public static CorrelationProcess createProcess(final Set<EapEventType> eventTypes) {
		final CorrelationProcess process = new CorrelationProcess("Kino", eventTypes);
		return process;
	}

}
