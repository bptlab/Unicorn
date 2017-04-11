/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.application.pages.input.generator.validation.RegexValidator;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.log4j.Logger;
import org.apache.wicket.validation.IValidator;

import java.util.regex.Pattern;


public class IntegerAttributeInput extends AttributeInput {

	private Integer value;
	{
		availableMethods.add("Uniform");
		availableMethods.add("Normal");
	}

	static final Logger logger = Logger.getLogger(IntegerAttributeInput.class);

	public IntegerAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
	}

	/**
	 * Select random int value from the input.
	 */
	@Override
	public void calculateRandomValue() {
		if ("Uniform".equals(this.getSelectedMethod())) {
			this.calculateUniformDistributedValue();
			return;
		}
		if ("Normal".equals(this.getSelectedMethod())) {
			this.calculateNormalDistributedValue();
			return;
		}
	}

	private void calculateUniformDistributedValue() {
//		https://commons.apache.org/proper/commons-math/javadocs/api-3.2/org/apache/commons/math3/distribution/UniformIntegerDistribution.html
		String userInput = this.getInputOrDefault();
		if (userInput.contains("-")) {
			int start = Integer.parseInt(userInput.split("-")[0]);
			int end = Integer.parseInt(userInput.split("-")[1]);
			UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(start, end);
			this.value = uniformIntegerDistribution.sample();
		}
		else {
			String[] possibleValues = userInput.split(";");
			UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(0, possibleValues.length - 1);
			this.value = Integer.parseInt(possibleValues[uniformIntegerDistribution.sample()]);
		}
		logger.warn("Calculated uniform: " + this.value.toString());
	}

	private void calculateNormalDistributedValue() {
//		https://commons.apache.org/proper/commons-math/javadocs/api-3.2/org/apache/commons/math3/distribution/NormalDistribution.html
		String userInput = this.getInputOrDefault();
		if (userInput.contains(";")) {
			double mean = Double.parseDouble(userInput.split(";")[0]);
			double standardDeviation = Double.parseDouble(userInput.split(";")[1]);
			NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
			logger.warn("Bsp:" + normalDistribution.sample());
			this.value = (int) Math.round(normalDistribution.sample());
		}
		logger.warn("Calculated normals: " + this.value.toString());
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

	String getDefaultInput() { return "1-50"; }

	@Override
	public IValidator<String> getAttributeInputValidator() {
		if ("Normal".equals(this.getSelectedMethod())) {
			return new RegexValidator(Pattern.compile("\\d+(?:\\.\\d+)?;\\d+(?:\\.\\d+)?"));
		}
		return super.getAttributeInputValidator();
	}
}
