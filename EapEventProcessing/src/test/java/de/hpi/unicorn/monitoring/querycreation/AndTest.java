/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation;

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
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.TestHelper;

/**
 * This class tests the import of a BPMN process with and gateways, the creation
 * of queries for this BPMN process and simulates the execution of the process
 * to monitor the execution.
 * 
 * @author micha
 */
public class AndTest extends AbstractQueryCreationTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/AndTest.bpmn20.xml";
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
		this.queryCreationTemplateMethod(this.filePath, "ANDProcess",
				Arrays.asList(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER)));
	}

	@Override
	protected Set<EapEventType> createEventTypes() {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();

		AttributeTypeTree values;

		values = this.createAttributeTree();
		final EapEventType first = new EapEventType("FirstEvent", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType branch1First = new EapEventType("Branch1_FirstEvent", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType branch1Second = new EapEventType("Branch1_SecondEvent", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType branch2First = new EapEventType("Branch2_FirstEvent", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType second = new EapEventType("SecondEvent", values, "TimeStamp");

		eventTypes.add(first);
		eventTypes.add(branch1First);
		eventTypes.add(branch2First);
		eventTypes.add(branch1Second);
		eventTypes.add(second);

		return eventTypes;
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		for (final EapEventType eventType : eventTypes) {
			Broker.getInstance().importEvents(TestHelper.createDummyEvents(eventType, 3));
		}
	}

	@AfterClass
	public static void tearDown() {
		AbstractMonitoringTest.resetDatabase();
	}

}
