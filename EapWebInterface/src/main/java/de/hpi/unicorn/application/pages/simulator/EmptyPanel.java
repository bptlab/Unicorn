/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;

/**
 * This panel is intended as a spacer in tables to let them render larger.
 */
public class EmptyPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public EmptyPanel(final String id, final int entryId, final AbstractDataProvider dataProvider) {
		super(id);
	}

}
