/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

/**
 * An exception for errors while generating queries from BPMN models.
 * 
 * @author anne
 */
@SuppressWarnings("serial")
public class QueryGenerationException extends Exception {

	public QueryGenerationException() {

	}

	public QueryGenerationException(final String s) {
		super(s);
	}

}
