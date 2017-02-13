/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation.complex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
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
 * This class tests the import of a BPMN process with various control flow
 * structures, the creation of queries for this BPMN process and simulates the
 * execution of the process to monitor the execution. Only some elements of the
 * process have associated monitoring points.
 * 
 * @author micha
 */
public class ComplexProcessWithSomeMonitoringPointsTest extends AbstractQueryCreationTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir")
				+ "/src/test/resources/bpmn/ComplexProcessTestWithSomeMonitoringPoints.bpmn20.xml";
	}

	@Test
	@Override
	public void testImport() throws XMLParsingException {
		this.BPMNProcess = BPMNParser.generateProcessFromXML(this.filePath);
		Assert.assertNotNull(this.BPMNProcess);
		Assert.assertTrue("Number of BPMN elements without sequence flows should be 17 but was "
				+ this.BPMNProcess.getBPMNElementsWithOutSequenceFlows().size(), this.BPMNProcess
				.getBPMNElementsWithOutSequenceFlows().size() == 17);
	}

	@Test
	@Override
	public void testQueryCreation() throws XMLParsingException, RuntimeException {
		this.queryCreationTemplateMethod(this.filePath, "ComplexProcess",
				Arrays.asList(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER)));
	}

	@Override
	protected Set<EapEventType> createEventTypes() {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();

		AttributeTypeTree values;

		values = this.createAttributeTree();
		final EapEventType task1 = new EapEventType("Task1", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType task2 = new EapEventType("Task2", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType task3 = new EapEventType("Task3", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType task4 = new EapEventType("Task4", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType task5 = new EapEventType("Task5", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType task6 = new EapEventType("Task6", values, "Timestamp");
		// values = createAttributeTree();
		// EapEventType task7 = new EapEventType("Task7", values, "Timestamp");

		eventTypes.add(task1);
		eventTypes.add(task2);
		eventTypes.add(task3);
		eventTypes.add(task2);
		eventTypes.add(task4);
		eventTypes.add(task5);
		eventTypes.add(task6);
		// eventTypes.add(task7);

		return eventTypes;
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		// XOR-Pfade bauen
		final Random random = new Random();
		final int choose = random.nextInt(3);

		// Events für verschiedene Prozessinstanzen erzeugen und an
		// Broker.getInstance().senden
		for (final EapEventType eventType : eventTypes) {

			if (choose == 0) { /* oberer Pfad */
				if (eventType.getTypeName().equals("Task4") || eventType.getTypeName().equals("Task5")
						|| eventType.getTypeName().equals("Task6")) {
					continue;
				}
			} else if (choose == 1) { /* mittlerer Pfad */
				if (eventType.getTypeName().equals("Task2") || eventType.getTypeName().equals("Task3")
						|| eventType.getTypeName().equals("Task5") || eventType.getTypeName().equals("Task6")) {
					continue;
				}
			} else { /* unterer Pfad */
				if (eventType.getTypeName().equals("Task2") || eventType.getTypeName().equals("Task3")
						|| eventType.getTypeName().equals("Task4")) {
					continue;
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
