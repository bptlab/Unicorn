/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for various patterns of well-structured BPMN components.
 * 
 * @author micha
 */
public enum PatternQueryType {

	AND("AND"), XOR("XOR"), SEQUENCE("SEQUENCE"), LOOP("LOOP"), SUBPROCESS("SUBPROCESS"), TIMER("TIMER"), STATETRANSITION(
			"STATETRANSITION");

	public String value;

	private PatternQueryType(final String value) {
		this.value = value;
	}

	public static boolean contains(final String value) {
		for (final PatternQueryType pattern : PatternQueryType.values()) {
			if (pattern.value.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getValues() {
		final List<String> values = new ArrayList<String>();
		for (final PatternQueryType pattern : PatternQueryType.values()) {
			values.add(pattern.value);
		}
		return values;
	}

}
