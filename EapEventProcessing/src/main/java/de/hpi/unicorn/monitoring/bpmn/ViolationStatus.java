/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

/**
 * The status of violation a monitored element can adopt.
 * 
 * @author micha
 */
public enum ViolationStatus {
	Exclusiveness("Exclusiveness"), Order("Order"), Missing("Missing"), Duplication("Duplication"), Loop("Loop");

	private String textValue;

	private ViolationStatus(final String text) {
		this.textValue = text;
	}

	public String getTextValue() {
		return this.textValue;
	}
}
