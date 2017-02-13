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

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import de.hpi.unicorn.application.AuthenticatedSession;
import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.notification.Notification;
import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.user.EapUser;

/**
 * This page displays notifications for logged in users, existing notification
 * rules and allows to create new notification rules via a @see
 * AddNotificationModal
 */
public class NotificationPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private AjaxButton addButton, deleteAllButton;
	private final Form<Void> form;
	public AddNotificationModal addNotificationModal;
	public NotificationRuleList notificationRulesListView;

	public NotificationPage() {
		super();

		// Create the modal window.
		this.addNotificationModal = new AddNotificationModal("addNotificationModal", this);
		this.add(this.addNotificationModal);

		// add notificationList
		if (((AuthenticatedSession) Session.get()).getUser() != null) {
			// logged in users see their notifications
			final NotificationList notificationList = new NotificationList("notificationList", this);
			notificationList.setOutputMarkupId(true);
			this.add(notificationList);
		} else {
			final Label notificationListLabel = new Label("notificationList", "Log in to check your notifications.");
			notificationListLabel.setOutputMarkupId(true);
			this.add(notificationListLabel);
		}

		this.form = new Form<Void>("form");
		this.form.add(this.addAddButton());
		this.form.add(this.addDeleteAllButton());
		this.add(this.form);

		this.addNotificationRules();
	}

	IModel<List<NotificationRule>> notificationRules = new LoadableDetachableModel<List<NotificationRule>>() {
		@Override
		protected List<NotificationRule> load() {
			return NotificationRule.findAll();
		}
	};

	IModel<List<Notification>> notifications = new LoadableDetachableModel<List<Notification>>() {
		@Override
		protected List<Notification> load() {
			// get User
			final EapUser user = ((AuthenticatedSession) Session.get()).getUser();
			if (user == null) {
				return new ArrayList<Notification>();
			}
			// get Notifications
			final List<Notification> notifications = Notification.findUnseenForUser(user);
			if (notifications == null) {
				return new ArrayList<Notification>();
			}
			return notifications;
		}
	};

	private Component addAddButton() {
		this.addButton = new AjaxButton("addNotificationButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				NotificationPage.this.addNotificationModal.show(target);
			}
		};
		return this.addButton;
	}

	private Component addDeleteAllButton() {
		this.deleteAllButton = new BlockingAjaxButton("deleteAllNotificationsButton", this.form) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				final List<NotificationRule> rules = new ArrayList<NotificationRule>(
						NotificationPage.this.notificationRulesListView.notificationRuleProvider.getEntities());
				for (final NotificationRule rule : rules) {
					rule.remove();
					NotificationPage.this.notificationRulesListView.notificationRuleProvider.removeItem(rule);
					NotificationPage.this.notificationRulesListView.notificationRuleTable.detach();
					target.add(NotificationPage.this.notificationRulesListView.notificationRuleTable);
				}
			}
		};
		this.deleteAllButton.setOutputMarkupId(true);
		return this.deleteAllButton;
	}

	@SuppressWarnings({ "unchecked" })
	private void addNotificationRules() {
		this.notificationRulesListView = new NotificationRuleList("notificationRuleList", this);
		this.notificationRulesListView.setOutputMarkupId(true);
		this.add(this.notificationRulesListView);
	}
}
