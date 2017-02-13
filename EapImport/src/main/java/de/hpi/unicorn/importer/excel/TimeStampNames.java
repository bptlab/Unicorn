/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.excel;

/**
 * This enum contains typical names for timestamps.
 * 
 * @author micha
 */
public enum TimeStampNames {

	TIMESTAMP, TIME, ZEIT, ZEITSTEMPEL, DATE, DATUM, UHRZEIT;

	public static boolean contains(final String test) {
		for (final TimeStampNames timeStampName : TimeStampNames.values()) {
			if (timeStampName.name().equals(test.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
}
