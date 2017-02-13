/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.eventviews;

import de.hpi.unicorn.application.components.form.BootstrapModal;

/**
 * This modal contains the @see ViewConfigurationPanel to create new event
 * views.
 */
public class AddViewModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private final ViewConfigurationPanel panel;

	/**
	 * 
	 * @param visualisation
	 * @param window
	 */
	public AddViewModal(final String id, final EventViewPage visualisation) {
		super(id, "Add View");
		this.panel = new ViewConfigurationPanel("viewConfigurationPanel", visualisation);
		this.add(this.panel);
	}
}
