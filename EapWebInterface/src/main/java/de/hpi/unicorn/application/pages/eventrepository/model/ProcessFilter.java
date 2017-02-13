/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

import de.hpi.unicorn.application.pages.eventrepository.ProcessPanel;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This class filters {@link CorrelationProcess}es in the {@link ProcessPanel}.
 * 
 * @author micha
 */
public class ProcessFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for the class, which filters {@link CorrelationProcess}es in
	 * the {@link ProcessPanel}.
	 */
	public ProcessFilter() {
		super();
	}

	/**
	 * Constructor for the class, which filters {@link CorrelationProcess}es in
	 * the {@link ProcessPanel}.
	 * 
	 * @param processFilterCriteria
	 * @param processFilterCondition
	 * @param filterValue
	 */
	public ProcessFilter(final String processFilterCriteria, final String processFilterCondition,
			final String filterValue) {
		super(processFilterCriteria, processFilterCondition, filterValue);
	}

	public boolean match(final CorrelationProcess process) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (process.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (process.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (process.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else if (this.filterCriteria.equals("Name")) {
			return (process.getName().equals(this.filterValue));
		} else if (this.filterCriteria.equals("Process Instance")) {
			try {
				final int processInstanceID = Integer.parseInt(this.filterValue);
				boolean match = true;
				if (this.filterCondition.equals("<")) {
					for (final CorrelationProcessInstance instance : process.getProcessInstances()) {
						match = instance.getID() < processInstanceID ? true : false;
					}
					return match;
				} else if (this.filterCondition.equals(">")) {
					for (final CorrelationProcessInstance instance : process.getProcessInstances()) {
						match = instance.getID() > processInstanceID ? true : false;
					}
					return match;
				} else {
					for (final CorrelationProcessInstance instance : process.getProcessInstances()) {
						match = instance.getID() == processInstanceID ? true : false;
					}
					return match;
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else if (this.filterCriteria.equals("Correlation Attribute")) {
			// return
			// CorrelationProcess.getCorrelationAttributesForProcess(process).contains(filterValue);
			return process.getCorrelationAttributes().contains(this.filterValue);
		}
		return false;
	}
}
