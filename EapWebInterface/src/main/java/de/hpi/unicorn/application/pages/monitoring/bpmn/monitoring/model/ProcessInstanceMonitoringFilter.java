/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import de.hpi.unicorn.application.pages.eventrepository.model.AbstractFilter;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;

/**
 * This class filters {@link ProcessInstanceMonitor}s.
 * 
 * @author micha
 */
public class ProcessInstanceMonitoringFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for the class, which filters {@link ProcessInstanceMonitor}s.
	 */
	public ProcessInstanceMonitoringFilter() {
		super();
	}

	public ProcessInstanceMonitoringFilter(final String processInstanceMonitorFilterCriteria,
			final String processInstanceMonitorFilterCondition, final String filterValue) {
		super(processInstanceMonitorFilterCriteria, processInstanceMonitorFilterCondition, filterValue);
	}

	public boolean match(final ProcessInstanceMonitor processInstanceMonitor) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (processInstanceMonitor.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (processInstanceMonitor.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (processInstanceMonitor.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else {
			return false;
		}
		return false;
	}
}
