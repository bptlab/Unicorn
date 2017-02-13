/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for various patterns of well-structured BPMN components.
 * 
 * @author micha
 */
public enum IPattern {

	AND("AND"), XOR("XOR"), SEQUENCE("SEQUENCE"), LOOP("LOOP"), SUBPROCESS("SUBPROCESS");

	public String value;

	private IPattern(final String value) {
		this.value = value;
	}

	public static boolean contains(final String value) {
		for (final IPattern pattern : IPattern.values()) {
			if (pattern.value.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getValues() {
		final List<String> values = new ArrayList<String>();
		for (final IPattern pattern : IPattern.values()) {
			values.add(pattern.value);
		}
		return values;
	}

}
