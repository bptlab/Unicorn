/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration of supported pattern operators.
 */
public enum PatternOperatorEnum {

	EVERY, EVERY_DISTINCT, REPEAT, UNTIL, AND, OR, NOT, FOLLOWED_BY;

	/**
	 * Method to retrieve possible pattern operators for one event type (unary
	 * operators).
	 * 
	 * @return list of unary operators
	 */
	public static List<PatternOperatorEnum> getUnaryOperators() {
		return Arrays.asList(EVERY, EVERY_DISTINCT, REPEAT, NOT);
	}

	/**
	 * Method to retrieve possible pattern operators for two event types (binary
	 * operators).
	 * 
	 * @return list of binary operators
	 */
	public static List<PatternOperatorEnum> getBinaryOperators() {
		return Arrays.asList(UNTIL, AND, OR, FOLLOWED_BY);
	}

}
