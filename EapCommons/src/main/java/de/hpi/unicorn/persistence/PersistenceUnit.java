/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

/**
 * This enum specificies the supported data base stages.
 */
public enum PersistenceUnit {

	DEVELOPMENT("development"), TEST("testing"), PRODUCTION("production");

	private String unitName;

	PersistenceUnit(final String environments) {
		this.unitName = environments;
	}

	public String getName() {
		return this.unitName;
	}

}
