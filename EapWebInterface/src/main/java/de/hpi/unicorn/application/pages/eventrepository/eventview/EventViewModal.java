/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.eventview;

import de.hpi.unicorn.application.components.form.BootstrapModal;
import de.hpi.unicorn.event.EapEvent;

/**
 * This modal contains a panel that displays a single event with its attributes.
 */
public class EventViewModal extends BootstrapModal {

	private EventViewPanel panel;

	public EventViewModal(final String id, final EapEvent event) {
		super(id, "Event View");
		this.panel = new EventViewPanel("eventViewPanel", event);
		this.panel.setOutputMarkupId(true);
		this.add(this.panel);
	}

	public EventViewModal(final String id) {
		super(id, "Event View");
		this.panel = new EventViewPanel("eventViewPanel");
		this.panel.setOutputMarkupId(true);
		this.add(this.panel);
	}

	public EventViewPanel getPanel() {
		return this.panel;
	}

	public void setPanel(final EventViewPanel panel) {
		this.panel = panel;
	}

}
