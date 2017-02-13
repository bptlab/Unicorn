/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying;

import org.apache.wicket.markup.html.basic.MultiLineLabel;

import de.hpi.unicorn.application.components.form.BootstrapModal;

/**
 * This class is a Modal that displays the help-text for query creation.
 */
public class QueryEditorHelpModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;

	public QueryEditorHelpModal(final String id, final String helpText) {
		super(id, "Help: Query Editor");
		this.add(new MultiLineLabel("helpTextLabel", helpText));
	}
}
