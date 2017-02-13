/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.exception;

public class UnparsableException extends Exception {

	public enum ParseType {
		EVENT("event"), EVENT_TYPE("event type");

		private String typeName;

		private ParseType(final String typeName) {
			this.typeName = typeName;
		}

		public String getTypeName() {
			return this.typeName;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7905491060252837546L;

	public UnparsableException(final ParseType type) {
		super(String.format("Cannot parse %s xml string.", type.getTypeName()));
	}

}
