/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper.eventimport;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.edifact.EdifactImporter;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

public class EdifactXMLImporterTest {

	private static String edifactPath = "src/test/resources/1_BERMAN.txt";
	private static String copinoPath = "src/test/resources/6_COPINO.txt";
	private static String xmlPath = "src/test/resources/1_BERMAN.xml";
	private static String xsdPath = "src/test/resources/berman.xsd";
	private StreamProcessingAdapter esper;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.esper = StreamProcessingAdapter.getInstance();
	}

	@Test
	public void importEdifactXML() throws XMLParsingException, IOException, SAXException {
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(EdifactXMLImporterTest.xsdPath,
				FileUtils.getFileNameWithoutExtension(EdifactXMLImporterTest.xsdPath));
		Broker.getInstance().importEventType(eventType);
		// System.out.println(eventType.getValueTypeTree());
		Assert.assertTrue("not found eventType, but found " + EapEventType.findAll(),
				EapEventType.findByID(eventType.getID()) == eventType);
		final List<EapEvent> events = XMLParser.generateEventsFromXML(EdifactXMLImporterTest.xmlPath);
		final QueryWrapper query = new QueryWrapper("xmlEvent", "Select * from " + eventType.getTypeName(),
				QueryTypeEnum.LIVE);
		final QueryWrapper testAtts = new QueryWrapper(
				"testAtts",
				"Select env_syntaxIdentifier.env_id as SyntaxID, env_interchangeMessage.env_UNH.env_messageIdentifier.env_id as messageIdentifier "
						+ "from " + eventType.getTypeName(), QueryTypeEnum.LIVE);
		// LiveQueryListener listener = query.addToEsper();
		// LiveQueryListener listenerAtts = testAtts.addToEsper();
		for (final EapEvent event : events) {
			this.esper.addEvent(event);
		}
		System.out.println(testAtts.getPrintableLog());
		// System.out.println(event.fullEvent());

		Assert.assertTrue("should have found 1 event in eventType, but found " + query.getNumberOfLogEntries(),
				query.getNumberOfLogEntries() == 1);
	}

	@Test
	public void testGenerateEventFromEdifact() throws XMLParsingException, Exception {
		final List<EapEvent> events = EdifactImporter.getInstance().generateEventFromEdifact(
				EdifactXMLImporterTest.edifactPath);
		Assert.assertTrue("not created event", events.get(0) != null);
		final EapEvent event = events.get(0);
		Broker.getInstance().importEventType(event.getEventType());
		// TODO:
		// assertTrue(StreamProcessingAdapter.getInstance().isEventType(event.getEventType()));
		System.out.println("EventType: ");
		System.out.println(event.getEventType().getValueTypeTree().toString());
		Broker.getInstance().importEvent(event);
	}

	@Test
	public void testCopinoEventUpload() throws XMLParsingException, Exception {
		final List<EapEvent> events = EdifactImporter.getInstance().generateEventFromEdifact(
				EdifactXMLImporterTest.copinoPath);
		Assert.assertTrue(events.size() == 1);
		final EapEvent event = events.get(0);
		Assert.assertTrue("not created event", event != null);
		Broker.getInstance().importEventType(event.getEventType());
		Assert.assertTrue(StreamProcessingAdapter.getInstance().isRegistered(event.getEventType()));
		System.out.println("EventType: ");
		System.out.println(event.getEventType().getValueTypeTree().toString());
		Broker.getInstance().importEvent(event);
	}

}
