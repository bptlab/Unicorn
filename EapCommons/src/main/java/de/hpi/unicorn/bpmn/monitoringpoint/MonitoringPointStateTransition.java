/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.monitoringpoint;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * This enum specifies the supported state transition types of an BPMN element,
 * which can be monitored with PatternQueries.
 * 
 * @author micha
 */
public enum MonitoringPointStateTransition {

	@Enumerated(EnumType.STRING)
	enable("enable"), begin("begin"), terminate("terminate"), skip("skip"), initialize("initialize"), disrupt("disrupt");

	private String name;

	MonitoringPointStateTransition(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
