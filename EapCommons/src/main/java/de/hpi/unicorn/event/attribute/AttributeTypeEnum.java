/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event.attribute;

/**
 * Encapsulates types which we support for attributes
 */
public enum AttributeTypeEnum {

	STRING("String"), INTEGER("Integer"), FLOAT("Float"), DATE("Date");

	private String type;

	AttributeTypeEnum(final String type) {
		this.type = type;
	}

	public String getName() {
		return this.type;
	}

	@Override
	public String toString() {
		// only capitalize the first letter
		final String s = super.toString();
		return s.substring(0, 1) + s.substring(1).toLowerCase();
	}
}
