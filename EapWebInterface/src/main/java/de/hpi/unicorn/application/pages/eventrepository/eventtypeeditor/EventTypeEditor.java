/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;

/**
 * This page contains the {@link NewEventTypeEditor} and
 * {@link ExistingEventTypeEditor} as tabs.
 * 
 * @author micha
 */
public class EventTypeEditor extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private AbstractEapPage eventTypeEditor;

	/**
	 * Constructor for the EventTypeEditor page, which contains the
	 * {@link NewEventTypeEditor} and {@link ExistingEventTypeEditor} as tabs.
	 */
	public EventTypeEditor() {
		super();
		this.eventTypeEditor = this;

		final List<ITab> tabs = new ArrayList<ITab>();

		tabs.add(new AbstractTab(new Model<String>("Create new event type")) {

			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(final String panelId) {
				return new NewEventTypeEditor(panelId, EventTypeEditor.this.eventTypeEditor);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Create from existing event type")) {

			private static final long serialVersionUID = 1L;

			@Override
			public Panel getPanel(final String panelId) {
				return new ExistingEventTypeEditor(panelId, EventTypeEditor.this.eventTypeEditor);
			}
		});

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));
	}

}
