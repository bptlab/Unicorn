/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.simulation;

import java.io.Serializable;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class ValueRule implements Serializable {

	private TypeTreeNode attribute;
	private ValueRuleType ruleType;

	public ValueRule() {
	}

	public TypeTreeNode getAttribute() {
		return this.attribute;
	}

	public void setAttribute(final TypeTreeNode attribute) {
		this.attribute = attribute;
	}

	public ValueRuleType getRuleType() {
		return this.ruleType;
	}

	public void setRuleType(final ValueRuleType ruleType) {
		this.ruleType = ruleType;
	}

}
