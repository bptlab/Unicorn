/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

/**
 * @author micha
 */
public enum EsperPatternOperators {

	AND("AND"), XOR("OR"), LOOP("UNTIL"), SEQUENCE("->");

	public String operator;

	EsperPatternOperators(final String operator) {
		this.operator = operator;
	}

}
