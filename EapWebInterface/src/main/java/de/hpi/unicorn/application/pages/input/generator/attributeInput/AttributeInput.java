/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.application.pages.input.generator.validation.AttributeValidator;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;
import org.apache.wicket.validation.IValidator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Abstract class handling user input for the event generator.
 * Each instance represents the input for one attribute.
 * Depending on this input, a "random" value can be generated,
 * used while creating new events in the {@link de.hpi.unicorn.application.pages.input.generator.EventGenerator}.
 * The type of the attribute defines which subclass will be used (see {@link AttributeInput#attributeInputFactory(TypeTreeNode)}.
 *
 */
public abstract class AttributeInput implements Serializable {
	private final TypeTreeNode attribute;
	private String input = "";
	private static Random random = new Random();

	private final List<ProbabilityDistributionEnum> availableMethods = new ArrayList<>();

	private ProbabilityDistributionEnum selectedMethod;

	static final Logger logger = Logger.getLogger(AttributeInput.class);

	/**
	 * 'Super'-constructor for AttributeInput.
	 * Associates the given attribute with the instance.
	 *
	 * @param attribute the object should be associated with.
	 */
	AttributeInput(TypeTreeNode attribute) {
		this.attribute = attribute;
	}

	/**
	 * Returns an AttributeInput-Object fitting to the attribute type.
	 * Gets initialized with empty input.
	 *
	 * @param attribute the AttributeInput should be chosen for
	 * @return a subclass of AttributeInput
	 */
	public static AttributeInput attributeInputFactory(TypeTreeNode attribute) {
		return attributeInputFactory(attribute, "");
	}

	/**
	 * Returns an AttributeInput-Object fitting to the attribute type.
	 * Gets initialized with the given input.
	 *
	 * @param attribute the AttributeInput should be chosen for
	 * @param initialInput will be set in AttributeInput object
	 * @return a subclass of AttributeInput
	 */
	public static AttributeInput attributeInputFactory(TypeTreeNode attribute, String initialInput) {
		if (attribute.getType() == null) {
			attribute.setType(AttributeTypeEnum.STRING);
		}
		AttributeInput newAttributeInput;
		switch (attribute.getType()) {
			case STRING:
				newAttributeInput = new StringAttributeInput(attribute);
				break;
			case INTEGER:
				newAttributeInput = new IntegerAttributeInput(attribute);
				break;
			case FLOAT:
				newAttributeInput = new FloatAttributeInput(attribute);
				break;
			case DATE:
				newAttributeInput = new DateAttributeInput(attribute);
				break;
			default:
				newAttributeInput = new StringAttributeInput(attribute);
				break;
		}
		newAttributeInput.setInput(initialInput);
		return newAttributeInput;
	}

	/**
	 * Getter for the attribute associated to the AttributeInput-Instance.
	 *
	 * @return an attribute
	 */
	public TypeTreeNode getAttribute() {
		return attribute;
	}

	/**
	 * Get the type of the attribute associated to the AttributeInput-Instance.
	 *
	 * @return the type of the attribute
	 */
	public AttributeTypeEnum getAttributeType() {
		return this.getAttribute().getType();
	}

	/**
	 * Get the name of the attribute associated to the AttributeInput-Instance.
	 *
	 * @return a String containing the name
	 */
	public String getAttributeName() {
		return this.getAttribute().getName();
	}

	/**
	 * Returns a list containing all (probability) methods supported by the instance.
	 * This differs from e.g. StringAttributeInput to IntegerAttributeInput.
	 *
	 * @return a list with all supported methods (from ProbabilityDistributionEnum)
	 */
	public List<ProbabilityDistributionEnum> getAvailableMethods() {
		return availableMethods;
	}

	/**
	 * Adds a method to the available methods of the object.
	 *
	 * @param method to be marked as available
	 */
	void addAvailableMethod(ProbabilityDistributionEnum method) {
		availableMethods.add(method);
	}

	/**
	 * Returns the currently selected method for the computation of a value.
	 *
	 * @return the by the user selected method
	 */
	public ProbabilityDistributionEnum getSelectedMethod() {
		return selectedMethod;
	}

	/**
	 * Setter for the currently selected method.
	 *
	 * @param selectedMethod that should be set
	 */
	public void setSelectedMethod(ProbabilityDistributionEnum selectedMethod) {
		this.selectedMethod = selectedMethod;
	}

	/**
	 * Getter for the current input.
	 *
	 * @return the current input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Returns the current input, in case this is empty, a default value defined by the class is returned.
	 *
	 * @return a string containing the current input or the default input
	 */
	String getInputOrDefault() {
		if (input != null && !input.isEmpty()) {
			return input;
		}
		return this.getDefaultInput();
	}

	/**
	 * Setter for the input.
	 *
	 * @param input to be set
	 */
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
	 * Will be overridden in subclasses.
	 *
	 * @return a serializable object
	 */
	abstract Serializable getValue();

	/**
	 * Returns the chosen value as string instead of serializable object.
	 * It will execute the calculation of a value in case this wasn't done until now.
	 * Might be overriden in case the parsing to string isn't this easy (e.g. in DateAttributeInput).
	 *
	 * @return a string
	 */
	public String getValueAsString() {
		return String.valueOf(this.getCalculatedValue());
	}

	/**
	 * Returns the default value, that should be used if the user input is empty.
	 * Will be overridden in subclasses.
	 *
	 * @return a default value as string
	 */
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

	/**
	 * Returns the object as a string containing the most important information concerning the instance.
	 *
	 * @return a string describing the AttributeInput-Object
	 */
	@Override
	public String toString() {
		String lineSeparator = System.getProperty("line.separator");
		StringBuilder stringify = new StringBuilder(lineSeparator);
		stringify.append(this.getClass() + lineSeparator);
		stringify.append("Current input: " + this.getInput() + lineSeparator);
		stringify.append("Default input: " + this.getDefaultInput() + lineSeparator);
		stringify.append("Input used: " + this.getInputOrDefault() + lineSeparator);
		stringify.append("Current value: " + this.getValueAsString());
		return stringify.toString();
	}

	public JSONObject toJson() {
		try {
			JSONObject attributeInputJson = new JSONObject();
			JSONObject attributeJson = new JSONObject();
			attributeJson.put("name", this.getAttributeName());
			attributeJson.put("type", this.getAttributeType());
			attributeInputJson.put("attribute", attributeJson);

			attributeInputJson.put("value", this.getInput());
			if (this.hasDifferentMethods()) {
				attributeInputJson.put("probabilityMethod", this.getSelectedMethod().toString());
			}
			return attributeInputJson;
		}
		catch (JSONException e) {
			logger.warn(e);
		}
		return null;
	}

	public static AttributeInput fromJson(EapEventType eventType, JSONObject inputJson) {
		try {
			JSONObject attributeJson = inputJson.getJSONObject("attribute");
			TypeTreeNode attribute = eventType.getValueTypeTree().getAttributeByExpression(attributeJson.getString("name"));
			AttributeInput importedInput = AttributeInput.attributeInputFactory(attribute, inputJson.getString("value"));
			if (inputJson.has("probabilityMethod")) {
				importedInput.setSelectedMethod(ProbabilityDistributionEnum.valueOf(inputJson.getString("probabilityMethod")));
			}
			return importedInput;
		}
		catch (JSONException e) {
			logger.warn(e);
		}
		return null;
	}

	/**
	 * Checks whether the AttributeInput-Instance provides different methods that can be selected for computation.
	 *
	 * @return a bool indicating if different methods are supported
	 */
	public boolean hasDifferentMethods() {
		return !this.getAvailableMethods().isEmpty();
	}

	/**
	 * Returns a validator for wicket input fields fitting the AttributeInput and the selected method.
	 *
	 * @return an IValidator object from the generator.validation package
	 */
	public IValidator<String> getAttributeInputValidator() {
		return AttributeValidator.getValidatorForAttribute(this.getAttribute());
	}

	/**
	 * Get a random index for the provided array.
	 *
	 * @param inputArray the index should be generated for
	 * @return an int containing the index
	 */
	public static int getRandomIndex(Object[] inputArray) {
		return random.nextInt(inputArray.length);
	}

	/**
	 *  Get a random index for the provided list.
	 *
	 * @param inputList the index should be generated for
	 * @return an int containing the index
	 */
	public static int getRandomIndex(List inputList) { return random.nextInt(inputList.size()); }

	/**
	 * Enum containing all possible methods for value generation of any AttributeInput-Subclasses.
	 */
	public enum ProbabilityDistributionEnum implements Serializable {

		UNIFORM("uniform"), NORMAL("normal");

		private String distribution;

		/**
		 * Constructor for the enum, sets the given distribution string.
		 *
		 * @param distribution string to be set as distribution name
		 */
		ProbabilityDistributionEnum(final String distribution) {
			this.distribution = distribution;
		}

		/**
		 * Implements the toString method and returns the name of the distribution as string.
		 *
		 * @return a string containing the distribution
		 */
		@Override
		public String toString() {
			// only capitalize the first letter
			final String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
	}

}
