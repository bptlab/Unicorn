/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.correlation;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.button.TypedAjaxButton;
import de.agilecoders.wicket.markup.html.bootstrap.dialog.Alert;
import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.correlation.Correlator;
import de.hpi.unicorn.process.CorrelationProcess;

class ExistingCorrelationAlert extends Alert {

	private final ExistingCorrelationAlert alert;
	private final CorrelationPage correlationPage;
	private CorrelationProcess selectedProcess;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the wicket component id.
	 * @param correlationPage
	 */
	public ExistingCorrelationAlert(final String id, final String message, final CorrelationPage correlationPage) {
		super(id, Model.of(message), Model.of(""));
		this.type(Alert.Type.Warning);
		this.alert = this;
		this.correlationPage = correlationPage;
	}

	/**
	 * creates a new message component.
	 * 
	 * @param markupId
	 *            The component id
	 * @param message
	 *            The message as {@link IModel}
	 * @return new message component
	 */
	@Override
	protected Component createMessage(final String markupId, final IModel<String> message) {
		final Form container = new Form(markupId);

		container.add(new Label("messageText", new Model<Serializable>(
				"Correlation exists! Do you want to override it?")));
		final BlockingAjaxButton correlateButton = new BlockingAjaxButton("correlateButton", new Model("Correlate")) {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				Correlator.removeExistingCorrelation(ExistingCorrelationAlert.this.selectedProcess);
				ExistingCorrelationAlert.this.correlationPage
						.correlateEvents(ExistingCorrelationAlert.this.selectedProcess);

				ExistingCorrelationAlert.this.alert.setVisible(false);

				target.add(ExistingCorrelationAlert.this.correlationPage.getFeedbackPanel());
				target.add(ExistingCorrelationAlert.this.alert);
			}
		};

		container.add(correlateButton);
		final TypedAjaxButton abortButton = new TypedAjaxButton("abortButton", new Model("Abort"), ButtonType.Primary) {

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				ExistingCorrelationAlert.this.correlationPage.getAlert().setVisible(false);
				target.add(ExistingCorrelationAlert.this.correlationPage);
			}
		};
		container.add(abortButton);
		return container;
	}

	public void setSelectedProcess(final CorrelationProcess selectedProcess) {
		this.selectedProcess = selectedProcess;
	}
}
