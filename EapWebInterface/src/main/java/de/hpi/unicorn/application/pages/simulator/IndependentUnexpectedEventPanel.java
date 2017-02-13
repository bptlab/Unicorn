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

public class IndependentUnexpectedEventPanel extends Panel {

	private final List<String> effectList = new ArrayList<String>(Arrays.asList("Delay", "Cancel", "None"));
	private final List<EapEventType> listOfAllEventTypes = EapEventType.findAll();
	private final WebMarkupContainer unexpectedEventMarkupContainer;
	private final List<Object> unexpectedEvents;
	private final ListView<Object> unexpectedEventsListView;

	public IndependentUnexpectedEventPanel(final String id) {
		super(id);
		this.setOutputMarkupId(true);
		this.unexpectedEvents = new ArrayList<Object>();

		final Form<Void> form = new Form<Void>("form");

		final AjaxButton addButton = new AjaxButton("addButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				IndependentUnexpectedEventPanel.this.unexpectedEvents.add(new Object());
				target.add(IndependentUnexpectedEventPanel.this.unexpectedEventMarkupContainer);
			}
		};
		form.add(addButton);

		this.unexpectedEventsListView = new ListView<Object>("unexpectedEventListView", this.unexpectedEvents) {

			@Override
			protected void populateItem(final ListItem<Object> item) {
				final TextField<String> occurenceInput = new TextField<String>("occurenceInput", new Model<String>());
				item.add(occurenceInput);
				final DropDownChoice<String> effectSelect = new DropDownChoice<String>("effectSelect",
						new Model<String>(), IndependentUnexpectedEventPanel.this.effectList);
				item.add(effectSelect);
				final DropDownChoice<EapEventType> additionalEventTypeSelect = new DropDownChoice<EapEventType>(
						"additionalEventTypeSelect", new Model<EapEventType>(),
						IndependentUnexpectedEventPanel.this.listOfAllEventTypes);
				item.add(additionalEventTypeSelect);
			}

		};
		this.unexpectedEventMarkupContainer = new WebMarkupContainer("unexpectedEventMarkupContainer");
		this.unexpectedEventMarkupContainer.add(this.unexpectedEventsListView);
		this.unexpectedEventMarkupContainer.setOutputMarkupId(true);
		form.addOrReplace(this.unexpectedEventMarkupContainer);
		this.add(form);
	}
}
