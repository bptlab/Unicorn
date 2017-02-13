/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.notification.NotificationMethod;
import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.user.EapUser;

/**
 * This panel creates a @see NotificationRule. This can either be a @see
 * NotificationRuleForEvent or a @see NotificationRuleForQuery
 */
public class NotificationCreationPanel extends Panel {

	private static final long serialVersionUID = 1L;

	List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
	private final NotificationPage visualisationPage;
	private final NotificationCreationPanel panel;

	private NotificationPanel feedbackPanel;
	private final Form<Void> layoutForm;

	private DropDownChoice<EapUser> userSelect;
	// private DropDownChoice<String> typeSelect;
	private DropDownChoice<NotificationMethod> prioritySelect;
	// private WebMarkupContainer eventTypeContainer;
	private WebMarkupContainer queryContainer;
	private DropDownChoice<QueryWrapper> querySelect;

	// private String selectedEventTypeName = null;
	// private EapEventType selectedEventType = null;
	private final EapUser selectedUser = null;
	private final QueryWrapper selectedQuery = null;
	// private static final List<String> TYPES = Arrays.asList("Event Type",
	// "Query");
	// private IModel<String> selectedType = Model.of("Event Type");
	private static final List<NotificationMethod> PRIORITIES = Arrays.asList(NotificationMethod.values());
	private final IModel<NotificationMethod> selectedPriority = Model.of(NotificationMethod.GUI);

	public NotificationCreationPanel(final String id, final NotificationPage notificationPage) {
		super(id);
		this.panel = this;

		this.visualisationPage = notificationPage;

		this.layoutForm = new Form<Void>("layoutForm");
		this.add(this.layoutForm);
		this.addFeedbackPanel(this.layoutForm);
		// layoutForm.add(addTypeSelect());
		this.layoutForm.add(this.addPrioritySelect());
		this.layoutForm.add(this.addUserSelect());
		// layoutForm.add(addEventDiv());
		this.layoutForm.add(this.addQueryDiv());
		this.addButtonsToForm(this.layoutForm);
	}

	// private Component addTypeSelect() {
	// typeSelect = new DropDownChoice<String>("typeSelect", selectedType,
	// TYPES);
	// typeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange"){
	// @Override
	// protected void onUpdate(AjaxRequestTarget target) {
	// target.add(eventTypeContainer);
	// target.add(queryContainer);
	// target.add(eventTypeSelect);
	// target.add(conditionInput);
	// }
	// });
	// typeSelect.setOutputMarkupId(true);
	// return typeSelect;
	// }

	// private boolean isQueryDivVisible() {
	// return selectedType.getObject().equals("Query");
	// };

	// private boolean isEventDivVisible() {
	// return !isQueryDivVisible();
	// };

	private Component addQueryDiv() {
		this.queryContainer = new WebMarkupContainer("QueryDiv") {
			// public boolean isVisible() {
			// return isQueryDivVisible();
			// }
		};
		this.queryContainer.setOutputMarkupPlaceholderTag(true);
		this.queryContainer.add(this.addQuerySelect());
		return this.queryContainer;
	}

	private Component addPrioritySelect() {
		this.prioritySelect = new DropDownChoice<NotificationMethod>("prioritySelect", this.selectedPriority,
				NotificationCreationPanel.PRIORITIES);
		this.prioritySelect.setOutputMarkupId(true);
		return this.prioritySelect;
	}

	// private Component addEventDiv() {
	// eventTypeContainer = new WebMarkupContainer("EventDiv") {
	// public boolean isVisible() {
	// return isEventDivVisible();
	// }
	// };
	// eventTypeContainer.setOutputMarkupPlaceholderTag(true);
	// eventTypeContainer.add(addEventTypeSelect());
	// eventTypeContainer.add(addConditionInput());
	// return eventTypeContainer;
	// }

	// private Component addEventTypeSelect() {
	// eventTypeSelect = new DropDownChoice<String>("eventTypeSelect", new
	// PropertyModel<String>(this, "selectedEventTypeName"),
	// eventTypeNameProvider) {
	// public boolean isVisible() {
	// return isEventDivVisible();
	// }
	// };
	// eventTypeSelect.setOutputMarkupPlaceholderTag(true);
	//
	// eventTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange"){
	//
	// @Override
	// protected void onUpdate(AjaxRequestTarget target) {
	// selectedEventType = EapEventType.findByTypeName(selectedEventTypeName);
	// conditionInput.clearSelectedEventType();
	// conditionInput.addSelectedEventType(selectedEventType);
	// target.add(conditionInput.getConditionAttributeSelect());
	// target.add(conditionInput.getConditionValueSelect());
	// }
	// });
	// return eventTypeSelect;
	// }

