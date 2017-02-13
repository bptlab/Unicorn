/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query;

/**
 * This enumeration encapsulates the types of @see QueryWrapper s. LIVE: The
 * query is saved in the database and activated. It is executed on event
 * streams. ONDEMAND: The query is saved in the database and can be executed by
 * the user whenever he wants.
 * 
 */
public enum QueryTypeEnum {
	LIVE, ONDEMAND
}