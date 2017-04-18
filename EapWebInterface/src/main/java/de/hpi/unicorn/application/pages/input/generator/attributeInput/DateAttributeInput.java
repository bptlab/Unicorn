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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Concrete implementation of {@link AttributeInput} for date attributes.
 * * Supported methods:
 * - Uniform (no special implementation)
 *
 */
public class DateAttributeInput extends AttributeInput {

	private Date value;
	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

	static final Logger dateLogger = Logger.getLogger(DateAttributeInput.class);

	/**
	 * Constructor for the DateAttributeInput.
	 *
	 * @param inputAttribute the object should be associated with.
	 */
	public DateAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
	}

	/**
	 * Select random date from the input.
	 * The calculated value is saved in instance variable 'value'.
	 *
	 */
	@Override
	public void calculateRandomValue() {
		String userInput = this.getInputOrDefault();
		Date start = new Date();
		Date end = new Date();
		long timestamp;
		Date date = new Date();

		if (userInput.contains("-")) {
			try {
				start = dateFormatter.parse(userInput.split("-")[0]);
				end = dateFormatter.parse(userInput.split("-")[1]);
			} catch (ParseException e) { dateLogger.debug("Random Date from input", e); }
			timestamp = ThreadLocalRandom.current().nextLong(start.getTime(), end.getTime());
			date = new Date(timestamp);
		}
		else {
			try {
				date = dateFormatter.parse(userInput);
			} catch (ParseException e) { dateLogger.debug("Random Date from input", e); }
		}
		this.value = date;
	}

	/**
	 * Implements the "isInRange" function for a date range.
	 *
	 * @param range to be searched in
	 * @return bool if date is in range
	 */
	@Override
	public boolean isInRange(String range) {
		Date start;
		Date end;
		Date inputDate = this.getValue();

		if (range.contains("-")) {
			try {
				start = dateFormatter.parse(range.split("-")[0]);
				end = dateFormatter.parse(range.split("-")[1]);
			} catch (ParseException e) {
				dateLogger.debug("DateInRange: Parse range", e);
				return false;
			}
			return (inputDate.compareTo(start) >= 0) && (inputDate.compareTo(end) <= 0);
		}
		else {
			try {
				start = dateFormatter.parse(range);
			} catch (ParseException e) {
				dateLogger.debug("DateInRange: Parse single date", e);
				return false;
			}
			return inputDate.equals(start);
		}
	}

	/**
	 * Getter for the computed date-value.
	 *
	 * @return a date
	 */
	@Override
	Date getValue() {
		return this.value;
	}

	/**
	 * Returns the default value for date attributes.
	 *
	 * @return a default value as string
	 */
	@Override
	String getDefaultInput() { return "2017/01/22T12:00-2017/02/23T14:59"; }

	/**
	 * Returns the computed date-value as string.
	 *
	 * @return a string containing the value
	 */
	@Override
	public String getValueAsString() {
		return dateFormatter.format(this.getCalculatedValue());
	}
}
