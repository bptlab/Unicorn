/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.notification;

import de.hpi.unicorn.application.components.form.BootstrapModal;

/**
 * Modal that encapsulates the panel for the creation of a notificationRule
 */
public class AddNotificationModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private final NotificationCreationPanel panel;

	/**
	 * 
	 * @param notificationPage
	 * @param window
	 */
	public AddNotificationModal(final String id, final NotificationPage notificationPage) {
		super(id, "Add Notification");
		this.panel = new NotificationCreationPanel("notificationCreationPanel", notificationPage);
		this.add(this.panel);
	}
}
