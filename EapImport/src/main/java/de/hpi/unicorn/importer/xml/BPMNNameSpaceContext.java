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
 * This class specifies an name space for the parsing of BPMN2.0-XML files.
 * 
 * @author micha
 */
public class BPMNNameSpaceContext implements NamespaceContext {

	@Override
	public String getNamespaceURI(final String prefix) {
		if (prefix.equals("ns")) {
			return "http://www.omg.org/spec/BPMN/20100524/MODEL";
		} else {
			return XMLConstants.NULL_NS_URI;
		}
	}

	@Override
	public String getPrefix(final String namespace) {
		if (namespace.equals("http://www.omg.org/spec/BPMN/20100524/MODEL")) {
			return "ns";
		} else {
			return null;
		}
	}

	@Override
	public Iterator<?> getPrefixes(final String namespace) {
		return null;
	}

}
