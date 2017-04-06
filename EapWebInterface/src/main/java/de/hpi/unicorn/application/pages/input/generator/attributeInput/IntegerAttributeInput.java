/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class IntegerAttributeInput extends AttributeInput {

	public IntegerAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
	}

	/**
	 * Select random Float value from the input.
	 *
	 * @return a float
	 */
	@Override
	public Float getRandomValue() {
		String[] possibleValues = this.getInput().split(";");
		return Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
	}
}
