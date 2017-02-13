/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.exception;

public class EventTypeNotFoundException extends Exception {

	public EventTypeNotFoundException(final String schemaName) {
		super(String.format("Event type '%s' not found.", schemaName));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -169552174093973647L;

}
