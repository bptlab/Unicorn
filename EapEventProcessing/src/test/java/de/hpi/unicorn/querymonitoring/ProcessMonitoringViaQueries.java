/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.querymonitoring;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.monitoring.QueryMonitoringPoint;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

public class ProcessMonitoringViaQueries {

	private QueryWrapper query1;
	private CorrelationProcess process1;
	private CorrelationProcessInstance processInstance1;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void monitoringTest() {
		final TypeTreeNode attribute = new TypeTreeNode("TestAttribute", AttributeTypeEnum.STRING);
		final AttributeTypeTree attributes = new AttributeTypeTree(attribute);
		final EapEventType eventType = new EapEventType("TestType", attributes);
		Broker.getInstance().importEventType(eventType);

		this.query1 = new QueryWrapper("NotifyTestType", "Select * from TestType", QueryTypeEnum.LIVE);
		this.query1.save();
		this.query1.addToEsper();

		this.process1 = new CorrelationProcess("process1");
		this.process1.save();

		QueryMonitoringPoint.removeAll();
		final QueryMonitoringPoint point = new QueryMonitoringPoint(this.process1, this.query1, 40, false);
		point.save();
		Assert.assertTrue(QueryMonitoringPoint.findAll().size() == 1);

		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		values.put(attribute.getAttributeExpression(), "Wert");
		final EapEvent event = new EapEvent(eventType, new Date(), values);
		event.save();
		this.processInstance1 = new CorrelationProcessInstance();
		this.processInstance1.addEvent(event);
		this.processInstance1.save();
		event.addProcessInstance(this.processInstance1);
		event.merge();
		StreamProcessingAdapter.getInstance().addEvent(event);

		Assert.assertTrue(CorrelationProcessInstance.findByID(this.processInstance1.getID()).getProgress() == 40);
	}

}
