/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;

import de.hpi.unicorn.application.components.table.EapProvider;
import de.hpi.unicorn.notification.Notification;
import de.hpi.unicorn.user.EapUser;

/**
 * This class is a provider for @see Notification
 */
public class NotificationProvider extends EapProvider<Notification> implements IFilterStateLocator {

	private EapUser user = null;
	private NotificationFilter notificationFilter = new NotificationFilter();

	public NotificationProvider(final EapUser user) {
		super(new ArrayList<Notification>());
		this.user = user;
		this.update();
	}

	public void update() {
		if (this.user == null) {
			return;
		}
		final List<Notification> notifications = Notification.findUnseenForUser(this.user);
		if (notifications != null) {
			;
		}
		this.entities = notifications;
	}

	public void markSeenSelectedEntries() {
		for (final Notification notification : this.selectedEntities) {
			notification.setSeen();
		}
	}

	private List<Notification> filterNotifications(final List<Notification> notificationsToFilter,
			final NotificationFilter notificationFilter) {
		final List<Notification> returnedNotifications = new ArrayList<Notification>();
		for (final Notification notification : notificationsToFilter) {
			if (notificationFilter.match(notification)) {
				returnedNotifications.add(notification);
			}
		}
		return returnedNotifications;
	}

	@Override
	public Object getFilterState() {
		return this.notificationFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.notificationFilter = (NotificationFilter) state;
	}

	public NotificationFilter getNotificationFilter() {
		return this.notificationFilter;
	}

	public void setNotificationFilter(final NotificationFilter notificationFilter) {
		this.notificationFilter = notificationFilter;
		this.entities = this.filterNotifications(Notification.findUnseenForUser(this.user), notificationFilter);
	}

}
