/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.eventhandling.NotificationObservable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

public class NotificationEventTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void notificationTest() {
		NotificationObservable.getInstance().clearInstance();
		final TypeTreeNode attribute = new TypeTreeNode("TestAttribute", AttributeTypeEnum.STRING);
		final AttributeTypeTree attributes = new AttributeTypeTree(attribute);
		final EapEventType eventType = new EapEventType("TestType", attributes);
		Broker.getInstance().importEventType(eventType);

		final EapUser user = new EapUser("name", "1234", "email");
		user.save();

		NotificationRule.removeAll();
		final NotificationRuleForEvent rule = new NotificationRuleForEvent(eventType, user, NotificationMethod.GUI);
		Broker.getInstance().send(rule);
		Assert.assertTrue(NotificationRule.findAll().size() == 1);

		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		values.put(attribute.getAttributeExpression(), "Wert");
		final EapEvent event = new EapEvent(eventType, new Date(), values);
		Broker.getInstance().importEvent(event);

		final List<NotificationForEvent> listOfNotifications = NotificationForEvent
				.findUnseenEventNotificationForUser(user);
		Assert.assertTrue(listOfNotifications.size() == 1);
		final NotificationForEvent notification = listOfNotifications.get(0);
		notification.setSeen();
		Assert.assertTrue(Notification.findUnseenForUser(user).size() == 0);

	}

	@Test
	public void notificationWithConditionTestInteger() {
		NotificationObservable.getInstance().clearInstance();
		final TypeTreeNode attribute = new TypeTreeNode("TestAttribute", AttributeTypeEnum.INTEGER);
		final AttributeTypeTree attributes = new AttributeTypeTree(attribute);
		final EapEventType eventType = new EapEventType("TestType1", attributes);
		Broker.getInstance().importEventType(eventType);

		final EapUser user = new EapUser("name", "1234", "email");
		user.save();

		final NotificationRuleForEvent rule = new NotificationRuleForEvent(eventType, new EventCondition(
				"TestAttribute", "1"), user, NotificationMethod.GUI);
		Broker.getInstance().send(rule);

		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		values.put(attribute.getAttributeExpression(), 1);
		final EapEvent event = new EapEvent(eventType, new Date(), values);
		Broker.getInstance().importEvent(event);

		Assert.assertTrue("should be 1, but was "
				+ NotificationForEvent.findUnseenEventNotificationForUser(user).size(), NotificationForEvent
				.findUnseenEventNotificationForUser(user).size() == 1);

	}

	@Test
	public void notificationWithConditionTest() {
		NotificationObservable.getInstance().clearInstance();
		final TypeTreeNode attribute = new TypeTreeNode("TestAttribute", AttributeTypeEnum.STRING);
		final AttributeTypeTree attributes = new AttributeTypeTree(attribute);
		final EapEventType eventType = new EapEventType("TestType2", attributes);
		Broker.getInstance().importEventType(eventType);

		final EapUser user = new EapUser("name", "1234", "email");
		user.save();

		final NotificationRuleForEvent rule = new NotificationRuleForEvent(eventType, new EventCondition(
				"TestAttribute", "Wert"), user, NotificationMethod.GUI);
		Broker.getInstance().send(rule);

		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		values.put(attribute.getAttributeExpression(), "Wert");
		final EapEvent event = new EapEvent(eventType, new Date(), values);
		Broker.getInstance().importEvent(event);

		Assert.assertTrue(Notification.findUnseenForUser(user).size() == 1);

	}

}
