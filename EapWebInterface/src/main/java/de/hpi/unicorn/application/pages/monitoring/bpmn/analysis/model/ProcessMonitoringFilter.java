/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.model;

import de.hpi.unicorn.application.pages.eventrepository.model.AbstractFilter;
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;

/**
 * This class filters {@link ProcessMonitor}s.
 * 
 * @author micha
 */
public class ProcessMonitoringFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for the class, which filters {@link ProcessMonitor}s.
	 */
	public ProcessMonitoringFilter() {
		super();
	}

	/**
	 * Constructor for the class, which filters {@link ProcessMonitor}s.
	 */
	public ProcessMonitoringFilter(final String processMonitorFilterCriteria,
			final String processMonitorFilterCondition, final String filterValue) {
		super(processMonitorFilterCriteria, processMonitorFilterCondition, filterValue);
	}

	public boolean match(final ProcessMonitor processMonitor) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (processMonitor.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (processMonitor.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (processMonitor.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		}
		return false;
	}
}
