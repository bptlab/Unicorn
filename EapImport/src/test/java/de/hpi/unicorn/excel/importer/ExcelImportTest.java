/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.excel.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.importer.excel.ExcelImporter;
import de.hpi.unicorn.importer.excel.ImportEvent;
import de.hpi.unicorn.utils.TestHelper;

/**
 * This class tests the import of excel files.
 * 
 * @author micha
 */
public class ExcelImportTest {

	private static String filePath = System.getProperty("user.dir") + "/src/test/resources/Kino.xls";
	private static String cinemaSchedulePath = System.getProperty("user.dir")
			+ "/src/test/resources/CinemaSchedule.xls";
	private static String cinemaScheduleWithoutTimestampPath = System.getProperty("user.dir")
			+ "/src/test/resources/CinemaScheduleWithoutTime.xls";
	private static String emptyFilePath = System.getProperty("user.dir") + "/src/test/resources/EmptySheet.xls";

	@Test
	public void testExtractionOfColumnTitles() {
		final ExcelImporter excelNormalizer = new ExcelImporter();
		final ArrayList<String> columnTitles = excelNormalizer.getColumnTitlesFromFile(ExcelImportTest.filePath);
		Assert.assertTrue("Timestamp column could not be read.", columnTitles.contains("Timestamp"));
		Assert.assertTrue("Location column could not be read.", columnTitles.contains("Location"));
		Assert.assertTrue("Rating column could not be read.", columnTitles.contains("Rating"));
	}

	@Test
	public void testExtractionOfEvents() {
		final ExcelImporter excelNormalizer = new ExcelImporter();
		final List<String> columnTitles = excelNormalizer.getColumnTitlesFromFile(ExcelImportTest.filePath);
		columnTitles.remove("Timestamp");
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(columnTitles);
		final EapEventType eventType = new EapEventType("Kino", attributes, "Timestamp");
		final List<EapEvent> events = excelNormalizer.importEventsFromFile(ExcelImportTest.filePath, attributes,
				"Timestamp");
		for (final EapEvent event : events) {
			event.setEventType(eventType);
		}
		Assert.assertTrue("Not the right number of events imported.", events.size() == 999);
		// 12: 30.10.2012 20:46 1 yellow
		final EapEvent testEvent1 = events.get(10);
		Assert.assertTrue("Date of event 1 does not match.",
				testEvent1.getTimestamp().compareTo(new Date(2012 - 1900, 9, 30, 20, 46, 58)) == 0);
		// ID is set when saving in DB
		// assertTrue("ID of event 1 (" + testEvent1.getID()
		// +") does not match.", testEvent1.getID() == 10);
		System.out.println(testEvent1.getValues());
		Assert.assertTrue("Location of event 1 does not match.",
				testEvent1.getValues().get("Location").equals(new Long(1)));
		Assert.assertTrue("Rating of event 1 is not right.", testEvent1.getValues().get("Rating").equals("yellow"));
		// 28: 30.10.2012 20:27 1 red
		final EapEvent testEvent2 = events.get(26);
		Assert.assertTrue("Date of event 2 does not match.",
				testEvent2.getTimestamp().compareTo(new Date(2012 - 1900, 9, 30, 20, 27, 58)) == 0);
		// ID is set when saving in DB
		// assertTrue("ID of event 2 (" + testEvent2.getID()
		// +") does not match.", testEvent2.getID() == 26);
		Assert.assertTrue("Rating of event 2 is not right.", testEvent2.getValues().get("Rating").equals("red"));

	}

	@Test
	public void testColumnSelection() {
		final ExcelImporter excelNormalizer = new ExcelImporter();
		// ArrayList<String> columnTitles =
		// excelNormalizer.getColumnTitlesFromXLS(filePath);
		final ArrayList<String> columnTitles = new ArrayList<String>();
		columnTitles.add("Timestamp");
		columnTitles.add("Location");
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(columnTitles);
		final List<EapEvent> events = excelNormalizer.importEventsFromFile(ExcelImportTest.filePath, attributes);
		Assert.assertTrue("Not the right number of events imported.", events.size() == 999);
		// 12: 30.10.2012 20:46 1 yellow
		final EapEvent testEvent1 = events.get(10);
		Assert.assertTrue("Date of event 1 does not match.",
				testEvent1.getTimestamp().compareTo(new Date(2012 - 1900, 9, 30, 20, 46, 58)) == 0);
		// ID is set when saving in DB
		// assertTrue("ID of event 1 (" + testEvent1.getID()
		// +") does not match.", testEvent1.getID() == 10);
		// 28: 30.10.2012 20:27 1 red
		final EapEvent testEvent2 = events.get(26);
		Assert.assertTrue("Date of event 2 does not match.",
				testEvent2.getTimestamp().compareTo(new Date(2012 - 1900, 9, 30, 20, 27, 58)) == 0);
		// ID is set when saving in DB
		// assertTrue("ID of event 2 (" + testEvent2.getID()
		// +") does not match.", testEvent2.getID() == 26);
	}

