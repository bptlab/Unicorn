/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.notification.NotificationForEvent;
import de.hpi.unicorn.notification.NotificationMethod;
import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.notification.NotificationRuleForEvent;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

public class NotificationPersistenceTest implements PersistenceTest {

	private EapEventType type1;
	private EapEventType type2;
	private NotificationRuleForEvent rule2;
	private String michaMail = "micha@mail.de";
	private EapUser user1;
	private EapUser user2;
	private NotificationRuleForEvent rule1;
	private EapEvent event1;
	private EapEvent event2;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		storeExampleNotificationRules();
		storeExampleNotifications();
		assertTrue("Value should be 2, but was " + NotificationForEvent.findAll().size(), NotificationForEvent
				.findAll().size() == 2);
		NotificationForEvent.removeAll();
		assertTrue("Value should be 0, but was " + NotificationForEvent.findAll().size(), NotificationForEvent
				.findAll().size() == 0);

	}

	@Test
	@Override
	public void testFind() {
		storeExampleNotificationRules();
		storeExampleNotifications();
		assertTrue(NotificationForEvent.findAll().size() == 2);

		assertTrue(NotificationForEvent.findUnseenForUser(user1).size() == 1);
		NotificationForEvent notification1 = NotificationForEvent.findUnseenEventNotificationForUser(user1).get(0);
		assertTrue(notification1.getUser().getMail().equals(michaMail));
		notification1.setSeen();
		assertTrue(NotificationForEvent.findUnseenForUser(user1).size() == 0);

		assertTrue(NotificationForEvent.findForNotificationRule(rule2).size() == 1);
		assertTrue(((NotificationForEvent) NotificationForEvent.findForNotificationRule(rule2).get(0)).getEvent()
				.getID() == event2.getID());
	}

	@Test
	@Override
	public void testRemove() {
		storeExampleNotificationRules();
		storeExampleNotifications();
		List<NotificationForEvent> notificitations;
		notificitations = NotificationForEvent.findAllEventNotifications();
		assertTrue(notificitations.size() == 2);

		NotificationForEvent deletedNotification = notificitations.get(0);
		deletedNotification.remove();

		notificitations = NotificationForEvent.findAllEventNotifications();
		assertTrue(notificitations.size() == 1);

		assertTrue(notificitations.get(0).getID() != deletedNotification.getID());
	}

	@Test
	public void testRemoveRuleWithExistingNotifications() {
		storeExampleNotificationRules();
		storeExampleNotifications();

		List<NotificationForEvent> notifications = NotificationForEvent.findAllEventNotifications();
		assertTrue(notifications.size() == 2);

		NotificationForEvent deletedNotification = notifications.get(0);
		NotificationRule deletedNotificationRule = deletedNotification.getNotificationRule();
		deletedNotificationRule.remove();

		List<NotificationRuleForEvent> notificationRules = NotificationRuleForEvent.findAllEventNotificationRules();
		assertTrue(notificationRules.size() == 1);
		assertTrue(notificationRules.get(0).getID() != deletedNotificationRule.getID());

		// notification should be deleted as well
		notifications = NotificationForEvent.findAllEventNotifications();
		assertTrue(notifications.size() == 1);
		assertTrue(notifications.get(0).getID() != deletedNotification.getID());
	}

	@Test
	public void testRemoveUserWithExistingNotification() {
		storeExampleNotificationRules();
		storeExampleNotifications();

		List<NotificationForEvent> notifications = NotificationForEvent.findAllEventNotifications();
		assertTrue(notifications.size() == 2);

		NotificationForEvent deletedNotification = notifications.get(0);
		EapUser deletedUser = deletedNotification.getUser();
		deletedUser.remove();

		List<EapUser> users = EapUser.findAll();
		assertTrue(users.size() == 1);
		assertTrue(users.get(0).getID() != deletedUser.getID());

		// notification should be deleted as well
		notifications = NotificationForEvent.findAllEventNotifications();
		assertTrue(notifications.size() == 1);
		assertTrue(notifications.get(0).getID() != deletedNotification.getID());
	}

	@Test
	public void testRemoveEventWithExistingNotification() {
		storeExampleNotificationRules();
		storeExampleNotifications();

		List<NotificationForEvent> notifications = NotificationForEvent.findAllEventNotifications();
		assertTrue(notifications.size() == 2);

		NotificationForEvent deletedNotification = notifications.get(0);
		EapEvent deletedEvent = deletedNotification.getEvent();
		deletedEvent.remove();

		List<EapEvent> events = EapEvent.findAll();
		assertTrue(events.size() == 1);
		assertTrue(events.get(0).getID() != deletedEvent.getID());

		// notification should be deleted as well
		notifications = NotificationForEvent.findAllEventNotifications();
		assertTrue(notifications.size() == 1);
		assertTrue(notifications.get(0).getID() != deletedNotification.getID());

	}

	private void storeExampleNotificationRules() {
		user1 = new EapUser("Micha", "Micha1234", michaMail);
		user1.save();
		type1 = new EapEventType("ToNotify");
		type1.save();
		rule1 = new NotificationRuleForEvent(type1, user1, NotificationMethod.GUI);
		rule1.save();

		user2 = new EapUser("Tsun", "Tsun1234", "tsun@mail.de");
		user2.save();
		type2 = new EapEventType("ToNotify2");
		type2.save();
		rule2 = new NotificationRuleForEvent(type2, user2, NotificationMethod.GUI);
		rule2.save();
	}

	private void storeExampleNotifications() {
		event1 = new EapEvent(type1, new Date());
		event1.save();
		NotificationForEvent notification1 = new NotificationForEvent(event1, user1, rule1);
		notification1.save();

		event2 = new EapEvent(type2, new Date());
		event2.save();
		NotificationForEvent notification2 = new NotificationForEvent(event2, user2, rule2);
		notification2.save();
	}

}
