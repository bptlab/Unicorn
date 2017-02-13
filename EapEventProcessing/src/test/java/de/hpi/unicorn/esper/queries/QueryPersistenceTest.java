/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper.queries;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

@FixMethodOrder(MethodSorters.JVM)
public class QueryPersistenceTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void testSaveQueries() {
		QueryWrapper q1 = new QueryWrapper("testquery", "select * from stuff", QueryTypeEnum.ONDEMAND);
		q1 = q1.save();
		Assert.assertTrue(q1 != null);
	}

	@Test
	public void testRemoveQueries() {
		QueryWrapper q1 = new QueryWrapper("testquery", "select * from stuff", QueryTypeEnum.ONDEMAND);
		q1 = q1.save();
		q1.remove();
		Assert.assertTrue(QueryWrapper.findQueryByTitle("testquery") == null);
		QueryWrapper q2 = new QueryWrapper("testquery2", "select * from stuff2", QueryTypeEnum.ONDEMAND);
		q2 = q2.save();
		QueryWrapper q3 = new QueryWrapper("testquery3", "select * from stuff3", QueryTypeEnum.ONDEMAND);
		q3 = q3.save();
		QueryWrapper.removeAll();
		Assert.assertTrue(QueryWrapper.findQueryByTitle("testquery2") == null);
		Assert.assertTrue(QueryWrapper.findQueryByTitle("testquery3") == null);
	}

	@Test
	public void testRemoveQueryWithTitle() {
		QueryWrapper q1 = new QueryWrapper("testquery", "select * from stuff", QueryTypeEnum.ONDEMAND);
		q1 = q1.save();
		QueryWrapper.removeQueryWithTitle("testquery");
		Assert.assertTrue(QueryWrapper.findQueryByTitle("testquery") == null);
	}

	@Test
	public void testGetAllTitlesOfQueries() {
		QueryWrapper q1 = new QueryWrapper("testquery", "select * from stuff", QueryTypeEnum.ONDEMAND);
		q1 = q1.save();
		QueryWrapper q2 = new QueryWrapper("testquery2", "select * from stuff2", QueryTypeEnum.ONDEMAND);
		q2 = q2.save();
		QueryWrapper q3 = new QueryWrapper("testquery3", "select * from stuff3", QueryTypeEnum.ONDEMAND);
		q3 = q3.save();
		// System.out.println(QueryWrapper.getAllTitlesOfOnDemandQueries());
		final ArrayList<String> names = new ArrayList<String>(Arrays.asList("testquery", "testquery2", "testquery3"));
		Assert.assertTrue(QueryWrapper.getAllTitlesOfQueries().containsAll(names));
		Assert.assertTrue(QueryWrapper.getAllTitlesOfOnDemandQueries().containsAll(names));
	}

	@Test
	public void testGetQueryByTitle() {
		QueryWrapper q1 = new QueryWrapper("testquery", "select * from stuff", QueryTypeEnum.ONDEMAND);
		q1 = q1.save();
		Assert.assertTrue("should find query in db, but did not", QueryWrapper.findQueryByTitle("testquery") == q1);
	}

}
