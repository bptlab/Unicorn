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
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceStatus;
import de.hpi.unicorn.monitoring.querycreation.AbstractQueryCreationTest;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.utils.TestHelper;

/**
 * This class tests the import of a BPMN process with intermediate events, the
 * creation of queries for this BPMN process and simulates the execution of the
 * process to monitor the execution.
 * 
 * @author micha
 */
public class IntermediateTimerTest extends AbstractQueryCreationTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/Pizzalieferung.bpmn20.xml";
	}

	public static void afterQueriesTests(final CorrelationProcess process) {
		final BPMNQueryMonitor queryMonitor = BPMNQueryMonitor.getInstance();

		Assert.assertNotNull(queryMonitor.getProcessMonitorForProcess(process));
		Assert.assertTrue(queryMonitor.getProcessMonitorForProcess(process)
				.getProcessInstances(ProcessInstanceStatus.Finished).size() == 3);
	}

	@Test
	@Override
	public void testImport() throws XMLParsingException {
		this.BPMNProcess = BPMNParser.generateProcessFromXML(this.filePath);
		Assert.assertNotNull(this.BPMNProcess);
		Assert.assertTrue(this.BPMNProcess.getBPMNElementsWithOutSequenceFlows().size() == 11);
	}

	@Test
	@Override
	public void testQueryCreation() throws XMLParsingException, RuntimeException {
		this.queryCreationTemplateMethod(this.filePath, "MessageAndTimer",
				Arrays.asList(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER)));
		IntermediateTimerTest.afterQueriesTests(this.process);
	}

	@Override
	protected Set<EapEventType> createEventTypes() {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();

		AttributeTypeTree values;

		values = this.createAttributeTree();
		final EapEventType pizzaAuswahl = new EapEventType("PizzaAuswahl", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType pizzaBestellen = new EapEventType("PizzaBestellen", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType pizzaNachfragen = new EapEventType("PizzaNachfragen", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType pizzaEssen = new EapEventType("PizzaEssen", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType pizzaErhalten = new EapEventType("PizzaErhalten", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType pizzaErhaltenDelay = new EapEventType("PizzaErhaltenDelay", values, "Timestamp");

		eventTypes.add(pizzaAuswahl);
		eventTypes.add(pizzaBestellen);
		eventTypes.add(pizzaErhalten);
		eventTypes.add(pizzaNachfragen);
		eventTypes.add(pizzaErhaltenDelay);
		eventTypes.add(pizzaEssen);

		return eventTypes;
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		// Events für verschiedene Prozessinstanzen erzeugen und an
		// Broker.getInstance().senden
		for (final EapEventType eventType : eventTypes) {
			// Nur den Timer-Pfad ausführen
			if (eventType.getTypeName().equals("PizzaErhalten")) {
				continue;
			}

			// Warten, um den Timer zu testen
			if (eventType.getTypeName().equals("PizzaNachfragen")) {
				try {
					Thread.sleep(15 * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
			Broker.getInstance().importEvents(TestHelper.createDummyEvents(eventType, 3));
		}
	}

	@AfterClass
	public static void tearDown() {
		AbstractMonitoringTest.resetDatabase();
	}
}
