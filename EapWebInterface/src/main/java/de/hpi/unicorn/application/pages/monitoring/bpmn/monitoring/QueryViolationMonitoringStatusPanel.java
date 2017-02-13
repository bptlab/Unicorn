/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring;

import java.util.Set;

import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.form.BootStrapLabel;
import de.hpi.unicorn.application.components.form.BootStrapTextEmphasisClass;
import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringTreeTableElement;
import de.hpi.unicorn.monitoring.bpmn.ViolationStatus;
import de.hpi.unicorn.query.PatternQuery;

/**
 * This panel contains a label for displaying the {@link ViolationStatus} of a
 * {@link PatternQuery}.
 */
public class QueryViolationMonitoringStatusPanel extends Panel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a panel, which contains a label for displaying the
	 * {@link ViolationStatus} of a {@link PatternQuery}.
	 * 
	 * @param id
	 * @param entryId
	 * @param dataprovider
	 */
	public QueryViolationMonitoringStatusPanel(final String id, final int entryId,
			final AbstractDataProvider dataprovider) {
		super(id);
		final ProcessInstanceMonitoringTreeTableElement treeTableElement = (ProcessInstanceMonitoringTreeTableElement) dataprovider
				.getEntry(entryId);
		BootStrapTextEmphasisClass textEmphasisClass = BootStrapTextEmphasisClass.Muted;
		final Set<ViolationStatus> violationStatus = treeTableElement.getProcessInstanceMonitor()
				.getViolationStatusForQuery(treeTableElement.getQuery());

		if (violationStatus != null) {
			if (violationStatus.isEmpty()) {
				textEmphasisClass = BootStrapTextEmphasisClass.Success;
				this.add(new BootStrapLabel("label", "No violations", textEmphasisClass));
			} else {
				textEmphasisClass = BootStrapTextEmphasisClass.Error;
				this.add(new BootStrapLabel("label", violationStatus.toString(), textEmphasisClass));
			}
		} else {
			this.add(new BootStrapLabel("label", "", textEmphasisClass));
		}
	}

}
