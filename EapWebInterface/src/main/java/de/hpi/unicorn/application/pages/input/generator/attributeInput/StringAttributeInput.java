/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class StringAttributeInput extends AttributeInput {

	private String value;

	public StringAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
		defaultInput = "String1;String2;String3";
	}

	/**
	 * Select random String from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String[] possibleValues = this.getInput().split(";");
		this.value = possibleValues[getRandomIndex(possibleValues)];
	}

	String getValue() {
		return this.value;
	}
}
