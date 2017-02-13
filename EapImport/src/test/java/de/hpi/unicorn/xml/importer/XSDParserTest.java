/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.xml.importer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;

/**
 * This class tests the parsing of a XSD file to generate an event type.
 * 
 * @author micha
 */
public class XSDParserTest {

	private static String filePath = System.getProperty("user.dir") + "/src/test/resources/EventTaxonomy.xsd";

	@Test
	public void testXSDParsing() throws XPathExpressionException, ParserConfigurationException, SAXException,
			IOException {
		EapEventType eventType = null;
		try {
			eventType = XSDParser.generateEventTypeFromXSD(XSDParserTest.filePath, "EventTaxonomy");
		} catch (final XMLParsingException e) {
			Assert.fail();
		}
		Assert.assertNotNull(eventType);
	}

}
