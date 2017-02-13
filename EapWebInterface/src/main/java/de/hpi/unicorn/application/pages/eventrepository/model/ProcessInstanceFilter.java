/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

import java.util.ArrayList;
import java.util.List;

import de.hpi.unicorn.application.pages.eventrepository.ProcessInstancePanel;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This class filters {@link CorrelationProcessInstance}es in the
 * {@link ProcessInstancePanel}.
 * 
 * @author micha
 */
public class ProcessInstanceFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for the class, which filters
	 * {@link CorrelationProcessInstance}s in the {@link ProcessInstancePanel}.
	 */
	public ProcessInstanceFilter() {
		super();
	}

	/**
	 * Constructor for the class, which filters
	 * {@link CorrelationProcessInstance}s in the {@link ProcessInstancePanel}.
	 * 
	 * @param filterCriteria
	 * @param filterCondition
	 * @param filterValue
	 */
	public ProcessInstanceFilter(final String filterCriteria, final String filterCondition, final String filterValue) {
		super(filterCriteria, filterCondition, filterValue);
	}

	public boolean match(final CorrelationProcessInstance processInstance) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (processInstance.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (processInstance.getID() > Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (processInstance.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else if (this.filterCriteria.equals("Process")) {
			final List<CorrelationProcess> filterProcesses = CorrelationProcess.findByName(this.filterValue);
			if (!filterProcesses.isEmpty()) {
				final List<CorrelationProcessInstance> filteredProcessInstances = new ArrayList<CorrelationProcessInstance>();
				for (final CorrelationProcess process : filterProcesses) {
					filteredProcessInstances.addAll(process.getProcessInstances());
				}
				return filteredProcessInstances.contains(processInstance);
			}
		} else if (this.filterCriteria.equals("Process (ID)")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (processInstance.getProcess().getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (processInstance.getProcess().getID() > Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (processInstance.getProcess().getID() == Integer.parseInt(this.filterValue)) {
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
