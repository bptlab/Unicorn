/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.notification;

import java.text.DateFormat;
import java.text.ParseException;

import de.hpi.unicorn.application.pages.eventrepository.model.AbstractFilter;
import de.hpi.unicorn.notification.Notification;

/**
 * Filter for Notification-List on @see NotificationPage
 */
public class NotificationFilter extends AbstractFilter {

	public NotificationFilter(final String eventFilterCriteria, final String eventFilterCondition,
			final String filterValue) {
		super(eventFilterCriteria, eventFilterCondition, filterValue);
	}

	public NotificationFilter() {
		super();
	}

	/**
	 * checks whether a notification matches a filtercriteria
	 * 
	 * @param notification
	 * @return
	 */
	public boolean match(final Notification notification) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}

		// "ID", "Timestamp", "NotificationRule (ID)"
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (notification.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (notification.getID() > Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (notification.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else if (this.filterCriteria.equals("Timestamp")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (notification.getTimestamp().before(DateFormat.getInstance().parse(this.filterValue))) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (notification.getTimestamp().after(DateFormat.getInstance().parse(this.filterValue))) {
						return true;
					}
				} else {
					if (notification.getTimestamp().equals(DateFormat.getInstance().parse(this.filterValue))) {
						return true;
					}
				}
				return false;
			} catch (final ParseException e) {
				return false;
			}

		} else if (this.filterCriteria.equals("NotificationRule (ID)")) {
			if (this.filterCondition.equals("<")) {
				if (notification.getNotificationRule().getID() < Integer.parseInt(this.filterValue)) {
					return true;
				}
			} else if (this.filterCondition.equals(">")) {
				if (notification.getNotificationRule().getID() > Integer.parseInt(this.filterValue)) {
					return true;
				}
			} else {
				if (notification.getNotificationRule().getID() == Integer.parseInt(this.filterValue)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
		return false;
	}

}
