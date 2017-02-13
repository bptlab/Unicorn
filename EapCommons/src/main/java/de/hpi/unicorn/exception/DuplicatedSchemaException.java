/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.exception;

public class DuplicatedSchemaException extends Exception {

	public DuplicatedSchemaException(final String schemaName) {
		super(String.format("Duplicated schema name %s. Already on the server.", schemaName));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1695521740939760047L;

}
