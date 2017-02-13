/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.csv.importer;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.importer.csv.CSVExporter;
import de.hpi.unicorn.importer.csv.CSVImporter;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.TestHelper;

/**
 * This class tests import of CSV files.
 * 
 * @author micha
 */
public class CSVImportTest {

	private static String filePath = System.getProperty("user.dir") + "/src/test/resources/Kino.csv";
	private static String filePath2 = System.getProperty("user.dir") + "/src/test/resources/Kino2.csv";
	private static String filePathSmallKino = System.getProperty("user.dir") + "/src/test/resources/KinoSmall.csv";

	@Test
	public void testExtractionOfColumnTitles() {
		final CSVImporter csvNormalizer = new CSVImporter();
		final List<String> columnTitles = csvNormalizer.getColumnTitlesFromFile(CSVImportTest.filePath);
		Assert.assertTrue("Timestamp column could not be read.", columnTitles.contains("Timestamp"));
		Assert.assertTrue("Location column could not be read.", columnTitles.contains("Location"));
		Assert.assertTrue("Rating column could not be read.", columnTitles.contains("Rating"));

		final List<String> columnTitles2 = csvNormalizer.getColumnTitlesFromFile(CSVImportTest.filePath2);
		Assert.assertTrue("Timestamp column could not be read.", columnTitles2.contains("Timestamp"));
		Assert.assertTrue("Location column could not be read.", columnTitles2.contains("Location"));
		Assert.assertTrue("Rating column could not be read.", columnTitles2.contains("Rating"));
	}

	@Test
	public void testExtractionOfEvents() {
		final CSVImporter csvNormalizer = new CSVImporter();
		final List<String> columnTitles = csvNormalizer.getColumnTitlesFromFile(CSVImportTest.filePath);
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(columnTitles);
		final List<EapEvent> events = csvNormalizer.importEventsFromFile(CSVImportTest.filePath, attributes);
		Assert.assertTrue("Not the right number of events imported. Number should be 999 but was " + events.size(),
				events.size() == 999);

		// ArrayList<String> columnTitles2 =
		// csvNormalizer.getColumnTitlesFromFile(filePath2);
		// ArrayList<EapEvent> events2 =
		// csvNormalizer.importEventsFromFile(filePath2, columnTitles2);
		// assertTrue("Not the right number of events imported. Number should be 999 but was "
		// + events2.size(), events2.size() == 999);
	}

	@Test
	public void testImportAndExport() {
		Persistor.useTestEnviroment();
		final CSVImporter csvNormalizer = new CSVImporter();
		final List<String> columnTitles = csvNormalizer.getColumnTitlesFromFile(CSVImportTest.filePath);
		columnTitles.remove("Timestamp");
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(columnTitles);
		final List<EapEvent> events = csvNormalizer.importEventsFromFile(CSVImportTest.filePath, attributes);
		final EapEventType eventType = new EapEventType("Kino1", attributes, "Timestamp");
		eventType.save();
		for (final EapEvent e : events) {
			e.setEventType(eventType);
		}
		EapEvent.save(events);
		final CSVExporter exporter = new CSVExporter();
		exporter.generateExportFile(eventType, events);
	}

	@Test
	public void testExtractionOfMeanEvents() {
		final CSVImporter csvNormalizer = new CSVImporter();
		final List<String> columnTitles = csvNormalizer.getColumnTitlesFromFile(CSVImportTest.filePathSmallKino);
		Assert.assertTrue("Timestamp column could not be read.", columnTitles.contains("Timestamp"));
		Assert.assertTrue("Location column could not be read.", columnTitles.contains("Location"));
		Assert.assertTrue("Rating column could not be read.", columnTitles.contains("Rating"));
		columnTitles.remove("Timestamp");
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(columnTitles);
		final EapEventType eventType = new EapEventType("Kino", attributes, "Timestamp");
		final List<EapEvent> events = csvNormalizer.importEventsFromFile(CSVImportTest.filePathSmallKino, attributes,
				"Timestamp");
		for (final EapEvent event : events) {
			event.setEventType(eventType);
		}
		Assert.assertTrue("Not the right number of events imported. Number should be 3 but was " + events.size(),
				events.size() == 3);

		final EapEvent event1 = events.get(0);
		Assert.assertEquals("re;d;", event1.getValues().get("Rating"));
		final EapEvent event2 = events.get(1);
		Assert.assertEquals("yel\"\"\"low", event2.getValues().get("Rating"));
		final EapEvent event3 = events.get(2);
		Assert.assertEquals("red", event3.getValues().get("Rating"));
		Assert.assertEquals(new Long(1), event3.getValues().get("Location"));
	}

}
