/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring;

import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.form.BootStrapLabel;
import de.hpi.unicorn.application.components.form.BootStrapTextEmphasisClass;
import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;

/**
 * This panel contains a label for displaying the status of a
 * {@link ProcessInstanceMonitor}.
 */
public class ProcessInstanceMonitoringStatusPanel extends Panel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a panel, which contains a label for displaying the status
	 * of a {@link ProcessInstanceMonitor}.
	 * 
	 * @param id
	 * @param entryId
	 * @param dataprovider
	 */
	public ProcessInstanceMonitoringStatusPanel(final String id, final int entryId,
			final AbstractDataProvider dataprovider) {
		super(id);
		final ProcessInstanceMonitor processInstanceMonitor = (ProcessInstanceMonitor) dataprovider.getEntry(entryId);
		BootStrapTextEmphasisClass textEmphasisClass = BootStrapTextEmphasisClass.Muted;

		switch (processInstanceMonitor.getStatus()) {
		case Aborted:
			textEmphasisClass = BootStrapTextEmphasisClass.Error;
			break;
		case Finished:
			textEmphasisClass = BootStrapTextEmphasisClass.Success;
			break;
		case NotExisting:
			textEmphasisClass = BootStrapTextEmphasisClass.Muted;
			break;
		case Running:
			textEmphasisClass = BootStrapTextEmphasisClass.Info;
			break;
		default:
			break;

		}
		this.add(new BootStrapLabel("label", processInstanceMonitor.getStatus().name(), textEmphasisClass));
	}

}
