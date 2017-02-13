/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Model that wraps the expansion state handler of a tree component.
 */
public class ProcessInstanceMonitoringTreeTableExpansionModel extends
		AbstractReadOnlyModel<Set<ProcessInstanceMonitoringTreeTableElement>> {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("serial")
	private static MetaDataKey<ProcessInstanceMonitoringTreeTableExpansion> KEY = new MetaDataKey<ProcessInstanceMonitoringTreeTableExpansion>() {
	};

	@Override
	public Set<ProcessInstanceMonitoringTreeTableElement> getObject() {
		return ProcessInstanceMonitoringTreeTableExpansion.get();
	}

	public static ProcessInstanceMonitoringTreeTableExpansion get() {
		ProcessInstanceMonitoringTreeTableExpansion expansion = Session.get().getMetaData(
				ProcessInstanceMonitoringTreeTableExpansionModel.KEY);
		if (expansion == null) {
			expansion = new ProcessInstanceMonitoringTreeTableExpansion();
			Session.get().setMetaData(ProcessInstanceMonitoringTreeTableExpansionModel.KEY, expansion);
		}
		return expansion;
	}
}
