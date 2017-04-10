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

public class FloatAttributeInput extends AttributeInput {

	private Float value;

	static final Logger logger = Logger.getLogger(FloatAttributeInput.class);

	public FloatAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
	}

	/**
	 * Select random Float value from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String[] possibleValues = this.getInputOrDefault().split(";");
		this.value = Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
	}

	Float getValue() {
		return this.value;
	}

	String getDefaultInput() { return "1.1;1.2;2.0;2.5"; }
}
