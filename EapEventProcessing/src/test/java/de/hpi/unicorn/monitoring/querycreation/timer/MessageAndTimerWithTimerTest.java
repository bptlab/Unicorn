/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation.timer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.xml.BPMNParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.monitoring.AbstractMonitoringTest;
import de.hpi.unicorn.monitoring.querycreation.AbstractQueryCreationTest;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.TestHelper;

/**
 * Tests the boundary intermediate timer event with a waiting time and not
 * sending the message intermediate event that could follow the TimerTask.
 * 
 * @author micha
 */
public class MessageAndTimerWithTimerTest extends AbstractQueryCreationTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/MessageAndTimer.bpmn20.xml";
	}

	@Test
	@Override
	public void testImport() throws XMLParsingException {
		this.BPMNProcess = BPMNParser.generateProcessFromXML(this.filePath);
		Assert.assertNotNull(this.BPMNProcess);
		Assert.assertTrue(this.BPMNProcess.getBPMNElementsWithOutSequenceFlows().size() == 7);
	}

	@Test
	@Override
	public void testQueryCreation() throws XMLParsingException, RuntimeException {
		this.queryCreationTemplateMethod(this.filePath, "MessageAndTimer",
				Arrays.asList(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER)));
		MessageAndTimerTest.afterQueriesTests(this.process);
	}

	@Override
	protected Set<EapEventType> createEventTypes() {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();

		AttributeTypeTree values;

		values = this.createAttributeTree();
		final EapEventType messageStart = new EapEventType("MessageStart", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType timerTask = new EapEventType("TimerTask", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType messageIntermediate = new EapEventType("MessageIntermediate", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType secondTask = new EapEventType("SecondTask", values, "Timestamp");

		eventTypes.add(messageStart);
		eventTypes.add(timerTask);
		eventTypes.add(messageIntermediate);
		eventTypes.add(secondTask);

		return eventTypes;
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		// Events für verschiedene Prozessinstanzen erzeugen und an
		// Broker.getInstance().senden
		for (final EapEventType eventType : eventTypes) {
			// MessageIntermediate skippen um attached intermediate timer zu
			// testen
			if (eventType.getTypeName().equals("MessageIntermediate")) {
				try {
					Thread.sleep(15 * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			Broker.getInstance().importEvents(TestHelper.createDummyEvents(eventType, 3));
		}
	}

	@AfterClass
	public static void tearDown() {
		AbstractMonitoringTest.resetDatabase();
	}
}
