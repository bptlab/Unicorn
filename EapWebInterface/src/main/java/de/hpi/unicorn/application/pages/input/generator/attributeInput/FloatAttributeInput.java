/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class FloatAttributeInput extends AttributeInput {

	private Float value;

	public FloatAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
		defaultInput = "1.1;1.2;2.0;2.5";
	}

	/**
	 * Select random Float value from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String[] possibleValues = this.getInput().split(";");
		this.value = Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
	}

	Float getValue() {
		return this.value;
	}
}
