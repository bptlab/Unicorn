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

	/**
	 * Constructor for the StringAttributeInput.
	 *
	 * @param inputAttribute the object should be associated with.
	 */
	StringAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
	}

	/**
	 * Select random string from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String[] possibleValues = this.getInputOrDefault().split(";");
		this.value = possibleValues[getRandomIndex(possibleValues)];
	}

	/**
	 * Getter for the computed string-value.
	 *
	 * @return an integer
	 */
	@Override
	String getValue() {
		return this.value;
	}

	/**
	 * Returns the default value for string attributes.
	 *
	 * @return a default value as string
	 */
	@Override
	String getDefaultInput() { return "String1;String2;String3"; }
}
