/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.form;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * This form provides an ajax behavior to block the UI while processing a
 * request of a {@link BlockingAjaxButton}, which is placed in this form.
 */
public class BlockingForm extends Form<Void> {

	private static final long serialVersionUID = 1L;
	private final JavaScriptResourceReference blockPageJS = new JavaScriptResourceReference(WarnOnExitForm.class,
			"blockUI.js");

	public BlockingForm(final String id) {
		super(id);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BlockingForm(final String id, final IModel model) {
		super(id, model);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(this.blockPageJS));
	}

}