/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.modal.ProcessInstanceMonitoringModal;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;

/**
 * This is a button within a form, which shows on an ajax submit of the button
 * the {@link ProcessInstanceMonitoringModal}.
 * 
 * @author micha
 */
public class ProcessInstanceMonitorEntryDetailsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final ProcessInstanceMonitoringModal processInstanceMonitorModal;

	/**
	 * Constructor for a form, which contains a button. The button shows on an
	 * ajax submit the {@link ProcessInstanceMonitoringModal}.
	 * 
	 * @param id
	 * @param entryId
	 * @param dataprovider
	 * @param modal
	 */
	public ProcessInstanceMonitorEntryDetailsPanel(final String id, final int entryId,
			final AbstractDataProvider dataprovider, final ProcessInstanceMonitoringModal modal) {
		super(id);
		this.processInstanceMonitorModal = modal;
		final Form<Void> form = new Form<Void>("form");

		final AjaxButton detailsButton = new AjaxButton("detailsButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final ProcessInstanceMonitor processInstanceMonitor = (ProcessInstanceMonitor) dataprovider
						.getEntry(entryId);
				ProcessInstanceMonitorEntryDetailsPanel.this.processInstanceMonitorModal.setProcessInstanceMonitor(
						processInstanceMonitor, target);
				ProcessInstanceMonitorEntryDetailsPanel.this.processInstanceMonitorModal.show(target);
			}
		};

		form.add(detailsButton);

		this.add(form);
	}
}
