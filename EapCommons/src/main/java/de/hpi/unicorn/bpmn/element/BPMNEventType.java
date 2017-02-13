/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.element;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * This enum contains the event types of an BPMN event.
 * 
 * @author micha
 */
public enum BPMNEventType {

	@Enumerated(EnumType.STRING)
	Blank, Timer, Compensation, Cancel, Error, Message, Link, Signal;

}
