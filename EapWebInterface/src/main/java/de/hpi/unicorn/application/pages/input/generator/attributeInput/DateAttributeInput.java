/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator.attributeInput;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class DateAttributeInput extends AttributeInput {

	private Date value;
	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

	public DateAttributeInput(TypeTreeNode inputAttribute) {
		super(inputAttribute);
		defaultInput = "2017/01/22T12:00-2017/02/23T14:59";
	}

	/**
	 * Select random date from the input.
	 */
	@Override
	public void calculateRandomValue() {
		String userInput = this.getInput();
		Date start = new Date();
		Date end = new Date();
		long timestamp;
		Date date = new Date();

		if (userInput.contains("-")) {
			try {
				start = dateFormatter.parse(userInput.split("-")[0]);
				end = dateFormatter.parse(userInput.split("-")[1]);
			} catch (ParseException e) { logger.debug("Random Date from input", e); }
			timestamp = ThreadLocalRandom.current().nextLong(start.getTime(), end.getTime());
			date = new Date(timestamp);
		}
		else {
			try {
				date = dateFormatter.parse(userInput);
			} catch (ParseException e) { logger.debug("Random Date from input", e); }
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
				logger.debug("DateInRange: Parse range", e);
				return false;
			}
			return (inputDate.compareTo(start) >= 0) && (inputDate.compareTo(end) <= 0);
		}
		else {
			try {
				start = dateFormatter.parse(range);
			} catch (ParseException e) {
				logger.debug("DateInRange: Parse single date", e);
				return false;
			}
			return inputDate.equals(start);
		}
	}

	Date getValue() {
		return this.value;
	}

	public String getValueAsString() {
		return dateFormatter.format(this.getCalculatedValue());
	}
}
