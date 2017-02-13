/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation.subprocess;

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
 * This class tests the import of a BPMN process with a subprocess, the creation
 * of queries for this BPMN process and simulates the execution of the process
 * to monitor the execution.
 * 
 * @author micha
 */
public class SubProcessTest extends AbstractQueryCreationTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/Automontage_TwoTerminal.bpmn20.xml";
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
		this.queryCreationTemplateMethod(this.filePath, "SubProcess",
				Arrays.asList(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER)));
	}

	@Override
	protected void simulate(final Set<EapEventType> eventTypes) {
		for (final EapEventType eventType : eventTypes) {
			// Durchlauf ohne Fehler
			if (eventType.getTypeName().equals("Fehlerbehandlung") || eventType.getTypeName().equals("Winterreifen")) {
				continue;
			}

			Broker.getInstance().importEvents(TestHelper.createDummyEvents(eventType, 3));
		}
	}

	@Override
	protected Set<EapEventType> createEventTypes() {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();

		AttributeTypeTree values;

		values = this.createAttributeTree();
		final EapEventType karosserie = new EapEventType("Karosserie", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType sommerReifen = new EapEventType("Sommerreifen", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType winterReifen = new EapEventType("Winterreifen", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType ausliefern = new EapEventType("Ausliefern", values, "TimeStamp");

		values = this.createAttributeTree();
		final EapEventType fehlerbehandlung = new EapEventType("Fehlerbehandlung", values, "TimeStamp");

		eventTypes.add(karosserie);
		eventTypes.add(fehlerbehandlung);
		eventTypes.add(sommerReifen);
		eventTypes.add(winterReifen);
		eventTypes.add(ausliefern);

		return eventTypes;
	}

	@AfterClass
	public static void tearDown() {
		AbstractMonitoringTest.resetDatabase();
	}

}
