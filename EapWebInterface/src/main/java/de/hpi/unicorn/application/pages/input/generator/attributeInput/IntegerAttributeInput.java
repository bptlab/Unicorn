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
		availableMethods.add(ProbabilityDistributionEnum.UNIFORM);
		availableMethods.add(ProbabilityDistributionEnum.NORMAL);
	}

	static final Logger logger = Logger.getLogger(IntegerAttributeInput.class);

	IntegerAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
		this.setSelectedMethod(ProbabilityDistributionEnum.UNIFORM);
	}

	/**
	 * Select random int value from the input.
	 */
	@Override
	public void calculateRandomValue() {
		if (ProbabilityDistributionEnum.UNIFORM.equals(this.getSelectedMethod())) {
			this.calculateUniformDistributedValue();
			return;
		}
		if (ProbabilityDistributionEnum.NORMAL.equals(this.getSelectedMethod())) {
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
			if (!userInput.contains(";")) {
				this.value = Integer.parseInt(userInput);
			} else {
				String[] possibleValues = userInput.split(";");
				UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(0, possibleValues.length - 1);
				this.value = Integer.parseInt(possibleValues[uniformIntegerDistribution.sample()]);
			}
		}
	}

	private void calculateNormalDistributedValue() {
//		https://commons.apache.org/proper/commons-math/javadocs/api-3.2/org/apache/commons/math3/distribution/NormalDistribution.html
		String userInput = this.getInputOrDefault();
		double mean = Double.parseDouble(userInput.split(";")[0]);
		double standardDeviation = Double.parseDouble(userInput.split(";")[1]);
		NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
		this.value = (int) Math.round(normalDistribution.sample());
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

	@Override
	Integer getValue() {
		return this.value;
	}

	@Override
	String getDefaultInput() {
		if (this.getSelectedMethod().equals(ProbabilityDistributionEnum.NORMAL)) {
			return "5;1";
		}
		return "1-50";
	}

	@Override
	public IValidator<String> getAttributeInputValidator() {
		if (ProbabilityDistributionEnum.NORMAL.equals(this.getSelectedMethod())) {
			return new RegexValidator(Pattern.compile("\\d+(?:\\.\\d+)?;\\d+(?:\\.\\d+)?"));
		}
		return super.getAttributeInputValidator();
	}
}
