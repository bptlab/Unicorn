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

	private Integer value;

	public IntegerAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
		defaultInput = "1-50";
	}

	/**
	 * Select random int value from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String userInput = this.getInputOrDefault();
		if (userInput.contains("-")) {
			int start = Integer.parseInt(userInput.split("-")[0]);
			int end = Integer.parseInt(userInput.split("-")[1]);
			this.value = random.nextInt(end - start + 1) + start;
		}
		else {
			String[] possibleValues = userInput.split(";");
			this.value = Integer.parseInt(possibleValues[getRandomIndex(possibleValues)]);
		}
	}

	/**
	 * Implements the "isInRange" function for an integer range.
	 *
	 * @param range to be searched in
	 * @return bool if int is in range
	 */
	@Override
	public boolean isInRange(String range) {
		if (range.contains("-")) {
			int start = Integer.parseInt(range.split("-")[0]);
			int end = Integer.parseInt(range.split("-")[1]);
			return (this.getValue() >= start) && (this.getValue() <= end);
		}
		else {
			return super.isInRange(range);
		}
	}

	Integer getValue() {
		return this.value;
	}
}
