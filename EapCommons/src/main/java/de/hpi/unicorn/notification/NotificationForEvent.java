/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import java.util.Date;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

/**
 * This class is a certain @see Notification. This kind of notification is
 * created from a @see NotificationRuleForEvent informing a user about the
 * occurence of an event.
 */
@Entity
@DiscriminatorValue("E")
public class NotificationForEvent extends Notification {

	@ManyToOne
	protected EapEvent event;

	/**
	 * Creates a default event notification.
	 * 
	 * @param event
	 * @param user
	 * @param rule
	 */
	public NotificationForEvent() {
		this.timestamp = new Date();
		this.event = event;
		this.user = user;
		this.notificationRule = new NotificationRuleForEvent();
	}
	
	/**
	 * Creates an event notification.
	 * 
	 * @param event
	 * @param user
	 * @param rule
	 */
	public NotificationForEvent(final EapEvent event, final EapUser user, final NotificationRuleForEvent rule) {
		this.timestamp = new Date();
		this.event = event;
		this.user = user;
		this.notificationRule = rule;
	}

	// Getter and Setter

	public EapEvent getEvent() {
		return this.event;
	}

	public void setEvent(final EapEvent event) {
		this.event = event;
	}

	@Override
	public String getTriggeringText() {
		return this.event.shortenedString();
	}

	/**
	 * creates a string representation of the event notification
	 */
	@Override
	public String toString() {
		// cast notification rule to NotificationRuleForEventType
		final NotificationRuleForEvent notificationEventType = (NotificationRuleForEvent) this.notificationRule;
		if (!notificationEventType.hasCondition()) {
			return this.event.shortenedString() + " was received on " + this.timestamp;
		} else {
			return this.event.shortenedString() + " with " + notificationEventType.getCondition().getConditionString()
					+ " was received on " + this.timestamp;
		}
	}

	// JPA-Methods

	/**
	 * Finds all notifications for an event.
	 * 
	 * @param event
	 * @return all notifications for an event
	 */
	public static List<NotificationForEvent> findForEvent(final EapEvent event) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Notification WHERE EVENT_ID = '" + event.getID() + "'", NotificationForEvent.class);
		return query.getResultList();
	}

	/**
	 * Finds all event notifications.
	 * 
	 * @return all event notifications
	 */
	public static List<NotificationForEvent> findAllEventNotifications() {
		final Query q = Persistor.getEntityManager().createNativeQuery("SELECT * FROM Notification WHERE Disc = 'E'",
				NotificationForEvent.class);
		return q.getResultList();
	}

	/**
	 * Finds unseen event notifications for a user
	 * 
	 * @param user
	 * @return unseen event notifications for user
	 */
	public static List<NotificationForEvent> findUnseenEventNotificationForUser(final EapUser user) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Notification WHERE USER_ID = '" + user.getID() + "' AND seen = 0 AND Disc = 'E'",
				NotificationForEvent.class);
		return query.getResultList();
	}

}
