/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.utils.XMLUtils;

public class HierarchicalEventsTest {

	StreamProcessingAdapter esper;
	private static String pathToEventTaxonomyXSD = "src/test/resources/EventTaxonomy.xsd";
	private static String pathToEventXML = "src/test/resources/Event1.xml";
	private static String pathToDoubleTagEventXML = "src/test/resources/EventDoubleTags.xml";

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		StreamProcessingAdapter.clearInstance();
		this.esper = StreamProcessingAdapter.getInstance();
	}

	@Test
	public void testHierarchicalEventtypes() throws XMLParsingException, SAXException, IOException,
			ParserConfigurationException {
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(
				HierarchicalEventsTest.pathToEventTaxonomyXSD,
				FileUtils.getFileNameWithoutExtension(HierarchicalEventsTest.pathToEventTaxonomyXSD));
		Broker.getInstance().importEventType(eventType);
		// create direct esper event from xml
		final Document doc = this.transformXMLFileToEventNode(HierarchicalEventsTest.pathToDoubleTagEventXML);
		final Node rootElement = doc.getChildNodes().item(0);
		doc.renameNode(rootElement, "", eventType.getTypeName());
		this.esper.getEsperRuntime().sendEvent(doc);
	}

	@Test
	public void testEventsWithDoubleTags() throws XMLParsingException, IOException, SAXException {
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(
				HierarchicalEventsTest.pathToEventTaxonomyXSD,
				FileUtils.getFileNameWithoutExtension(HierarchicalEventsTest.pathToEventTaxonomyXSD));
		Broker.getInstance().importEventType(eventType);

		// query
		final String query = "Select ID, vehicle_information.goods[0], vehicle_information.goods[1], vehicle_information.goods[2] From "
				+ eventType.getTypeName();
		final QueryWrapper liveQuery = new QueryWrapper("All", query, QueryTypeEnum.LIVE);
		liveQuery.save();
		liveQuery.addToEsper();

		// prepare Event
		final List<EapEvent> events = XMLParser.generateEventsFromXML(HierarchicalEventsTest.pathToDoubleTagEventXML);
		Assert.assertTrue(events.size() == 1);
		final EapEvent event = events.get(0);
		Broker.getInstance().importEvent(event);

		Assert.assertTrue("did not find an event, but" + liveQuery.getNumberOfLogEntries(),
				liveQuery.getNumberOfLogEntries() == 1);
		// System.out.println(listener.getLog());
	}

	@Test
	public void testHierarchicalEvents() throws XMLParsingException, SAXException, IOException {
		// prepare EventType
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(
				HierarchicalEventsTest.pathToEventTaxonomyXSD,
				FileUtils.getFileNameWithoutExtension(HierarchicalEventsTest.pathToEventTaxonomyXSD));
		Broker.getInstance().importEventType(eventType);

		// query
		final String query = "Select ID, vehicle_information.vehicle_identifier, count(vehicle_information.vehicle_identifier) From "
				+ eventType.getTypeName();
		final QueryWrapper liveQuery = new QueryWrapper("All", query, QueryTypeEnum.LIVE);
		liveQuery.save();
		liveQuery.addToEsper();

		// prepare Event
		final List<EapEvent> events = XMLParser.generateEventsFromXML(HierarchicalEventsTest.pathToEventXML);
		Assert.assertTrue(events.size() == 1);
		final EapEvent event = events.get(0);
		Broker.getInstance().importEvent(event);

		Assert.assertTrue("did not find an event, but" + liveQuery.getNumberOfLogEntries(),
				liveQuery.getNumberOfLogEntries() == 1);
		// System.out.println(listener.getLog());
	}

	@Test
	public void testEventTransformation() throws ParserConfigurationException, XMLParsingException, IOException,
			SAXException {
		// prepare EventType
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(
				HierarchicalEventsTest.pathToEventTaxonomyXSD,
				FileUtils.getFileNameWithoutExtension(HierarchicalEventsTest.pathToEventTaxonomyXSD));
		Broker.getInstance().importEventType(eventType);

		final List<EapEvent> events = XMLParser.generateEventsFromXML(HierarchicalEventsTest.pathToEventXML);
		Assert.assertTrue(events.size() == 1);
		final EapEvent event = events.get(0);
		final Node node = XMLUtils.eventToNode(event);
		final int numberOfChildren = node.getFirstChild().getChildNodes().getLength();
		Assert.assertTrue("event should have 7 elements, but had " + numberOfChildren, numberOfChildren == 8);
	}

	@Test
	public void testAttributeWithSpaces() {
		final ArrayList<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>(Arrays.asList(new TypeTreeNode(
				"Attribute 1", AttributeTypeEnum.STRING)));
		final EapEventType eventType = new EapEventType("newEventType", attributes);
		Broker.getInstance().importEventType(eventType);

		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		values.put("Attribute 1", "Wert 1");
		final EapEvent event = new EapEvent(eventType, new Date());
		event.setValues(values);
		Broker.getInstance().importEvent(event);
	}

	public Document transformXMLFileToEventNode(final String filePath) {
		String eventString = "";
		try {
			eventString = FileUtils.getFileContentAsString(filePath);
		} catch (final IOException e1) {
			e1.printStackTrace();
			Assert.fail("The InputXML-Filepath was not valid.");
		}
		final InputSource source = new InputSource(new StringReader(eventString));
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		Document doc = null;
		try {
			doc = builderFactory.newDocumentBuilder().parse(source);
		} catch (final SAXException e) {
			e.printStackTrace();
			Assert.fail("The InputXML-String was not valid.");
		} catch (final IOException e) {
			e.printStackTrace();
			Assert.fail("The InputXML-String was not valid.");
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
			Assert.fail("The InputXML-String was not valid.");
		}
		return doc;
	}

}
