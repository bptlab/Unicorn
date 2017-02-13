/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

/**
 * This class is a helper to create a @see ColumnChartOptions object. It
 * represents one value for a column chart of an integer attribute. This object
 * counts the number of appearances of integer values in a certain range.
 */
public class IntegerBarChartValue {

	private int startPeriod;
	private int endPeriod;
	// contains the number of events with attribut values in the defined range
	private int frequency = 0;

	public IntegerBarChartValue(final int start, final int end) {
		this.startPeriod = start;
		this.endPeriod = end;
	}

	public int getStartPeriod() {
		return this.startPeriod;
	}

	public void setStartPeriod(final int startPeriod) {
		this.startPeriod = startPeriod;
	}

	public int getEndPeriod() {
		return this.endPeriod;
	}

	public void setEndPeriod(final int endPeriod) {
		this.endPeriod = endPeriod;
	}

	public int getFrequency() {
		return this.frequency;
	}

	public void setFrequency(final int frequency) {
		this.frequency = frequency;
	}

	public void increaseFrequency() {
		this.frequency += 1;
	}

	public String getNameOfPeriod() {
		if (this.startPeriod == this.endPeriod) {
			return this.startPeriod + "";
		}
		return this.startPeriod + " to " + this.endPeriod;
	}

	public boolean containsValue(final int value) {
		return (value >= this.startPeriod && value <= this.endPeriod);
	}

}
