/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import de.hpi.unicorn.email.EmailUtils;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

/**
 * This class is a certain @see NotificationRule. An event notification rule
 * saves a event type and a condition. If an event from that event type occurs
 * that matches the condition the user is informed.
 */
@Entity
@DiscriminatorValue("E")
@Deprecated
public class NotificationRuleForEvent extends NotificationRule {

	@ManyToOne
	protected EapEventType eventType;

	@OneToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private EventCondition condition;

	/**
	 * Default-Constructor for JPA.
	 */
	public NotificationRuleForEvent() {
		this.ID = 0;
		this.eventType = null;
		this.user = null;
	}

	/**
	 * Creates an event notification rule with event type and condition.
	 * 
	 * @param eventType
	 * @param condition
	 * @param user
	 * @param priority
	 */
	public NotificationRuleForEvent(final EapEventType eventType, final EventCondition condition, final EapUser user,
			final NotificationMethod priority) {
		this.eventType = eventType;
		this.condition = condition;
		this.user = user;
		this.priority = priority;
	}

	/**
	 * Creates an event notification rule for an event type without condition.
	 * 
	 * @param eventType
	 * @param user
	 * @param priority
	 */
	public NotificationRuleForEvent(final EapEventType eventType, final EapUser user, final NotificationMethod priority) {
		this.eventType = eventType;
		this.condition = new EventCondition();
		this.user = user;
		this.priority = priority;
	}

	/**
	 * This method is called, when an event occurs, that matches the event type
	 * and condition of the notification rule. This will create a new
	 * notification.
	 * 
	 * @param event
	 */
	public void trigger(final EapEvent event) {
		final NotificationForEvent notification = new NotificationForEvent(event, this.user, this);
		notification.save();
		// here can be added other actions connected to the creation of a
		// notification
		if (this.priority == NotificationMethod.MAIL) {
			// send mail
			EmailUtils.sendBP2013Mail(this.user.getMail(), "Notification GET-Events", notification.toString());
		}
	}

	/**
	 * Checks whether an event matches the condition of this notification rule.
	 * Forwards the question whether an event matches the condition to the
	 * condition itself.
	 * 
	 * @param event
	 * @return whether an event matches a condition
	 */
	public boolean matches(final EapEvent event) {
		if (!this.hasCondition()) {
			return true;
		}
		return this.condition.matches(event);
	}

	/**
	 * Creates a string representation of the notification rule.
	 */
	@Override
	public String toString() {
		String representation = "Notification for " + this.eventType;
		if (this.hasCondition()) {
			representation += " with " + this.condition.getConditionString();
		}
		representation += " for user " + this.user.getName();
		return representation;
	}

	public boolean hasCondition() {
		return (this.condition != null && this.condition.getConditionString() != "");
	}

	// Getter and Setter

	public EapEventType getEventType() {
		return this.eventType;
	}

	public void setEventType(final EapEventType eventType) {
		this.eventType = eventType;
	}

	public EventCondition getCondition() {
		return this.condition;
	}

	public void setCondition(final EventCondition condition) {
		this.condition = condition;
	}

	@Override
	public Persistable getTriggeringEntity() {
		return this.getEventType();
	}

	// JPA-Methods

	/**
	 * Finds an event notification rule by ID from database.
	 * 
	 * @param ID
	 * @return event notification rule
	 */
	public static NotificationRuleForEvent findByID(final int ID) {
		return Persistor.getEntityManager().find(NotificationRuleForEvent.class, ID);
	}

	/**
	 * Finds all event notification rules for an event type
	 * 
	 * @param eventType
	 * @return all event notification rules for an event type
	 */
	public static List<NotificationRuleForEvent> findByEventType(final EapEventType eventType) {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE EVENTTYPE_ID = '" + eventType.getID() + "'",
				NotificationRuleForEvent.class);
		return q.getResultList();
	}

	/**
	 * Finds all event notification rules from database.
	 * 
	 * @return all event notification rules
	 */
	public static List<NotificationRuleForEvent> findAllEventNotificationRules() {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE Disc = 'E'", NotificationRuleForEvent.class);
		return q.getResultList();
	}

}
