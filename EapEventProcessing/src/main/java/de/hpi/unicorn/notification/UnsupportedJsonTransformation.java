/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

public class UnsupportedJsonTransformation extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7217437626987007415L;

	public UnsupportedJsonTransformation() {
		super("Cannot transform event to JSON: Either nested XML tag or XML content is allowed "
				+ "inside of one XML tag");
	}

}
