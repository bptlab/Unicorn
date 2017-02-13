/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.eventrepository.eventview.EventViewModal;
import de.hpi.unicorn.configuration.EapConfiguration;

/**
 * The event repository gives information about events, event types, processes
 * and process instances, stored in the application.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class EventRepository extends AbstractEapPage {

	private EventRepository eventRepository;
	private EventViewModal eventViewModal;

	/**
	 * Constructor for the {@link EventRepository}. Initializes and adds the
	 * {@link EventPanel}, {@link EventTypePanel}, {@link ProcessPanel},
	 * {@link ProcessInstancePanel} and {@link BPMNProcessPanel} .
	 */
	public EventRepository() {
		super();
		this.eventRepository = this;

		if (!EapConfiguration.persistEvents) {
			EventRepository.this.getFeedbackPanel().info("Storage of new incoming events has been disabled.");
		}

		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Event")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new EventPanel(panelId, EventRepository.this.eventRepository);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Event Type")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new EventTypePanel(panelId, EventRepository.this.eventRepository);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Process")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new ProcessPanel(panelId, EventRepository.this.eventRepository);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Process Instance")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new ProcessInstancePanel(panelId, EventRepository.this.eventRepository);
			}
		});

		// TODO: Remove also in backend
		// tabs.add(new AbstractTab(new Model<String>("BPMN")) {
		// @Override
		// public Panel getPanel(String panelId) {
		// return new BPMNProcessPanel(panelId, eventRepository);
		// }
		// });

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));

		// add modal
		this.eventViewModal = new EventViewModal("eventViewModal");
		this.eventViewModal.setOutputMarkupId(true);
		this.add(this.eventViewModal);

	}

	public EventViewModal getEventViewModal() {
		return this.eventViewModal;
	}

	public void setEventViewModal(final EventViewModal eventViewModal) {
		this.eventViewModal = eventViewModal;
	}

}
