/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

import de.hpi.unicorn.application.pages.eventrepository.EventPanel;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This class filters {@link EapEvent}s in the {@link EventPanel}.
 * 
 * @author micha
 */
public class EventFilter extends AbstractFilter {

	private static final long serialVersionUID = 1L;

	public EventFilter() {
		super();
	}

	/**
	 * Constructor for the class, which filters {@link EapEvent} in the
	 * {@link EventPanel}.
	 * 
	 * @param filterCriteria
	 * @param filterCondition
	 * @param filterValue
	 */
	public EventFilter(final String filterCriteria, final String filterCondition, final String filterValue) {
		super(filterCriteria, filterCondition, filterValue);
	}

	public boolean match(final EapEvent event) {
		if (this.filterCriteria == null || this.filterCondition == null || this.filterValue == null) {
			return true;
		}
		if (this.filterCriteria.equals("ID")) {
			try {
				if (this.filterCondition.equals("<")) {
					if (event.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else if (this.filterCondition.equals(">")) {
					if (event.getID() < Integer.parseInt(this.filterValue)) {
						return true;
					}
				} else {
					if (event.getID() == Integer.parseInt(this.filterValue)) {
						return true;
					}
				}
			} catch (final NumberFormatException e) {
				return false;
			}
		} else if (this.filterCriteria.equals("Event Type (ID)")) {
			if (this.filterCondition.equals("<")) {
				if (event.getEventType().getID() < Integer.parseInt(this.filterValue)) {
					return true;
				}
			} else if (this.filterCondition.equals(">")) {
				if (event.getEventType().getID() > Integer.parseInt(this.filterValue)) {
					return true;
				}
			} else {
				if (event.getEventType().getID() == Integer.parseInt(this.filterValue)) {
					return true;
				}
			}
			return false;
		} else if (this.filterCriteria.equals("Process Instance")) {
			if (this.filterCondition.equals("<")) {
				for (final CorrelationProcessInstance processInstance : event.getProcessInstances()) {
					if (!(processInstance.getID() < Integer.parseInt(this.filterValue))) {
						return false;
					}
				}
				return true;
			} else if (this.filterCondition.equals(">")) {
				for (final CorrelationProcessInstance processInstance : event.getProcessInstances()) {
					if (!(processInstance.getID() > Integer.parseInt(this.filterValue))) {
						return false;
					}
				}
				return true;
			} else {
				for (final CorrelationProcessInstance processInstance : event.getProcessInstances()) {
					if (!(processInstance.getID() == Integer.parseInt(this.filterValue))) {
						return false;
					}
				}
				return true;
			}
		} else if (EapEvent.findAllEventAttributes().contains(this.filterCriteria)) {
			return EapEvent.findByValue(this.filterCriteria, this.filterValue).contains(event);
		} else {
			return false;
		}
		return false;
	}
}
