/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.ProcessAnalysingModal;
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;

/**
 * This is a button within a form, which shows on an ajax submit of the button
 * the {@link ProcessAnalysingModal}.
 * 
 * @author micha
 */
public class ProcessMonitorEntryDetailsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final ProcessAnalysingModal processMonitorModal;

	/**
	 * Constructor for a form, which contains a button. The button shows on an
	 * ajax submit the {@link ProcessAnalysingModal}.
	 * 
	 * @param id
	 * @param entryId
	 * @param dataprovider
	 * @param modal
	 */
	public ProcessMonitorEntryDetailsPanel(final String id, final int entryId, final AbstractDataProvider dataprovider,
			final ProcessAnalysingModal modal) {
		super(id);
		this.processMonitorModal = modal;
		final Form<Void> form = new Form<Void>("form");

		final AjaxButton detailsButton = new AjaxButton("detailsButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final ProcessMonitor processMonitor = (ProcessMonitor) dataprovider.getEntry(entryId);
				ProcessMonitorEntryDetailsPanel.this.processMonitorModal.setProcessMonitor(processMonitor, target);
				ProcessMonitorEntryDetailsPanel.this.processMonitorModal.show(target);
			}
		};

		form.add(detailsButton);

		this.add(form);
	}
}
