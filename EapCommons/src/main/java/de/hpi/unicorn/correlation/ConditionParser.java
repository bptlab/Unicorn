/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import java.io.Serializable;

import de.hpi.unicorn.event.collection.TransformationTree;

/**
 * Parses attribute-value pairs of events from a string.
 * 
 * @author micha
 * 
 */
public class ConditionParser {

	/**
	 * Parses attribute-value pairs of events from a string. Syntax of the
	 * string must be: attribute1=attributevalue1[;attribute2=attributevalue2]
	 */
	public static TransformationTree<String, Serializable> extractEventAttributes(final String conditionString) {
		final TransformationTree<String, Serializable> eventAttributes = new TransformationTree<String, Serializable>();
		final String[] attributes = conditionString.split(";");
		for (final String attribute : attributes) {
			final String[] attributePair = attribute.split("=");
			if (attributePair.length == 2) {
				eventAttributes.put(attributePair[0], attributePair[1]);
			}
		}
		return eventAttributes;
	}

}
