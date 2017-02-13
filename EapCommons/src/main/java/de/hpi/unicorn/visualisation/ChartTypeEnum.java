/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.visualisation;

/**
 * This enumeration encapsulates the types of attribute charts ( @see
 * ChartConfiguration). COLUMN : attribute chart for the frequency of values for
 * a certain attribute. SPLATTER: attribute chart visualizing the single values
 * of a certain attribute
 */
public enum ChartTypeEnum {
	COLUMN("distribution chart"), SPLATTER("point chart");

	private String type;

	ChartTypeEnum(final String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.type;
	}

}
