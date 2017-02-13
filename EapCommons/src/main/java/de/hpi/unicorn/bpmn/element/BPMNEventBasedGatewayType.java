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
 * This enum contains the gateway types of an BPMN event-based gateway.
 * 
 * @author micha
 */
public enum BPMNEventBasedGatewayType {

	@Enumerated(EnumType.STRING)
	Exclusive, Parallel;

}