	// private Component addConditionInput() {
	// conditionInput = new FlexConditionInputPanel("conditionInput") {
	// public boolean isVisible() {
	// return isEventDivVisible();
	// }
	// };
	// conditionInput.setOutputMarkupPlaceholderTag(true);
	// return conditionInput;
	// }

	private Component addQuerySelect() {
		this.querySelect = new DropDownChoice<QueryWrapper>("querySelect", new PropertyModel<QueryWrapper>(this,
				"selectedQuery"), QueryWrapper.getAllLiveQueries()) {
			// public boolean isVisible() {
			// return isQueryDivVisible();
			// }
		};
		this.querySelect.setOutputMarkupPlaceholderTag(true);
		return this.querySelect;
	}

	private Component addUserSelect() {
		this.userSelect = new DropDownChoice<EapUser>("userSelect", new PropertyModel<EapUser>(this, "selectedUser"),
				EapUser.findAll());
		return this.userSelect;
	}

	private void addFeedbackPanel(final Form<Void> layoutForm) {
		this.feedbackPanel = new NotificationPanel("feedback");
		this.feedbackPanel.setOutputMarkupId(true);
		this.feedbackPanel.setOutputMarkupPlaceholderTag(true);
		layoutForm.add(this.feedbackPanel);
	}

	private void addButtonsToForm(final Form<Void> layoutForm) {

		final AjaxButton createButton = new AjaxButton("createButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				NotificationCreationPanel.this.panel.getFeedbackPanel().setVisible(true);
				boolean error = false;
				// if (selectedType.getObject().equals("Event Type") &&
				// selectedEventType == null) {
				// panel.getFeedbackPanel().error("Choose an Event Type!");
				// panel.getFeedbackPanel().setVisible(true);
				// target.add(panel.getFeedbackPanel());
				// error = true;
				// };
				// if (selectedType.getObject().equals("Query") && selectedQuery
				// == null) {
				// panel.getFeedbackPanel().error("Choose a Query!");
				// panel.getFeedbackPanel().setVisible(true);
				// target.add(panel.getFeedbackPanel());
				// error = true;
				// };
				if (NotificationCreationPanel.this.selectedQuery == null) {
					NotificationCreationPanel.this.panel.getFeedbackPanel().error("Choose a live query!");
					NotificationCreationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(NotificationCreationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;

				if (NotificationCreationPanel.this.selectedUser == null) {
					NotificationCreationPanel.this.panel.getFeedbackPanel().error("Choose a user!");
					NotificationCreationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(NotificationCreationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;

				if (error == false) {
					// create and save notificationRule
					NotificationRule notification;
					// if (selectedType.getObject().equals("Event Type")) {
					// //create event notificationRule
					// notification = new
					// NotificationRuleForEvent(selectedEventType,
					// conditionInput.getCondition(), selectedUser,
					// selectedPriority.getObject());
					// Broker.getInstance().send(notification);
					// } else {
					// //create query notificationRule
					// notification = new
					// NotificationRuleForQuery(selectedQuery, selectedUser,
					// selectedPriority.getObject(),
					// UUID.randomUUID().toString());
					// notification.save();
					// }
					notification = new NotificationRuleForQuery(NotificationCreationPanel.this.selectedQuery,
							NotificationCreationPanel.this.selectedUser,
							NotificationCreationPanel.this.selectedPriority.getObject());
					notification.save();

					// update notification rule list in notification page
					final NotificationPage visualisation = NotificationCreationPanel.this.visualisationPage;
					visualisation.notificationRulesListView.notificationRuleProvider.addItem(notification);
					visualisation.notificationRulesListView.notificationRuleTable.detach();
					target.add(visualisation.notificationRulesListView.notificationRuleTable);
					// close this Panel
					NotificationCreationPanel.this.visualisationPage.addNotificationModal.close(target);
					// TODO: after closing, the page is still disabled/overlayed
				}
			};
		};
		layoutForm.add(createButton);
	}

	public NotificationPanel getFeedbackPanel() {
		return this.feedbackPanel;
	}

}
