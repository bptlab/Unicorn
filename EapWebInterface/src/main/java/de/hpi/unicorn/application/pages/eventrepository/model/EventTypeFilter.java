/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

import de.hpi.unicorn.application.pages.eventrepository.EventTypePanel;
import de.hpi.unicorn.event.EapEventType;

/**
 * This class filters {@link EapEventType}s in the {@link EventTypePanel}.
 * 
 * @author micha
 */
public class EventTypeFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public EventTypeFilter() {
		super();
	}

	/**
	 * Constructor for the class, which filters {@link EapEventType}s in the
	 * {@link EventTypePanel}.
	 * 
	 * @param filterCriteria
	 * @param filterCondition
	 * @param filterValue
	 */
	public EventTypeFilter(final String filterCriteria, final String filterCondition, final String filterValue) {
		super(filterCriteria, filterCondition, filterValue);
	}

	public boolean match(final EapEventType eventType) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (eventType.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (eventType.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (eventType.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else if (this.filterCriteria.equals("Name")) {
			if (eventType.getTypeName().equals(this.filterValue)) {
				return true;
			}
		}
		return false;
	}
}
