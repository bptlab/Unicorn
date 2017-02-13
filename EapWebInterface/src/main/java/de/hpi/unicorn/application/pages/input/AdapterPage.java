/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

import de.hpi.unicorn.adapter.AdapterManager;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;

public class AdapterPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public AdapterPage() {
		super();

		final WarnOnExitForm form = new WarnOnExitForm("form") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
			}
		};
		form.add(new AjaxButton("start") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				AdapterManager.getInstance().startNokiaHereAdapterForDemoRoute();
				AdapterPage.this.getFeedbackPanel().success("Adapter started!");
				target.add(AdapterPage.this.getFeedbackPanel());
			}
		});

		form.add(new AjaxButton("stop") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				AdapterManager.getInstance().stopAndRemoveNokiaHereAdapterForDemoRoute();
				AdapterPage.this.getFeedbackPanel().success("Adapter stopped!");
				target.add(AdapterPage.this.getFeedbackPanel());
			}
		});
		this.add(form);

	}
}
