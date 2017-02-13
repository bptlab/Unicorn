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
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.user.EapUser;

public class NotificationQueryTest {

	private QueryWrapper query1;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void notificationTest() {
		final TypeTreeNode attribute = new TypeTreeNode("TestAttribute", AttributeTypeEnum.STRING);
		final AttributeTypeTree attributes = new AttributeTypeTree(attribute);
		final EapEventType eventType = new EapEventType("TestType", attributes);
		Broker.getInstance().importEventType(eventType);

		final EapUser user = new EapUser("name", "1234", "email");
		user.save();

		this.query1 = new QueryWrapper("NotifyTestType", "Select * from TestType", QueryTypeEnum.LIVE);
		this.query1.save();
		this.query1.addToEsper();

		NotificationRule.removeAll();
		final NotificationRuleForQuery rule = new NotificationRuleForQuery(this.query1, user, NotificationMethod.GUI);
		rule.save();
		Assert.assertTrue(NotificationRule.findAll().size() == 1);

		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		values.put(attribute.getAttributeExpression(), "Wert");
		final EapEvent event = new EapEvent(eventType, new Date(), values);
		Broker.getInstance().importEvent(event);

		final List<NotificationForQuery> listOfNotifications = NotificationForQuery
				.findUnseenQueryNotificationForUser(user);
		Assert.assertTrue(listOfNotifications.size() == 1);
		final NotificationForQuery notification = listOfNotifications.get(0);
		notification.setSeen();
		Assert.assertTrue(Notification.findUnseenForUser(user).size() == 0);

	}

}
