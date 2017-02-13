/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;

import de.hpi.unicorn.esper.CEPListener;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.excel.ExcelImporter;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.TestHelper;

public class StatementTest {

	private static String filePathRating = System.getProperty("user.dir") + "/src/test/resources/Kino.xls";
	private static String filePathKino = System.getProperty("user.dir") + "/src/test/resources/Kino_Filme.xls";
	private final ExcelImporter excelNormalizer = new ExcelImporter();
	private final StreamProcessingAdapter esper = StreamProcessingAdapter.getInstance();

	private List<EapEvent> createRatingEvents() {
		final List<String> ratingColumnTitles = this.excelNormalizer
				.getColumnTitlesFromFile(StatementTest.filePathRating);
		final List<TypeTreeNode> ratingAttributes = TestHelper.createAttributes(ratingColumnTitles);
		return this.excelNormalizer.importEventsFromFile(StatementTest.filePathRating, ratingAttributes);
	}

	private List<EapEvent> createKinoEvents() {
		final List<String> kinoColumnTitles = this.excelNormalizer.getColumnTitlesFromFile(StatementTest.filePathKino);
		final List<TypeTreeNode> kinoAttributes = TestHelper.createAttributes(kinoColumnTitles);
		return this.excelNormalizer.importEventsFromFile(StatementTest.filePathKino, kinoAttributes);
	}

	private EapEventType createCompositeEventType() {
		final Set<String> columnTitles = new HashSet<String>();
		columnTitles.addAll(this.excelNormalizer.getColumnTitlesFromFile(StatementTest.filePathRating));
		columnTitles.addAll(this.excelNormalizer.getColumnTitlesFromFile(StatementTest.filePathKino));
		columnTitles.remove("Timestamp");
		final List<TypeTreeNode> attributes = TestHelper.createAttributes(new ArrayList<String>(columnTitles));
		return new EapEventType("Event", attributes, "Timestamp");
	}

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	// TODO: Query und Listener anlegen und dann Events reinladen und Abfragen
	// Bewertung pro Film
	@Test
	public void testNormalQuery() {
		// Configuration cepConfig = new Configuration();
		// cepConfig.addEventType("Event", EapEvent.class.getName());
		final EapEventType eventType = this.createCompositeEventType();
		Broker.getInstance().importEventType(eventType);
		// cepConfig.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
		// EPServiceProvider cep =
		// EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
		// EPRuntime cepRT = cep.getEPRuntime();
		final EPRuntime cepRT = this.esper.getEsperRuntime();

		// cepRT.sendEvent(new
		// TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));

		final EPAdministrator cepAdm = this.esper.getEsperAdministrator();

		// cepAdm.createEPL("CREATE WINDOW EventWindow.win:keepall() AS SELECT * FROM Event");
		// cepAdm.createEPL("INSERT INTO EventWindow SELECT * FROM Event");

		final EPStatement transformationStatement = cepAdm.createEPL(""
				+ "SELECT A.Timestamp.getTime(), B.Timestamp.getTime(), A.values('Location'), A.values('Movie') "
				+ "FROM EventWindow AS A, EventWindow AS B " + "WHERE " + "B.values('Action')='Ende' AND "
				+ "A.values('Action')='Start' AND " + "A.values('Location')=B.values('Location') AND "
				+ "A.values('Movie')=B.values('Movie') AND "
				+ "(A.Timestamp.getTime()).before(B.Timestamp.getTime(), 0 hours, 3 hours)");

		transformationStatement.addListener(new CEPListener());

		// create events
		final List<EapEvent> ratingEvents = this.createRatingEvents();

		// send events
		for (final EapEvent event : ratingEvents) {
			cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
			// cepRT.sendEvent(new
			// CurrentTimeEvent(event.getTimestamp().getTime()));
			// cepRT.sendEvent(event);
			event.setEventType(eventType);
			Broker.getInstance().importEvent(event);
		}

		// create events
		final List<EapEvent> kinoEvents = this.createKinoEvents();

		// send events
		for (final EapEvent event : kinoEvents) {
			cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
			// cepRT.sendEvent(new
			// CurrentTimeEvent(event.getTimestamp().getTime()));
			// cepRT.sendEvent(event);
			event.setEventType(eventType);
			Broker.getInstance().importEvent(event);
		}

		cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_INTERNAL));
	}

	@Test
	public void testTimestamp() {
		final Configuration cepConfig = new Configuration();
		cepConfig.addEventType("Event", EapEvent.class.getName());
		final EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
		final EPRuntime cepRT = cep.getEPRuntime();
		final EPAdministrator cepAdm = cep.getEPAdministrator();

		// create statement
		final EPStatement timeStatement = cepAdm.createEPL("select count(*) from Event.win:time(1 hour)");
		timeStatement.addListener(new CEPListener());

		// create events
		final List<EapEvent> ratingEvents = this.createRatingEvents();
		this.sortEventListByDate(ratingEvents);

		// pass events to Esper engine
		for (final EapEvent event : ratingEvents) {
			cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
			// System.out.println(new
			// CurrentTimeEvent(event.getTimestamp().getTime()).toString());
			cepRT.sendEvent(new CurrentTimeEvent(event.getTimestamp().getTime()));
			cepRT.sendEvent(event);
		}
	}

	@Test
	public void testContextQuery() {
		final Configuration cepConfig = new Configuration();
		cepConfig.addEventType("Event", EapEvent.class.getName());
		final EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
		final EPRuntime cepRT = cep.getEPRuntime();

		final EPAdministrator cepAdm = cep.getEPAdministrator();

		cepAdm.createEPL("" + "CREATE CONTEXT NestedContext "
				+ "CONTEXT SegmentedByLocation PARTITION BY values('Location') FROM Event, "
				+ "CONTEXT SegmentedByTime INITIATED BY Event(values('Action')='Ende') TERMINATED AFTER 1 hour, "
				+ "CONTEXT SegmentedByRating PARTITION BY values('Rating') FROM Event");

		final EPStatement transformationStatement = cepAdm.createEPL("" + "CONTEXT NestedContext "
				+ "SELECT ID, values('Location'), values('Rating'), count(*) " + "FROM Event "
				+ "GROUP BY values('Location'), values('Rating') " + "OUTPUT LAST EVERY 30 minute");

		transformationStatement.addListener(new CEPListener());

		final List<EapEvent> events = new ArrayList<EapEvent>();
		events.addAll(this.createRatingEvents());
		events.addAll(this.createKinoEvents());
		this.sortEventListByDate(events);

		for (final EapEvent event : events) {
			cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
			cepRT.sendEvent(new CurrentTimeEvent(event.getTimestamp().getTime()));
			cepRT.sendEvent(event);
		}

		cepRT.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_INTERNAL));
	}

	private void sortEventListByDate(final List<EapEvent> events) {
		Collections.sort(events, new Comparator<EapEvent>() {

			@Override
			public int compare(final EapEvent event1, final EapEvent event2) {
				return event1.getTimestamp().compareTo(event2.getTimestamp());
			}

		});
	}

}
