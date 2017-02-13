/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * This class specifies an name space for the parsing of ordinary XML files.
 * 
 * @author micha
 */
public class XSDNameSpaceContext implements NamespaceContext {

	@Override
	public String getNamespaceURI(final String prefix) {
		if (prefix.equals("xs")) {
			return "http://www.w3.org/2001/XMLSchema";
		} else {
			return XMLConstants.NULL_NS_URI;
		}
	}

	@Override
	public String getPrefix(final String namespace) {
		if (namespace.equals("http://www.w3.org/2001/XMLSchema")) {
			return "xs";
		} else {
			return null;
		}
	}

	@Override
	public Iterator<?> getPrefixes(final String namespace) {
		return null;
	}

}
