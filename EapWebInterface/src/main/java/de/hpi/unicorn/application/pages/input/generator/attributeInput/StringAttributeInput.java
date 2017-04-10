/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;

public class StringAttributeInput extends AttributeInput {

	private String value;

	static final Logger logger = Logger.getLogger(StringAttributeInput.class);

	public StringAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
	}

	/**
	 * Select random String from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String[] possibleValues = this.getInputOrDefault().split(";");
		this.value = possibleValues[getRandomIndex(possibleValues)];
	}

	String getValue() {
		return this.value;
	}

	String getDefaultInput() { return "String1;String2;String3"; }
}
