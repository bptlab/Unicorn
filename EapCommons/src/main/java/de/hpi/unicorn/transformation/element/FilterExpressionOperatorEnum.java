/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element;

/**
 * Enumeration of supported filter expression operators.
 */
public enum FilterExpressionOperatorEnum {

	EQUALS("="), NOT_EQUALS("!="), SMALLER_THAN("<"), GREATER_THAN(">"), SMALLER_OR_EQUALS("<="), GREATER_OR_EQUALS(
			">="), IN("IN"), NOT_IN("NOT IN");

	private final String value;

	private FilterExpressionOperatorEnum(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
