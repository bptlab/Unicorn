/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring;

import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.RPSTBuilder;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.correlation.AttributeCorrelator;
import de.hpi.unicorn.correlation.TimeCondition;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.xml.BPMNParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.monitoring.querycreation.IQueryCreationTest;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.bpmn.PatternQueryGenerator;
import de.hpi.unicorn.query.bpmn.QueryGenerationException;

/**
 * This class centralizes methods for monitoring and query creation tests. All
 * these tests should derive from these class. It provides a template method for
 * the query creation and monitoring testing process.
 * 
 * @author micha
 */
public abstract class AbstractMonitoringTest implements IQueryCreationTest {

	protected String filePath;
	protected BPMNProcess BPMNProcess;
	protected CorrelationProcess process;
	protected Set<EapEventType> eventTypes;

	/**
	 * Template method for the query creation for Esper.
	 * 
	 * @param filePath
	 * @param processName
	 * @param correlationAttributes
	 * @throws XMLParsingException
	 */
	protected void queryCreationTemplateMethod(final String filePath, final String processName,
			final List<TypeTreeNode> correlationAttributes) throws XMLParsingException {
		this.filePath = filePath;
		this.eventTypes = this.createEventTypes();
		this.sendEventTypes(this.eventTypes);

		this.BPMNProcess = this.createBPMNProcess(filePath);

		// Prozess und Corelation anlegen
		this.process = this.createProcess(this.eventTypes, this.BPMNProcess, processName);

		this.correlate(this.eventTypes, correlationAttributes, this.process, null);

		this.generateQueries(this.BPMNProcess);

		this.simulate(this.eventTypes);

		this.assertQueryStatus();
	}

	/**
	 * Creates the event types used in the test for query creation.
	 * 
	 * @return
	 */
	protected abstract Set<EapEventType> createEventTypes();

	/**
	 * Sends the given event types to Esper and to the database.
	 * 
	 * @param eventTypes2
	 */
	protected void sendEventTypes(final Set<EapEventType> eventTypes2) {
		for (final EapEventType eventType : eventTypes2) {
			Broker.getInstance().importEventType(eventType);
		}
	}

	/**
	 * Creates a sample {@link BPMNProcess} and saves it in the database.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 */
	protected BPMNProcess createBPMNProcess(final String filePath) throws XMLParsingException {
		final BPMNProcess BPMNProcess = BPMNParser.generateProcessFromXML(filePath);
		BPMNProcess.save();
		return BPMNProcess;
	}

	/**
	 * Creates a sample {@link CorrelationProcess} with the given eventtypes,
	 * processName and BPMNProcess and saves it in the database.
	 * 
	 * @param eventTypes
	 * @param bpmnProcess
	 * @param processName
	 * @return
	 */
	protected CorrelationProcess createProcess(final Set<EapEventType> eventTypes, final BPMNProcess bpmnProcess,
			final String processName) {
		final CorrelationProcess process = new CorrelationProcess(processName, eventTypes);
		process.setBpmnProcess(bpmnProcess);
		process.save();
		return process;
	}

	/**
	 * Creates a correlation for the sample process.
	 * 
	 * @param eventTypes
	 * @param correlationAttributes
	 * @param process
	 * @param timeCondition
	 */
	protected void correlate(final Set<EapEventType> eventTypes, final List<TypeTreeNode> correlationAttributes,
			final CorrelationProcess process, final TimeCondition timeCondition) {
		AttributeCorrelator.correlate(eventTypes, correlationAttributes, process, timeCondition);
	}

	/**
	 * Creates the queries from the given {@link BPMNProcess}. Therefore,
	 * computes the RPST of the BPMNProcess and derives queries from that.
	 * Finally, the queries are registered at Esper.
	 * 
	 * @param BPMNProcess
	 */
	protected void generateQueries(final BPMNProcess BPMNProcess) {
		final RPSTBuilder rpst = new RPSTBuilder(BPMNProcess);
		// System.out.println(rpst.getProcessDecompositionTree());

		this.process.setProcessDecompositionTree(rpst.getProcessDecompositionTree());

		final PatternQueryGenerator queryGenerator = new PatternQueryGenerator(rpst);
		try {
			queryGenerator.generateQueries();
		} catch (final QueryGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates events for the different process instances and send them to the
	 * {@link Broker}. The ordering of the events is important for the
	 * monitoring of the execution and is assured because of the usage of a
	 * ordered list.
	 * 
	 * @param eventTypes2
	 */
	protected abstract void simulate(Set<EapEventType> eventTypes2);

	/**
	 * Asserts, that all monitored queries have the right status after the
	 * simulation.
	 */
	protected abstract void assertQueryStatus();

	/**
	 * Removes all created test data from the database.
	 */
	public static void resetDatabase() {
		EapEvent.removeAll();
		EapEventType.removeAll();
		CorrelationProcess.removeAll();
		CorrelationProcessInstance.removeAll();
		// TODO: Ist es sinnvoll für den Test jedesmal wieder von einem frischen
		// QueryEditor auszugehen?
		BPMNQueryMonitor.reset();
	}

	/**
	 * Creates the tree of attributes for the event types.
	 * 
	 * @return
	 */
	protected AttributeTypeTree createAttributeTree() {
		final AttributeTypeTree values = new AttributeTypeTree();
		values.addRoot(new TypeTreeNode("Location", AttributeTypeEnum.INTEGER));
		values.addRoot(new TypeTreeNode("Movie", AttributeTypeEnum.STRING));
		return values;
	}

}
