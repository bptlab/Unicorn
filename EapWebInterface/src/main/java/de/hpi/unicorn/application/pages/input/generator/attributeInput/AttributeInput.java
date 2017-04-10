/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public abstract class AttributeInput implements Serializable {
	private final TypeTreeNode attribute;
	private String input = "";
	static Random random = new Random();
	static final Logger logger = Logger.getLogger(AttributeInput.class);

	AttributeInput(TypeTreeNode attribute) {
		this.attribute = attribute;
	}

	public static AttributeInput attributeInputFactory(TypeTreeNode attribute) {
		switch (attribute.getType()) {
			case STRING:
				return new StringAttributeInput(attribute);
			case INTEGER:
				return new IntegerAttributeInput(attribute);
			case FLOAT:
				return new FloatAttributeInput(attribute);
			case DATE:
				return new DateAttributeInput(attribute);
			default:
				attribute.setType(AttributeTypeEnum.STRING);
				return new StringAttributeInput(attribute);
		}
	}

	public TypeTreeNode getAttribute() {
		return attribute;
	}

	public AttributeTypeEnum getAttributeType() {
		return this.getAttribute().getType();
	}

	public String getAttributeName() {
		return this.getAttribute().getName();
	}

	public String getInput() {
		return input;
	}

	String getInputOrDefault() {
		if(input != null && !input.isEmpty()) {
			return input;
		}
		return this.getDefaultInput();
	}

	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * Returns the chosen value from user input. If no value was chosen up to now, this will be done first.
	 *
	 * @return a serializable object containing a single value chosen from user input
	 */
	public Serializable getCalculatedValue() {
		if (this.getValue() == null) {
			this.calculateRandomValue();
		}
		return this.getValue();
	}

	/**
	 * Chooses a value from the user input and saves it in the value-instance variable.
	 *
	 */
	public abstract void calculateRandomValue();

	/**
	 * Abstract getter for the value instance variable defined in subclasses.
	 * @return a serializable object
	 */
	abstract Serializable getValue();

	/**
	 * Returns the chosen value as string instead of serializable object.
	 * It will execute the calculation of a value in case this wasn't done until now.
	 *
	 * @return a string
	 */
	public String getValueAsString() {
		return String.valueOf(this.getCalculatedValue());
	}

	String getDefaultInput() { return "UNDEFINED"; }

	/**
	 * Checks whether the user input is contained in a given range.
	 * Might get overridden in subclass.
	 *
	 * @param range to be searched in
	 * @return bool if string is in range
	 */
	public boolean isInRange(String range) {
		String[] possibleValues = range.split(";");
		for (String possibleValue : possibleValues) {
			if (possibleValue.equals(this.getValueAsString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder stringify = new StringBuilder();
		stringify.append(this.getClass() + System.getProperty("line.separator"));
		stringify.append("Current input: " + this.getInput() + System.getProperty("line.separator"));
		stringify.append("Default input: " + this.getDefaultInput() + System.getProperty("line.separator"));
		stringify.append("Input used: " + this.getInputOrDefault() + System.getProperty("line.separator"));
		stringify.append("Current value: " + this.getValueAsString());
		return stringify.toString();
	}

	public static int getRandomIndex(Object[] inputArray) {
		return random.nextInt(inputArray.length);
	}
	public static int getRandomIndex(List inputList) { return random.nextInt(inputList.size()); }
}
