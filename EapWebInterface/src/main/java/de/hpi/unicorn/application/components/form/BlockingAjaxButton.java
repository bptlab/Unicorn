/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.form;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

/**
 * This button provides an ajax behavior to block the UI while processing a
 * request. The {@link BlockingAjaxButton} must be placed in a
 * {@link BlockingForm}.
 */
public class BlockingAjaxButton extends AjaxButton {

	private static final long serialVersionUID = 1L;

	public BlockingAjaxButton(final String id, final Form<?> form) {
		super(id, form);
		this.addOnClickBehavior();
	}

	public BlockingAjaxButton(final String id, final Model<String> model) {
		super(id, model);
		this.addOnClickBehavior();

	}

	private void addOnClickBehavior() {

		this.add(new AjaxEventBehavior("onclick") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(final AjaxRequestTarget target) {
				target.appendJavaScript("$.blockUI();");
			}
		});
	}

	@Override
	public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
		super.onSubmit(target, form);
		target.appendJavaScript("$.unblockUI();");
	}

}