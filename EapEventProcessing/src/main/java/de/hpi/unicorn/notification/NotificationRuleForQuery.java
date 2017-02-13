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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.persistence.*;

import net.sf.json.JSONObject;
import de.hpi.unicorn.email.EmailUtils;
import de.hpi.unicorn.messageQueue.JMSProvider;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.user.EapUser;

/**
 * This class represents a special @see NotificationRule that notfies a user
 * about triggered queries.
 */
@SuppressWarnings("serial")
@Entity
@DiscriminatorValue("Q")
public class NotificationRuleForQuery extends NotificationRule {

	private static final String EVENT_KEY_HIRARCHY_SEPERATOR = "\\.";

	@ManyToOne
	protected QueryWrapper query;

	protected String uuid;


	/**
	 * Default-Constructor for JPA.
	 */
	public NotificationRuleForQuery() {
		this.ID = 0;
		this.query = null;
		this.user = null;
	}

	/**
	 * Creates a new query notification rule.
	 *
	 * @param query
	 * @param user
	 * @param priority
	 */
	public NotificationRuleForQuery(final QueryWrapper query, final EapUser user, final NotificationMethod priority) {
		this.query = query;
		this.timestamp = new Date();
		this.user = user;
		this.priority = priority;
		this.uuid = UUID.randomUUID().toString();
	}

	/**
	 * This method creates a new @see NotificationForQuery. It is called when
	 * the query of this notification rule is triggered.
	 *
	 * @param eventObject
	 */
	@SuppressWarnings("incomplete-switch")
	public void trigger(final Map<Object, Serializable> eventObject) {
		try {
			final JSONObject event = NotificationRuleUtils.toJSON(eventObject);
			final NotificationForQuery notification = new NotificationForQuery(this.user, event.toString(), this);
			notification.save();

			switch (this.priority) {
			case MAIL:
				// send mail
				EmailUtils.sendBP2013Mail(this.user.getMail(), "Notification GET-Events", notification.toString()
						+ ". The query is " + this.query.getEsperQuery() + " {" + this.query.getSparqlQuery() + "}");
				break;
			case QUEUE:
				// send to queue
				JMSProvider.sendMessage(JMSProvider.HOST, JMSProvider.PORT, this.getUuid(), event.toString());
				break;
			}
		} catch (final UnsupportedJsonTransformation e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		String representation = "Notification for " + this.query;
		representation += " for user " + this.user.getName();
		return representation;
	}

	@Override
	public Persistable remove() {
		if (this.priority == NotificationMethod.QUEUE) {
			try {
				JMSProvider.destroyMessageQueue(JMSProvider.HOST, JMSProvider.PORT, this.uuid);
			} catch (final JMSException e) {
				e.printStackTrace();
			}
		}
		return super.remove();
	}

	// Getter and Setter

	public QueryWrapper getQuery() {
		return this.query;
	}

	public void setQuery(final QueryWrapper query) {
		this.query = query;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	@Override
	public Persistable getTriggeringEntity() {
		return this.getQuery();
	}

	// JPA-Methods

	/**
	 * Finds all query notification rules from database.
	 *
	 * @return all query notification rules
	 */
	@SuppressWarnings("unchecked")
	public static List<NotificationRuleForQuery> findAllQueryNotificationRules() {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE Disc = 'Q' OR Disc = 'R'", NotificationRuleForQuery.class);
		return q.getResultList();
	}

	/**
	 * Finds a query notification rule from database by ID.
	 *
	 * @param ID
	 * @return query notification rule
	 */
	public static NotificationRuleForQuery findByID(final int ID) {
		return Persistor.getEntityManager().find(NotificationRuleForQuery.class, ID);
	}

	/**
	 * Finds query notification rules from database that are connected with a
	 * certain query.
	 *
	 * @param query
	 * @return query notification rules for query
	 */
	@SuppressWarnings("unchecked")
	public static List<NotificationRuleForQuery> findByQuery(final QueryWrapper query) {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE QUERY_ID = '" + query.getID() + "'",
				NotificationRuleForQuery.class);
		return q.getResultList();
	}

	public static NotificationRuleForQuery findByUUID(final String uuid) {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE UUID = '" + uuid + "'", NotificationRuleForQuery.class);

		if(q.getResultList().isEmpty()) {
			return null;
		} else {
			return (NotificationRuleForQuery) q.getResultList().get(0);
		}
	}

	/**
	 * Find all notification rules for a user from the database.
	 *
	 * @param user
	 * @return all notification rules for a user
	 */
	public static List<NotificationRuleForQuery> findQueriesByUser(final EapUser user) {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE Disc = 'Q' AND USER_ID = '" + user.getID() + "'",
				NotificationRuleForQuery.class);
		return q.getResultList();
	}

}
