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
	 * Select random int value from the input.
	 *
	 * @return an integer
	 */
	@Override
	public Integer getRandomValue() {
		String userInput = this.getInput();
		if (userInput.contains("-")) {
			int start = Integer.parseInt(userInput.split("-")[0]);
			int end = Integer.parseInt(userInput.split("-")[1]);
			return random.nextInt(end - start + 1) + start;
		}
		else {
			String[] possibleValues = userInput.split(";");
			return Integer.parseInt(possibleValues[getRandomIndex(possibleValues)]);
		}
	}
}
