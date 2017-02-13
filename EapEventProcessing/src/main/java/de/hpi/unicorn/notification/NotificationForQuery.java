/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * This class is a special @see Notification that is created when a query is
 * triggered.
 */
@SuppressWarnings("serial")
@Entity
@DiscriminatorValue("Q")
public class NotificationForQuery extends Notification {

	@Column(name = "Log", length = 10000)
	String log;

	public NotificationForQuery() {
		this.timestamp = new Date();
		this.notificationRule = new NotificationRuleForQuery();
		this.user = null;
		this.log = null;
	}

	public NotificationForQuery(final EapUser user, final String log, final NotificationRuleForQuery rule) {
		this.timestamp = new Date();
		this.user = user;
		this.log = log;
		this.notificationRule = rule;
	}

	@Override
	public String toString() {
		final NotificationRuleForQuery notificationQueryType = (NotificationRuleForQuery) this.notificationRule;
		return notificationQueryType.getQuery() + " was triggered on " + this.timestamp + " : " + this.log;
	}

	@Override
	public String getTriggeringText() {
		return this.log;
	}

	// JPA-Methods

	@SuppressWarnings("unchecked")
	public static List<NotificationForQuery> findUnseenQueryNotificationForUser(final EapUser user) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Notification WHERE USER_ID = '" + user.getID() + "' AND seen = 0 AND Disc = 'Q'",
				NotificationForQuery.class);
		return query.getResultList();
	}
}
