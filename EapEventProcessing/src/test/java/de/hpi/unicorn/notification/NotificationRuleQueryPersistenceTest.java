/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.user.EapUser;

public class NotificationRuleQueryPersistenceTest {

	private EapEventType type1;
	private EapUser user1;
	private EapUser user2;
	private EapEventType type2;
	private final String michaMail = "micha@mail.de";
	private QueryWrapper query1;
	private QueryWrapper query2;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void testStoreAndRetrieve() {
		this.storeExampleNotificationRules();
		Assert.assertTrue("Value should be 2, but was " + NotificationRule.findAll().size(), NotificationRule.findAll()
				.size() == 2);
		NotificationRule.removeAll();
		Assert.assertTrue("Value should be 0, but was " + NotificationRule.findAll().size(), NotificationRule.findAll()
				.size() == 0);
	}

	@Test
	public void testFind() {
		this.storeExampleNotificationRules();
		Assert.assertTrue(NotificationRule.findAll().size() == 2);

		Assert.assertTrue(NotificationRuleForQuery.findByQuery(this.query1).size() == 1);
		Assert.assertTrue(NotificationRuleForQuery.findByQuery(this.query1).get(0).getUser().getMail()
				.equals(this.michaMail));

		Assert.assertTrue(NotificationRule.findByUser(this.user1).size() == 1);
		Assert.assertTrue(NotificationRule.findByUser(this.user1).get(0).getTriggeringEntity() instanceof QueryWrapper);
		Assert.assertTrue(NotificationRule.findByUser(this.user1).get(0).getTriggeringEntity().getID() == this.query1
				.getID());

	}

	@Test
	public void testRemove() {
		this.storeExampleNotificationRules();
		List<NotificationRuleForQuery> notificitations;
		notificitations = NotificationRuleForQuery.findAllQueryNotificationRules();
		Assert.assertTrue(notificitations.size() == 2);

		final NotificationRuleForQuery deletedNotification = notificitations.get(0);
		deletedNotification.remove();

		notificitations = NotificationRuleForQuery.findAllQueryNotificationRules();
		Assert.assertTrue(notificitations.size() == 1);

		Assert.assertTrue(notificitations.get(0).getID() != deletedNotification.getID());
	}

	@Test
	public void testRemoveUserWithNotificationRule() {
		this.storeExampleNotificationRules();
		List<NotificationRuleForQuery> notificitations = NotificationRuleForQuery.findAllQueryNotificationRules();
		Assert.assertTrue(notificitations.size() == 2);

		final NotificationRuleForQuery deletedNotification = notificitations.get(0);
		final EapUser user = deletedNotification.getUser();
		user.remove();

		final List<EapUser> users = EapUser.findAll();
		Assert.assertTrue(users.size() == 1);
		Assert.assertTrue(users.get(0).getID() != user.getID());

		// notification was deleted as well
		notificitations = NotificationRuleForQuery.findAllQueryNotificationRules();
		Assert.assertTrue(notificitations.size() == 1);
		Assert.assertTrue(notificitations.get(0).getID() != deletedNotification.getID());
	}

	@Test
	public void testRemoveQueryWithNotificationRule() {
		this.storeExampleNotificationRules();
		List<NotificationRuleForQuery> notificitations = NotificationRuleForQuery.findAllQueryNotificationRules();
		Assert.assertTrue(notificitations.size() == 2);

		final NotificationRuleForQuery deletedNotification = notificitations.get(0);
		final QueryWrapper query = deletedNotification.getQuery();
		query.remove();

		final List<QueryWrapper> queries = QueryWrapper.getAllLiveQueries();
		Assert.assertTrue(queries.size() == 1);
		Assert.assertTrue(queries.get(0).getID() != query.getID());

		// notification was deleted as well
		notificitations = NotificationRuleForQuery.findAllQueryNotificationRules();
		Assert.assertTrue(notificitations.size() == 1);
		Assert.assertTrue(notificitations.get(0).getID() != deletedNotification.getID());
	}

	private void storeExampleNotificationRules() {
		this.user1 = new EapUser("Micha", "Micha1234", this.michaMail);
		this.user1.save();
		this.type1 = new EapEventType("ToNotify");
		this.type1.save();
		this.query1 = new QueryWrapper("allToNotify1", "Select * from ToNotify", QueryTypeEnum.LIVE);
		this.query1.save();
		final NotificationRuleForQuery notification1 = new NotificationRuleForQuery(this.query1, this.user1,
				NotificationMethod.GUI);
		notification1.save();

		this.user2 = new EapUser("Tsun", "Tsun1234", "tsun@mail.de");
		this.user2.save();
		this.type2 = new EapEventType("ToNotify2");
		this.type2.save();
		this.query2 = new QueryWrapper("allToNotify2", "Select * from ToNotify2", QueryTypeEnum.LIVE);
		this.query2.save();
		final NotificationRuleForQuery notification2 = new NotificationRuleForQuery(this.query2, this.user2,
				NotificationMethod.GUI);
		notification2.save();

	}

}
