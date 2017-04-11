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

public class FloatAttributeInput extends AttributeInput {

	private Float value;
	{
		availableMethods.add(ProbabilityDistributionEnum.UNIFORM);
		availableMethods.add(ProbabilityDistributionEnum.NORMAL);
	}

	static final Logger logger = Logger.getLogger(FloatAttributeInput.class);

	FloatAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
		this.setSelectedMethod(ProbabilityDistributionEnum.UNIFORM);
	}

	/**
	 * Select random Float value from the input.
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
		String[] possibleValues = this.getInputOrDefault().split(";");
		this.value = Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
	}

	private void calculateUniformDistributedValue() {
		//		https://commons.apache.org/proper/commons-math/javadocs/api-3.2/org/apache/commons/math3/distribution/UniformIntegerDistribution.html
		String userInput = this.getInputOrDefault();
		if (!userInput.contains(";")) {
			this.value = Float.parseFloat(userInput);
			return;
		}
		String[] possibleValues = this.getInputOrDefault().split(";");
		UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(0, possibleValues.length - 1);
		this.value = Float.parseFloat(possibleValues[uniformIntegerDistribution.sample()]);
	}

	private void calculateNormalDistributedValue() {
		//		https://commons.apache.org/proper/commons-math/javadocs/api-3.2/org/apache/commons/math3/distribution/NormalDistribution.html
		String userInput = this.getInputOrDefault();
		double mean = Double.parseDouble(userInput.split(";")[0]);
		double standardDeviation = Double.parseDouble(userInput.split(";")[1]);
		NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
		this.value = (float) normalDistribution.sample();
	}

	@Override
	Float getValue() {
		return this.value;
	}

	@Override
	String getDefaultInput() {
		if (this.getSelectedMethod().equals(ProbabilityDistributionEnum.NORMAL)) {
			return "5;1";
		}
		return "1.1;1.2;2.0;2.5";
	}


	@Override
	public IValidator<String> getAttributeInputValidator() {
		if (ProbabilityDistributionEnum.NORMAL.equals(this.getSelectedMethod())) {
			return new RegexValidator(Pattern.compile("\\d+(?:\\.\\d+)?;\\d+(?:\\.\\d+)?"));
		}
		return super.getAttributeInputValidator();
	}
}
