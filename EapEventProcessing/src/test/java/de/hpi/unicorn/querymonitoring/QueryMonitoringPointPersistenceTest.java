/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.querymonitoring;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.monitoring.QueryMonitoringPoint;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

public class QueryMonitoringPointPersistenceTest {

	private EapEventType type1;
	private EapEventType type2;
	private QueryWrapper query1;
	private QueryWrapper query2;
	private CorrelationProcess process1;
	private CorrelationProcess process2;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void testStoreAndRetrieve() {
		this.storeExampleQueryMonitoringPoints();
		Assert.assertTrue("Value should be 2, but was " + QueryMonitoringPoint.findAll().size(), QueryMonitoringPoint
				.findAll().size() == 2);
		QueryMonitoringPoint.removeAll();
		Assert.assertTrue("Value should be 0, but was " + QueryMonitoringPoint.findAll().size(), QueryMonitoringPoint
				.findAll().size() == 0);
	}

	@Test
	public void testFind() {
		this.storeExampleQueryMonitoringPoints();
		Assert.assertTrue(QueryMonitoringPoint.findAll().size() == 2);

		Assert.assertTrue(QueryMonitoringPoint.findByQuery(this.query1).size() == 1);
		Assert.assertTrue(QueryMonitoringPoint.findByQuery(this.query1).get(0).getProcess().getName()
				.equals(this.process1.getName()));
	}

	@Test
	public void testRemove() {
		this.storeExampleQueryMonitoringPoints();
		List<QueryMonitoringPoint> points;
		points = QueryMonitoringPoint.findAll();
		Assert.assertTrue(points.size() == 2);

		final QueryMonitoringPoint deletedPoint = points.get(0);
		deletedPoint.remove();

		points = QueryMonitoringPoint.findAll();
		Assert.assertTrue(points.size() == 1);

		Assert.assertTrue(points.get(0).getID() != deletedPoint.getID());
	}

	@Test
	public void testRemoveQueryWithPoint() {
		this.storeExampleQueryMonitoringPoints();
		List<QueryMonitoringPoint> points = QueryMonitoringPoint.findAll();
		Assert.assertTrue(points.size() == 2);

		final QueryMonitoringPoint deletedPoint = points.get(0);
		final QueryWrapper query = deletedPoint.getQuery();
		query.remove();

		final List<QueryWrapper> queries = QueryWrapper.getAllLiveQueries();
		Assert.assertTrue(queries.size() == 1);
		Assert.assertTrue(queries.get(0).getID() != query.getID());

		// monitoringpoint was deleted as well
		points = QueryMonitoringPoint.findAll();
		Assert.assertTrue(points.size() == 1);
		Assert.assertTrue(points.get(0).getID() != deletedPoint.getID());
	}

	private void storeExampleQueryMonitoringPoints() {
		this.type1 = new EapEventType("ToNotify");
		this.type1.save();
		this.query1 = new QueryWrapper("allToNotify1", "Select * from ToNotify", QueryTypeEnum.LIVE);
		this.query1.save();
		this.process1 = new CorrelationProcess("testProcess");
		this.process1.save();
		final QueryMonitoringPoint point1 = new QueryMonitoringPoint(this.process1, this.query1, 30, false);
		point1.save();

		this.type2 = new EapEventType("ToNotify2");
		this.type2.save();
		this.query2 = new QueryWrapper("allToNotify2", "Select * from ToNotify2", QueryTypeEnum.LIVE);
		this.query2.save();
		this.process2 = new CorrelationProcess("testProcess2");
		this.process2.save();
		final QueryMonitoringPoint point2 = new QueryMonitoringPoint(this.process2, this.query2, 40, true);
		point2.save();

	}

}
