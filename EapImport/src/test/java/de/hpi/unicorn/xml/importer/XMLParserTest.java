/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.xml.importer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.configuration.MultipleEventValueHandling;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.xml.EventXMLSplitter;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.DateUtils;

/**
 * This class tests the parsing of events and event types from an XML file.
 * 
 * @author micha, tw
 */

public class XMLParserTest {

	private static String filePath = System.getProperty("user.dir") + "/src/test/resources/Event1.xml";
	private static String filePathToXSD = System.getProperty("user.dir") + "/src/test/resources/EventTaxonomy.xsd";
	private static String filePathToXMLWithHierarchicalTimestamp = System.getProperty("user.dir")
			+ "/src/test/resources/Event2.xml";
	private static String filePathToXMLWithMultipleAttributeValues = System.getProperty("user.dir")
			+ "/src/test/resources/Event3.xml";
	private static String filePathToMultipleValueXSD = System.getProperty("user.dir")
			+ "/src/test/resources/Testaaa.xsd";
	private static String filePathToMultipleValueXML = System.getProperty("user.dir")
			+ "/src/test/resources/Testaaa.xml";

	@Before
	public void setup() {
		Persistor.useTestEnviroment();

	}

	@Test
	public void testXMLParsing() throws XPathExpressionException, ParserConfigurationException, SAXException,
			IOException, XMLParsingException {
		final EapEventType eventTyp = new EapEventType("EventTaxonomy");
		eventTyp.setXMLName("EventTaxonomy");
		eventTyp.setTimestampName("timestamp");
		eventTyp.save();
		final List<EapEvent> events = XMLParser.generateEventsFromXML(XMLParserTest.filePath);
		Assert.assertTrue(!events.isEmpty());
	}

	@Test
	public void testHierarchicalTimestampParsing() throws XMLParsingException, IOException, SAXException {
		final EapEventType eventType = new EapEventType("EventTaxonomy");
		eventType.setXMLName("EventTaxonomy");
		eventType.setTimestampName("location.timestamp");
		eventType.save();
		final List<EapEvent> events = XMLParser
				.generateEventsFromXML(XMLParserTest.filePathToXMLWithHierarchicalTimestamp);
		Assert.assertTrue(events.size() == 1);
		final EapEvent event = events.get(0);
		Assert.assertNotNull(event);
		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2013, 11, 25, 20, 25, 00);
		Assert.assertTrue("Should be " + cal.getTime() + " but was " + event.getTimestamp(), event.getTimestamp()
				.equals(cal.getTime()));
	}

	@Test
	public void testNonHierarchicalTimestampParsing() throws XMLParsingException, IOException, SAXException {
		final EapEventType eventType = new EapEventType("EventTaxonomy");
		eventType.setXMLName("EventTaxonomy");
		eventType.setTimestampName("timestamp");
		eventType.save();
		final List<EapEvent> events = XMLParser
				.generateEventsFromXML(XMLParserTest.filePathToXMLWithHierarchicalTimestamp);
		Assert.assertTrue(events.size() == 1);
		final EapEvent event = events.get(0);
		Assert.assertNotNull(event);
		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2013, 11, 24, 20, 25, 00);
		Assert.assertTrue("Should be " + cal.getTime() + " but was " + event.getTimestamp(), event.getTimestamp()
				.equals(cal.getTime()));
	}

	@Test
	public void testMultipleAttributeValues() throws XMLParsingException {
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(XMLParserTest.filePathToXSD, "EventTaxonomy");
		eventType.save();

		final List<String> xmlEvents = EventXMLSplitter.simpleTransform(
				XMLParserTest.filePathToXMLWithMultipleAttributeValues, true);
		Assert.assertTrue("expected 12 events, got " + xmlEvents.size(), xmlEvents.size() == 12);

		for (final String xmlEvent : xmlEvents) {
			final List<EapEvent> event = XMLParser.generateEventsFromDoc(XMLParser.XMLStringToDoc(xmlEvent));
			Assert.assertNotNull(event);
		}
	}

	@Test
	public void testMultipleAttributeValuesWithDifferentConfigurations() throws XMLParsingException {
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(XMLParserTest.filePathToMultipleValueXSD,
				"Testaaa");
		eventType.save();
		List<EapEvent> events = null;

		EapConfiguration.eventValueHandling = new MultipleEventValueHandling[] { MultipleEventValueHandling.CROSS };
		events = XMLParser.generateEventsFromXML(XMLParserTest.filePathToMultipleValueXML);
		Assert.assertTrue("Expected 8 events,  got " + events.size(), events.size() == 8);

		EapConfiguration.eventValueHandling = new MultipleEventValueHandling[] { MultipleEventValueHandling.CONCAT };
		events = XMLParser.generateEventsFromXML(XMLParserTest.filePathToMultipleValueXML);
		Assert.assertTrue("Expected 4 events,  got " + events.size(), events.size() == 4);

		EapConfiguration.eventValueHandling = new MultipleEventValueHandling[] { MultipleEventValueHandling.FIRST };
		events = XMLParser.generateEventsFromXML(XMLParserTest.filePathToMultipleValueXML);
		Assert.assertTrue("Expected 1 event,  got " + events.size(), events.size() == 1);
		Assert.assertTrue("Expected value for 'a' was 3, but is " + events.get(0).getValues().get("a"), events.get(0)
				.getValues().get("a").equals(3L));
		Assert.assertTrue("Expected value for 'b' was auto, but is " + events.get(0).getValues().get("b"), events
				.get(0).getValues().get("b").equals("auto"));
		Assert.assertTrue("Expected value for 'd' was 1, but is " + events.get(0).getValues().get("d"), events.get(0)
				.getValues().get("d").equals(1L));

		EapConfiguration.eventValueHandling = new MultipleEventValueHandling[] { MultipleEventValueHandling.LAST };
		events = XMLParser.generateEventsFromXML(XMLParserTest.filePathToMultipleValueXML);
		Assert.assertTrue("Expected 1 event,  got " + events.size(), events.size() == 1);
		Assert.assertTrue("Expected value for 'a' was 4, but is " + events.get(0).getValues().get("a"), events.get(0)
				.getValues().get("a").equals(4L));
		Assert.assertTrue("Expected value for 'b' was Bahn, but is " + events.get(0).getValues().get("b"), events
				.get(0).getValues().get("b").equals("Bahn"));
		Assert.assertTrue("Expected value for 'd' was 2, but is " + events.get(0).getValues().get("d"), events.get(0)
				.getValues().get("d").equals(2L));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testDateParsing() {
		final String timeStampString = "24.12.2013 20:25";
		final Date timeStamp = DateUtils.parseDate(timeStampString);
		Assert.assertTrue(timeStamp.getDate() == 24);
		Assert.assertTrue(timeStamp.getMonth() == 12 - 1);
		Assert.assertTrue(timeStamp.getYear() == 2013 - 1900);
	}

}
