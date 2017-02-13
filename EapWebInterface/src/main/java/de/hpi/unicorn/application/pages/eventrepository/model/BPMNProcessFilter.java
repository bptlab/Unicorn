/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

import de.hpi.unicorn.application.pages.eventrepository.BPMNProcessPanel;
import de.hpi.unicorn.bpmn.element.BPMNProcess;

/**
 * This class filters {@link BPMNProcess} in the {@link BPMNProcessPanel}.
 * 
 * @author micha
 */
public class BPMNProcessFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public BPMNProcessFilter() {
		super();
	}

	/**
	 * Constructor for the class, which filters {@link BPMNProcess} in the
	 * {@link BPMNProcessPanel}.
	 * 
	 * @param processFilterCriteria
	 * @param processFilterCondition
	 * @param filterValue
	 */
	public BPMNProcessFilter(final String processFilterCriteria, final String processFilterCondition,
			final String filterValue) {
		super(processFilterCriteria, processFilterCondition, filterValue);
	}

	public boolean match(final BPMNProcess process) {
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
		}
		return false;
	}
}
