/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.simulation;

import java.util.Date;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;

public class InstanceIndependetUnexpectedEvent extends PathSimulator {

	public InstanceIndependetUnexpectedEvent(final AbstractBPMNElement startElement,
			final InstanceSimulator parentSimulator, final Date currentSimulationDate) {
		super(startElement, parentSimulator, currentSimulationDate);
	}

}