	@Test
	public void testBuggyExcelFile() {
		final ExcelImporter excelNormalizer = new ExcelImporter();
		final List<String> columnTitles = excelNormalizer.getColumnTitlesFromFile(ExcelImportTest.cinemaSchedulePath);
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(columnTitles);
		final List<EapEvent> events = excelNormalizer.importEventsFromFile(ExcelImportTest.cinemaSchedulePath,
				attributes);
		Assert.assertTrue("Not the right number of events imported. Should be 12, but were " + events.size(),
				events.size() == 12);
		System.out.println(events.get(0));
	}

	// TODO
	@Test
	public void testFormattedCellsText() {
		// ExcelNormalizer excelNormalizer = new ExcelNormalizer();
		// ArrayList<String> columnTitles =
		// excelNormalizer.getColumnTitlesFromXLS(formattedFilePathText);
		// excelNormalizer.importEventsFromXLS(filePath,
		// excelNormalizer.getColumnTitlesFromXLS(filePath), 1);
		// ArrayList<EapEvent> events =
		// excelNormalizer.importEventsFromXLS(cinemaSchedulePath,
		// columnTitles);
		// //Timestamp of cell formatted Text || 12.09.2011 05:00:00
		// assertTrue("date formatted Text was not read correctly, timestamp was "
		// + events.get(0).getTimestamp(),
		// events.get(0).getTimestamp().compareTo(new Date(2011 - 1900, 9, 12,
		// 05, 00, 00))==0);
	}

	// TODO
	@Test
	public void testFormattedCellsDateShort() {
		// ExcelNormalizer excelNormalizer = new ExcelNormalizer();
		// ArrayList<String> columnTitles =
		// excelNormalizer.getColumnTitlesFromXLS(formattedFilePathDateShort);
		// excelNormalizer.importEventsFromXLS(filePath,
		// excelNormalizer.getColumnTitlesFromXLS(filePath), 1);
		// ArrayList<EapEvent> events =
		// excelNormalizer.importEventsFromXLS(cinemaSchedulePath,
		// columnTitles);
		// //Timestamp of cell formatted Date(short) || 12.09.2011 05:00:00
		// assertTrue("date formatted Date(short) was not read correctly, timestamp was "
		// + events.get(0).getTimestamp(),
		// events.get(0).getTimestamp().compareTo(new Date(2011 - 1900, 9, 12,
		// 05, 00, 00))==0);
	}

	// TODO
	@Test
	public void testFormattedCellsDateLong() {
		// ExcelNormalizer excelNormalizer = new ExcelNormalizer();
		// ArrayList<String> columnTitles =
		// excelNormalizer.getColumnTitlesFromXLS(formattedFilePathDateLong);
		// excelNormalizer.importEventsFromXLS(filePath,
		// excelNormalizer.getColumnTitlesFromXLS(filePath), 1);
		// ArrayList<EapEvent> events =
		// excelNormalizer.importEventsFromXLS(cinemaSchedulePath,
		// columnTitles);
		// //Timestamp of cell formatted Date(long) || 12.09.2011 05:00:00
		// assertTrue("date formatted Date(long) was not read correctly, timestamp was "
		// + events.get(0).getTimestamp(),
		// events.get(0).getTimestamp().compareTo(new Date(2011 - 1900, 9, 12,
		// 05, 00, 00))==0);
	}

	@Test
	public void testExtractionOfImportEvent() {
		// Importieren mit ImportEvent von Events mit TimeStamp
		final ExcelImporter excelNormalizer = new ExcelImporter();
		final List<String> columnTitles = excelNormalizer.getColumnTitlesFromFile(ExcelImportTest.cinemaSchedulePath);
		final List<ImportEvent> events = excelNormalizer.importEventsForPreviewFromFile(
				ExcelImportTest.cinemaSchedulePath, columnTitles);
		Assert.assertTrue("Not the right number of events imported. Should be 12, but were " + events.size(),
				events.size() == 12);
		System.out.println("Event: " + events.get(0));
		// Importieren mit ImportEvent von Events ohne TimeStamp
		final List<String> columnTitlesWithoutTimestamp = excelNormalizer
				.getColumnTitlesFromFile(ExcelImportTest.cinemaScheduleWithoutTimestampPath);
		final List<ImportEvent> eventsWithoutTimestamp = excelNormalizer.importEventsForPreviewFromFile(
				ExcelImportTest.cinemaScheduleWithoutTimestampPath, columnTitlesWithoutTimestamp);
		Assert.assertTrue(
				"Not the right number of events imported. Should be 12, but were " + eventsWithoutTimestamp.size(),
				eventsWithoutTimestamp.size() == 12);
		System.out.println("Event: " + eventsWithoutTimestamp.get(0));
	}

	@Test
	public void testEmptySheet() {
		final ExcelImporter excelNormalizer = new ExcelImporter();
		final List<String> columnTitles = excelNormalizer.getColumnTitlesFromFile(ExcelImportTest.emptyFilePath);
		final List<ImportEvent> events = excelNormalizer.importEventsForPreviewFromFile(ExcelImportTest.emptyFilePath,
				columnTitles);
		Assert.assertTrue(events.isEmpty());
	}

}
