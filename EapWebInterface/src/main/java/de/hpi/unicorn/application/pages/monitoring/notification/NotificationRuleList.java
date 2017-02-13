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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.BlockingForm;
import de.hpi.unicorn.application.components.form.DeleteButtonPanel;
import de.hpi.unicorn.application.components.table.EapProvider;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.notification.NotificationRuleForQuery;

/**
 * This class is a List to display @see NotificationRule on the @see
 * NotificationPage
 */
public class NotificationRuleList extends Panel {

	private final BlockingForm notificationForm;
	private ArrayList<IColumn<NotificationRule, String>> columns;
	public DefaultDataTable<NotificationRule, String> notificationRuleTable;
	public EapProvider<NotificationRule> notificationRuleProvider;
	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public NotificationRuleList(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		this.notificationForm = new BlockingForm("notificationForm");
		this.notificationForm.add(this.addNotificationRules());
		this.add(this.notificationForm);
	}

	@SuppressWarnings({ "unchecked" })
	private Component addNotificationRules() {

		this.notificationRuleProvider = new EapProvider<NotificationRule>(NotificationRule.findAll());

		this.columns = new ArrayList<IColumn<NotificationRule, String>>();
		this.columns.add(new PropertyColumn<NotificationRule, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<NotificationRule, String>(Model.of("Timestamp"), "timestamp") {
			@Override
			public void populateItem(final Item<ICellPopulator<NotificationRule>> item, final String componentId,
					final IModel<NotificationRule> rowModel) {
				final String shortenedValues = NotificationRuleList.this.formatter.format(rowModel.getObject()
						.getTimestamp());
				final Label label = new Label(componentId, shortenedValues);
				item.add(label);
			}
		});
		this.columns.add(new PropertyColumn<NotificationRule, String>(Model.of("Priority"), "priority"));
		this.columns.add(new PropertyColumn<NotificationRule, String>(Model.of("User"), "user"));
		this.columns.add(new AbstractColumn(new Model("Trigger")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				// notification rule for event - no longer supported
				// if (rowModel.getObject() instanceof NotificationRuleForEvent)
				// {
				// NotificationRuleForEvent rule = (NotificationRuleForEvent)
				// rowModel.getObject();
				// Label label = new Label(componentId, rule.getEventType() +
				// " : " + rule.getCondition().getConditionString());
				// cellItem.add(label);
				// }
				final NotificationRule rule = (NotificationRule) rowModel.getObject();
				Label label = new Label(componentId, "");
				// notification rule query
				if (rule instanceof NotificationRuleForQuery) {
					label = new Label(componentId, ((NotificationRuleForQuery) rule).getQuery().toString());
				}
				label.setOutputMarkupId(true);
				cellItem.add(label);
			}
		});
		this.columns.add(new AbstractColumn(new Model("UUID")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final NotificationRule rule = (NotificationRule) rowModel.getObject();
				Label label = new Label(componentId, "");
				if (rule instanceof NotificationRuleForQuery) {
					label = new Label(componentId, ((NotificationRuleForQuery) rule).getUuid());
				}
				label.setOutputMarkupId(true);
				cellItem.add(label);
			};
		});
		this.columns.add(new AbstractColumn(new Model("Delete")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final NotificationRule rule = (NotificationRule) rowModel.getObject();
				final AjaxButton removeButton = new AjaxButton("button") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form form) {
						rule.remove();
						// if (rule instanceof NotificationRuleForEvent) {
						// NotificationRuleForEvent eventRule =
						// (NotificationRuleForEvent) rule;
						// NotificationObservable.getInstance().removeNotificationObserver(eventRule);
						// }
						NotificationRuleList.this.notificationRuleProvider.removeItem(rule);
						NotificationRuleList.this.notificationRuleTable.detach();
						target.add(NotificationRuleList.this.notificationRuleTable);
					}
				};

				WebMarkupContainer buttonPanel = new WebMarkupContainer(componentId);
				try {
					buttonPanel = new DeleteButtonPanel(componentId, removeButton);
				} catch (final Exception e) {
					e.printStackTrace();
				}
				buttonPanel.setOutputMarkupId(true);
				cellItem.add(buttonPanel);
			};
		});

		this.notificationRuleTable = new DefaultDataTable<NotificationRule, String>("notificationRules", this.columns,
				this.notificationRuleProvider, 20000);
		this.notificationRuleTable.setOutputMarkupId(true);

		return this.notificationRuleTable;
	}
}
