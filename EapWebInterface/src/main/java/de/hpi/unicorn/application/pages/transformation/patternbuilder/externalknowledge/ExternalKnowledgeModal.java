/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.externalknowledge;

import de.hpi.unicorn.application.components.form.BootstrapModal;

public class ExternalKnowledgeModal extends BootstrapModal {

	private static final long serialVersionUID = -9020117235863750792L;

	private ExternalKnowledgePanel panel;

	public ExternalKnowledgeModal(final String id) {
		super(id, "External Knowledge Usage");
		this.panel = new ExternalKnowledgePanel("externalKnowledgePanel");
		this.panel.setOutputMarkupId(true);
		this.add(this.panel);
	}

	public ExternalKnowledgePanel getPanel() {
		return this.panel;
	}

	public void setPanel(final ExternalKnowledgePanel panel) {
		this.panel = panel;
	}

}
