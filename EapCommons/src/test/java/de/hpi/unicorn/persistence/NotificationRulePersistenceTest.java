/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.notification.NotificationMethod;
import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.notification.NotificationRuleForEvent;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

/**
 * This class tests the saving, finding and removing of {@link NotificationRule}
 * .
 */
public class NotificationRulePersistenceTest implements PersistenceTest {

	private EapEventType type1;
	private EapUser user1;
	private EapUser user2;
	private EapEventType type2;
	private String michaMail = "micha@mail.de";

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		storeExampleNotificationRules();
		assertTrue("Value should be 2, but was " + NotificationRuleForEvent.findAll().size(), NotificationRuleForEvent
				.findAll().size() == 2);
		NotificationRuleForEvent.removeAll();
		assertTrue("Value should be 0, but was " + NotificationRuleForEvent.findAll().size(), NotificationRuleForEvent
				.findAll().size() == 0);
	}

	@Test
	@Override
	public void testFind() {
		storeExampleNotificationRules();
		assertTrue(NotificationRuleForEvent.findAll().size() == 2);

		assertTrue(NotificationRuleForEvent.findByEventType(type1).size() == 1);
		assertTrue(NotificationRuleForEvent.findByEventType(type1).get(0).getUser().getMail().equals(michaMail));

		assertTrue(NotificationRuleForEvent.findByUser(user1).size() == 1);
		assertTrue(NotificationRuleForEvent.findByUser(user1).get(0).getTriggeringEntity() instanceof EapEventType);
		assertTrue(NotificationRuleForEvent.findByUser(user1).get(0).getTriggeringEntity().getID() == type1.getID());

	}

	@Test
	@Override
	public void testRemove() {
		storeExampleNotificationRules();
		List<NotificationRuleForEvent> notificitations;
		notificitations = NotificationRuleForEvent.findAllEventNotificationRules();
		assertTrue(notificitations.size() == 2);

		NotificationRuleForEvent deletedNotification = notificitations.get(0);
		deletedNotification.remove();

		notificitations = NotificationRuleForEvent.findAllEventNotificationRules();
		assertTrue(notificitations.size() == 1);

		assertTrue(notificitations.get(0).getID() != deletedNotification.getID());
	}

	@Test
	public void testRemoveEventTypeWithNotificationRule() {
		storeExampleNotificationRules();
		List<NotificationRuleForEvent> notificitations = NotificationRuleForEvent.findAllEventNotificationRules();
		assertTrue(notificitations.size() == 2);

		NotificationRuleForEvent deletedNotification = notificitations.get(0);
		EapUser user = deletedNotification.getUser();
		user.remove();

		List<EapUser> users = EapUser.findAll();
		assertTrue(users.size() == 1);
		assertTrue(users.get(0).getID() != user.getID());

		// notification was deleted as well
		notificitations = NotificationRuleForEvent.findAllEventNotificationRules();
		assertTrue(notificitations.size() == 1);
		assertTrue(notificitations.get(0).getID() != deletedNotification.getID());
	}

	private void storeExampleNotificationRules() {
		user1 = new EapUser("Micha", "Micha1234", michaMail);
		user1.save();
		type1 = new EapEventType("ToNotify");
		type1.save();
		NotificationRuleForEvent notification1 = new NotificationRuleForEvent(type1, user1, NotificationMethod.GUI);
		notification1.save();

		user2 = new EapUser("Tsun", "Tsun1234", "tsun@mail.de");
		user2.save();
		type2 = new EapEventType("ToNotify2");
		type2.save();
		NotificationRuleForEvent notification2 = new NotificationRuleForEvent(type2, user2, NotificationMethod.GUI);
		notification2.save();

	}

}
