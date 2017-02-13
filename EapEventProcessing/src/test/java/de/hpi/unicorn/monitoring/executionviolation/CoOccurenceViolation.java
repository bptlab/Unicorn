/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.executionviolation;

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
import de.hpi.unicorn.monitoring.bpmn.DetailedQueryStatus;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceStatus;
import de.hpi.unicorn.monitoring.bpmn.ViolationStatus;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.utils.TestHelper;

/**
 * The test proofs the monitoring and detection of a violation of the order of
 * process elements.
 * 
 * @author micha
 */
public class CoOccurenceViolation extends AbstractMonitoringTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/XORTest.bpmn20.xml";
	}

	@Test
	@Override
	public void testImport() throws XMLParsingException {
		this.BPMNProcess = BPMNParser.generateProcessFromXML(this.filePath);
		Assert.assertNotNull(this.BPMNProcess);
		Assert.assertTrue(this.BPMNProcess.getBPMNElementsWithOutSequenceFlows().size() == 9);
	}

	@Test
	@Override
	public void testQueryCreation() throws XMLParsingException, RuntimeException {
		this.queryCreationTemplateMethod(this.filePath, "SimpleProcess",
				Arrays.asList(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER)));
	}

	@Override
	protected Set<EapEventType> createEventTypes() {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();

		AttributeTypeTree values;

		values = this.createAttributeTree();
		final EapEventType first = new EapEventType("FirstEvent", values, "TimeStamp");
		first.save();

		values = this.createAttributeTree();
		final EapEventType branch1First = new EapEventType("Branch1_FirstEvent", values, "TimeStamp");
		branch1First.save();

		values = this.createAttributeTree();
		final EapEventType branch1Second = new EapEventType("Branch1_SecondEvent", values, "TimeStamp");
		branch1Second.save();

		values = this.createAttributeTree();
		final EapEventType branch2First = new EapEventType("Branch2_FirstEvent", values, "TimeStamp");
		branch2First.save();

		values = this.createAttributeTree();
		final EapEventType second = new EapEventType("SecondEvent", values, "TimeStamp");
		second.save();

		eventTypes.add(first);
		eventTypes.add(branch1First);
		eventTypes.add(branch2First);
		eventTypes.add(branch1Second);
		eventTypes.add(second);

		return eventTypes;
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		// Events für verschiedene Prozessinstanzen erzeugen und an
		// Broker.getInstance().senden
		// Reihenfolge der Events ist wichtig (über Liste abgebildet)
		for (final EapEventType eventType : eventTypes) {
			// EventType Branch1_SecondEvent überspringen um MissingViolation zu
			// erzeugen, Branch2_FirstEvent überspringen, wegen XOR
			if (eventType.getTypeName().equals("Branch1_SecondEvent")
					|| eventType.getTypeName().equals("Branch2_FirstEvent")) {
				continue;
			}

			// Alle Eventtypen werden gesendet, also werden beide XOR-Pfade
			// simuliert --> ExclusivenessViolation

			Broker.getInstance().importEvents(TestHelper.createDummyEvents(eventType, 2));
		}
	}

	@AfterClass
	public static void tearDown() {
		AbstractMonitoringTest.resetDatabase();
	}

	@Override
	protected void assertQueryStatus() {
		// Auf Listener hören
		final BPMNQueryMonitor queryMonitor = BPMNQueryMonitor.getInstance();
		for (final CorrelationProcessInstance processInstance : CorrelationProcessInstance.findAll()) {
			Assert.assertTrue(queryMonitor.getStatus(processInstance) == ProcessInstanceStatus.Finished);
			boolean coOccurenceViolationStatusContained = false;
			for (final DetailedQueryStatus detailedQueryStatus : queryMonitor.getDetailedStatus(processInstance)
					.getElements()) {
				if (detailedQueryStatus.getViolationStatus().contains(ViolationStatus.Missing)) {
					coOccurenceViolationStatusContained = true;
				}
			}
			Assert.assertTrue(coOccurenceViolationStatusContained);
		}
	}

}
