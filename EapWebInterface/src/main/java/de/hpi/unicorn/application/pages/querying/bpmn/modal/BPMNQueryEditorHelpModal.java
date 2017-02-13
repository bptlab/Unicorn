/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn.modal;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import de.hpi.unicorn.application.components.form.BootstrapModal;

/**
 * This is a {@link BootstrapModal}, which displays a help text for BPMN query
 * creation.
 * 
 * @author micha
 */
public class BPMNQueryEditorHelpModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private static final ResourceReference MODAL_SIZE_CSS = new PackageResourceReference(BootstrapModal.class,
			"modal_size.css");

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BPMNQueryEditorHelpModal.MODAL_SIZE_CSS));
	}

	/**
	 * Constructor for a {@link BootstrapModal}, which displays a help text for
	 * BPMN query creation.
	 * 
	 * @param id
	 */
	public BPMNQueryEditorHelpModal(final String id) {
		super(id, "Help: BPMN Query Editor");
	}
}
