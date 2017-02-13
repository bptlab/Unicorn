/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.eventhandling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.notification.NotificationRuleForEvent;

/**
 * This method implements the Observable Pattern. It saves the notification
 * rules. It is informed by the broker about incoming events and triggers the
 * notification rules.
 */
public class NotificationObservable {

	private static NotificationObservable instance = null;
	private final Map<EapEventType, HashSet<NotificationRuleForEvent>> notifications = new HashMap<EapEventType, HashSet<NotificationRuleForEvent>>();

	/**
	 * Singleton, therefore the constructor is private.
	 */
	private NotificationObservable() {
		this.initiateWithNotificationsFromDB();
	}

	/**
	 * Singleton class. Use this method to get the instance.
	 * 
	 * @return
	 */
	public static NotificationObservable getInstance() {
		if (NotificationObservable.instance == null) {
			NotificationObservable.instance = new NotificationObservable();
		}
		return NotificationObservable.instance;
	}

	/**
	 * Clears the singleton-object.
	 */
	public void clearInstance() {
		NotificationObservable.instance = null;
	}

	/**
	 * Triggers the notification rule with an event. A new notification will be
	 * created.
	 * 
	 * @param event
	 */
	public void trigger(final EapEvent event) {
		final Set<NotificationRuleForEvent> notificationsToTrigger = this.notifications.get(event.getEventType());
		if (notificationsToTrigger == null) {
			return;
		}
		for (final NotificationRuleForEvent notification : notificationsToTrigger) {
			if (notification.matches(event)) {
				notification.trigger(event);
			}
		}
	}

	/**
	 * Registers the notifications from the database.
	 */
	private void initiateWithNotificationsFromDB() {
		final List<NotificationRuleForEvent> notificationsFromDB = NotificationRuleForEvent
				.findAllEventNotificationRules();
		for (final NotificationRuleForEvent notification : notificationsFromDB) {
			this.addNotificationObserver(notification);
		}
	}

	/**
	 * Adds a new notification rule.
	 * 
	 * @param notification
	 */
	public void addNotificationObserver(final NotificationRuleForEvent notification) {
		// get notifications already registered for this eventtype
		HashSet<NotificationRuleForEvent> listOfNotifcationsForEventType = this.notifications.get(notification
				.getEventType());
		if (listOfNotifcationsForEventType == null) {
			listOfNotifcationsForEventType = new HashSet<NotificationRuleForEvent>();
		}
		listOfNotifcationsForEventType.add(notification);
		this.notifications.put(notification.getEventType(), listOfNotifcationsForEventType);
	}

	/**
	 * Removes all notification rules that subscribed for the event type
	 * 
	 * @param eventType
	 */
	public void removeNotificationObserversForEventType(final EapEventType eventType) {
		this.notifications.remove(eventType);
	}

	public void clearNotifications() {
		this.notifications.clear();
	}

	/**
	 * Removes a certain notification rule
	 * 
	 * @param notification
	 */
	public void removeNotificationObserver(final NotificationRuleForEvent notification) {
		// get notifications already registered for this eventtype
		final HashSet<NotificationRuleForEvent> listOfNotifcationsForEventType = this.notifications.get(notification
				.getEventType());
		if (listOfNotifcationsForEventType == null) {
			return;
		}
		listOfNotifcationsForEventType.remove(notification);
		this.notifications.put(notification.getEventType(), listOfNotifcationsForEventType);
	}

	/**
	 * Removes several notification rules.
	 * 
	 * @param notifications
	 */
	public void removeNotificationObservers(final List<NotificationRuleForEvent> notifications) {
		for (final NotificationRuleForEvent notification : notifications) {
			this.removeNotificationObserver(notification);
		}
	}

	/**
	 * Takes several events and forwards the triggering to the coressponding
	 * notification rules.
	 * 
	 * @param events
	 */
	public void trigger(final List<EapEvent> events) {
		for (final EapEvent event : events) {
			this.trigger(event);
		}
	}

}
