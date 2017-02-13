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
 * This test tests the creation of queries for a simple sequence A->B->C, but
 * also under consideration of multiple monitoring points for one task, so that
 * the life cycle of a task is monitorable.
 * 
 * @author micha
 */
public class SimpleSequenceStateTransitionTest extends AbstractQueryCreationTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir")
				+ "/src/test/resources/bpmn/SimpleSequenceStateTransition.bpmn20.xml";
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
		final EapEventType firstBegin = new EapEventType("FirstEventBegin", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType firstTerminate = new EapEventType("FirstEventTerminate", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType secondEnable = new EapEventType("SecondEventEnable", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType secondTerminate = new EapEventType("SecondEventTerminate", values, "Timestamp");
		values = this.createAttributeTree();
		final EapEventType third = new EapEventType("ThirdEvent", values, "Timestamp");

		eventTypes.add(firstBegin);
		eventTypes.add(firstTerminate);
		eventTypes.add(secondEnable);
		eventTypes.add(secondTerminate);
		eventTypes.add(third);

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

}
