/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper.queries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.EPOnDemandQueryResult;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.excel.ExcelImporter;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.LiveQueryListener;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

public class EsperTests {
	StreamProcessingAdapter esper;
	private static String kinoFileName = "Kino.xls";
	private static String kinoFilePath = System.getProperty("user.dir") + "/src/test/resources/"
			+ EsperTests.kinoFileName;
	private static List<EapEvent> events;
	private EapEventType eventType;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		StreamProcessingAdapter.clearInstance();
		Assert.assertTrue(StreamProcessingAdapter.instanceIsCleared());
		this.esper = StreamProcessingAdapter.getInstance();
		final ExcelImporter excelNormalizer = new ExcelImporter();
		final ArrayList<String> columnTitles = excelNormalizer.getColumnTitlesFromFile(EsperTests.kinoFilePath);
		Assert.assertTrue("Not the right attributes",
				columnTitles.equals(new ArrayList<String>(Arrays.asList("Timestamp", "Location", "Rating"))));
		columnTitles.remove("Timestamp");
		final List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
		for (final String attributeName : columnTitles) {
			if (attributeName.equals("Location")) {
				attributes.add(new TypeTreeNode(attributeName, AttributeTypeEnum.INTEGER));
			} else if (attributeName.equals("Rating")) {
				attributes.add(new TypeTreeNode(attributeName, AttributeTypeEnum.STRING));
			}
		}
		EsperTests.events = excelNormalizer.importEventsFromFile(EsperTests.kinoFilePath, attributes);
		this.eventType = new EapEventType("Kino", attributes, "Timestamp");
		Assert.assertTrue("KinoWindow already exists", !this.esper.hasWindow("KinoWindow"));
		// System.out.println("Events imported: " + events.size());
	}

	@Test
	public void testEventTypes() {
		// eventTyp erzeugen
		final AttributeTypeTree attributes = new AttributeTypeTree();
		attributes.addRoot(new TypeTreeNode("Sorte", AttributeTypeEnum.STRING));
		attributes.addRoot(new TypeTreeNode("Leckerheitsgrad", AttributeTypeEnum.STRING));
		final EapEventType eventType = new EapEventType("Eiscreme", attributes);
		this.esper.addEventType(eventType);
		Assert.assertTrue("Event type was not added.", this.esper.isRegistered(eventType));

		// Events erzeugen
		final Map<String, Serializable> values1 = new HashMap<String, Serializable>();
		values1.put("Sorte", "Vanille");
		values1.put("Leckerheitsgrad", "awesome");
		final EapEvent event1 = new EapEvent(eventType, new Date(System.currentTimeMillis()), values1);

		final Map<String, Serializable> values2 = new HashMap<String, Serializable>();
		values1.put("Sorte", "Schoko");
		values1.put("Leckerheitsgrad", "super awesome");
		final EapEvent event2 = new EapEvent(eventType, new Date(System.currentTimeMillis()), values2);

		final ArrayList<EapEvent> events = new ArrayList<EapEvent>();
		events.add(event1);
		events.add(event2);
		EapEvent.setEventType(events, eventType);
		this.esper.addEvents(events);

	}

	@Test
	public void testAddingLiveQuery() {
		this.eventType = Broker.getInstance().importEventType(this.eventType);
		final EapEventType eventTypeFromDB = EapEventType.findByTypeName("Kino");
		Assert.assertTrue("eventType not found", eventTypeFromDB != null);
		Assert.assertTrue("eventType not the same", eventTypeFromDB == this.eventType);
		final String query = "Select * From " + this.eventType.getTypeName();
		final QueryWrapper liveQuery = new QueryWrapper("All", query, QueryTypeEnum.LIVE);
		liveQuery.save();
		final LiveQueryListener listener = liveQuery.addToEsper();
		EapEvent.setEventType(EsperTests.events, this.eventType);

		Broker.getInstance().importEvents(EsperTests.events);

		final LiveQueryListener listener2 = this.esper.getListenerByQuery(liveQuery);
		Assert.assertTrue("should be same Listeners", listener == listener2);
		Assert.assertTrue(liveQuery.getNumberOfLogEntries() == EsperTests.events.size());
		final QueryWrapper liveQueryFromDB = QueryWrapper.findQueryByTitle("All");
		Assert.assertTrue(liveQueryFromDB.getLog().size() == EsperTests.events.size());
	}

	@Test
	public void testQuery() {
		this.eventType = Broker.getInstance().importEventType(this.eventType);
		final QueryWrapper query = new QueryWrapper("testquery", "Select * From KinoWindow", QueryTypeEnum.ONDEMAND);
		final EapEventType eventType = EapEventType.findByTypeName("Kino");
		Assert.assertTrue("Event type not found", eventType != null);
		EapEvent.setEventType(EsperTests.events, eventType);

		Broker.getInstance().importEvents(EsperTests.events);
		final String log = query.execute();
		final String numberOfEvents = log.substring(log.indexOf(System.getProperty("line.separator")) - 3,
				log.indexOf(System.getProperty("line.separator")));
		Assert.assertTrue("expected 999, got: " + numberOfEvents, numberOfEvents.equals("999")); // 999
																									// events,
																									// last
																									// line
																									// has
																									// \n
																									// too
	}

	@Test
	public void testWindowCreation() {
		Assert.assertTrue("Window has already been created",
				!StreamProcessingAdapter.getInstance().hasWindow(this.eventType.getTypeName() + "Window"));
		StreamProcessingAdapter.getInstance().addEventType(this.eventType);
		Assert.assertTrue("Window has not been created",
				StreamProcessingAdapter.getInstance().hasWindow(this.eventType.getTypeName() + "Window"));
		// Send Events
		for (final EapEvent event : EsperTests.events) {
			event.setEventType(this.eventType);
			StreamProcessingAdapter.getInstance().addEvent(event);
		}

		final EPOnDemandQueryResult result = StreamProcessingAdapter.getInstance().getEsperRuntime()
				.executeQuery("Select * From KinoWindow");
		Assert.assertTrue("Number of events should have been 999, instead of " + result.getArray().length,
				result.getArray().length == 999);
	}

}
