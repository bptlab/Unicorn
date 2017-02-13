/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

/**
 * A exception for error while parsing XML files.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class XMLParsingException extends Exception {

	public XMLParsingException() {

	}

	public XMLParsingException(final String s) {
		super(s);
	}

}
