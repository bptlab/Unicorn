/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.event.EapEventType;

public class UnexpectedEventPanel extends Panel {

	private final SimulationPanel simulationPanel;
	private final List<String> effectList = new ArrayList<String>(Arrays.asList("Delay", "Cancel", "None"));
	private final List<EapEventType> listOfAllEventTypes = EapEventType.findAll();
	private final List<EapEventType> usedEventTypes;
	private final WebMarkupContainer unexpectedEventMarkupContainer;
	private final List<Object> unexpectedEvents;
	private final ListView<Object> unexpectedEventsListView;

	public UnexpectedEventPanel(final String id, final SimulationPanel simulationPanel) {
		super(id);
		this.simulationPanel = simulationPanel;
		this.setOutputMarkupId(true);
		this.usedEventTypes = new ArrayList<EapEventType>();
		this.unexpectedEvents = new ArrayList<Object>();

		final Form<Void> form = new Form<Void>("form");

		final AjaxButton addButton = new AjaxButton("addButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				UnexpectedEventPanel.this.unexpectedEvents.add(new Object());
				target.add(UnexpectedEventPanel.this.unexpectedEventMarkupContainer);
			}
		};
		form.add(addButton);

		this.unexpectedEventsListView = new ListView<Object>("unexpectedEventListView", this.unexpectedEvents) {

			@Override
			protected void populateItem(final ListItem<Object> item) {

				final TextField<String> probabilityInput = new TextField<String>("probabilityInput",
						new Model<String>());
				item.add(probabilityInput);

				final DropDownChoice<EapEventType> previousEventTypeSelect = new DropDownChoice<EapEventType>(
						"previousEventTypeSelect", new Model<EapEventType>(), UnexpectedEventPanel.this.usedEventTypes);
				item.add(previousEventTypeSelect);

				final DropDownChoice<String> effectSelect = new DropDownChoice<String>("effectSelect",
						new Model<String>(), UnexpectedEventPanel.this.effectList);
				item.add(effectSelect);

				final DropDownChoice<EapEventType> additionalEventTypeSelect = new DropDownChoice<EapEventType>(
						"additionalEventTypeSelect", new Model<EapEventType>(),
						UnexpectedEventPanel.this.listOfAllEventTypes);
				item.add(additionalEventTypeSelect);
			}

		};
		this.unexpectedEventMarkupContainer = new WebMarkupContainer("unexpectedEventMarkupContainer");
		this.unexpectedEventMarkupContainer.add(this.unexpectedEventsListView);
		this.unexpectedEventMarkupContainer.setOutputMarkupId(true);
		form.addOrReplace(this.unexpectedEventMarkupContainer);
		this.add(form);
	}

	public void refreshUsedEventTypes() {
		this.usedEventTypes.addAll(this.simulationPanel.getUsedEventTypes());
	}
}
