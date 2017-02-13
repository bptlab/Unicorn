/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.eventviews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.user.EapUser;
import de.hpi.unicorn.visualisation.EventView;
import de.hpi.unicorn.visualisation.TimePeriodEnum;

/**
 * This panel is used to configure and save a new @see EventView Object
 */
public class ViewConfigurationPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final EventViewPage parentPage;
	private final ViewConfigurationPanel panel;
	private NotificationPanel feedbackPanel;

	private final Form<Void> layoutForm;
	private ListMultipleChoice<EapEventType> eventTypeSelect;
	private DropDownChoice<TimePeriodEnum> timePeriodSelect;
	private DropDownChoice<EapUser> userSelect;

	private final ArrayList<EapEventType> selectedEventTypes = new ArrayList<EapEventType>();
	private static final List<TimePeriodEnum> TIMEPERIODS = Arrays.asList(TimePeriodEnum.values());
	private final IModel<TimePeriodEnum> selectedTimePeriod = Model.of(TimePeriodEnum.INF);
	private final EapUser selectedUser = null;

	public ViewConfigurationPanel(final String id, final EventViewPage visualisationPanel) {
		super(id);
		this.panel = this;
		this.parentPage = visualisationPanel;

		this.layoutForm = new Form<Void>("layoutForm");
		this.add(this.layoutForm);
		this.addFeedbackPanel(this.layoutForm);

		this.layoutForm.add(this.addEventTypeSelect());
		this.layoutForm.add(this.addTimePeriodSelect());
		this.layoutForm.add(this.addUserSelect());

		this.addButtonsToForm(this.layoutForm);
	}

	private void addFeedbackPanel(final Form<Void> layoutForm) {
		this.feedbackPanel = new NotificationPanel("feedback");
		this.feedbackPanel.setOutputMarkupId(true);
		this.feedbackPanel.setOutputMarkupPlaceholderTag(true);
		layoutForm.add(this.feedbackPanel);
	}

	private Component addEventTypeSelect() {
		this.eventTypeSelect = new ListMultipleChoice<EapEventType>("eventTypeSelect", new Model(
				this.selectedEventTypes), EapEventType.findAll());
		this.eventTypeSelect.setOutputMarkupId(true);
		return this.eventTypeSelect;
	}

	private Component addUserSelect() {
		this.userSelect = new DropDownChoice<EapUser>("userSelect", new PropertyModel<EapUser>(this, "selectedUser"),
				EapUser.findAll());
		this.userSelect.setOutputMarkupId(true);
		return this.userSelect;
	}

	private Component addTimePeriodSelect() {
		this.timePeriodSelect = new DropDownChoice<TimePeriodEnum>("timePeriodSelect", this.selectedTimePeriod,
				ViewConfigurationPanel.TIMEPERIODS);
		this.timePeriodSelect.setOutputMarkupId(true);
		return this.timePeriodSelect;
	}

	private void addButtonsToForm(final Form<Void> layoutForm) {

		final AjaxButton createButton = new AjaxButton("createButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				ViewConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
				boolean error = false;

				if (ViewConfigurationPanel.this.selectedEventTypes.isEmpty()) {
					ViewConfigurationPanel.this.panel.getFeedbackPanel().error("Choose at least one event type!");
					ViewConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(ViewConfigurationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;

				if (ViewConfigurationPanel.this.selectedUser == null) {
					ViewConfigurationPanel.this.panel.getFeedbackPanel().error("Choose a user");
					ViewConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(ViewConfigurationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;

				if (error == false) {
					// create new EventView configuration
					final EventView view = new EventView(ViewConfigurationPanel.this.selectedUser,
							ViewConfigurationPanel.this.selectedEventTypes,
							ViewConfigurationPanel.this.selectedTimePeriod.getObject());
					view.save();
					final EventViewPage visualisation = ViewConfigurationPanel.this.parentPage;
					visualisation.views.detach();
					target.add(visualisation.listview.getParent());
					// close this Panel
					visualisation.addViewModal.close(target);
				}
				;
			};
		};

		layoutForm.add(createButton);
	}

	public NotificationPanel getFeedbackPanel() {
		return this.feedbackPanel;
	}

}