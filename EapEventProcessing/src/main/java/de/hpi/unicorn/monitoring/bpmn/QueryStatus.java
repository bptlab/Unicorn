/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

/**
 * @author micha
 */
public enum QueryStatus {
	Started("Started"), Finished("Finished"), Skipped("Skipped"), NotExisting("Not Existing"), Aborted("Aborted");

	private String textValue;

	private QueryStatus(final String text) {
		this.textValue = text;
	}

	public String getTextValue() {
		return this.textValue;
	}
}
