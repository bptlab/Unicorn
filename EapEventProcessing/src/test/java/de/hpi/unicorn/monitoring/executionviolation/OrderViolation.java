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
import de.hpi.unicorn.query.PatternQueryType;
import de.hpi.unicorn.utils.TestHelper;

/**
 * The test proofs the monitoring and detection of a violation of the order of
 * process elements.
 * 
 * @author micha
 */
public class OrderViolation extends AbstractMonitoringTest {

	@Before
	public void setup() {
		this.filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/SimpleSequence.bpmn20.xml";
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testImport() throws XMLParsingException {
		this.BPMNProcess = BPMNParser.generateProcessFromXML(this.filePath);
		Assert.assertNotNull(this.BPMNProcess);
		Assert.assertTrue(this.BPMNProcess.getBPMNElementsWithOutSequenceFlows().size() == 5);
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
		final EapEventType first = new EapEventType("FirstEvent", values, "Timestamp");

		values = this.createAttributeTree();
		final EapEventType second = new EapEventType("SecondEvent", values, "Timestamp");

		values = this.createAttributeTree();
		final EapEventType third = new EapEventType("ThirdEvent", values, "Timestamp");

		eventTypes.add(first);

		eventTypes.add(third);
		// Order-Violation
		eventTypes.add(second);

		return eventTypes;
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		for (final EapEventType eventType : eventTypes) {

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
			boolean orderViolationStatusContained = false;
			for (final DetailedQueryStatus detailedQueryStatus : queryMonitor.getDetailedStatus(processInstance)
					.getElements()) {
				if (detailedQueryStatus.getViolationStatus().contains(ViolationStatus.Order)
						&& detailedQueryStatus.getQuery().getPatternQueryType().equals(PatternQueryType.SEQUENCE)) {
					orderViolationStatusContained = true;
				}
			}
			Assert.assertTrue(orderViolationStatusContained);
		}
	}

}
