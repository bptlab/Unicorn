/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.AuthenticatedSession;
import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.table.SelectEntryPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.eventrepository.eventview.EventViewModal;
import de.hpi.unicorn.notification.Notification;
import de.hpi.unicorn.notification.NotificationForEvent;

/**
 * List to display notifications on the @see NotificationPage
 */
@SuppressWarnings("serial")
public class NotificationList extends Panel {

	private final EventViewModal eventViewModal;
	private final Form<Void> notificationForm;
	private ArrayList<IColumn<Notification, String>> columns;
	public DefaultDataTable<Notification, String> notificationTable;
	private NotificationProvider notificationProvider;
	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public NotificationList(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		// add modal
		this.eventViewModal = new EventViewModal("eventViewModal");
		this.eventViewModal.setOutputMarkupId(true);
		this.add(this.eventViewModal);

		this.add(this.addFilterComponents());

		this.notificationForm = new Form<Void>("notificationForm");
		this.notificationForm.add(this.addNotifications());
		this.add(this.notificationForm);

	}

	private Component addFilterComponents() {
		final Form<Void> buttonForm = new WarnOnExitForm("buttonForm");

		final List<String> notificationFilterCriteriaList = new ArrayList<String>(Arrays.asList(new String[] { "ID",
				"Timestamp", "NotificationRule (ID)" }));
		final String selectedNotificationCriteria = "ID";

		final DropDownChoice<String> notificationFilterCriteriaSelect = new DropDownChoice<String>(
				"notificationFilterCriteria", new Model<String>(selectedNotificationCriteria),
				notificationFilterCriteriaList);
		buttonForm.add(notificationFilterCriteriaSelect);

		final List<String> conditions = new ArrayList<String>(Arrays.asList(new String[] { "<", "=", ">" }));
		final String selectedCondition = "=";

		final DropDownChoice<String> eventFilterConditionSelect = new DropDownChoice<String>(
				"notificationFilterCondition", new Model<String>(selectedCondition), conditions);
		buttonForm.add(eventFilterConditionSelect);

		final TextField<String> searchValueInput = new TextField<String>("searchValueInput", Model.of(""));
		buttonForm.add(searchValueInput);

		final AjaxButton filterButton = new AjaxButton("filterButton", buttonForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String notificationFilterCriteria = notificationFilterCriteriaSelect.getChoices().get(
						Integer.parseInt(notificationFilterCriteriaSelect.getValue()));
				final String notificationFilterCondition = eventFilterConditionSelect.getChoices().get(
						Integer.parseInt(eventFilterConditionSelect.getValue()));
				final String filterValue = searchValueInput.getValue();
				NotificationList.this.notificationProvider.setNotificationFilter(new NotificationFilter(
						notificationFilterCriteria, notificationFilterCondition, filterValue));
				target.add(NotificationList.this.notificationTable);
			}
		};
		buttonForm.add(filterButton);

		final AjaxButton resetButton = new BlockingAjaxButton("resetButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				;
				NotificationList.this.notificationProvider.setNotificationFilter(new NotificationFilter());
				target.add(NotificationList.this.notificationTable);
			}
		};
		buttonForm.add(resetButton);

		final AjaxButton markSeenButton = new BlockingAjaxButton("seenButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				;
				NotificationList.this.notificationProvider.markSeenSelectedEntries();
				NotificationList.this.notificationProvider.clearSelectedEntities();
				NotificationList.this.notificationProvider.update();
				target.add(NotificationList.this.notificationTable);
			}
		};
		buttonForm.add(markSeenButton);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				NotificationList.this.notificationProvider.selectAllEntries();
				target.add(NotificationList.this.notificationTable);
			}
		};
		buttonForm.add(selectAllButton);

		return buttonForm;
	}

	/**
	 * prepares list of notifications
	 * 
	 * @return list of notifications
	 */
	@SuppressWarnings({ "unchecked" })
	private Component addNotifications() {

		// collect all notifications for logged in user
		this.notificationProvider = new NotificationProvider(((AuthenticatedSession) Session.get()).getUser());

		this.columns = new ArrayList<IColumn<Notification, String>>();
		this.columns.add(new PropertyColumn<Notification, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<Notification, String>(Model.of("Timestamp"), "timestamp") {
			@Override
			public void populateItem(final Item<ICellPopulator<Notification>> item, final String componentId,
					final IModel<Notification> rowModel) {
				final String shortenedValues = NotificationList.this.formatter.format(rowModel.getObject()
						.getTimestamp());
				final Label label = new Label(componentId, shortenedValues);
				item.add(label);
			}
		});
		this.columns.add(new PropertyColumn<Notification, String>(Model.of("Notification Rule"), "notificationRule"));
		this.columns.add(new AbstractColumn<Notification, String>(Model.of("Trigger"), "trigger") {
			@Override
			public void populateItem(final Item<ICellPopulator<Notification>> item, final String componentId,
					final IModel<Notification> rowModel) {
				String shortenedValues = rowModel.getObject().getTriggeringText();
				if (shortenedValues.length() > 200) {
					shortenedValues = shortenedValues.substring(0, 200) + "...";
				}
				final Label label = new Label(componentId, shortenedValues);

				// for events add event view modal
				if (rowModel.getObject() instanceof NotificationForEvent) {
					final NotificationForEvent notification = (NotificationForEvent) rowModel.getObject();
					label.add(new AjaxEventBehavior("onclick") {
						@Override
						protected void onEvent(final AjaxRequestTarget target) {
							// on click open Event View Modal
							NotificationList.this.eventViewModal.getPanel().setEvent(notification.getEvent());
							NotificationList.this.eventViewModal.getPanel().detach();
							target.add(NotificationList.this.eventViewModal.getPanel());
							NotificationList.this.eventViewModal.show(target);
						}
					});
				}
				item.add(label);
			}
		});
		this.columns.add(new AbstractColumn(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((Notification) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId, NotificationList.this.notificationProvider));
			};
		});

		this.notificationTable = new DefaultDataTable<Notification, String>("notifications", this.columns,
				this.notificationProvider, 20);
		this.notificationTable.setOutputMarkupId(true);

		return this.notificationTable;
	}
}
