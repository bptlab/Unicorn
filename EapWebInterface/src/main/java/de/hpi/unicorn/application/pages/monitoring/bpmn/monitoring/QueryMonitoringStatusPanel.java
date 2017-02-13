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
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringTreeTableElement;
import de.hpi.unicorn.monitoring.bpmn.QueryStatus;
import de.hpi.unicorn.query.PatternQuery;

/**
 * This panel contains a label for displaying the {@link QueryStatus} of a
 * {@link PatternQuery}.
 */
public class QueryMonitoringStatusPanel extends Panel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a panel, which contains a label for displaying the
	 * {@link QueryStatus} of a {@link PatternQuery}.
	 * 
	 * @param id
	 * @param entryId
	 * @param dataprovider
	 */
	public QueryMonitoringStatusPanel(final String id, final int entryId, final AbstractDataProvider dataprovider) {
		super(id);
		final ProcessInstanceMonitoringTreeTableElement treeTableElement = (ProcessInstanceMonitoringTreeTableElement) dataprovider
				.getEntry(entryId);
		BootStrapTextEmphasisClass textEmphasisClass = BootStrapTextEmphasisClass.Muted;
		final QueryStatus queryStatus = treeTableElement.getProcessInstanceMonitor().getStatusForQuery(
				treeTableElement.getQuery());

		switch (queryStatus) {
		case Finished:
			textEmphasisClass = BootStrapTextEmphasisClass.Success;
			break;
		case NotExisting:
			textEmphasisClass = BootStrapTextEmphasisClass.Muted;
			break;
		case Skipped:
			textEmphasisClass = BootStrapTextEmphasisClass.Error;
			break;
		case Started:
			textEmphasisClass = BootStrapTextEmphasisClass.Info;
			break;
		default:
			break;
		}
		this.add(new BootStrapLabel("label", queryStatus.getTextValue(), textEmphasisClass));
	}

}
