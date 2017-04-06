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

import java.io.Serializable;
import java.util.List;
import java.util.Random;

abstract class AttributeInput {
	private final TypeTreeNode attribute;
	private String input;
	static Random random = new Random();
	static final Logger logger = Logger.getLogger(AttributeInput.class);

	AttributeInput(TypeTreeNode inputAttribute) {
		attribute = inputAttribute;
	}

	public TypeTreeNode getAttribute() {
		return attribute;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	abstract Serializable getRandomValue();

	/**
	 * Checks whether the user input is contained in a given range.
	 * Might get overridden in subclass.
	 *
	 * @param range to be searched in
	 * @return bool if string is in range
	 */
	boolean isInRange(String range) {
		String[] possibleValues = range.split(";");
		for (String possibleValue : possibleValues) {
			if (possibleValue.equals(this.getInput())) {
				return true;
			}
		}
		return false;
	}

	static int getRandomIndex(Object[] inputArray) {
		return random.nextInt(inputArray.length);
	}
	static int getRandomIndex(List inputList) { return random.nextInt(inputList.size()); }
}
